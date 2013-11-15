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

package org.objectscape.colanes.coreceivers.stm;

import org.objectscape.colanes.CoLane;
import org.objectscape.colanes.CoReceiver;
import org.objectscape.colanes.registry.Registry;
import org.objectscape.colanes.util.atomic.map.ListenableAtomicMap;

import java.util.concurrent.atomic.AtomicBoolean;

import static scala.concurrent.stm.japi.STM.atomic;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class TrafficLightSTM implements CoReceiver
{
    public static final String TagName = "TrafficLightSTM";

    public enum Color { RED, GREEN }

    private CoLane lane = new CoLane();
    private ListenableAtomicMap<Color, Boolean> lights = null;
    private int maxLightCycles = 5;
    private int lightCycles = 0;
    private AtomicBoolean turnedOn = new AtomicBoolean(false);

    public TrafficLightSTM() {
    }

    public TrafficLightSTM(int maxLightCycles) {
        super();
        this.maxLightCycles = maxLightCycles;
    }

    private void startInternal()
    {
        System.out.println(getName() + ": traffic light starting ...");
        lights = Registry.getDefault().getListenableAtomicMapNamed(TagName);
        lights.clear();

        turnedOn.compareAndSet(false, true);

        atomic(()-> {
            lights.put(Color.RED, true);
            lights.put(Color.GREEN, false);
        });

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
            atomic(() -> {
                lights.remove(Color.RED);
                lights.remove(Color.GREEN);
            });
            return;
        }

        lightCycles++;
        sleep(100);

        if(lights.get(Color.RED))
        {
            System.out.println(getName() + ": changing from red to green ...");
            atomic(()-> {
                lights.put(Color.RED, false);
                lights.put(Color.GREEN, true);
            });
            lane.run(() -> {
                changeState();
            });
            return;
        }

        if(lights.get(Color.GREEN))
        {
            System.out.println(getName() + ": changing from green to red ...");
            atomic(() -> {
                lights.put(Color.GREEN, false);
                lights.put(Color.RED, true);
            });
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
