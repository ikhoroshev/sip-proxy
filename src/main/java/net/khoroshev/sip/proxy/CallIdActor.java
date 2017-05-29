package net.khoroshev.sip.proxy;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import gov.nist.javax.sip.message.SIPMessage;

/**
 * Created by sbt-khoroshev-iv on 25/05/17.
 */
public class CallIdActor extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SIPMessage.class, r -> {
            //log.debug(String.format("<<%s", r.encode()));
            String cSeqActorName = "CSeq-" + r.getCSeq().getSeqNumber() + "-" + r.getCSeq().getMethod();
            ActorRef cSeqActor = getCSeqActor(cSeqActorName);
            cSeqActor.tell(r, getSelf());
        }).build();
    }

    private ActorRef getCSeqActor(String name) {
        String allowedPath = Util.allowedPath(name);
        ActorRef result = getContext().getChild(allowedPath);
        if (result == null) {
            result = context().actorOf(Props.create(CSeqActor.class), allowedPath);
        }
        return result;
    }
}
