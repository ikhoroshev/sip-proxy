package net.khoroshev.sip.proxy;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import gov.nist.javax.sip.message.SIPMessage;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public class RegistrarSystemActor extends AbstractActor implements SipSystem{
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(SIPMessage.class, sipMessage -> {
                    //TODO
                    log.debug("<<", sipMessage.encode());
                }).build();
    }
}
