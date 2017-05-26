package net.khoroshev.sip.proxy.transport;

import akka.actor.ActorRef;
import gov.nist.javax.sip.message.SIPMessage;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public interface Channel {
    /**
     * После того, как вновь созданый канал направит первый пакет SIP системам, системы связываю его с конечными потребителями
     * посредством этого запроса.
     * Канал должен добавить подписчика, отправить ему первый и все последующие пакеты.
     */
    class LinkReq {
        protected final ActorRef subscriber;
        protected final SIPMessage firstPacket;
        public LinkReq(ActorRef subscriber, SIPMessage firstPacket) {
            this.subscriber = subscriber;
            this.firstPacket = firstPacket;
        }
    }
    /**
     * подписчик может отписаться
     */
    class UnsubscribeReq {
    }
}
