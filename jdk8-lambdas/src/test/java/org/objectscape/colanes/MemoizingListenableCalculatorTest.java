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
import org.objectscape.colanes.coreceivers.stm.MemoizingListenableCalculatorSTM;
import org.objectscape.colanes.coreceivers.concurrent.MemoizingListenableCalculatorConcurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class MemoizingListenableCalculatorTest
{

    @Test
    public void calculateFactorialListenableConcurrentMap() throws InterruptedException, ExecutionException {
        MemoizingListenableCalculatorConcurrent[] calculators = new MemoizingListenableCalculatorConcurrent[4];
        for (int i = 0; i < calculators.length; i++)
            calculators[i] = new MemoizingListenableCalculatorConcurrent();

        int n = 10;
        int additive = 20;
        int maxIterations = 50;

        List<CompletableFuture<Long>> calculatedResults = new ArrayList<>();

        for (int i = 0; i < maxIterations; i++)
        {
            for (MemoizingListenableCalculatorConcurrent calculator : calculators) {
                CompletableFuture<Long> result = new CompletableFuture<>();
                calculatedResults.add(result);
                calculator.factorialMemoized(n, result);
            }
            n += additive;
        }

        for(CompletableFuture<Long> calculatedResult : calculatedResults)
            calculatedResult.get();

        System.out.println("done.");
    }

    @Test
    public void calculateFactorialListenableSTM() throws InterruptedException, ExecutionException {

        MemoizingListenableCalculatorSTM[] calculators = new MemoizingListenableCalculatorSTM[4];
        for (int i = 0; i < calculators.length; i++)
            calculators[i] = new MemoizingListenableCalculatorSTM();

        int n = 10;
        int additive = 20;
        int maxIterations = 50;

        List<CompletableFuture<Long>> calculatedResults = new ArrayList<>();

        for (int i = 0; i < maxIterations; i++)
        {
            for (int j = 0; j < calculators.length; j++)
            {
                CompletableFuture<Long> result = new CompletableFuture<>();
                calculatedResults.add(result);
                calculators[j].factorialMemoized(n, result);
            }
            n += additive;
        }

        for(CompletableFuture<Long> calculatedResult : calculatedResults)
            calculatedResult.get();

        System.out.println("done.");
    }
}
