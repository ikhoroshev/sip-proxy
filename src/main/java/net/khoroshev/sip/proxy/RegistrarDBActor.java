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
import java.util.stream.Stream;

/**
 * Created by sbt-khoroshev-iv on 25/05/17.
 */
public class RegistrarDBActor extends AbstractActor /*TODO: AbstractPersistentActor*/   {
    private static final int MAX_EXPIRES = 3600;
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Map<String, ContactList> db = new HashMap<>();
    Map<String, Long> expiresDb = new HashMap<>();

    //TODO scheduled cleanup expired records

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
                    register(r);
                    getSender().tell(new RegisterReq.Asc(true), getSelf());
                })
            .build();
    }

    private void register(RegisterReq r) {
        SIPRequest sipRequest = r.getReq();
        String addressOfRecord = sipRequest.getTo().getAddress().getURI().toString();
        ContactList contacts = sipRequest.getContactHeaders();
        int expires = sipRequest.getExpires().getExpires();
        if (expires > 0) {
            expires = Math.min(expires, MAX_EXPIRES);
            db.put(addressOfRecord, contacts);
            expiresDb.put(addressOfRecord, System.currentTimeMillis() + expires * 1000);
        } else {
            db.remove(addressOfRecord);
            expiresDb.remove(addressOfRecord);
        }
        db.put(addressOfRecord, contacts);
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
