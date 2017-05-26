package net.khoroshev.sip.proxy.deprecated;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.sip.message.Request;

/**
 * Created by sbt-khoroshev-iv on 25/05/17.
 */
public class RegistrarActor  extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    final ActorRef messageChannel;
    private ActorRef registrarDB;

    public RegistrarActor(ActorRef messageChannel) {
        this.messageChannel = messageChannel;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(SIPRequest.class, r -> {
            log.debug(String.format("<<\n%s", r.encode()));
            if (Request.REGISTER.equals(r.getMethod())) {
                //registrarDB.tell(new RegistrarDBActor.RegisterReq(r), getSelf());
                Timeout timeout = new Timeout(Duration.create(5, "seconds"));
                Future<Object> future = Patterns.ask(registrarDB,
                        new RegistrarDBActor.RegisterReq(r), timeout);
                RegistrarDBActor.RegisterReq.Asc result = (RegistrarDBActor.RegisterReq.Asc) Await.result(future, timeout.duration());

                if (result.isSuccess()) {
                    SIPResponse response = r.createResponse(100);
                    messageChannel.tell(response, getSelf());
                    log.debug(String.format(">>\n%s", response.encode()));
                    response = r.createResponse(200);
                    log.debug(String.format(">>\n%s", response.encode()));
                    messageChannel.tell(response, getSelf());
                } else {
                    //timeout
                    SIPResponse response = r.createResponse(504);
                }

            }
        }).build();
    }

    @Override
    public void preStart() throws Exception {
        registrarDB = this.context().actorFor("/user/registrarDB");
    }
}
