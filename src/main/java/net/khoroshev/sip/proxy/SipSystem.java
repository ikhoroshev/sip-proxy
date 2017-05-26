package net.khoroshev.sip.proxy;

import akka.actor.ActorRef;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public interface SipSystem {
    class AddTransportReq {
        public ActorRef getTransport() {
            return transport;
        }

        private final ActorRef transport;

        public AddTransportReq(ActorRef transport) {
            this.transport = transport;
        }
    }
    class RemoveTransportReq {
        public ActorRef getTransport() {
            return transport;
        }

        private final ActorRef transport;

        public RemoveTransportReq(ActorRef transport) {
            this.transport = transport;
        }
    }
}
