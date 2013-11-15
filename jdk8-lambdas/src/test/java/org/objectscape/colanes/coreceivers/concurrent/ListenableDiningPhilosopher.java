package org.objectscape.colanes.coreceivers.concurrent;

import org.objectscape.colanes.CoLane;
import org.objectscape.colanes.CoReceiver;
import org.objectscape.colanes.HandbackFutureCompletion;
import org.objectscape.colanes.registry.Registry;
import org.objectscape.colanes.util.ImmutableList;
import org.objectscape.colanes.util.concurrent.map.ListenableConcurrentMap;
import org.objectscape.colanes.util.concurrent.map.PutEvent;

import java.util.Random;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 14.05.13
 * Time: 18:25
 * To change this template use File | Settings | File Templates.
 */
public class ListenableDiningPhilosopher implements CoReceiver
{

    public static final String AvailableForksMapName = "AvailableForks";
    public static final String AvailablePhilosphersMapName = "AvailablePhilosphers";

    private static Random Randomizer = new Random(System.currentTimeMillis());
    private static AtomicInteger LogLineCount = new AtomicInteger();

    private CoLane lane = new CoLane();

    private boolean hasLeftFork = false;
    private boolean hasRightFork = false;
    private AtomicBoolean hasLeftTable = new AtomicBoolean(false);
    private int id = -1;
    private int numPhilosophers = -1;
    private int eatTimes = 1;
    private int timesEaten = 0;
    private CountDownLatch waitTillDoneLatch = null;
    private ListenableDiningPhilosopher leftPhilosopher = null;
    private ListenableConcurrentMap<String, String> availableForks = Registry.getDefault().getListenableConcurrentMapNamed(ListenableDiningPhilosopher.AvailableForksMapName);
    private ConcurrentMap<Integer,ListenableDiningPhilosopher> availablePhilosophers = Registry.getDefault().getConcurrentMapNamed(ListenableDiningPhilosopher.AvailablePhilosphersMapName);

    public ListenableDiningPhilosopher() {
    }

    public ListenableDiningPhilosopher(int id, int numPhilosophers, int eatTimes, CountDownLatch waitTillDoneLatch)
    {
        super();
        this.id = id;
        this.eatTimes = eatTimes;
        this.waitTillDoneLatch = waitTillDoneLatch;
        this.numPhilosophers = numPhilosophers;
    }

    public void start(Registry registry) {
        lane.run(() -> {
            startInternal(registry);
        });
    }

    public HandbackFutureCompletion<String> startHandback(Registry registry) {
        return lane.<String>getHandbackFutureCompletion().supplier(() -> {
            return startInternal(registry);
        });
    }

    private String startInternal(Registry registry)
    {
        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": am spending my time thinking and am waiting for my left and right fork to become available");

        availableForks = registry.getListenableConcurrentMapNamed(AvailableForksMapName);

        availableForks.addSynchronousListener(
                getRightForkName(),
                (PutEvent<String> event) -> {
                    rightForkAvailable((String) event.getKey());
                },
                true);

        availableForks.addSynchronousListener(
                getLeftForkName(),
                (PutEvent<String> event) -> {
                    leftForkAvailable((String) event.getKey());
                },
                true);

        return "";
    }

    private ListenableDiningPhilosopher getLeftPhilosopher() {
        if(leftPhilosopher == null) {
            leftPhilosopher = availablePhilosophers.get(LeftPhilosopherId(id, numPhilosophers));
        }
        return leftPhilosopher;
    }

    public void rightForkAvailable(String fork)
    {
        lane.run(() -> {
            rightForkAvailableInternal(fork);
        });
    }

    private void rightForkAvailableInternal(String fork)
    {
        if(hasLeftTable.get())
            return;

        String msg = LogLineCount.incrementAndGet() + " - philosopher " + id + ": right fork " + fork + " available";

        // always take left fork first
        if(!hasLeftFork && !getLeftPhilosopher().hasLeftTable()) {
            msg += ", but the left fork isn't. So I will not pick up the right fork to prevent possible deadlock from happening.";
            System.out.println(msg);
            return;
        }

        System.out.println(msg);
        ImmutableList<String> forks = availableForks.remove(fork);

        // someone else obtained the fork before me?
        if(forks == null) {
            System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": somebody else received right fork " + fork);
            return;
        }

        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": obtained my right fork " + fork);

        hasRightFork = true;

        startEating();
    }

    private void startEating() {
        int eatDurationInSecs = Math.max(Randomizer.nextInt(3), 1);
        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": have obtained both forks and will be eating for " + eatDurationInSecs + " second(s)");
        lane.runAfter(eatDurationInSecs, TimeUnit.SECONDS, () -> {
            doneEating();
        });
    }

    private void doneEating()
    {
        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": am finished eating and will return my forks");
        hasRightFork = false;
        String fork = getRightForkName();
        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": returning right fork " + fork);
        availableForks.putSingleValue(fork, fork);
        hasLeftFork = false;
        fork = getLeftForkName();
        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": returning left fork " + fork);
        availableForks.putSingleValue(fork, fork);
        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": am now spending my time thinking");

        timesEaten++;

        if(timesEaten == eatTimes) {
            hasLeftTable.compareAndSet(false, true);
            System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": have eaten " + eatTimes + " times and will leave the table");
            waitTillDoneLatch.countDown();
        }
    }

    public void leftForkAvailable(String fork) {
        lane.run(() -> {
            leftForkAvailableInternal(fork);
        });
    }

    private void leftForkAvailableInternal(String fork)
    {
        if(hasLeftTable.get())
            return;

        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": left fork " + fork + " available");

        if(availableForks.size() == 1) {
            System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": only 1 fork left, not picking it up to prevent deadlock from happening");
            getLeftPhilosopher().rightForkAvailable(fork);
            return;
        }

        ImmutableList<String> forks = availableForks.remove(fork);

        // someone else obtained the fork before me?
        if(forks == null) {
            System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": somebody else obtained " + fork);
            return;
        }

        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": obtained my left fork " + fork);

        hasLeftFork = true;

        rightForkAvailableInternal(getRightForkName());
    }

    public String getRightForkName() {
        return "ForkBetweenPhilosopher" + id + "And" + RightPhilosopherId(id, numPhilosophers);
    }

    public String getLeftForkName() {
        return "ForkBetweenPhilosopher" + LeftPhilosopherId(id, numPhilosophers)+ "And" + id;
    }

    public static int RightPhilosopherId(int philosopherId, int numPhilosophers) {
        int rightPhilosopherId = philosopherId + 1;
        if(rightPhilosopherId > numPhilosophers)
            return 1;
        return rightPhilosopherId;
    }

    public static int LeftPhilosopherId(int philosopherId, int numPhilosophers) {
        int leftPhilosopherId = philosopherId - 1;
        if(leftPhilosopherId < 1)
            return numPhilosophers;
        return leftPhilosopherId;
    }

    public int getId() {
        return id;
    }

    public boolean hasLeftTable() {
        return hasLeftTable.get();
    }
}
