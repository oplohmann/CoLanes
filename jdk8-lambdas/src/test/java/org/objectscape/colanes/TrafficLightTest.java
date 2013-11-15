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
import org.objectscape.colanes.coreceivers.stm.CarSTM;
import org.objectscape.colanes.coreceivers.stm.TrafficLightSTM;
import org.objectscape.colanes.registry.Registry;
import org.objectscape.colanes.coreceivers.concurrent.CarConcurrent;
import org.objectscape.colanes.coreceivers.concurrent.TrafficLightConcurrent;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static scala.concurrent.stm.japi.STM.atomic;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class TrafficLightTest
{

    @Test
    public void startConcurrent() throws InterruptedException
    {
        CountDownLatch simulationTerminated = new CountDownLatch(1);
        Map<String, CountDownLatch> latchMap = Registry.getDefault().getConcurrentMapNamed("Latches");
        latchMap.put(TrafficLightSTM.TagName, simulationTerminated);

        CarConcurrent car = new CarConcurrent();
        car.start();

        TrafficLightConcurrent trafficLiqht = new TrafficLightConcurrent();
        trafficLiqht.start();

        simulationTerminated.await();

        Assert.assertFalse(car.isDriving());
        Assert.assertFalse(trafficLiqht.isTurnedOn());
        System.out.println("traffic light simulation terminated");
    }

    @Test
    public void startSTM() throws InterruptedException
    {
        CountDownLatch simulationTerminated = new CountDownLatch(1);

        atomic(() -> {
            Map<String, CountDownLatch> latchMap = Registry.getDefault().getAtomicMapNamed("Latches");
            latchMap.put(TrafficLightSTM.TagName, simulationTerminated);
        });

        CarSTM car = new CarSTM();
        car.start();

        TrafficLightSTM trafficLight = new TrafficLightSTM();
        trafficLight.start();

        simulationTerminated.await();

        Assert.assertFalse(car.isDriving());
        Assert.assertFalse(trafficLight.isTurnedOn());
        System.out.println("traffic light simulation terminated");
    }
}
