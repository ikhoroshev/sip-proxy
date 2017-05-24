package net.khoroshev.sip.proxy;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;

/**
 * Created by Igor on 24.05.2017.
 */
public class SipRequestActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SIPRequest.class, r -> {
            log.debug(String.format("<<%s", r.encode()));
            ActorRef messageChanell = (ActorRef) r.getMessageChannel();
            SIPResponse response = r.createResponse(100);
            messageChanell.tell(response, getSelf());
            log.debug(String.format(">>%s", response.encode()));
            response = r.createResponse(200);
            log.debug(String.format(">>%s", response.encode()));
            messageChanell.tell(response, getSelf());
        }).build();
    }
}
