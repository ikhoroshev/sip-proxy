package net.khoroshev.sip.proxy;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Udp;
import akka.io.UdpMessage;
import akka.util.ByteString;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.parser.StringMsgParser;

import java.net.InetSocketAddress;

/**
 * Created by Igor on 24.05.2017.
 */
public class EndpointActor  extends AbstractActor {

    private final InetSocketAddress endpoint;

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef sipRequestActor;

    public EndpointActor(InetSocketAddress endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, r -> {
                    //log.debug(String.format("<<%s", r));
                    //log.debug(String.format("<<%s", r.data().decodeString("utf8")));
                    StringMsgParser smp = new StringMsgParser();
                    SIPMessage sipMessage = smp.parseSIPMessage(r.data().toArray(), true, false, null);
                    if (sipMessage instanceof SIPRequest) {
                        SIPRequest sipRequest = (SIPRequest) sipMessage;
                        sipRequest.setMessageChannel(getSelf());
                        sipRequestActor.tell(sipRequest, getSelf());
                    } else {
                        //TODO
                        // echo server example: send back the data
                        getSender().tell(UdpMessage.send(r.data(), r.sender()), getSelf());
                    }

                })
                .match(SIPMessage.class, m -> { //from child
                    String payload = m.encode();
                    ActorRef parent = getContext().getParent();
                    parent.tell(UdpMessage.send(ByteString.fromString(payload), endpoint), getSelf());
                })
                .match(Udp.Unbound.class, message -> {
                    getContext().stop(getSelf());
                })
                .build();
    }

    @Override
    public void preStart() throws Exception {
        this.sipRequestActor = context().actorOf(Props.create(SipRequestActor.class), "sipRequest");
    }
}
