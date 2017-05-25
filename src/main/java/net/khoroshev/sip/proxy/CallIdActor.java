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
public class CallIdActor extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SIPMessage.class, r -> {
            //log.debug(String.format("<<%s", r.encode()));
            String cSeq = "CSeq-" + r.getCSeq().getSeqNumber() + r.getCSeq().getMethod();
            ActorRef cSeqActor = getCSeqActor(cSeq);
            cSeqActor.tell(r, getSelf());
        }).build();
    }

    private ActorRef getCSeqActor(String cSeq) {
        String allowedcSeq = Util.allowedPath(cSeq);
        ActorRef result = getContext().getChild(allowedcSeq);
        if (result == null) {
            result = context().actorOf(Props.create(CSeqActor.class), allowedcSeq);
        }
        return result;
    }
}
