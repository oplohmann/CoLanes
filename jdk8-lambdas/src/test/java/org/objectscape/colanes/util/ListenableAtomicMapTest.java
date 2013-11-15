package org.objectscape.colanes.util;

import org.objectscape.colanes.util.atomic.map.*;
import org.junit.Assert;
import org.junit.Test;

import static scala.concurrent.stm.japi.STM.atomic;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 03.06.13
 * Time: 08:20
 * To change this template use File | Settings | File Templates.
 */
public class ListenableAtomicMapTest {

    @Test
    public void put() {
        boolean[] listener1Called = new boolean[]{false};
        boolean[] listener2Called = new boolean[]{false};

        atomic(() -> {
            ListenableAtomicMap<String, Integer> map = new ListenableAtomicMap<>("map1");
            PutListener<Integer> listener = (PutEvent<Integer> event) -> {
                listener1Called[0] = true;
            };
            map.addSynchronousListener("1", listener);
            map.put("1", 1);

            ListenableAtomicMap<String, Integer> map2 = new ListenableAtomicMap<>("map2");
            PutListener<Integer> listener2 = (PutEvent<Integer> event) -> {
                listener2Called[0] = true;
            };
            map2.addSynchronousListener("2", listener2);
            map2.put("2", 2);
        });

        Assert.assertTrue(listener1Called[0]);
        Assert.assertTrue(listener2Called[0]);
    }

    @Test
    public void putRollback() {
        boolean[] listener1Called = new boolean[]{false};
        boolean[] listener2Called = new boolean[]{false};

        boolean exceptionOccurred = false;

        ListenableAtomicMap<String, Integer>[] map = new ListenableAtomicMap[1];
        ListenableAtomicMap<String, Integer>[] map2 = new ListenableAtomicMap[1];

        atomic(() ->
                {
                    map[0] = new ListenableAtomicMap<>("map1");
                    map2[0] = new ListenableAtomicMap<>("map1");
                });

        try {
            atomic(() ->
                    {
                        PutListener<Integer> listener = (PutEvent<Integer> event) -> {
                            listener1Called[0] = true;
                        };
                        map[0].addSynchronousListener("1", listener);
                        map[0].put("1", 1);

                        PutListener<Integer> listener2 = (PutEvent<Integer> event) -> {
                            listener2Called[0] = true;
                        };
                        map2[0].addSynchronousListener("2", listener2);
                        map2[0].put("2", 2);

                        int i = 1 / 0;
                        System.out.println("we won't get here");
                    });
        } catch (Exception e) {
            exceptionOccurred = true;
        }

        Assert.assertFalse(listener1Called[0]);
        Assert.assertFalse(listener2Called[0]);

        Assert.assertTrue(exceptionOccurred);

        atomic(() ->
                {
                    Assert.assertNull(map[0].get("1"));
                    Assert.assertNull(map2[0].get("1"));
                });
    }

    @Test
    public void putRemoveSend()
    {
        boolean[] listener1Called = new boolean[]{ false };
        boolean[] listener2Called = new boolean[]{ false };
        boolean[] sendListenerCalled = new boolean[] { false };

        boolean exceptionOccurred = false;

        ListenableAtomicMap<String, Integer>[] map = new ListenableAtomicMap[1];
        ListenableAtomicMap<String, Integer>[] map2 = new ListenableAtomicMap[1];

        atomic(() ->
        {
            map[0] = new ListenableAtomicMap<>("map1");
            map2[0] = new ListenableAtomicMap<>("map2");
        });

        try
        {
            atomic(() ->
            {
                PutListener<Integer> listener = (PutEvent<Integer> event) -> {
                    listener1Called[0] = true;
                };

                map[0].addSynchronousListener("1", listener);
                map[0].put("1", 1);

                PutListener<Integer> listener2 = (PutEvent<Integer> event) -> {
                    listener2Called[0] = true;
                 };

                map2[0].addSynchronousListener("2", listener2);
                map2[0].put("2", map[0].get("1"));

                map[0].remove("1");

                SendListener<Integer> sendListener = (SendEvent<Integer> event) -> {
                    sendListenerCalled[0] = true;
                };

                map2[0].addSynchronousListener("2", sendListener);
                map2[0].send("2");
             });
        }
        catch (Exception e) {
            exceptionOccurred = true;
        }

        Assert.assertTrue(listener1Called[0]);
        Assert.assertTrue(listener2Called[0]);
        Assert.assertTrue(sendListenerCalled[0]);

        Assert.assertFalse(exceptionOccurred);

        atomic(() ->
        {
            Assert.assertNull(map[0].get("1"));
            Assert.assertEquals(new Integer(1), map2[0].get("2"));
        });
    }

    @Test
    public void putRemoveSendRollback()
    {
        boolean[] listener1Called = new boolean[]{false};
        boolean[] listener2Called = new boolean[]{false};

        boolean exceptionOccurred = false;

        ListenableAtomicMap<String, Integer>[] map = new ListenableAtomicMap[1];
        ListenableAtomicMap<String, Integer>[] map2 = new ListenableAtomicMap[1];

        atomic(() ->
        {
            map[0] = new ListenableAtomicMap<>("map1");
            map2[0] = new ListenableAtomicMap<>("map2");
        });

        try
        {
            atomic(() ->
            {
                PutListener<Integer> listener = (PutEvent<Integer> event) -> {
                    listener1Called[0] = true;
                };

                map[0].addSynchronousListener("1", listener);
                map[0].put("1", 1);

                PutListener<Integer> listener2 = (PutEvent<Integer> event) -> {
                    listener2Called[0] = true;
                };

                map2[0].addSynchronousListener("2", listener2);
                map2[0].put("2", map[0].get("1"));

                map[0].remove("1");

                boolean sendListenerCalled[] = new boolean[] { false };
                SendListener<Integer> sendListener = (SendEvent<Integer> event) -> {
                    sendListenerCalled[0] = true;
                };

                map2[0].send("2");

                int i = 1 / 0;
                System.out.println("we won't get here");
            });
        }
        catch (Exception e) {
            exceptionOccurred = true;
        }

        Assert.assertFalse(listener1Called[0]);
        Assert.assertFalse(listener2Called[0]);

        Assert.assertTrue(exceptionOccurred);

        atomic(() ->
                {
                    Assert.assertNull(map[0].get("1"));
                    Assert.assertNull(map2[0].get("1"));
                });
    }
}
