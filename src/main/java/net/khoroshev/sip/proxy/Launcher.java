package net.khoroshev.sip.proxy;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.khoroshev.sip.proxy.transport.HTTPTransport;
import net.khoroshev.sip.proxy.transport.UDPTransport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public class Launcher {
    public static void main(String[] args) {
        Config conf = ConfigFactory.load();
        conf = conf.getConfig("system");
        ActorSystem system = ActorSystem.create(conf.getString("name"));
        ActorRef registrar = makeRegistrar(conf, system);
        ActorRef b2b = makeB2B(conf, system);
        List<ActorRef> sipSystems = new ArrayList<ActorRef>(){{
            add(registrar); add(b2b);
        }};
        ActorRef udpTransport = makeUdpTransport(conf.getConfig("transport"), system, sipSystems);
        ActorRef wsTransport = makeWsTransport(conf.getConfig("transport"), system, sipSystems);
    }

    private static ActorRef makeUdpTransport(Config conf, ActorSystem system, List<ActorRef> sipSystems) {
        ActorRef result = null;
        if (conf.hasPath("udp")) {
            result = system.actorOf(Props.create(UDPTransport.class
                    , conf.getString("udp.bindAddress")
                    , conf.getInt("udp.bindPort"), sipSystems)
                , conf.getString("udp.name"));
        }
        return result;
    }

    private static ActorRef makeWsTransport(Config conf, ActorSystem system, List<ActorRef> sipSystems) {
        ActorRef result = null;
        if (conf.hasPath("websocket")) {
            result = system.actorOf(Props.create(HTTPTransport.class
                    , conf.getString("websocket.http.proto")
                    , conf.getString("websocket.http.bindAddress")
                    , conf.getInt("websocket.http.bindPort"), sipSystems)
                    , conf.getString("websocket.name"));
        }
        return result;
    }

    private static ActorRef makeB2B(Config conf, ActorSystem system) {
        ActorRef result = null;
        if (conf.hasPath("b2b")) {
            result = system.actorOf(Props.create(B2BSystemActor.class)
                    , conf.getString("b2b.name"));
        }
        return result;
    }

    private static ActorRef makeRegistrar(Config conf, ActorSystem system) {
        ActorRef result = null;
        if (conf.hasPath("registrar")) {
            result = system.actorOf(Props.create(RegistrarSystemActor.class)
                    , conf.getString("registrar.name"));
        }
        return result;
    }
}