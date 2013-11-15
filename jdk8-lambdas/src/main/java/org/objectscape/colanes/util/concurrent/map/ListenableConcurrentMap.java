package org.objectscape.colanes.util.concurrent.map;


import org.objectscape.colanes.util.ImmutableEntry;
import org.objectscape.colanes.util.ImmutableList;
import org.objectscape.colanes.util.ImmutableSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 25.04.13
 * Time: 07:36
 * To change this template use File | Settings | File Templates.
 */
public interface ListenableConcurrentMap<K, V>
{
    int size();

    boolean isEmpty();

    boolean containsKey(Object key);

    boolean containsValue(Object value);

    ImmutableList<V> get(Object key);

    public V getSingleValue(Object key);

    ImmutableList<V> put(K key, ImmutableList<V> value);

    ImmutableList<V> remove(Object key);

    void putAll(Map<? extends K, ? extends ImmutableList<V>> map);

    void clear();

    Set<K> keySet();

    Collection<ImmutableList<V>> values();

    ImmutableSet<ImmutableEntry<K, ImmutableList<V>>> entrySet();

    void addAsynchronousListener(K key, PutListener<V> listener);

    void addAsynchronousListener(K key, PutListener<V> listener, boolean notifyWhenKeyPresent);

    void addSynchronousListener(K key, RemoveListener<V> listener);

    void addSynchronousListener(K key, SendListener<V> listener);

    void addSynchronousListener(K key, SendListener<V> listener, boolean notifyWhenKeyPresent);

    boolean removeListener(K key, PutListener<V> listener);

    boolean removeListener(K key, RemoveListener<V> listener);

    boolean removeListener(K key, SendListener<V> listener);

    int clearListeners();

    ImmutableList<V> putIfAbsent(K key, ImmutableList<V> value);

    ImmutableList<V> putIfAbsentSingleValue(K key, V value);

    ImmutableList<V> putIfAbsentOrIfEmpty(K key, ImmutableList<V> value);

    ImmutableList<V> putIfAbsentOrIfEmpty(K key, V value);

    public ImmutableList<V> putSingleValue(K key, V value);

    boolean remove(Object key, Object value);

    boolean replace(K key, ImmutableList<V> oldValue, ImmutableList<V> newValue);

    ImmutableList<V> replace(K key, ImmutableList<V> values);

    ImmutableList<V> replaceSingleValue(K key, V value);

    int send(K key);

    void addSynchronousListener(K key, PutListener<V> listener, boolean notifyWhenKeyPresent);

    void addAsynchronousListener(K key, SendListener<V> listener, boolean notifyWhenKeyPresent);

    void addAsynchronousListener(K key, SendListener<V> listener);

    void addAsynchronousListener(K key, RemoveListener<V> listener);

    void addSynchronousListener(K key, PutListener<V> listener);
}
