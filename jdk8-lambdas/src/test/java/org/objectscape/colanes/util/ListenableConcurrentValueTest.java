package org.objectscape.colanes.util;

import org.junit.Assert;
import org.junit.Test;
import org.objectscape.colanes.util.concurrent.value.ListenableConcurrentValue;
import org.objectscape.colanes.util.concurrent.value.SendEvent;
import org.objectscape.colanes.util.concurrent.value.SetEvent;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 29.05.13
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */
public class ListenableConcurrentValueTest {

    @Test
    public void set()
    {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue("TestValueHolder", new Integer(0));
        boolean success = false;

        do {
            Integer currentValue = valueHolder.get();
            if(currentValue != null && currentValue.equals(new Integer(1))) {
                success = true;
                break;
            }
            success = valueHolder.set(currentValue, currentValue + 1);
        }
        while(!success);

        Assert.assertEquals(new Integer(1), valueHolder.get());
    }

    @Test
    public void setExpectWithFunction()
    {
        int delta = new Random(System.currentTimeMillis()).nextInt();
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue<>("TestValueHolder", new Integer(0));
        boolean success = false;

        do {
            Integer currentValue = valueHolder.get();
            success = valueHolder.set(currentValue, (Integer i)-> { return i + delta; });
        }
        while(!success);

        Assert.assertEquals(new Integer(delta), valueHolder.get());
    }

    @Test
    public void setWithFunction()
    {
        int delta = new Random(System.currentTimeMillis()).nextInt();
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue("TestValueHolder", new Integer(0));
        Integer newValue = null;

        Integer currentValue = valueHolder.get();
        newValue = valueHolder.set((Integer i)-> {
            if(valueHolder.get() == 0)
                return i + delta;
            return -1;
        });

        Assert.assertEquals(new Integer(delta), valueHolder.get());
    }

    @Test
    public void setWithFunctionException()
    {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue("TestValueHolder", new Integer(0));
        boolean[] listenerInvoked = new boolean[] { false };
        valueHolder.addListener((SetEvent<Integer> event) -> {
            listenerInvoked[0] = true;
        });

        try
        {
            valueHolder.set((Integer i)-> {
                if(true)
                    throw new RuntimeException("test exception");
                return -1;
            });
        }
        catch (RuntimeException e) {
            // value remained unchanged, because changes were rolled back when exception occurred
            Assert.assertEquals(new Integer(0), valueHolder.get());
        }

        Assert.assertFalse(listenerInvoked[0]);
    }

    @Test
    public void setWithAynchronousSetListener() throws InterruptedException {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue("TestValueHolder", new Integer(0));

        Integer[] previousAndNewValue = new Integer[] { -1, -1};

        CountDownLatch latch = new CountDownLatch(1);

        valueHolder.addListener((SetEvent<Integer> event) -> {
            previousAndNewValue[0] = event.getPreviousValue();
            previousAndNewValue[1] = event.getValue();
            latch.countDown();
        });

        valueHolder.set((Integer i)-> { return 5; });

        latch.await();
        Assert.assertEquals(new Integer(0), previousAndNewValue[0]);
        Assert.assertEquals(new Integer(5), previousAndNewValue[1]);
    }

    @Test
    public void setWithSynchronousSetListener() throws InterruptedException
    {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue("TestValueHolder", new Integer(0));

        Integer[] previousAndNewValue = new Integer[] { -1, -1};

        valueHolder.addSynchronousListener((SetEvent<Integer> event) -> {
            previousAndNewValue[0] = event.getPreviousValue();
            previousAndNewValue[1] = event.getValue();
        });

        valueHolder.set((Integer i)-> { return 5; });

        Assert.assertEquals(new Integer(0), previousAndNewValue[0]);
        Assert.assertEquals(new Integer(5), previousAndNewValue[1]);
    }

    @Test
    public void setWithSynchronousSendListener() throws InterruptedException {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue("TestValueHolder", new Integer(0));

        Integer[] newValue = new Integer[] { -1 };

        valueHolder.addSynchronousListener((SendEvent<Integer> event) -> {
            newValue[0] = event.getValue();
        });

        valueHolder.set((Integer i)-> { return i + 5; });
        valueHolder.send();

        Assert.assertEquals(new Integer(5), newValue[0]);
    }

    @Test
    public void setWithAsynchronousSendListener() throws InterruptedException {
        ListenableConcurrentValue<Integer> valueHolder = new ListenableConcurrentValue("TestValueHolder", new Integer(0));

        Integer[] newValue = new Integer[] { -1 };
        CountDownLatch latch = new CountDownLatch(1);

        valueHolder.addListener((SendEvent<Integer> event) -> {
            newValue[0] = event.getValue();
            latch.countDown();
        });

        valueHolder.set((Integer i)-> { return i + 5; });
        valueHolder.send();

        latch.await();
        Assert.assertEquals(new Integer(5), newValue[0]);
    }
}
