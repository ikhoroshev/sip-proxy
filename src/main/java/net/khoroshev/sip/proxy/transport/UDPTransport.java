package net.khoroshev.sip.proxy.transport;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Udp;
import akka.io.UdpMessage;
import net.khoroshev.sip.proxy.Util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public class UDPTransport extends AbstractActor implements Transport {
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String bindAddress;
    private final int bindPort;
    private final List<ActorRef> nextActors;

    public UDPTransport(String bindAddress, int bindPort, List<ActorRef> nextActors) {
        this.bindAddress = bindAddress;
        this.bindPort = bindPort;
        this.nextActors = nextActors;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Udp.Bound.class, bound -> {
                    getContext().become(ready(getSender()));
                    log.debug("bound");
                })
                .build();
    }

    private Receive ready(final ActorRef socket) {
        return receiveBuilder()
                .match(Udp.Received.class, r -> {
                    log.debug(String.format("<<%s", r));
                    log.debug(String.format("<<%s", r.data().decodeString("utf8")));
                    ActorRef channel = getOrCreate(r.sender(), socket);
                    channel.tell(r, getSelf());
                    // echo server example: send back the data
                    //socket.tell(UdpMessage.send(r.data(), r.sender()), getSelf());
                    // or do some processing and forward it on
                    /*final Object processed = // parse data etc., e.g. using PipelineStage
                            nextActor.tell(processed, getSelf());*/
                })
                .match(Udp.Command.class, command -> { //from channels
                    socket.tell(command, getSelf());
                })
                .matchEquals(UdpMessage.unbind(), message -> {
                    socket.tell(message, getSelf());
                })
                .match(Udp.Unbound.class, message -> {
                    getContext().stop(getSelf());
                })
                .build();
    }

    private ActorRef getOrCreate(InetSocketAddress a, ActorRef socket) {
        String channelName = nameByAddress(a);
        ActorRef result = getContext().getChild(channelName);
        if (result == null) {
            result = context().actorOf(Props.create(UDPChannel.class, socket, nextActors), channelName);
        }
        return result;
    }
    private String nameByAddress(InetSocketAddress a) {
        return new StringBuilder()
                .append(getSelf().path().name())
                .append("-")
                .append(Util.allowedPath(a.getAddress().getHostAddress()))
                .append('-')
                .append(a.getPort())
                .toString();
    }

    @Override
    public void preStart() throws Exception {
        // request creation of a bound listen socket
        final ActorRef mgr = Udp.get(getContext().getSystem()).getManager();
        mgr.tell(
                UdpMessage.bind(getSelf(), new InetSocketAddress(bindAddress, bindPort)),
                getSelf());
    }
}
