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

package org.objectscape.colanes.coreceivers.stm;

import org.objectscape.colanes.coreceivers.AbstractMemoizingCalculator;
import org.objectscape.colanes.registry.Registry;
import org.objectscape.colanes.util.atomic.map.ListenableAtomicMap;
import org.objectscape.colanes.util.atomic.map.SendEvent;
import org.objectscape.colanes.util.atomic.map.SendListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class MemoizingListenableCalculatorSTM extends AbstractMemoizingCalculator {

    protected static final String FactorialCalculationRegistration = "FactorialCalculationRegistration";

    private Map<Integer, Long> factorialMap;
    private ListenableAtomicMap<Integer, Integer> calculationReservationMap;
    private Map<Integer, SendListener<Integer>> listenerMap = new HashMap<>();

    public MemoizingListenableCalculatorSTM() {
        factorialMap = Registry.getDefault().getAtomicMapNamed(FactorialCacheName);
        calculationReservationMap = Registry.getDefault().getListenableAtomicMapNamed(FactorialCalculationRegistration);
    }

    public void factorialMemoized(Integer n, CompletableFuture<Long> result)
    {
        lane.run(() -> {
            factorialMemoizedInternal(n, result, false);
        });
    }

    private void factorialMemoizedInternal(Integer n, CompletableFuture<Long> result, boolean repeatedCall)
    {
        if(repeatedCall) {
            // remove eclusive calculation reservation token as the result has meanwhile been
            // calculated by some other calculator lane
            atomic(()->{
                calculationReservationMap.removeListener(n, listenerMap.get(n));
            });
        }

        if(n <= 1) {
            result.complete(1L);
            return;
        }

        atomic(()->
        {
            Long factorial = factorialMap.get(n);
            if(factorial != null) {
                System.out.println(getName() + ": found factorial(" + n + ") in cache");
                result.complete(factorial);
                return;
            }

            factorial = factorial(n);

            System.out.println(getName() + ": calculated factorial(" + n + ") and put it into cache");
            factorialMap.put(n, factorial);
            result.complete(factorial);
        });


        if(true)
            return;

        // Result not yet calculated. Register a reservation token to calculate it exclusively, so that no other calculator
        // lane starts calculating it and it is unnecessarily calculated several times at the "same time"
        atomic(()->
        {
            Integer earlierReservation = calculationReservationMap.put(n, n);

            if(earlierReservation != null)
            {
                // Somebody else has already registered a calculation reservation. Add a listener to get notified when
                // the other calculator has provided the result.
                System.out.println(getName() + ": postponing calculating factorial(" + n + ") as someone else is currently calculating it");
                boolean notifyWhenKeyMeanWhilePresent = true; // very important to prevent lost notification
                SendListener<Integer> listener = (SendEvent<Integer> event) -> {
                    lane.run(() -> {
                        factorialMemoizedInternal(n, result, true);
                    });
                };
                listenerMap.put(n, listener);
                calculationReservationMap.addSynchronousListener(n, listener, notifyWhenKeyMeanWhilePresent);
            }
        });

        long factorial = factorial(n);

        System.out.println(getName() + ": calculated factorial(" + n + ") and put it into cache");
        factorialMap.put(n, factorial);
        calculationReservationMap.send(n);
        result.complete(factorial);
    }

}
