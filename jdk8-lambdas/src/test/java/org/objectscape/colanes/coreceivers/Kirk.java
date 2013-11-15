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
import org.objectscape.colanes.registry.Registry;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class Kirk implements CoReceiver {

    private static final String Spock = "Spock";
    private static final String McCoy = "McCoy";

    private static final int NthFibonacci = 20;
    private static final int NthFactorial = 15;

    private CoLane lane = new CoLane();

    private CountDownLatch latch = new CountDownLatch(4);

    private int resultFibonacciSpock = -1;
    private int resultFibonacciMcCoy = -1;

    private long resultFactorialSpock = -1;
    private long resultFactorialMcCoy = -1;

    public Kirk(CountDownLatch latch) {
        super();
        this.latch = latch;
    }

    private void receivedResultFibonacciSpock(int resultFibonacciSpock)
    {
        this.resultFibonacciSpock = resultFibonacciSpock;

        McCoy mcCoy = Registry.getDefault().getSingletonActiveObject(McCoy.class);
        mcCoy.calculateFibonacci(NthFibonacci).runAsyncWhenDone((Integer result) -> { receivedResultFibonacciMcCoy(result); });

        latch.countDown();
    }

    private void receivedResultFibonacciMcCoy(int resultFibonacciMcCoy)
    {
       this.resultFibonacciMcCoy = resultFibonacciMcCoy;

        if(this.resultFibonacciSpock == -1 || this.resultFibonacciMcCoy == -1)
            return;

        System.out.println(getName() + ": result for fibonacci(" + NthFibonacci + ") from " + Spock + ": " + this.resultFibonacciSpock);
        System.out.println(getName() + ": result for fibonacci(" + NthFibonacci + ") from " + McCoy + ": " + this.resultFibonacciMcCoy);

        if(this.resultFibonacciSpock == this.resultFibonacciMcCoy)
            System.out.println(getName() + ": Spock and McCoy agree on the same result for fibonacci(" + NthFactorial + ")! Unbelievable, then the result must be correct ...");
        else
            System.out.println(getName() + ": Oh no, Spock and McCoy got different results for fibonacci(" + NthFibonacci + ")! I knew they would disagree ... ");

        latch.countDown();
    }

    public void fibonacciExercise() {
        lane.run(() -> {
            Spock spock = Registry.getDefault().getSingletonActiveObject(Spock.class);
            spock.calculateFibonacci(NthFibonacci).runAsyncWhenDone((Integer result) -> {
                receivedResultFibonacciSpock(result);
            });
        });
    }

    public void factorialExercise() {
        lane.run(() -> {
            McCoy mcCoy = Registry.getDefault().getSingletonActiveObject(McCoy.class);
            mcCoy.calculateFactorial(NthFactorial).runAsyncWhenDone((Long result) -> {
                receivedResultFactorialMcCoy(result);
            });
        });
    }

    private void receivedResultFactorialMcCoy(long resultFactorialMcCoy)
    {
        this.resultFactorialMcCoy = resultFactorialMcCoy;

        System.out.println(getName() + ": result of factorial(" + NthFactorial + ") from " + McCoy + " is " + resultFactorialMcCoy);

        lane.run(() -> {
            Spock spock = Registry.getDefault().getSingletonActiveObject(Spock.class);
            spock.calculateFactorial(NthFactorial).runAsyncWhenDone((Long result) -> {
                receivedResultFactorialSpock(result);
            });
        });

        latch.countDown();
    }

    private void receivedResultFactorialSpock(long resultFactorialSpock)
    {
        this.resultFactorialSpock = resultFactorialSpock;

        System.out.println(getName() + ": result of factorial(" + NthFactorial + ") from " + Spock + " is " + resultFactorialSpock);

        if(this.resultFactorialMcCoy == this.resultFactorialSpock)
            System.out.println(getName() + ": Spock and McCoy agree on the same result for factorial(" + NthFactorial + ")! Unbelievable, then the result must be correct ...");
        else
            System.out.println(getName() + ": Oh no, Spock and McCoy got different results for factorial(" + NthFibonacci + ")! I knew they would disagree ... ");

        latch.countDown();
    }
}
