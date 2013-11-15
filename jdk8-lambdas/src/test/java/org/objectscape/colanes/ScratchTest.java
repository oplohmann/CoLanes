/**
 * Copyright (c) 2013 Oliver Plohmann
 * http://www.objectscape.org/colanes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.objectscape.colanes;

import org.junit.Test;
import org.objectscape.colanes.registry.Registry;
import org.objectscape.colanes.registry.LaneTypeMismatchException;
import org.objectscape.colanes.coreceivers.Ping;
import org.objectscape.colanes.coreceivers.Pong;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static scala.concurrent.stm.japi.STM.afterRollback;
import static scala.concurrent.stm.japi.STM.atomic;


/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class ScratchTest {

    @Test
    public void function() {
        System.out.println("test");
        Consumer<String> consumer = (String str)-> { System.out.println(str); };
        consumer.accept("hi");
    }

    @Test
    public void atomicRef() {
        AtomicReference<Integer> ref = new AtomicReference<>();
        Integer intRef = new Integer(0);
        ref.compareAndSet(null, intRef);
        System.out.println(ref.get().intValue());
        ref.compareAndSet(intRef, new Integer(1));
        System.out.println(ref.get().intValue());
    }

    @Test(expected = LaneTypeMismatchException.class)
    public void environment() {
        Registry registry = Registry.getDefault();
        Ping ping = new Ping();
        registry.register(ping);
        Pong pong = registry.getActiveObject(ping.getName(), Pong.class);
        System.out.println(pong);
    }

    @Test
    public void future() throws ExecutionException, InterruptedException, TimeoutException
    {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("hi");

        System.out.println(future.get(3, TimeUnit.MILLISECONDS));
    }

    @Test
    public void rollback() {
        try {
            atomic((Runnable) ()-> {
                afterRollback(()-> {
                    System.out.println("rollback");
                });
                if(true)
                    throw new RuntimeException("test");
            });
        }
        catch (Exception e) {

        }
    }

}
