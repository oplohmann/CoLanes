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

import org.objectscape.colanes.util.CallerMustSynchronize;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class HandbackFutureCompletion<T> extends AbstractHandback
{
    private CompletableFuture<T> future = new CompletableFuture<>();
    private Supplier<T> supplier = null;
    private Consumer<T> callbackConsumer = null;
    private Consumer<T> whenDoneConsumer = null;

    public HandbackFutureCompletion() {
        super();
    }

    public HandbackFutureCompletion(boolean allowOverwrite) {
        super(allowOverwrite);
    }

    public HandbackFutureCompletion(CoLane queue) {
        super(queue);
    }

    public HandbackFutureCompletion(CoLane queue, boolean allowOverwrite) {
        super(queue, allowOverwrite);
    }

    @Override
    @CallerMustSynchronize
    protected void checkValidBeforeEnqueue() {
        super.checkValidBeforeEnqueue();
        if(supplier == null)
            throw new NullPointerException("supplier null");
        checkNotAlreadyEnqueued();
    }

    public HandbackFutureCompletion<T> supplier(Supplier<T> supplier) {
        synchronized (lock) {
            checkNotAlreadyEnqueued();
            checkAllowOverwrite(supplier, "supplier");
            this.supplier = supplier;
            return this;
        }
    }

    public HandbackFutureCompletion<T> callback(Runnable callback) {
        setCallback(callback);
        return this;
    }

    public HandbackFutureCompletion<T> whenDone(Runnable whenDone) {
        setWhenDone(whenDone);
        return this;
    }

    public HandbackFutureCompletion<T> callback(Consumer<T> callback) {
        synchronized (lock) {
            checkNotAlreadyEnqueued();
            checkAllowOverwrite(callbackConsumer, "callbackConsumer");
            this.callbackConsumer = callback;
        }
        return this;
    }

    public HandbackFutureCompletion<T> whenDone(Consumer<T> whenDone) {
        synchronized (lock) {
            checkNotAlreadyEnqueued();
            checkAllowOverwrite(whenDoneConsumer, "whenDoneConsumer");
            this.whenDoneConsumer = whenDone;
        }
        return this;
    }

    public CompletableFuture<T> future() {
        synchronized (lock) {
            return future;
        }
    }

    @CallerMustSynchronize
    @Override
    protected void invokeFunction() {
        // future can be relied on never to be null
        future.complete(supplier.get());
    }

    @CallerMustSynchronize
    @Override
    protected void invokeCallback()
    {
        super.invokeCallback();

        if(callbackConsumer == null)
            return;

        try {
            callbackConsumer.accept(future().get());
        }
        catch (InterruptedException e) {
            // will never be thrown as the result has already been setAndGet in the
            // future when the callback is called.
        }
        catch (ExecutionException e) {
            // will never be thrown as the result has already been setAndGet in the
            // future when the callback is called.
        }
    }

    @CallerMustSynchronize
    @Override
    protected void invokeWhenDone()
    {
        super.invokeWhenDone();

        if(whenDoneConsumer == null)
            return;

        lane.run(() ->
                {
                    try {
                        whenDoneConsumer.accept(future.get());
                    } catch (Exception e) {
                        // will never be thrown as the result has already been setAndGet in the
                        // future when whenDone is called.
                    }
                });
    }

    public HandbackFutureCompletion<T> runAsync()
    {
        enqueue();
        return this;
    }

    public HandbackFutureCompletion<T> runAsyncWhenDone(Consumer<T> whenDoneConsumer)
    {
        whenDone(whenDoneConsumer);
        runAsync();
        return this;
    }

    public HandbackFutureCompletion<T> sender(CoReceiver coReceiver) {
        setSender(coReceiver);
        return this;
    }

    public HandbackFutureCompletion<T> namedSender(CoReceiver coReceiver) {
        setNamedSender(coReceiver);
        return this;
    }

}
