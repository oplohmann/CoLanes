package org.objectscape.colanes.util.concurrent;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 17.06.13
 * Time: 08:06
 * To change this template use File | Settings | File Templates.
 */
public class ListenerValue {

    private final boolean asynchronous;
    private int invocationCount = -1;

    public ListenerValue() {
        this.asynchronous = true;
    }

    public ListenerValue(boolean asynchronous) {
        super();
        this.asynchronous = asynchronous;
    }

    public int nextInvocationCount() {
        return ++invocationCount;
    }

    public boolean isAsynchronous() {
        return asynchronous;
    }

}
