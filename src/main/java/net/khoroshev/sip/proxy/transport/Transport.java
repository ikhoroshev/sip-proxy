package net.khoroshev.sip.proxy.transport;

import javax.sip.address.Address;

/**
 * Created by sbt-khoroshev-iv on 26/05/17.
 */
public interface Transport {
    class NewChanellReq{
        private final Address address;

        public NewChanellReq(Address address) {
            this.address = address;
        }
    }
}
