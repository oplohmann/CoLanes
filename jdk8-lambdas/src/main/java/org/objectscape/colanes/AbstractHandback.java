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
import org.objectscape.colanes.util.Signature;

/**
 * Abstract class for all Handback classes which defines common attributes and functionality. As a
 * <tt>Handback</tt> is exposed to all threads in the JVM it has to be made thread-safe. This is
 * achieved through the use of plain synchronized blocks. There is no potential for deadlocks in this case
 * as all operations that require synchronization are restricted to internal Handback methods that
 * are atomic similar to <code>java.util.Vector</code> where no atomic method call depends on another
 * atomic (this could lead to a cycle if not the case).
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public abstract class AbstractHandback
{

    /**
     * The <code>CoLane</code> of the lane, the Handback refers to.
     */
    protected CoLane lane;

    /**
     * Callback lambda that is invoked <tt>synchronously</tt> after the Handback has finished execution
     */
    protected Runnable callback;

    /**
     * Callback lambda that is invoked <tt>asynchronously</tt> after the Handback has finished execution
     */
    protected Runnable whenDone;

    /**
     * Signature of the lane that invoked the Handback. Can be used by the lane
     * associated with the Handback to reply to the sender.
     */
    protected Signature sender;

    /**
     * Used to mark a Handback as enqueued, e.g. the <code>Handback.runAsync()</code> method has been
     * called. Depending on the kind of Handback (that is subclass of <code>AbstractHandback)</code>
     * this attribute is used to determine whether invoking specific operations are still valid or no longer
     * allowed (such as changing the callback() method).
     */
    protected boolean enqueued = false;

    /**
     * Specify whether Handback attributes sender, callback, whenDone, and additional ones defined in subclasses
     * may be set more than once before the Handback has been enqueued.
     */
    protected boolean allowOverwrite = true;

    /**
     * Lock object used by synchronized blocks to synchronize access to attributes that effectively will be
     * shared data in case the Handback is exposed to several threads.
     */
    protected Object lock = new Object();

    /**
     * Creates a new <code>AbstractHandback</code> object
     */
    public AbstractHandback() {
        super();
    }

    /**
     * Creates a new <code>AbstractHandback</code> object
     */
    public AbstractHandback(boolean allowOverwrite) {
        super();
        this.allowOverwrite = allowOverwrite;
    }

    /**
     * Creates a new <code>AbstractHandback</code> object with an associated <code>CoLane</code>
     */
    public AbstractHandback(CoLane lane)
    {
        super();
        this.lane = lane;
    }

    /**
     * Creates a new <code>AbstractHandback</code> object with an associated <code>CoLane</code>
     */
    public AbstractHandback(CoLane lane, boolean allowOverwrite)
    {
        super();
        this.lane = lane;
        this.allowOverwrite = allowOverwrite;
    }

    /**
     * Set a callback object. Throws an IllegalStateException if the lambda of CoLane is already enqueued.
     *
     * @param callback the callback lambda
     */
    protected void setCallback(Runnable callback) {
        synchronized (lock) {
            checkNotAlreadyEnqueued();
            checkAllowOverwrite(this.callback, "callback");
            this.callback = callback;
        }
    }

    protected final void checkAllowOverwrite(Object attribute, String attributeName) {
        if(!allowOverwrite && attribute != null)
            throw new IllegalStateException(attributeName + " must not be changed after being set once to a value other than null");
    }

    /**
     * Set a whenDone object. Throws an IllegalStateException if the lambda of CoLane is already enqueued.
     *
     * @param whenDone
     */
    protected void setWhenDone(Runnable whenDone) {
        synchronized (lock) {
            checkNotAlreadyEnqueued();
            checkAllowOverwrite(this.whenDone, "whenDone");
            this.whenDone = whenDone;
        }
    }

    /**
     * Return the <code>Signature</code> of the lane that invoked the Handback or null if
     * none was provided
     *
     * @return the sender's lane Signature or null if none
     */
    public Signature sender() {
        synchronized (lock) {
            return sender;
        }
    }

    /**
     * Return the whenDone object or null if none was setAndGet.
     *
     * @return the whenDone object or null if none was setAndGet
     */
    public Runnable whenDone() {
        synchronized (lock) {
            return whenDone;
        }
    }

    /**
     * Add the lambda defined in the <code>CoLane</code> to the HawtDispatch lane which is
     * known to the <code>CoLane</code> referenced by the <tt>lane</tt> attribute. This method
     * is a template methad that is supposed to be used by all subclasses of <code>AbstractHandback</code>
     */
    protected void enqueue()
    {
        synchronized (lock)
        {
            checkValidBeforeEnqueue();
            checkNotAlreadyEnqueued();
            enqueued = true;

            lane.run(() ->
                    {
                        lane.sender(sender);
                        invokeFunction();
                        invokeCallback();
                        invokeWhenDone();
                        lane.sender((Signature) null);
                    });
        }
    }

    /**
     * Called from subclasses to invoke the callback lambda.
     */
    @CallerMustSynchronize
    protected void invokeCallback() {
        if(callback != null)
            callback.run();
    }

    /**
     * Called from subclasses to invoke the whenDone lambda.
     */
    @CallerMustSynchronize
    protected void invokeWhenDone() {
        if(whenDone != null)
            lane.run(whenDone);
    }

    /**
     * Check whether the <code>Handback</code> is valid and can be enqueued.
     */
    @CallerMustSynchronize
    protected void checkValidBeforeEnqueue() {
        if(lane == null)
            throw new NullPointerException("handback not attached to a lane");
    }

    /**
     * Require all subclasses to implement an invokeFunction() method as needed for their purposes.
     */
    @CallerMustSynchronize
    protected abstract void invokeFunction();

    /**
     * Answer whether the lambda has been enqueued or is still pending for execution by the
     * <tt>HawtDispacth</tt> lane.
     *
     * @return
     */
    public boolean isEnqueued() {
        synchronized (lock) {
            return enqueued;
        }
    }

    /**
     * Check that the lambda of the Handback has not already been enqueued.
     */
    @CallerMustSynchronize
    protected void checkNotAlreadyEnqueued() {
        if(enqueued)
            throw new IllegalStateException("lambda already enqueued");
    }

    /**
     * Set the coReceiver that invoked the Handback
     *
     * @param coReceiver
     */
    protected void setSender(CoReceiver coReceiver) {
        synchronized (lock) {
            checkNotAlreadyEnqueued();
            checkAllowOverwrite(sender, "sender");
            sender = coReceiver.getSignature();
        }
    }

    public void setNamedSender(CoReceiver coReceiver) {
        synchronized (lock) {
            checkNotAlreadyEnqueued();
            checkAllowOverwrite(sender, "sender");
            sender = coReceiver.getNamedSignature();
        }
    }

}
