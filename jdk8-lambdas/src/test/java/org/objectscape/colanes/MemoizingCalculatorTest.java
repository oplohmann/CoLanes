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
import org.objectscape.colanes.coreceivers.concurrent.MemoizingCalculator;

import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;

/**
 * Sample of several MemoizingCalculator lanes calculating Fibonacci numbers cooperatively. Each MemoizingCalculator
 * calculates a bunch of Fibonacci numbers and adds them to a shared <code>ConcurrentMap</code> or ScalaSTM <code>STMMap</code>
 * that serves as a cache for already calculated Fibonacci numbers.
 *
 * @see MemoizingListenableCalculatorTest
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class MemoizingCalculatorTest
{

    /**
     * Cooperatively calculate a series of Fibonacci numbers that are put to a shared <code>ConcurrentMap</code>
     * that serves as a cache for already calculated Fibonacci numbers.
     *
     * @throws InterruptedException
     */
    @Test
    public void calculateFactorialConcurrentMap() throws InterruptedException
    {
        BiFunction<MemoizingCalculator, Integer, HandbackFutureCompletion<Long>> function = (MemoizingCalculator calculator, Integer n) -> calculator.factorialMemoizedConcurrentMap(n);
        calculateFactorial(function);
    }

    /**
     * Cooperatively calculate a series of Fibonacci numbers that are put to a shared ScalaSTM <code>STMMap</code>
     * that serves as a cache for already calculated Fibonacci numbers.
     *
     * @throws InterruptedException
     */
    @Test
    public void calculateFactorialStmMap() throws InterruptedException
    {
        BiFunction<MemoizingCalculator, Integer, HandbackFutureCompletion<Long>> function = (MemoizingCalculator calculator, Integer n) -> { return calculator.factorialMemoizedStmMap(n); };
        calculateFactorial(function);
    }

    private void calculateFactorial(BiFunction<MemoizingCalculator, Integer, HandbackFutureCompletion<Long>> function) throws InterruptedException
    {
        // instantiate a bunch of MemoizingCalculators
        MemoizingCalculator[] calculators = new MemoizingCalculator[4];
        for (int i = 0; i < calculators.length; i++)
            calculators[i] = new MemoizingCalculator();

        int n = 10;
        int additive = 50;
        int maxIterations = 30;

        CountDownLatch waitTillDone = new CountDownLatch(calculators.length);

        // Make the MemoizingCalculators calculate several Fibonacci numbers.
        for (int i = 0; i < maxIterations; i++)
        {
            for (MemoizingCalculator calculator : calculators) {
                HandbackFutureCompletion<Long> handback = function.apply(calculator, n);
                handback.callback(() -> {
                    waitTillDone.countDown();
                });
                handback.runAsync();
            }
            n += additive;
        }

        // wait till all MemoizingCalculator are done calculating Fibonacci numbers.
        waitTillDone.await();

        System.out.println("done.");
    }

}
