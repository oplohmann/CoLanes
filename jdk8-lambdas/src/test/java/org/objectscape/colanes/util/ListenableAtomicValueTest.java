package org.objectscape.colanes.util;

import junit.framework.Assert;
import org.junit.Test;
import org.objectscape.colanes.util.atomic.value.ListenableAtomicIntegerValue;
import org.objectscape.colanes.util.atomic.value.ListenableAtomicValue;
import org.objectscape.colanes.util.concurrent.value.SendEvent;
import org.objectscape.colanes.util.concurrent.value.SetEvent;

import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 29.05.13
 * Time: 08:04
 * To change this template use File | Settings | File Templates.
 */
public class ListenableAtomicValueTest {

    @Test
    public void set()
    {
        ListenableAtomicValue<Integer> value = new ListenableAtomicValue<>(new Integer(0));
        Integer newValue = value.setAndGet((Integer val) -> {
            return val + 1;
        });
        Assert.assertEquals(new Integer(1), newValue);
    }

    @Test
    public void setInteger() throws InterruptedException
    {
        ListenableAtomicIntegerValue value = new ListenableAtomicIntegerValue();
        Integer newValue = value.incrementAndGet();
        Assert.assertEquals(new Integer(1), newValue);

        value = new ListenableAtomicIntegerValue();
        Integer returnedValue = value.getAndIncrement();
        Assert.assertEquals(new Integer(0), returnedValue);
        Assert.assertEquals(new Integer(1), value.get());

        value = new ListenableAtomicIntegerValue();
        newValue = value.decrementAndGet();
        Assert.assertEquals(new Integer(-1), newValue);

        value = new ListenableAtomicIntegerValue();
        returnedValue = value.getAndDecrement();
        Assert.assertEquals(new Integer(0), returnedValue);
        Assert.assertEquals(new Integer(-1), value.get());

        value = new ListenableAtomicIntegerValue();
        newValue = value.addAndGet(5);
        Assert.assertEquals(new Integer(5), newValue);

        value = new ListenableAtomicIntegerValue();
        returnedValue = value.getAndAdd(5);
        Assert.assertEquals(new Integer(0), returnedValue);
        Assert.assertEquals(new Integer(5), value.get());

        final CountDownLatch waitTillDone = new CountDownLatch(1);
        value = new ListenableAtomicIntegerValue(2);
        boolean[] eventWasSent = new boolean[] { false };
        value.addListener((SetEvent<Integer> event) -> {
            Assert.assertEquals(new Integer(2), event.getPreviousValue());
            Assert.assertEquals(new Integer(25), event.getValue());
            eventWasSent[0] = true;
            waitTillDone.countDown();
        });
        value.getAndAdd(23);
        waitTillDone.await();
        Assert.assertEquals(new Integer(25), value.get());
        Assert.assertTrue(eventWasSent[0]);

        value = new ListenableAtomicIntegerValue(2);
        final CountDownLatch waitTillDone2 = new CountDownLatch(1);
        eventWasSent[0] = false;
        value.addSynchronousListener((SendEvent<Integer> event) -> {
            Assert.assertEquals(new Integer(2), event.getValue());
            eventWasSent[0] = true;
            waitTillDone2.countDown();
        });

        value.send();
        waitTillDone.await();
        System.out.println("done");
        Assert.assertTrue(eventWasSent[0]);
    }

    @Test
    public void setIntegerSynchronousListener() throws InterruptedException
    {
        final CountDownLatch waitTillDone = new CountDownLatch(1);
        ListenableAtomicIntegerValue value = new ListenableAtomicIntegerValue(2);
        boolean[] eventWasSent = new boolean[] { false };
        value.addSynchronousListener((SetEvent<Integer> event) -> {
            Assert.assertEquals(new Integer(2), event.getPreviousValue());
            Assert.assertEquals(new Integer(25), event.getValue());
            eventWasSent[0] = true;
            waitTillDone.countDown();
        });
        value.getAndAdd(23);
        waitTillDone.await();
        Assert.assertEquals(new Integer(25), value.get());
        Assert.assertTrue(eventWasSent[0]);
    }
}
