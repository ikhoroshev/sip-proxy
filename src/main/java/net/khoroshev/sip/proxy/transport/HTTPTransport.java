package net.khoroshev.sip.proxy.transport;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

import java.util.List;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public class HTTPTransport extends AbstractActor implements Transport{
    private final String protocol;
    private final String bindAddress;
    private final int bindPort;
    private final List<ActorRef> nextActors;

    public HTTPTransport(String protocol, String bindAddress, int bindPort, List<ActorRef> nextActors) {
        this.protocol = protocol;
        this.bindAddress = bindAddress;
        this.bindPort = bindPort;
        this.nextActors = nextActors;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
        .matchEquals(NewChanellReq.class, r -> {
            //TODO
        }).build();
    }
}
