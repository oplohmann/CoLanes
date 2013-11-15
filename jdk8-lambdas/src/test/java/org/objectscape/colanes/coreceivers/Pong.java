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
import org.objectscape.colanes.HandbackFutureCompletion;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class Pong implements CoReceiver
{
    private CoLane lane = new CoLane();

    public Handback accept(String str) {
        return lane.getHandback().runnable(() -> { acceptInternal(str); });
    }

    private void acceptInternal(String msg)
    {
        /* TODO - get this to work:
            Ping sender = lane.currentSender().getCoReceiver();
            sender.accept(pong).sender(this).runAsync();
        */

        Ping ping = Registry.getDefault().getActiveObjectOrNull(lane.sender(Ping.class));
        if(ping != null) {
            String pong = "pong";
            System.out.println(this + " received \"" + msg + "\" from " + ping);
            System.out.println(this + " sending \"" + pong + "\" to " + ping);
            ping.accept(pong).sender(this).runAsync();
        }
    }

    @Override
    public String getName() {
        return "pong";
    }

    private String getAPong() {
        return "pong";
    }

    public HandbackFutureCompletion<String> createPong() {
        return lane.<String>getHandbackFutureCompletion().supplier(() -> { return getAPong(); });
    }

    public String toString() {
        return getNameNotNull() + "@" + Integer.toHexString(hashCode());
    }
}
