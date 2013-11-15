package org.objectscape.colanes.util.atomic;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 08.06.13
 * Time: 10:19
 * To change this template use File | Settings | File Templates.
 */
public interface AtomicUtils {

    default public void atomic(Runnable runnable) {
        scala.concurrent.stm.japi.STM.atomic(()-> {
            runnable.run();
        });
    }

    default public <T> T atomic(Callable<T> callable) {
        return scala.concurrent.stm.japi.STM.atomic(()-> {
            return callable.call();
        });
    }

    default public <K, V> Map<K, V> newMap() {
        return scala.concurrent.stm.japi.STM.newMap();
    }

    default public <E> Set<E> newSet() {
        return scala.concurrent.stm.japi.STM.newSet();
    }

}
