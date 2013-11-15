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

import org.objectscape.colanes.HandbackFutureCompletion;
import org.objectscape.colanes.coreceivers.AbstractMemoizingCalculator;
import org.objectscape.colanes.registry.Registry;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author <a href="http://www.objectscape.org/">Oliver Plohmann</a>
 *
 */
public class MemoizingCalculator extends AbstractMemoizingCalculator
{

    private ConcurrentMap<Integer, Long> concurrentMap = null;
    private Map<Integer, Long> stmMap = null;

    public HandbackFutureCompletion<Long> factorialMemoizedConcurrentMap(Integer n)
{
    return
            lane.<Long>getHandbackFutureCompletion().
                    supplier(() -> { return factorialMemoizedConcurrentMapInternal(n); });
}

    public HandbackFutureCompletion<Long> factorialMemoizedStmMap(Integer n)
    {
        return
                lane.<Long>getHandbackFutureCompletion().
                        supplier(() -> { return factorialMemoizedStmMapInternal(n); });
    }

    private Long factorialMemoizedConcurrentMapInternal(Integer n)
    {
        if(n <= 1)
            return 1L;

        ConcurrentMap<Integer, Long> map = Registry.getDefault().getConcurrentMapNamed(FactorialCacheName);
        Long factorialObject = map.get(n);
        if(factorialObject != null) {
            System.out.println(getName() + ": found factorial(" + n + ") in cache");
            return factorialObject;
        }

        long factorial = factorial(n);

        map.putIfAbsent(n, Long.valueOf(factorial(n)));

        return factorial;
    }

    private Long factorialMemoizedStmMapInternal(Integer n)
    {
        if(n <= 1)
            return 1L;

        final Map<Integer, Long> map = Registry.getDefault().getAtomicMapNamed(FactorialCacheName);

        return atomic(()-> {
            Long factorial = map.get(n);
            if(factorial != null)
                return factorial;
            return map.put(n, Long.valueOf(factorial(n)));
        });
    }

}
