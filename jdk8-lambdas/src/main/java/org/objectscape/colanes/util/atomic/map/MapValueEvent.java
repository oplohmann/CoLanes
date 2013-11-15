package org.objectscape.colanes.util.atomic.map;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 06.06.13
 * Time: 08:01
 * To change this template use File | Settings | File Templates.
 */
public abstract class MapValueEvent<V> extends MapEvent<V> {

    protected V value = null;

    protected MapValueEvent() {
        super();
    }

    protected MapValueEvent(V value) {
        super();
        this.value = value;
    }

    protected MapValueEvent(String mapName, Object key) {
        super();
        this.key = key;
    }

    protected MapValueEvent(String mapName, Object key, V value, int nextInvocationCount) {
        super(mapName, key, nextInvocationCount);
        this.value = value;
    }

    public V getValue() {
        return value;
    }

}
