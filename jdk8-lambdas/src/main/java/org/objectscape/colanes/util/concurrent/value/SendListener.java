package org.objectscape.colanes.util.concurrent.value;


import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 27.05.13
 * Time: 18:22
 * To change this template use File | Settings | File Templates.
 */
public interface SendListener<V> extends Consumer<SendEvent<V>> {
}
