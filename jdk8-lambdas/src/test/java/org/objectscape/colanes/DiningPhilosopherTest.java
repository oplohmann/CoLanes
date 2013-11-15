package org.objectscape.colanes;

import org.junit.Ignore;
import org.junit.Test;
import org.objectscape.colanes.coreceivers.DiningPhilosopher;
import org.objectscape.colanes.coreceivers.concurrent.ListenableDiningPhilosopher;
import org.objectscape.colanes.registry.Registry;
import org.objectscape.colanes.util.ImmutableList;
import org.objectscape.colanes.util.concurrent.map.ListenableConcurrentMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 14.05.13
 * Time: 18:34
 * To change this template use File | Settings | File Templates.
 */
public class DiningPhilosopherTest extends org.objectscape.colanes.AbstractTest {

    @Test
    @Ignore // still creates deadlocks
    public void haveDinnerSupplyForksLast() throws Exception
    {
        // TODO - not yet deadlock-free !!!
        int eatTimes = 3; // each philosopher will eat eatTimes
        int numPhilosophers = 5; // some odd number
        CountDownLatch waitTillAllDoneDining = new CountDownLatch(numPhilosophers);

        Registry registry = Registry.getDefault();
        ListenableConcurrentMap<String, String> availableForks = registry.getListenableConcurrentMapNamed(ListenableDiningPhilosopher.AvailableForksMapName);
        ConcurrentMap<Integer, ListenableDiningPhilosopher> availablePhilosophers = registry.getConcurrentMapNamed(ListenableDiningPhilosopher.AvailablePhilosphersMapName);

        Set<String> allForks = new HashSet<>();

        for(int i = 1; i <= numPhilosophers; i++)
        {
            ListenableDiningPhilosopher philosopher = new ListenableDiningPhilosopher(i, numPhilosophers, eatTimes, waitTillAllDoneDining);
            allForks.add(philosopher.getRightForkName());
            availablePhilosophers.put(philosopher.getId(), philosopher);
            philosopher.startHandback(registry).runAsync().future().get();
        }

        allForks.forEach((String fork) -> { availableForks.putSingleValue(fork, fork); });

        waitTillAllDoneDining.await();
        System.out.println("all philosophers done each dining " + eatTimes + " times");
    }

    @Test
    @Ignore // still creates deadlocks
    public void haveDinnerStartPhilosohersLast() throws Exception
    {
        // TODO - not yet deadlock-free !!!
        int eatTimes = 3; // each philosopher will eat eatTimes
        int numPhilosophers = 5; // some odd number
        CountDownLatch waitTillAllDoneDining = new CountDownLatch(numPhilosophers);

        Registry registry = Registry.getDefault();
        ListenableConcurrentMap<String, String> availableForks = registry.getListenableConcurrentMapNamed(ListenableDiningPhilosopher.AvailableForksMapName);
        ConcurrentMap<Integer, ListenableDiningPhilosopher> availablePhilosophers = registry.getConcurrentMapNamed(ListenableDiningPhilosopher.AvailablePhilosphersMapName);

        for(int i = 1; i <= numPhilosophers; i++)
        {
            ListenableDiningPhilosopher philosopher = new ListenableDiningPhilosopher(i, numPhilosophers, eatTimes, waitTillAllDoneDining);
            String fork = philosopher.getRightForkName();
            availableForks.putSingleValue(fork, fork);
            availablePhilosophers.put(philosopher.getId(), philosopher);
        }

        availablePhilosophers.values().forEach((ListenableDiningPhilosopher philosopher)-> { philosopher.start(registry); });

        waitTillAllDoneDining.await();
        System.out.println("all philosophers done each dining " + eatTimes + " times");
    }

    @Test
    @Ignore // implementation not finished
    public void haveDinner() throws Exception
    {
        int eatTimes = 3; // each philosopher will eat eatTimes
        int numPhilosophers = 5; // some odd number
        CountDownLatch waitTillAllInitialized = new CountDownLatch(numPhilosophers);
        CountDownLatch waitTillAllDoneDining = new CountDownLatch(numPhilosophers);

        AtomicInteger availableForks = new AtomicInteger(numPhilosophers);

        List<DiningPhilosopher> philosophers = new ArrayList<>();

        for(int i = 1; i <= numPhilosophers; i++)
        {
            DiningPhilosopher philosopher = new DiningPhilosopher(i, availableForks, eatTimes, waitTillAllDoneDining);
            philosophers.add(philosopher);
        }

        ImmutableList<DiningPhilosopher> allPhilosophers = new ImmutableList<>(philosophers);

        philosophers.forEach((DiningPhilosopher philosopher)-> {
            philosopher.start(allPhilosophers, Registry.getDefault());
        });

        waitTillAllDoneDining.await();
        System.out.println("all philosophers done each dining " + eatTimes + " times");
    }
}
