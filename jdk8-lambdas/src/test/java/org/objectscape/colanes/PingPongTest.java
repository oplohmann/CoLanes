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
import org.objectscape.colanes.coreceivers.Ping;
import org.objectscape.colanes.coreceivers.Pong;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class PingPongTest extends AbstractTest {

    @Test
    public void pingPong() throws InterruptedException
    {
        Registry registry = Registry.getDefault();
        CountDownLatch latch = new CountDownLatch(1);

        Ping ping = new Ping(latch);
        registry.register(ping);

        Pong pong = new Pong();
        registry.register(pong);

        ping.sendPing();
        latch.await();
    }

    @Test
    public void pingWhenDonePong() throws InterruptedException
    {
        CountDownLatch latch = new CountDownLatch(1);

        Ping ping = new Ping(latch);
        Pong pong = new Pong();

        ping.sendPingWhenDonePong(pong);
        latch.await();
    }

}
