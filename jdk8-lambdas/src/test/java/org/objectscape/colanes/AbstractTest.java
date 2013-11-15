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

import org.fusesource.hawtdispatch.internal.DispatcherConfig;
import org.junit.After;
import org.objectscape.colanes.registry.Registry;

/**
 * Abstract class with common functionality useful for all test classes.
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public abstract class AbstractTest
{

    @After
    public void tearDown() throws InterruptedException
    {
        // wait till queues have finished execution
        // TODO - work around that should be cleanup by using callback or similar
        sleep(1000);

        // clear the lane registry
        Registry.getDefault().clear();

        // shut down HawtDispatch
        DispatcherConfig.getDefaultDispatcher().shutdown();
    }

    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
