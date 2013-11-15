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

package org.objectscape.colanes.util;

import org.junit.Ignore;
import org.junit.Test;
import org.objectscape.colanes.util.atomic.value.ListenableAtomicIntegerValue;
import org.objectscape.colanes.util.concurrent.value.ListenableConcurrentIntegerValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

/**
 * Result of running <code>putConcurrentListenableMap</code>:
 *
 * time for 2 threads: 20098 ms
 * time for 4 threads: 25256 ms
 * time for 6 threads: 24958 ms
 * time for 8 threads: 25239 ms
 * time for 10 threads: 25004 ms
 * time for 12 threads: 24813 ms
 * time for 14 threads: 24844 ms
 * time for 16 threads: 24785 ms
 *
 * Result of running <code>putputListenableAtomicMap</code>:
 *
 * time for 2 threads atomic: 5031
 * time for 4 threads atomic: 5333
 * time for 6 threads atomic: 5836
 * time for 8 threads atomic: 6053
 * time for 10 threads atomic: 7067
 * time for 12 threads atomic: 8379
 * time for 14 threads atomic: 9706
 * time for 16 threads atomic: 8043
 *
 * So the solution using ScalaSTM atomic blocks beats the solution using ReentrantReadWriteLock
 * by a factor between 2,5 and 4,7. Both solutions seem to scale well.
 */
public class ListenableAtomicConcurrentComparisonTest
{

    private int max = 9000000;
    private int maxThreads = 16;

    @Test
    @Ignore // not part of regression tests - for performance comparison only
    public void incrementConcurrentValue() throws InterruptedException
    {
        for(int i = 1; i * 2 <= maxThreads; i++) {
            incrementConcurrentValue(i * 2, max);
        }
    }

    @Test
    @Ignore // not part of regression tests - for performance comparison only
    public void incrementAtomicValue() throws InterruptedException
    {
        for(int i = 1; i * 2 <= maxThreads; i++) {
            incrementAtomicValue(i * 2, max);
        }
    }

    private void incrementAtomicValue(int numThreads, int max) throws InterruptedException
    {
        CountDownLatch allDone = new CountDownLatch(numThreads);
        ListenableAtomicIntegerValue value = new ListenableAtomicIntegerValue();
        List<Thread> threads = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++)
            threads.add(new Thread(atomictIncrementBlock(value, max, allDone)));

        long start = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++)
            threads.get(i).start();

        allDone.await();

        System.out.println("time for " + numThreads + " threads atomic: " + (System.currentTimeMillis() - start) + " ms");
    }

    private void incrementConcurrentValue(int numThreads, int max) throws InterruptedException
    {
        CountDownLatch allDone = new CountDownLatch(numThreads);
        ListenableConcurrentIntegerValue value = new ListenableConcurrentIntegerValue();
        List<Thread> threads = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++)
            threads.add(new Thread(concurrentIncrementBlock(value, max, allDone)));

        long start = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++)
            threads.get(i).start();

        allDone.await();

        System.out.println("time for " + numThreads + " threads concurrent: " + (System.currentTimeMillis() - start) + " ms");
    }

    private  Runnable concurrentIncrementBlock(ListenableConcurrentIntegerValue value, int max, CountDownLatch done)
    {
        return ()->
        {
            int newValue = 0;

            Function<Integer, Integer> adder = incrementBlock(max);

            while(newValue < max)
                newValue = value.set(adder);

            done.countDown();
        };
    }

    private  Runnable atomictIncrementBlock(ListenableAtomicIntegerValue value, int max, CountDownLatch done)
    {
        return ()->
        {
            int newValue = 0;

            Function<Integer, Integer> adder = incrementBlock(max);

            while(newValue < max)
                newValue = value.setAndGet(adder);

            done.countDown();
        };
    }

    private  Function<Integer, Integer> incrementBlock(int max) {
        return (Integer currentValue)-> {
            if(currentValue == max)
                return currentValue;
            return currentValue + 1;
        };
    }
}
