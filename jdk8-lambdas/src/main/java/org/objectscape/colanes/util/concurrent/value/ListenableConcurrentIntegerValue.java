package org.objectscape.colanes.util.concurrent.value;

/**
 * Created with IntelliJ IDEA.
 * User: Nutzer
 * Date: 30.05.13
 * Time: 12:47
 * To change this template use File | Settings | File Templates.
 */
public class ListenableConcurrentIntegerValue extends ListenableConcurrentValue<Integer> {

    public ListenableConcurrentIntegerValue() {
        super(new Integer(0));
    }

    public ListenableConcurrentIntegerValue(String name) {
        super(name, new Integer(0));
    }

    public ListenableConcurrentIntegerValue(String name, Integer value) {
        super(name, value);
    }

    public Integer incrementAndGet() {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue + 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.writeLock().unlock();
        }
    }


    public Integer getAndIncrement() {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue + 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return previousValue;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public Integer decrementAndGet() {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue - 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.writeLock().unlock();
        }
    }


    public Integer getAndDecrement() {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue - 1;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return previousValue;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public Integer addAndGet(int delta) {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue + delta;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public Integer getAndAdd(int delta) {
        lock.writeLock().lock();
        try {
            Integer previousValue = value;
            this.value = previousValue + delta;
            notifySetListeners(new SetEvent<>(name, previousValue, value));
            return value;
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}
