package org.objectscape.colanes.coreceivers;

import org.objectscape.colanes.CoLane;
import org.objectscape.colanes.CoReceiver;
import org.objectscape.colanes.registry.Registry;
import org.objectscape.colanes.util.ImmutableList;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver
 * Date: 14.05.13
 * Time: 18:25
 * To change this template use File | Settings | File Templates.
 */
public class DiningPhilosopher implements CoReceiver
{

    private static Random Randomizer = new Random(System.currentTimeMillis());
    private static AtomicInteger LogLineCount = new AtomicInteger();
    private static final String InitializationCounterKey = "InitializationCounterKey";

    private CoLane lane = new CoLane();

    private boolean hasLeftTable = false;
    private boolean hasLeftFork = false;
    private boolean hasRightFork = false;
    private int id = -1;
    private int eatTimes = 1;
    private int timesEaten = 0;
    private AtomicInteger availableForks = null;
    private CountDownLatch waitTillDoneLatch = null;

    ImmutableList<DiningPhilosopher> philosophers = null;

    private AtomicReference<String> leftFork = null;
    private AtomicReference<String> rightFork = null;

    private DiningPhilosopher leftPhilosopher = null;
    private DiningPhilosopher rightPhilosopher = null;

    public DiningPhilosopher() {
    }

    public DiningPhilosopher(int id, AtomicInteger availableForks, int eatTimes, CountDownLatch waitTillDoneLatch) {
        super();
        this.id = id;
        this.eatTimes = eatTimes;
        this.availableForks = availableForks;
        this.waitTillDoneLatch = waitTillDoneLatch;
    }

    public void start(ImmutableList<DiningPhilosopher> philosophers, Registry registry) {
        lane.run(() -> {
            startInternal(philosophers, registry);
        });
    }

    private void startInternal(ImmutableList<DiningPhilosopher> philosophers, Registry registry)
    {
        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": am spending my time thinking and am waiting for my left and right fork to become available");

        this.philosophers = philosophers;
    }

    public void rightForkAvailable(String fork)
    {
        lane.run(() -> {
            rightForkAvailableInternal(fork);
        });
    }

    private void rightForkAvailableInternal(String fork)
    {
        if(hasLeftTable)
            return;

        String msg = LogLineCount.incrementAndGet() + " - philosopher " + id + ": right fork " + fork + " available";

        // always take left fork first
        if(!hasLeftFork) {
            msg += ", but the left fork isn't. So I will not pick up the right fork to prevent possible deadlock from happening.";
            System.out.println(msg);
            return;
        }

        System.out.println(msg);
        hasRightFork = rightFork.compareAndSet(getRightForkName(), null);

        // someone else obtained the fork before me?
        if(!hasRightFork) {
            System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": somebody else received right fork " + fork);
            return;
        }

        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": obtained my right fork " + fork);

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
        rightFork.compareAndSet(null, fork);
        rightPhilosopher.leftForkAvailable(fork);
        hasLeftFork = false;
        fork = getLeftForkName();
        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": returning left fork " + fork);
        leftFork.compareAndSet(null, fork);
        leftPhilosopher.rightForkAvailable(fork);
        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": am now spending my time thinking");

        timesEaten++;

        if(timesEaten == eatTimes) {
            hasLeftTable = true;
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
        if(hasLeftTable)
            return;

        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": left fork " + fork + " available");

//        if(availableForks.size() == 1) {
//            System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": only 1 fork left, not picking it up to prevent deadlock from happening");
//            leftPhilosopher.rightForkAvailable(fork);
//            return;
//        }

        hasLeftFork = leftFork.compareAndSet(fork, null);

        // someone else obtained the fork before me?
        if(!hasLeftFork) {
            System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": somebody else obtained " + fork);
            return;
        }

        System.out.println(LogLineCount.incrementAndGet() + " - philosopher " + id + ": obtained my left fork " + fork);
    }

    public String GetRightForkName(int id, int numPhilosophers) {
        return "ForkBetweenPhilosopher" + id + "And" + RightPhilosopherId(id, numPhilosophers);
    }

    public String getRightForkName() {
        return GetRightForkName(id, philosophers.size());
    }

    public String GetLeftForkName(int id, int numPhilosophers) {
        return "ForkBetweenPhilosopher" + LeftPhilosopherId(id, numPhilosophers)+ "And" + id;
    }

    public String getLeftForkName() {
        return GetLeftForkName(id, philosophers.size());
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

}
