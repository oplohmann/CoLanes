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

import org.fusesource.hawtdispatch.Dispatch;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.objectscape.colanes.util.Signature;

import java.util.concurrent.TimeUnit;

/**
 * This class is an envelope around the HawtDispatch <code>DispatchQueue</code> class. It hides
 * the creation of a HawtDispatch <code>DispatchQueue</code> object from the user and provides
 * convenience methods to obtain a Handback that knows the HawtDispatch <code>DispatchQueue</code>
 * it is associated with.
 *
 * Every lane needs to defines a <code>CoLane</code> that stores and executes the lambdas
 * that are invoked on it asynchronously in the order they were added to the <code>CoLane</code>.
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 */
public class CoLane {

    /**
     *
     */
    private DispatchQueue queue = null;
    private Signature sender = null;

    public CoLane() {
        super();
        queue = Dispatch.createQueue();
    }

    public CoLane(String label) {
        super();
        queue = Dispatch.createQueue(label);
    }

    public CoLane(DispatchQueue queue) {
        super();
        this.queue = queue;
    }

    public Handback getHandback() {
        return new Handback(this);
    }

    public Handback getHandback(boolean allowOverwrite) {
        return new Handback(this, allowOverwrite);
    }

    public <T> HandbackFutureCompletion<T> getHandbackFutureCompletion() {
        return new HandbackFutureCompletion<>(this);
    }

    public <T> HandbackFutureCompletion<T> getHandbackFutureCompletion(boolean allowOverwrite) {
        return new HandbackFutureCompletion<>(this, allowOverwrite);
    }

    public void run(Runnable runnable) {
        queue.execute(runnable);
    }

    public void runAfter(long duration, TimeUnit unit, Runnable runnable) {
        queue.executeAfter(duration, unit, runnable);
    }

    public void sender(Signature sender) {
        this.sender = sender;
    }

    public <T extends CoReceiver> Signature<T> sender(Class<T> senderClass) {
        if(sender.getActiveObjectClass().isAssignableFrom(senderClass)) {
            return (Signature<T>) sender;
        }
        return null;
    }

    public Signature<?> sender() {
        return sender;
    }

    public void suspend() {
        queue.suspend();
    }

    public void resume() {
        queue.resume();
    }

    public boolean isSuspended() {
        return queue.isSuspended();
    }
}
