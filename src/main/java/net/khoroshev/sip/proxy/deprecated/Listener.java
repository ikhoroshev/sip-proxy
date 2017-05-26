package net.khoroshev.sip.proxy.deprecated;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Udp;
import akka.io.UdpMessage;
import net.khoroshev.sip.proxy.Util;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by Igor on 24.05.2017.
 */
public class Listener extends AbstractActor {
    final ActorRef nextActor;
    LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("UDP_listener");
        system.registerOnTermination(()->{System.exit(0);});
        system.actorOf(Props.create(RegistrarDBActor.class), "registrarDB");
        system.actorOf(Props.create(Listener.class, "0.0.0.0", 5060, null), "ul");
    }

    public Listener(String bindAddress, int bindPort, ActorRef nextActor) {
        this.nextActor = nextActor;

        // request creation of a bound listen socket
        final ActorRef mgr = Udp.get(getContext().getSystem()).getManager();
        mgr.tell(
                UdpMessage.bind(getSelf(), new InetSocketAddress(bindAddress, bindPort)),
                getSelf());
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
                    //String childrenName = nameByPacket(r);
                    //log.debug(childrenName);
                    ActorRef children = getOrCreate(r);
                    children.tell(r, getSelf());
                    // echo server example: send back the data
                    //socket.tell(UdpMessage.send(r.data(), r.sender()), getSelf());
                    // or do some processing and forward it on
                    /*final Object processed = // parse data etc., e.g. using PipelineStage
                            nextActor.tell(processed, getSelf());*/
                })
                .match(Udp.Command.class, command -> { //from child
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

    private ActorRef getOrCreate(Udp.Received r) {
        String childrenName = nameByPacket(r);
        ActorRef result = getContext().getChild(childrenName);
        if (result == null) {
            result = context().actorOf(Props.create(EndpointActor.class, r.sender()), childrenName);
        }
        return result;
    }

    private String nameByPacket(Udp.Received r) {
        InetAddress a = r.sender().getAddress();
        return new StringBuilder(
                Util.allowedPath(a.getHostAddress()))
                .append('-')
                .append(r.sender().getPort())
                .toString();
    }
}
