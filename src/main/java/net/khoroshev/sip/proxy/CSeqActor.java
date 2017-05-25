package net.khoroshev.sip.proxy;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;

import javax.sip.message.Request;

/**
 * Created by sbt-khoroshev-iv on 25/05/17.
 */
public class CSeqActor extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef messageChannel;
    private ActorRef children;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SIPMessage.class, r -> {
            //log.debug(String.format("<<%s", r.encode()));
            if (children != null) {
                children.tell(r, getSelf());
            }
            if (r instanceof SIPRequest) {
                SIPRequest req = (SIPRequest) r;
                messageChannel = (ActorRef) req.getMessageChannel();
                if (Request.REGISTER.equals(req.getMethod())) {
                    getChildren(RegistrarActor.class, "reg").tell(req, getSelf());
                }
            } else {
                log.error("Unexpected message. " + r.encode());
            }
        }).build();
    }

    private ActorRef getChildren(Class actorClass, String name) {
        if (children == null) {
            children = context().actorOf(Props.create(actorClass, messageChannel), name);
        }
        return children;
    }
}