package org.objectscape.colanes.util.concurrent.value;

import org.objectscape.colanes.util.AsyncUtils;
import org.objectscape.colanes.util.CallerMustSynchronize;
import org.objectscape.colanes.util.concurrent.ListenerValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 27.05.13
 * Time: 18:03
 * To change this template use File | Settings | File Templates.
 */
public class ListenableConcurrentValue<V> implements AsyncUtils {

    protected String name = null;
    protected V value = null;
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    Map<SetListener<V>, ListenerValue> setListeners = new HashMap<>();
    Map<SendListener<V>, ListenerValue> sendListeners = new HashMap<>();

    public ListenableConcurrentValue() {
        super();
    }

    public ListenableConcurrentValue(String name) {
        super();
        this.name = name;
    }

    public ListenableConcurrentValue(V value) {
        super();
        this.value = value;
    }

    public ListenableConcurrentValue(String name, V value) {
        super();
        this.name = name;
        this.value = value;
    }

    public boolean set(V expectedValue, V newValue)
    {
        lock.writeLock().lock();
        try {
            if(value == null && expectedValue != null)
                return false;
            if(!value.equals(expectedValue))
                return false;
            V previousValue = value;
            this.value = newValue;
            notifySetListeners(previousValue);
            return true;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public boolean set(V expectedValue, Function<V, V> function)
    {
        lock.writeLock().lock();
        try {
            if(value == null && expectedValue != null)
                return false;
            if(!value.equals(expectedValue))
                return false;
            V previousValue = value;
            this.value = function.apply(value);
            notifySetListeners(previousValue);
            return true;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public V set(Function<V, V> function)
    {
        lock.writeLock().lock();
        try {
            V previousValue = value;
            this.value = function.apply(value);
            notifySetListeners(previousValue);
            return value;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public V get()
    {
        lock.readLock().lock();
        try {
            return value;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    @CallerMustSynchronize
    protected void notifySetListeners(V previousValue) {
        if(previousValue != null && previousValue.equals(value))
            return;
        else if(previousValue == value)
            return;
        notifySetListeners(new SetEvent<>(name, previousValue, value));
    }

    @CallerMustSynchronize
    protected void notifySendListeners(SendEvent<V> event) {
        for(Map.Entry<SendListener<V>, ListenerValue> entry : sendListeners.entrySet()) {
            ListenerValue value = entry.getValue();
            if(value.isAsynchronous()) {
                async(()-> {
                    entry.getKey().accept(new SendEvent<>(event, value.nextInvocationCount()));
                });
            }
            else {
                entry.getKey().accept(new SendEvent<>(event, value.nextInvocationCount()));
            }
        }
    }

    @CallerMustSynchronize
    protected void notifySetListeners(SetEvent<V> event) {
        for(Map.Entry<SetListener<V>, ListenerValue> entry : setListeners.entrySet()) {
            ListenerValue value = entry.getValue();
            if(value.isAsynchronous()) {
                async(()-> {
                    entry.getKey().accept(new SetEvent<>(event, value.nextInvocationCount()));
                });
            }
            else {
                entry.getKey().accept(new SetEvent<>(event, value.nextInvocationCount()));
            }
        }
    }

    @CallerMustSynchronize
    protected void notifySendListeners() {
        notifySendListeners(new SendEvent<>(name, value));
    }

    public V send()
    {
        lock.readLock().lock();
        try {
            notifySendListeners();
            return value;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void addListener(SetListener<V> listener)
    {
        lock.writeLock().lock();

        try
        {
            setListeners.put(listener, new ListenerValue());
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void addSynchronousListener(SetListener<V> listener)
    {
        lock.writeLock().lock();

        try
        {
            setListeners.put(listener, new ListenerValue(false));
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void addListener(SendListener<V> listener)
    {
        lock.writeLock().lock();

        try
        {
            sendListeners.put(listener, new ListenerValue());
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void addSynchronousListener(SendListener<V> listener)
    {
        lock.writeLock().lock();

        try
        {
            sendListeners.put(listener, new ListenerValue(false));
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean removeListener(SetListener<V> listener)
    {
        lock.writeLock().lock();

        try
        {
            return setListeners.remove(listener) != null;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean removeListener(SendListener<V> listener)
    {
        lock.writeLock().lock();

        try
        {
            return sendListeners.remove(listener) != null;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

}
