package net.khoroshev.sip.proxy.transport;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.io.Udp;
import akka.io.UdpMessage;
import akka.util.ByteString;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.parser.StringMsgParser;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public class UDPChannel extends AbstractActor implements Channel {
    private final ActorRef socket;
    private final List<ActorRef> sipSystems;
    private final Set<ActorRef> subscribers = new HashSet<>();
    private InetSocketAddress address;

    public UDPChannel(ActorRef socket, List<ActorRef> sipSystems) {
        this.socket = socket;
        this.sipSystems = sipSystems;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, r -> {
                    if (address == null) {
                        address = r.sender();
                    }
                    StringMsgParser smp = new StringMsgParser();
                    SIPMessage sipMessage = null;
                    try {
                        sipMessage = smp.parseSIPMessage(r.data().toArray(), true, false, null);
                    } catch (ParseException e) {
                        //echo
                        socket.tell(UdpMessage.send(r.data(), address), getSelf());
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
                    ActorRef parent = getContext().getParent();
                    parent.tell(UdpMessage.send(ByteString.fromString(payload), address), getSelf());
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
