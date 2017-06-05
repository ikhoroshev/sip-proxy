package net.khoroshev.sip.proxy.transport;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.http.javadsl.model.ws.TextMessage;

/**
 * Created by sbt-khoroshev-iv on 30/05/17.
 */
public class Echo extends AbstractActor{
    private ActorRef connection;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TextMessage.class, m -> {
                    if (m.getStrictText().equals("income")) {
                        connection = getSender();
                    } else if (m.getStrictText().equals("sinkclose")) {
                        connection = null;
                    } //else
                })
                .matchAny(o -> {
                    System.out.println(o);
                })
                //TODO
                .build();
    }
}
