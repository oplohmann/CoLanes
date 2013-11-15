package org.objectscape.colanes;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: Nutzer
 * Date: 30.05.13
 * Time: 18:13
 * To change this template use File | Settings | File Templates.
 */
public class AbstractCoReceiver implements CoReceiver {

    private CoLane lane = null;

    public AbstractCoReceiver() {
        lane = new CoLane();
    }

    public AbstractCoReceiver(String name) {
        lane = new CoLane(name);
    }


    protected void run(Runnable runnable) {
        lane.run(runnable);
    }

    protected void runAfter(long duration, TimeUnit unit, Runnable runnable) {
        lane.runAfter(duration, unit, runnable);
    }
}
