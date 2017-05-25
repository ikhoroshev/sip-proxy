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
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.parser.StringMsgParser;

import java.net.InetSocketAddress;
import java.text.ParseException;

/**
 * Created by Igor on 24.05.2017.
 */
public class EndpointActor  extends AbstractActor {

    private final InetSocketAddress endpoint;

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef sipMessageActor;

    public EndpointActor(InetSocketAddress endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Received.class, r -> {
                    StringMsgParser smp = new StringMsgParser();
                    SIPMessage sipMessage = null;
                    try {
                        sipMessage = smp.parseSIPMessage(r.data().toArray(), true, false, null);
                    } catch (ParseException e) {
                        //echo
                        getContext().getParent().tell(UdpMessage.send(r.data(), endpoint), getSelf());
                        return;
                    }
                    if (sipMessage instanceof SIPRequest) {
                        SIPRequest sipRequest = (SIPRequest) sipMessage;
                        sipRequest.setMessageChannel(getSelf());
                    }
                    sipMessageActor.tell(sipMessage, getSelf());
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
        this.sipMessageActor = context().actorOf(Props.create(SipMessageActor.class), "sipMessage");
    }
}
