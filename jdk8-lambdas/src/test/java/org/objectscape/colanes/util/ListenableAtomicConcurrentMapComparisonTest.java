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
import org.objectscape.colanes.util.atomic.AtomicUtils;
import org.objectscape.colanes.util.atomic.map.ListenableAtomicMap;
import org.objectscape.colanes.util.concurrent.map.ListenableConcurrentHashMap;
import org.objectscape.colanes.util.concurrent.map.ListenableConcurrentMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

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
 * Result of running <code>putListenableAtomicMap</code>:
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
public class ListenableAtomicConcurrentMapComparisonTest implements AtomicUtils
{

    private int max = 9000000;
    private int maxThreads = 16;

    @Test
    @Ignore // not part of regression tests - for performance comparison only
    public void putConcurrentMap() throws InterruptedException
    {
        for(int i = 1; i * 2 <= maxThreads; i++) {
            putConcurrentMap(i * 2, max);
        }
    }

    @Test
    @Ignore // not part of regression tests - for performance comparison only
    public void putConcurrentListenableMap() throws InterruptedException
    {
        for(int i = 1; i * 2 <= maxThreads; i++) {
            putConcurrentListenableMap(i * 2, max);
        }
    }

    @Test
    @Ignore // not part of regression tests - for performance comparison only
    public void putAtomicMap() throws InterruptedException
    {
        for(int i = 1; i * 2 <= maxThreads; i++) {
            putAtomicMap(i * 2, max);
        }
    }

    @Test
    @Ignore // not part of regression tests - for performance comparison only
    public void putListenableAtomicMap() throws InterruptedException
    {
        for(int i = 1; i * 2 <= maxThreads; i++) {
            putListenableAtomicMap(i * 2, max);
        }
    }

    private void putAtomicMap(int numThreads, int max) throws InterruptedException
    {
        CountDownLatch allDone = new CountDownLatch(numThreads);
        Map<String, String> map = newMap();
        List<Thread> threads = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++)
            threads.add(new Thread(atomictPutBlock(map, max, allDone)));

        long start = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++)
            threads.get(i).start();

        allDone.await();

        System.out.println("time for " + numThreads + " threads atomic: " + (System.currentTimeMillis() - start) + " ms");
    }

    private Runnable atomictPutBlock(Map<String, String> map, int max, CountDownLatch done)
    {
        return ()->
        {
            int count[] = new int[] { 0 };

            while(count[0] < max) {
                atomic(()->{
                    map.put("1", "1");
                    count[0]++;
                });
            }

            done.countDown();
         };
    }

    private void putListenableAtomicMap(int numThreads, int max) throws InterruptedException
    {
        CountDownLatch allDone = new CountDownLatch(numThreads);
        ListenableAtomicMap<String, String> map = new ListenableAtomicMap<>();
        List<Thread> threads = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++)
            threads.add(new Thread(listenableAtomictPutBlock(map, max, allDone)));

        long start = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++)
            threads.get(i).start();

        allDone.await();

        System.out.println("time for " + numThreads + " threads atomic: " + (System.currentTimeMillis() - start) + " ms");
    }

    private void putConcurrentListenableMap(int numThreads, int max) throws InterruptedException
    {
        CountDownLatch allDone = new CountDownLatch(numThreads);
        ListenableConcurrentMap<String, String> map = new ListenableConcurrentHashMap();
        List<Thread> threads = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++)
            threads.add(new Thread(listenableConcurrentPutBlock(map, max, allDone)));

        long start = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++)
            threads.get(i).start();

        allDone.await();

        System.out.println("time for " + numThreads + " threads concurrent: " + (System.currentTimeMillis() - start) + " ms");
    }

    private void putConcurrentMap(int numThreads, int max) throws InterruptedException
    {
        CountDownLatch allDone = new CountDownLatch(numThreads);
        ConcurrentMap<String, String> map = new ConcurrentHashMap();
        List<Thread> threads = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++)
            threads.add(new Thread(concurrentPutBlock(map, max, allDone)));

        long start = System.currentTimeMillis();

        for (int i = 0; i < numThreads; i++)
            threads.get(i).start();

        allDone.await();

        System.out.println("time for " + numThreads + " threads concurrent: " + (System.currentTimeMillis() - start) + " ms");
    }

    private Runnable concurrentPutBlock(ConcurrentMap<String, String> map, int max, CountDownLatch done) {
        return ()->
        {
            int count = 0;

            while(count < max) {
                map.put("1", "1");
                count++;
            }

            done.countDown();
        };
    }

    private  Runnable listenableConcurrentPutBlock(ListenableConcurrentMap<String, String> map, int max, CountDownLatch done)
    {
        return ()->
        {
            int count = 0;

            while(count < max) {
                map.putSingleValue("1", "1");
                count++;
            }

            done.countDown();
        };
    }

    private  Runnable listenableAtomictPutBlock(ListenableAtomicMap<String, String> map, int max, CountDownLatch done)
    {
        return ()->
        {
            int count[] = new int[] { 0 };

            while(count[0] < max) {
                atomic(()->{
                    map.put("1", "1");
                    count[0]++;
                });
            }

            done.countDown();
        };
    }

}
