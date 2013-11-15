package org.objectscape.colanes;

import org.junit.Assert;
import org.junit.Test;
import org.objectscape.colanes.util.AsyncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: Nutzer
 * Date: 09.06.13
 * Time: 08:29
 * To change this template use File | Settings | File Templates.
 */
public class GoroutineTest extends AbstractTest implements AsyncUtils {

    private static Random Rand = new Random(System.currentTimeMillis());

    @Test
    public void runAsyncLatch() throws InterruptedException
    {
        CountDownLatch latch = new CountDownLatch(1);

        async(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Assert.assertFalse(true);
            }
            latch.countDown();
        });

        doSomethingForAWhile();
        latch.await();
    }

    @Test
    public void runAsyncBlockingQueue() throws InterruptedException
    {
        BlockingQueue<Integer> channel = new LinkedBlockingQueue<>();

        async(()->{
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Assert.assertFalse(true);
            }
            channel.add(1);
        });

        doSomethingForAWhile();
        int value = channel.take();
        Assert.assertEquals(1, value);
    }

    @Test
    public void runAsyncSort() throws InterruptedException
    {
        BlockingQueue<Integer> channel = new LinkedBlockingQueue<>();
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            list.add(Rand.nextInt());
        }

        async(()-> {
            System.out.println("begin sorting");
            Collections.sort(list);
            System.out.println("end sorting");
            channel.add(1);
        });

        doSomethingForAWhile();
        int value = channel.take();
        System.out.println("done");
        Assert.assertEquals(1, value);
    }

    private void doSomethingForAWhile() throws InterruptedException
    {
        System.out.println("begin doSomethingForAWhile");
        Thread.sleep(1000);
        System.out.println("end doSomethingForAWhile");
    }

    @Test
    public void sumAsync() throws InterruptedException {
        BlockingQueue<Integer> channel = new LinkedBlockingQueue<>();

        async(()->{
            int result = 0;
            for(int i = 0; i < 100000000; i++) {
                result = result + i;
            }

            channel.add(result);
        });

        int sum = channel.take();
        System.out.println("The sum is: " + sum);
    }
}
