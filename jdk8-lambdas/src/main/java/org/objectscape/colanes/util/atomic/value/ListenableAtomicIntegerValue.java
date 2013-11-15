package org.objectscape.colanes.util.atomic.value;

import static scala.concurrent.stm.japi.STM.atomic;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 29.05.13
 * Time: 08:15
 * To change this template use File | Settings | File Templates.
 */
public class ListenableAtomicIntegerValue extends ListenableAtomicValue<Integer>
{
    public ListenableAtomicIntegerValue() {
        super(new Integer(0));
    }

    public ListenableAtomicIntegerValue(Integer immutableValue) {
        super(immutableValue);
    }

    public ListenableAtomicIntegerValue(String valueName, Integer immutableValue) {
        super(valueName, immutableValue);
    }

    public Integer incrementAndGet() {
        return setAndGet((Integer value) -> { return value + 1; });
    }

    public Integer getAndIncrement() {
        return getAndSet((Integer value) -> { return value + 1; });
    }

    public Integer decrementAndGet() {
        return setAndGet((Integer value) -> { return value - 1; });
    }

    public Integer getAndDecrement() {
        return getAndSet((Integer value) -> {
            return value - 1;
        });
    }

    public Integer addAndGet(int delta) {
        return setAndGet((Integer value) -> { return value + delta; });
    }

    public Integer getAndAdd(int delta) {
        return getAndSet((Integer value) -> { return value + delta; });
    }

}
