package net.khoroshev.sip.proxy;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import gov.nist.javax.sip.header.ContactList;
import gov.nist.javax.sip.message.SIPRequest;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by sbt-khoroshev-iv on 25/05/17.
 */
public class RegistrarDBActor extends AbstractActor /*TODO: AbstractPersistentActor*/   {
    private final int maxExpires;
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    Map<String, ContactList> db = new HashMap<>();
    Map<String, Long> expiresDb = new HashMap<>();

    public RegistrarDBActor(int maxExpires) {
        this.maxExpires = maxExpires;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(RegisterReq.class,
                r -> {
                    log.debug("<<RegisterReq");
                    int expires = register(r);
                    getSender().tell(new RegisterReq.Asc(true, expires), getSelf());
                })
            .match(RmRegistrationReq.class,
                    r -> {
                        String addressOfRecord = r.getAddressOfRecord();
                        log.debug(String.format("<<RmRegistrationReq for %s", addressOfRecord));
                        Long expired = expiresDb.get(addressOfRecord);
                        if (expired!= null && expired < System.currentTimeMillis()) {
                            rm(addressOfRecord);
                            log.debug(String.format("<<RmRegistrationReq. Record %s has been removed from DB.", addressOfRecord));
                        } //else record updated
                    })
            .build();
    }

    /**
     *
     * @param r
     * @return expires
     */
    private int register(RegisterReq r) {
        SIPRequest sipRequest = r.getReq();
        String addressOfRecord = sipRequest.getTo().getAddress().getURI().toString();
        ContactList contacts = sipRequest.getContactHeaders();
        int expires = sipRequest.getExpires().getExpires();
        if (expires > 0) {
            expires = add(addressOfRecord, contacts, expires);
            getContext().getSystem().scheduler().scheduleOnce(
                Duration.create(expires, TimeUnit.SECONDS),
                getSelf(),
                new RmRegistrationReq(addressOfRecord),
                getContext().dispatcher(),
                getSelf()
            );

        } else {
            rm(addressOfRecord);
        }
        return expires;
    }

    private void rm(String addressOfRecord) {
        db.remove(addressOfRecord);
        expiresDb.remove(addressOfRecord);
    }

    /**
     *
     * @param addressOfRecord
     * @param contacts
     * @param expires
     * @return expires
     */
    private int add(String addressOfRecord, ContactList contacts, int expires) {
        expires = Math.min(expires, maxExpires);
        db.put(addressOfRecord, contacts);
        expiresDb.put(addressOfRecord, System.currentTimeMillis() + expires * 1000);
        return expires;
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

            private final int expires;

            public int getExpires() {
                return expires;
            }

            public boolean isSuccess() {
                return success;
            }

            private final boolean success;

            public Asc(boolean success, int expires) {
                this.success = success;
                this.expires = expires;
            }
        }
    }

    public static class RmRegistrationReq{
        private final String addressOfRecord;

        public String getAddressOfRecord() {
            return addressOfRecord;
        }

        public RmRegistrationReq(String addressOfRecord) {
            this.addressOfRecord = addressOfRecord;
        }
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }
}
