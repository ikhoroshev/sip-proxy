package net.khoroshev.sip.proxy.deprecated;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import gov.nist.javax.sip.message.SIPMessage;
import net.khoroshev.sip.proxy.Util;

/**
 * Created by Igor on 24.05.2017.
 */
public class SipMessageActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SIPMessage.class, r -> {
            log.debug(String.format("<<%s", r.encode()));
            String callId = r.getCallId().getCallId();
            ActorRef callIdActor = getCallIdActor(callId);
            callIdActor.tell(r, getSelf());
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
