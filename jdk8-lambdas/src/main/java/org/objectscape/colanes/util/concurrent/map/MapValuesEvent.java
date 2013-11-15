package org.objectscape.colanes.util.concurrent.map;

import org.objectscape.colanes.util.ImmutableList;
import org.objectscape.colanes.util.atomic.map.MapEvent;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 06.06.13
 * Time: 07:45
 * To change this template use File | Settings | File Templates.
 */
public class MapValuesEvent<V> extends MapEvent<V> {

    /**
     * Values that were involved in the <code>MapEvent</code>, such as added or removed elements
     */
    protected ImmutableList<V> values = null;

    /**
     * Creates a new <code>MapEvent</code> object
     */
    public MapValuesEvent() {
        super();
    }

    /**
     * Creates a new <code>MapEvent</code> object
     *
     * @param mapName name of the map that signaled the event
     * @param key key of the values that changed as a result of the map change
     */
    public MapValuesEvent(String mapName, Object key, int nextInvocationCount) {
        super(mapName, key, nextInvocationCount);
    }

    /**
     * Creates a new <code>MapEvent</code> object
     *
     * @param mapName name of the map that signaled the event
     * @param key key of the values that changed as a result of the map change
     */
    public MapValuesEvent(String mapName, Object key, ImmutableList<V> values, int nextInvocationCount) {
        super(mapName, key, nextInvocationCount);
        this.values = values;
    }

    /**
     * Return the values that were involved in the <code>MapEvent</code>, such as added or removed elements
     *
     * @return
     */
    public ImmutableList<V> getValues() {
        return values;
    }

    /**
     * Return a single element in case the values of the key that signaled the event contains a single
     * element only. Throws an <code>IllegalStateException</code> in case the values collection contains
     * more than one element.
     *
     * @return the single element in the values collection or null if it is empty
     */
    public V getValue() {
        if(values == null || values.isEmpty())
            return null;
        if(values.size() > 1)
            throw new IllegalStateException("values contain " + values.size() + " elements");
        return values.get(0);
    }

}
