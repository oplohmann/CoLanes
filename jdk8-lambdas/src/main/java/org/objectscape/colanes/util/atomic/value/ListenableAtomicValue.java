package org.objectscape.colanes.util.atomic.value;

import org.objectscape.colanes.util.AsyncUtils;
import org.objectscape.colanes.util.atomic.AtomicUtils;
import org.objectscape.colanes.util.concurrent.ListenerValue;
import org.objectscape.colanes.util.concurrent.value.SendEvent;
import org.objectscape.colanes.util.concurrent.value.SendListener;
import org.objectscape.colanes.util.concurrent.value.SetEvent;
import org.objectscape.colanes.util.concurrent.value.SetListener;

import java.util.Map;
import java.util.function.Function;

import static scala.concurrent.stm.japi.STM.afterCommit;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 29.05.13
 * Time: 07:55
 * To change this template use File | Settings | File Templates.
 */
public class ListenableAtomicValue<V> implements AtomicUtils, AsyncUtils {

    protected String name = null;
    protected  V immutableValue = null;

    protected Map<SetListener<V>, ListenerValue> setListeners = newMap();
    protected Map<SendListener<V>, ListenerValue> sendListeners = newMap();

    protected ListenableAtomicValue() {
        super();
    }

    public ListenableAtomicValue(V immutableValue) {
        super();
        this.immutableValue = immutableValue;
    }

    public ListenableAtomicValue(String name, V immutableValue) {
        super();
        this.name = name;
        this.immutableValue = immutableValue;
    }

    public V setAndGet(Function<V, V> function) {
        return atomic(() -> {
            V previousValue = immutableValue;
            immutableValue = function.apply(immutableValue);
            notifySetListeners(previousValue);
            return immutableValue;
        });
    }

    public V getAndSet(Function<V, V> function) {
        return atomic(() -> {
            V previousValue = immutableValue;
            immutableValue = function.apply(immutableValue);
            notifySetListeners(previousValue);
            return previousValue;
        });
    }

    public V send() {
        return atomic(() -> {
            notifySendListeners();
            return immutableValue;
        });
    }

    private void notifySetListeners(V previousValue) {
        afterCommit(() -> {
            for(Map.Entry<SetListener<V>, ListenerValue> entry : setListeners.entrySet()) {
                ListenerValue value = entry.getValue();
                if(value.isAsynchronous()) {
                    async(() -> {
                        entry.getKey().accept(new SetEvent<>(name, previousValue, immutableValue, value.nextInvocationCount()));
                    });
                }
                else {
                    entry.getKey().accept(new SetEvent<>(name, previousValue, immutableValue, value.nextInvocationCount()));
                }
            }
        });
    }

    private void notifySendListeners()
    {
        afterCommit(() -> {
            for(Map.Entry<SendListener<V>, ListenerValue> entry : sendListeners.entrySet()) {
                ListenerValue value = entry.getValue();
                if(value.isAsynchronous()) {
                    async(() -> {
                        entry.getKey().accept(new SendEvent<>(name, immutableValue, value.nextInvocationCount()));
                    });
                }
                else {
                    entry.getKey().accept(new SendEvent<>(name, immutableValue, value.nextInvocationCount()));
                }
            }
        });
    }

    public V get() {
        return atomic(() -> {
            return immutableValue;
        });
    }

    public void addSynchronousListener(SetListener<V> listener)
    {
        atomic(() -> {
            setListeners.put(listener, new ListenerValue(false));
        });
    }

    public void addListener(SetListener<V> listener)
    {
        atomic(() -> {
            setListeners.put(listener, new ListenerValue());
        });
    }

    public void addSynchronousListener(SendListener<V> listener)
    {
        atomic(() -> {
            sendListeners.put(listener, new ListenerValue(false));
        });
    }

    public void addListener(SendListener<V> listener)
    {
        atomic(() -> {
            sendListeners.put(listener, new ListenerValue());
        });
    }

    public boolean removeListener(SetListener<V> listener)
    {
        return atomic(() -> {
            return setListeners.remove(listener) != null;
        });
    }

    public boolean removeListener(SendListener<V> listener)
    {
        return atomic(() -> {
            return sendListeners.remove(listener) != null;
        });
    }

}
