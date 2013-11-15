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

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class Handback extends AbstractHandback {

    protected Runnable runnable;

    public Handback() {
        super();
    }

    public Handback(boolean allowOverwrite) {
        super(allowOverwrite);
    }

    public Handback(CoLane queue) {
        super(queue);
    }

    public Handback(CoLane queue, boolean allowOverwrite) {
        super(queue, allowOverwrite);
    }

    @Override
    @CallerMustSynchronize
    protected void checkValidBeforeEnqueue() {
        super.checkValidBeforeEnqueue();
        if(runnable == null)
            throw new NullPointerException("runnable null");
    }

    public Runnable runnable() {
        synchronized (lock) {
            return runnable;
        }
    }

    public Handback callback(Runnable callback) {
        setCallback(callback);
        return this;
    }

    public Handback runnable(Runnable runnable) {
        synchronized (lock) {
            checkNotAlreadyEnqueued();
            checkAllowOverwrite(runnable, "runnable");
            this.runnable = runnable;
            return this;
        }
    }

    @Override
    @CallerMustSynchronize
    protected void invokeFunction() {
        runnable.run();
    }

    public Handback whenDone(Runnable whenDone) {
        setWhenDone(whenDone);
        return this;
    }

    public Handback sender(CoReceiver coReceiver) {
        setSender(coReceiver);
        return this;
    }

    public Handback namedSender(CoReceiver coReceiver) {
        setNamedSender(coReceiver);
        return this;
    }

    public Handback runAsync() {
        enqueue();
        return this;
    }
}
