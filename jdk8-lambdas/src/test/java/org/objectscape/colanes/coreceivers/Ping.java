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

package org.objectscape.colanes.coreceivers;

import org.objectscape.colanes.CoLane;
import org.objectscape.colanes.CoReceiver;
import org.objectscape.colanes.Handback;
import org.objectscape.colanes.registry.Registry;

import java.util.concurrent.CountDownLatch;


/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class Ping implements CoReceiver {

    private CoLane lane = new CoLane();
    private CountDownLatch latch = null;

    public Ping() {
        super();
    }

    public Ping(CountDownLatch latch) {
        super();
        this.latch = latch;
    }

    private void sendPingInternal()
    {
        Pong pong = Registry.getDefault().getActiveObjectOrNull("pong", Pong.class);
        if(pong != null) {
            String ping = "ping";
            System.out.println(this + " sending \"" + ping + "\" to " + pong);
            pong.accept(ping).sender(this).runAsync();
        }
    }

    public void sendPing()
    {
        lane.run(() -> {
            sendPingInternal();
        });
    }

    @Override
    public String getName() {
        return "ping";
    }

    private void acceptInternal(String msg)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(this + " received \"" + msg + "\"");

        Pong pong = Registry.getDefault().getActiveObjectOrNull(lane.sender(Pong.class));
        if(pong != null)
            builder.append(" from " + pong);

        System.out.println(builder.toString());
        if(latch != null)
            latch.countDown();
    }

    public Handback accept(String str) {
        return lane.getHandback().runnable(() -> { acceptInternal(str); });
    }

    public void printPing(String msg) {
        lane.run(() -> {
            printPingInternal(msg);
        });
    }

    private void printPingInternal(String msg) {
        System.out.println("ping");
        System.out.println(msg);
        if(latch != null)
            latch.countDown();
    }

    public void sendPingWhenDonePong(Pong pong) {
        pong.createPong().runAsyncWhenDone((String aPong) -> { printPing(aPong); });
    }

    public String toString() {
        return getNameNotNull() + "@" + Integer.toHexString(hashCode());
    }
}
