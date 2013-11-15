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
import org.objectscape.colanes.HandbackFutureCompletion;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class McCoy extends Calculator implements CoReceiver {

    private CoLane lane = new CoLane();

    protected CoLane getLane() {
        return lane;
    }

    protected int myFibonacci(int n) {
        return fibonacci(n) - 1; // -1 to make McCoy return an incorrect result
    }

    public HandbackFutureCompletion<Integer> calculateFibonacci(int n) {
        return lane.<Integer>getHandbackFutureCompletion().supplier(() -> { return myFibonacci(n); });
    }


}
