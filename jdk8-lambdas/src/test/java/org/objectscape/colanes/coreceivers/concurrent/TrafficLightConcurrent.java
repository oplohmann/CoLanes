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

package org.objectscape.colanes.coreceivers.concurrent;

import org.objectscape.colanes.CoReceiver;
import org.objectscape.colanes.CoLane;
import org.objectscape.colanes.coreceivers.stm.TrafficLightSTM;
import org.objectscape.colanes.registry.Registry;
import org.objectscape.colanes.util.concurrent.map.ListenableConcurrentMap;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class TrafficLightConcurrent implements CoReceiver
{

    private CoLane lane = new CoLane();
    private ListenableConcurrentMap<TrafficLightSTM.Color, Boolean> lights = null;
    private int maxLightCycles = 5;
    private int lightCycles = 0;
    private AtomicBoolean turnedOn = new AtomicBoolean(false);

    public TrafficLightConcurrent() {
    }

    public TrafficLightConcurrent(int maxLightCycles) {
        super();
        this.maxLightCycles = maxLightCycles;
    }

    private void startInternal()
    {
        System.out.println(getName() + ": traffic light starting ...");
        lights = Registry.getDefault().getListenableConcurrentMapNamed(TrafficLightSTM.TagName);
        lights.clear();

        turnedOn.compareAndSet(false, true);

        lights.putSingleValue(TrafficLightSTM.Color.RED, true);
        lights.putSingleValue(TrafficLightSTM.Color.GREEN, false);

        lane.run(() -> {
            changeState();
        });
    }

    private void changeState()
    {
        if(lightCycles >= maxLightCycles)
        {
            // shutting down traffic light
            System.out.println(getName() + ": max light change cycles reached: turning off all lights to shut down ...");
            turnedOn.compareAndSet(true, false);
            lights.remove(TrafficLightSTM.Color.RED);
            lights.remove(TrafficLightSTM.Color.GREEN);
            return;
        }

        lightCycles++;
        sleep(100);

        if(lights.getSingleValue(TrafficLightSTM.Color.RED))
        {
            System.out.println(getName() + ": changing from red to green ...");
            lights.putSingleValue(TrafficLightSTM.Color.RED, false);
            lights.putSingleValue(TrafficLightSTM.Color.GREEN, true);
            lane.run(() -> {
                changeState();
            });
            return;
        }

        if(lights.getSingleValue(TrafficLightSTM.Color.GREEN))
        {
            System.out.println(getName() + ": changing from green to red ...");
            lights.putSingleValue(TrafficLightSTM.Color.GREEN, false);
            lights.putSingleValue(TrafficLightSTM.Color.RED, true);
            lane.run(() -> {
                changeState();
            });
            return;
        }
    }

    public boolean isTurnedOn() {
        return turnedOn.get();
    }

    private void sleep(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) { }
    }

    public void start() {
        lane.run(() -> {
            startInternal();
        });
    }

}
