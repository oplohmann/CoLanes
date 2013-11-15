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

import org.junit.Test;
import org.objectscape.colanes.registry.Registry;
import org.objectscape.colanes.coreceivers.Kirk;
import org.objectscape.colanes.coreceivers.McCoy;
import org.objectscape.colanes.coreceivers.Spock;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class StartrekTest extends AbstractTest {

    @Test
    public void conversation() throws InterruptedException
    {
        Registry registry = Registry.getDefault();
        CountDownLatch latch = new CountDownLatch(4); // 4 numbers to calculate

        Kirk kirk = registry.registerSingleton(new Kirk(latch));

        registry.registerSingleton(new Spock());
        registry.registerSingleton(new McCoy());

        kirk.fibonacciExercise();
        kirk.factorialExercise();

        latch.await();
    }
}
