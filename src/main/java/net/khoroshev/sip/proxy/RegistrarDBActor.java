package net.khoroshev.sip.proxy;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.SnapshotOffer;
import gov.nist.javax.sip.header.ContactList;
import gov.nist.javax.sip.message.SIPRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sbt-khoroshev-iv on 25/05/17.
 */
public class RegistrarDBActor extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Map<String, ContactList> db = new HashMap<>();

    /*@Override
    public String persistenceId() {
        return "RegistrarDB";
    }

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                //TODO
                .match(SnapshotOffer.class, ss -> System.out.println(ss.snapshot()))
                .build();
    }*/

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(RegisterReq.class,
                r -> {
                    log.debug("<<");
                    String addressOfRecord = r.getReq().getTo().getAddress().getURI().toString();
                    ContactList contacts = r.getReq().getContactHeaders();
                    db.put(addressOfRecord, contacts);
                    getSender().tell(new RegisterReq.Asc(true), getSelf());
                })
            .build();
    }

    public static class RegisterReq{
        public SIPRequest getReq() {
            return req;
        }

        private final SIPRequest req;

        public RegisterReq(SIPRequest req) {
            this.req = req;
        }
        public static class Asc{
            public boolean isSuccess() {
                return success;
            }

            private final boolean success;

            public Asc(boolean success) {
                this.success = success;
            }
        }
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }
}
