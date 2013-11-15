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

import junit.framework.Assert;
import org.junit.Test;
import org.objectscape.colanes.coreceivers.HelloWorld;
import org.objectscape.colanes.coreceivers.HelloWorldWithFutureHandback;
import org.objectscape.colanes.coreceivers.HelloWorldWithHandback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * A collection of simple test cases to test basic lane behavior.
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class HelloWorldTest extends AbstractTest
{

    /**
     * Enqueue a lambda immediately without going through a Handback
     * @throws InterruptedException
     */
    @Test
    public void sayHello() throws InterruptedException {
        HelloWorld helloWorld = new HelloWorld();
        helloWorld.sayHello();
    }

    /**
     * Simple coreceivers of a lambda being enqueued deferred when <tt>runAsync</tt> is called on a
     * <code>Handback</code>
     * @throws InterruptedException
     */
    @Test
    public void sayHelloWithHandback() throws InterruptedException {
        HelloWorldWithHandback helloWorld = new HelloWorldWithHandback();
        helloWorld.sayHello().runAsync();
    }

    @Test(expected = IllegalStateException.class)
    public void sayHelloWithHandbackNoOverwrite() throws InterruptedException {
        HelloWorldWithHandback helloWorld = new HelloWorldWithHandback();
        boolean[] didExecute = new boolean[] { false };
        helloWorld.sayHelloNoHandbackOverwrite().runnable(()-> { didExecute[0] = true; }).runAsync();
        Assert.assertFalse(didExecute[0]);
    }

    /**
     * Sample of a callback lambda being invoked after the enqueued lambda has finished execution.
     * Note that the callback is called by the thread that serves the lane's lane and is therefore
     * exposed to other threads in addition and blocks the lane while it is executed. The callback
     * lambda therefore needs to be thread-safe and only run shortly.
     *
     * @throws InterruptedException
     */
    @Test
    public void sayHelloWithCallback() throws InterruptedException
    {
        CountDownLatch latch = new CountDownLatch(1);

        HelloWorldWithHandback helloWorld = new HelloWorldWithHandback();
        helloWorld.sayHello().callback(()-> { latch.countDown(); }).runAsync();

        boolean timeout = !latch.await(10, TimeUnit.SECONDS);
        Assert.assertFalse(timeout);
    }

    /**
     * Sample of a <code>whenDone</code> callback lambda being invoked after the enqueued lambda has finished execution.
     * Note that the whenDone callback is not called synchronously by the lane's thread as opposed to defining a
     * callback lambda. The <code>whenDone</code> callback lambda is also enqueued and thus executed asynchronously. It
     * therefore does not need any synchronization provided it does not change values of objects referenced outside the lane.
     *
     * @throws InterruptedException
     */
    @Test
    public void sayHelloWithWhenDone() throws InterruptedException
    {
        CountDownLatch latch = new CountDownLatch(1);

        HelloWorldWithHandback helloWorld = new HelloWorldWithHandback();
        helloWorld.sayHello().whenDone(()-> { latch.countDown(); }).runAsync();

        boolean timeout = !latch.await(10, TimeUnit.SECONDS);
        Assert.assertFalse(timeout);
    }

    /**
     * Sample of a lane that returns a future that blocks the calling thread till the enqueued lambda has finished
     * execution. Note that futures are provided for completeness, but their use is discouraged. The use of futures
     * is discouraged if used in a way that the calling thread is blocked until the enqueued lambda has produced a
     * result. Using a future in such a way opens up the doors for possible deadlocks and may reduce responsiveness as
     * asynchronous execution becomes synchronous.
     *
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void sayHelloWithFutureHandback() throws InterruptedException, ExecutionException {
        HelloWorldWithFutureHandback helloWorld = new HelloWorldWithFutureHandback();
        String msg = helloWorld.sayHelloWorld().runAsync().future().get();
        System.out.println(msg);

        helloWorld = new HelloWorldWithFutureHandback();
        helloWorld.sayHelloWorld().runAsync();
    }

    /**
     * Test that a callback lambda cannot be changed any more after some lambda expression
     * of the lane has been scheduled for execution as this would only open up doors for
     * possible race conditions.
     *
     * @throws InterruptedException
     */
    @Test
    public void sayHelloWithFutureHandbackMultiple() throws InterruptedException
    {
        HandbackFutureCompletion<String> handback = new HelloWorldWithFutureHandback().sayHelloWorld();
        handback.callback(() -> {
            System.out.println("hey!");
        });

        // changing the callback must be possible if lambda has not yet been enqueued
        handback.callback(() -> {
            System.out.println("dude!");
        }).runAsync();

        // now the lambda has been scheduled and changing the callback is no longer permitted
        boolean success = false;
        try {
            handback.callback(() -> {
                System.out.println("hola!");
            });
        } catch (Exception e) {
            success = true;
        }

        Assert.assertTrue(success);

        // test setting supplier several times works if lambda has not been enqueued before
        handback = new HelloWorldWithFutureHandback().sayHello2();
        handback.runAsync();

        // test lambda cannot be enqueued more than once for aHandbackFutureCompletion
        success = false;
        try {
            handback.runAsync();
        } catch (IllegalStateException e) {
            success = true;
        }

        Assert.assertTrue(success);
    }

}
