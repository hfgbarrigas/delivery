package io.hfgbarrigas.delivery.utils;

public interface Loggable {

    default org.slf4j.Logger logger() {
        return org.slf4j.LoggerFactory.getLogger(this.getClass());
    }
}
