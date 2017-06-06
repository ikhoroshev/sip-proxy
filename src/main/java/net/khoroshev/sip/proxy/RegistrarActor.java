package net.khoroshev.sip.proxy;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.ConfigFactory;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.sip.InvalidArgumentException;
import javax.sip.SipFactory;
import javax.sip.header.ExpiresHeader;
import javax.sip.message.Request;

/**
 * Created by sbt-khoroshev-iv on 25/05/17.
 */
public class RegistrarActor  extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef messageChannel;
    private ActorRef registrarDB;

    public RegistrarActor(ActorRef messageChannel) {
        this.messageChannel = messageChannel;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(SIPRequest.class, r -> {
                log.debug(String.format("<<\n%s", r.encode()));
                if (Request.REGISTER.equals(r.getMethod())) {
                    SIPResponse response = r.createResponse(100);
                    messageChannel.tell(response, getSelf());
                    log.debug(String.format(">>\n%s", response.encode()));
                    Timeout timeout = new Timeout(Duration.create(5, "seconds"));
                    Future<Object> future = Patterns.ask(registrarDB,
                            new RegistrarDBActor.RegisterReq(r), timeout);
                    RegistrarDBActor.RegisterReq.Asc result = (RegistrarDBActor.RegisterReq.Asc) Await.result(future, timeout.duration());

                    if (result.isSuccess()) {
                        int expires = result.getExpires();
                        response = r.createResponse(200);
                        response.setExpires(
                            SipFactory.getInstance().createHeaderFactory().createExpiresHeader(expires));
                    } else {
                        //timeout
                        response = r.createResponse(504);
                    }
                    log.debug(String.format(">>\n%s", response.encode()));
                    messageChannel.tell(response, getSelf());
                    //messageChannel.tell(PoisonPill.getInstance(), getSelf());
                    getSender().tell(new CallIdActor.KillReq(), getSelf());
                }
            })
            .build();
    }

    @Override
    public void preStart() throws Exception {
        String path = "/user/" + ConfigFactory.load().getString("system.registrarDB.name");
        registrarDB = this.context().actorFor(path);
    }

    @Override
    public void postStop() throws Exception {
        log.debug("postStop()");
    }
}
