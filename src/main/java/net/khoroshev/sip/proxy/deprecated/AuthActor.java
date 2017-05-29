package net.khoroshev.sip.proxy.deprecated;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import net.khoroshev.sip.proxy.RegistrarActor;

/**
 * Created by sbt-khoroshev-iv on 25/05/17.
 */
public class AuthActor extends AbstractActor {
    private final Class<RegistrarActor> nextActorClass;
    private final String nextActorName;
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef messageChannel;
    private ActorRef children;

    public AuthActor(Class<RegistrarActor> nextActorClass, String nextActorName) {
        this.nextActorClass = nextActorClass;
        this.nextActorName = nextActorName;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SIPMessage.class, r -> {
            log.debug("<<");
            if (children != null) {
                children.tell(r, getSelf());
            }
            if (r instanceof SIPRequest) {
                SIPRequest req = (SIPRequest) r;
                messageChannel = (ActorRef) req.getMessageChannel();
                //TODO
                if (false/*TODO*/) {
                    getChildren(nextActorClass, nextActorName).tell(req, getSelf());
                }
            } else {
                log.error("Unexpected message. " + r.encode());
            }
        }).build();
    }

    private ActorRef getChildren(Class actorClass, String name) {
        if (children == null) {
            children = context().actorOf(Props.create(actorClass, messageChannel), name);
        }
        return children;
    }
}