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

import junit.framework.Assert;
import org.objectscape.colanes.CoReceiver;
import org.objectscape.colanes.CoLane;
import org.objectscape.colanes.coreceivers.stm.TrafficLightSTM;
import org.objectscape.colanes.registry.Registry;
import org.objectscape.colanes.util.concurrent.map.PutEvent;
import org.objectscape.colanes.util.concurrent.map.RemoveEvent;
import org.objectscape.colanes.util.concurrent.map.ListenableConcurrentMap;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class CarConcurrent implements CoReceiver {

    private CoLane lane = new CoLane();
    private boolean redOn = false;
    private boolean greenOn = false;
    private AtomicBoolean driving = new AtomicBoolean(false);

    public void start() {
        lane.run(() -> {
            startInternal();
        });
    }

    private void startInternal()
    {
        ListenableConcurrentMap<TrafficLightSTM.Color, Boolean> lights = Registry.getDefault().getListenableConcurrentMapNamed(TrafficLightSTM.TagName);
        Assert.assertNotNull(lights);

        lights.addSynchronousListener(TrafficLightSTM.Color.RED, (PutEvent<Boolean> event) -> {
            lane.run(() -> {
                trafficLightChanged(event);
            });
        }, true);

        lights.addSynchronousListener(TrafficLightSTM.Color.GREEN, (PutEvent<Boolean> event) -> {
            lane.run(() -> {
                trafficLightChanged(event);
            });
        }, true);

        lights.addSynchronousListener(TrafficLightSTM.Color.RED, (RemoveEvent<Boolean> event) -> {
            lane.run(() -> {
                trafficLightTurnedOff(event);
            });
        });

        lights.addSynchronousListener(TrafficLightSTM.Color.GREEN, (RemoveEvent<Boolean> event) -> {
            lane.run(() -> {
                trafficLightTurnedOff(event);
            });
        });
    }

    private void trafficLightTurnedOff(RemoveEvent<Boolean> event)
    {
        System.out.println(getName() + ": " + event.getKey() + " traffic light has been turned off");
        if(event.getKey().equals(TrafficLightSTM.Color.GREEN)) {
            greenOn = false;
        }
        if(event.getKey().equals(TrafficLightSTM.Color.RED)) {
            redOn = false;
        }

        if(!greenOn && !redOn) {
            // traffic light has been turned of, e.g. traffic light simulation has been terminated.
            StringBuffer msg = new StringBuffer();
            msg.append(getName() + ": traffic light has been turned off.");
            if(driving.get()) {
                driving.compareAndSet(true, false);
                msg.append(" Stop driving.");
            }
            msg.append(" Releasing test case latch to terminate test case ...");
            System.out.println(msg);
            Map<String, CountDownLatch> latchMap = Registry.getDefault().getConcurrentMapNamed("Latches");
            latchMap.get(TrafficLightSTM.TagName).countDown();
        }
    }

    private void trafficLightChanged(PutEvent<Boolean> event)
    {
        if(event.getKey().equals(TrafficLightSTM.Color.GREEN)) {
            greenOn = event.getValue();
            if(!greenOn)
                System.out.println(getName() + ": green light turned off");
            else
                System.out.println(getName() + ": green light turned on");
            reactToTrafficLightChange();
            return;
        }
        if(event.getKey().equals(TrafficLightSTM.Color.RED)) {
            redOn = event.getValue();
            if(!redOn)
                System.out.println(getName() + ": red light turned off");
            else
                System.out.println(getName() + ": red light turned on");
            reactToTrafficLightChange();
            return;
        }
    }

    private void reactToTrafficLightChange()
    {
        if(greenOn && !redOn) {
            driving.compareAndSet(false, true);
            System.out.println(getName() + ": traffic light has turned green -> start driving");
            return;
        }
        if(!greenOn && redOn) {
            driving.compareAndSet(true, false);
            System.out.println(getName() + ": traffic light has turned red -> stop driving");
            return;
        }
    }

    public boolean isDriving() {
        return driving.get();
    }
}
