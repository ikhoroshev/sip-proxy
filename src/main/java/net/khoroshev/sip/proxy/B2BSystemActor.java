package net.khoroshev.sip.proxy;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public class B2BSystemActor extends AbstractActor implements SipSystem{

    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(SIPRequest.class, sipRequest -> {
                    log.debug("<<", sipRequest.encode());
                    /*if (SIPRequest.INVITE.equals(sipRequest.getMethod())) {

                    } else {

                    }*/
                }).match(SIPResponse.class, sipResponse -> {
                    log.debug("<<", sipResponse.encode());
                    //if ()
                }).build();
    }
}