package net.khoroshev.sip.proxy;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;

import javax.sip.message.Request;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public class RegistrarSystemActor extends AbstractActor implements SipSystem{
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(SIPRequest.class, sipRequest -> {
                    log.debug("<<", sipRequest.encode());
                    if (Request.REGISTER.equals(sipRequest.getMethod())) {
                        String callId = sipRequest.getCallId().getCallId();
                        ActorRef callIdActor = getCallIdActor(callId);
                        callIdActor.tell(sipRequest, getSelf());
                    }
                }).build();
    }

    private ActorRef getCallIdActor(String callId) {
        String allowedCallId = "CallId-" + Util.allowedPath(callId);
        ActorRef result = getContext().getChild(allowedCallId);
        if (result == null) {
            result = context().actorOf(Props.create(CallIdActor.class), allowedCallId);
        }
        return result;
    }
}
