package net.khoroshev.sip.proxy;

/**
 * Created by sbt-khoroshev-iv on 25/05/17.
 */
public interface Util {
    static String allowedPath(String source) {
        return source.replaceAll("[^a-zA-Z0-9-]", "_");
    }
}
