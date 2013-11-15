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

import org.objectscape.colanes.HandbackFutureCompletion;
import org.objectscape.colanes.CoLane;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class HelloWorldWithFutureHandback {

    private CoLane lane = new CoLane();

    private String sayHelloWorldInternal() {
        System.out.println("sayHelloWorldInternal invoked");
        return "hello world!";
    }

    private String sayHelloInternal() {
        System.out.println("sayHelloInternal invoked");
        return "hello ";
    }

    public HandbackFutureCompletion<String> sayHelloWorld() {
        return
                lane.<String>getHandbackFutureCompletion().
                        supplier(() -> { return sayHelloWorldInternal(); });
    }

    public HandbackFutureCompletion<String> sayHello2() {
        return
                lane.<String>getHandbackFutureCompletion().
                        supplier(() -> { return sayHelloWorldInternal(); }).
                        supplier(() -> { return sayHelloWorldInternal(); });
    }

    public HandbackFutureCompletion<String> sayHello() {
        return
                lane.<String>getHandbackFutureCompletion().
                        supplier(() -> { return sayHelloInternal(); });
    }

}
