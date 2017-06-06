package net.khoroshev.sip.proxy.transport;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.http.javadsl.model.ws.TextMessage;
import akka.io.Udp;
import akka.io.UdpMessage;
import akka.util.ByteString;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.parser.StringMsgParser;

import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public class WSChannel extends AbstractActor implements Channel {
    private ActorRef socket;
    private final List<ActorRef> sipSystems;
    private final Set<ActorRef> subscribers = new HashSet<>();

    public WSChannel(List<ActorRef> sipSystems) {
        this.sipSystems = sipSystems;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TextMessage.class, m -> {
                    StringMsgParser smp = new StringMsgParser();
                    if (socket == null) {
                        socket = getSender();
                    }
                    SIPMessage sipMessage;
                    try {
                        sipMessage = smp.parseSIPMessage(m.getStrictText().getBytes(), true, false, null);
                    } catch (ParseException e) {
                        //echo
                        socket.tell(TextMessage.create(m.getStrictText()), getSelf());
                        return;
                    }
                    if (sipMessage instanceof SIPRequest) {
                        SIPRequest sipRequest = (SIPRequest) sipMessage;
                        sipRequest.setMessageChannel(getSelf());
                    }
                    if (subscribers.isEmpty()) {
                        for (ActorRef ref : sipSystems) {
                            ref.tell(sipMessage, getSelf());
                        }
                    } else {
                        for (ActorRef ref : subscribers) {
                            ref.tell(sipMessage, getSelf());
                        }
                    }
                })
                .match(LinkReq.class, linkReq -> {
                    subscribers.add(linkReq.subscriber);
                    linkReq.subscriber.tell(linkReq.firstPacket, getSelf());
                })
                .match(SIPMessage.class, m -> { //from sip system
                    String payload = m.encode();
                    socket.tell(TextMessage.create(payload), getSelf());
                })
                .match(UnsubscribeReq.class, u -> {
                    sipSystems.remove(getSender());
                })
                .match(Udp.Unbound.class, message -> {
                    getContext().stop(getSelf());
                })
                .build();
    }
}
