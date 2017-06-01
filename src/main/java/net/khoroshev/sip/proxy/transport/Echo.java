package net.khoroshev.sip.proxy.transport;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

/**
 * Created by sbt-khoroshev-iv on 30/05/17.
 */
public class Echo extends AbstractActor{
    private ActorRef connection;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorRef.class, actorRef -> {
                    connection = actorRef;
                })
                //TODO
                .build();
    }
}
