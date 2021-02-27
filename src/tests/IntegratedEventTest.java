package tests;

import common.*;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class IntegratedEventTest {

    final int NUM_EVENTS = 4;

    @Test
    public void EventThreadTest() throws InterruptedException {
        URL resource = getClass().getResource("integratedTestEvents.txt");
        File f =  new File(resource.getFile());
        assert (f != null);
        ArrayList<RequestElevatorEvent> events = Parser.getRequestFromFile(f);

        assert (events.size() == NUM_EVENTS);

        long simulationStart = events.get(0).getEventTime();
        /*
            The simulation takes 3 minutes to complete,
            but we want it to be done in 3 seconds.
         */
        Time time = new Time(Time.SECOND_TO_MINUTE, simulationStart - 500);
        events.get(0).setTime(time);

        TimeQueue queueElevator = new TimeQueue();
        TimeQueue queueFloor = new TimeQueue();

        for (RequestElevatorEvent requestElevatorEvent : events) {
            assert queueElevator.add(requestElevatorEvent);
            assert queueFloor.add(requestElevatorEvent.getCarButtonEvent());
        }

        TestRunner elevatorRunner = new TestRunner(queueElevator);
        TestRunner floorRunner = new TestRunner(queueFloor);
        Thread elevatorThread = new Thread(elevatorRunner);
        Thread floorThread = new Thread(floorRunner);

        time.restart();
        elevatorThread.start();
        floorThread.start();

        assert (elevatorRunner.getEventsSent() == 0);
        assert (floorRunner.getEventsSent() == 0);

        Thread.sleep(500);

        for (int i=1; i<5; i++) {
            assert (elevatorRunner.getEventsSent() == i);
            assert (floorRunner.getEventsSent() == i);
            assert (elevatorRunner.getWaitTimeRecalculations() < 3 * i);
            assert (floorRunner.getWaitTimeRecalculations() < 3 * i);
            Thread.sleep(1000);
        }

        floorThread.join();
        elevatorThread.join();
    }
}

class TestRunner implements Runnable {

    private TimeQueue queue;
    private final long MINIMUM_WAIT_TIME = 50;
    private int eventsSent;
    private int waitTimeRecalculations;

    public TestRunner(TimeQueue queue) {
        this.queue = queue;
        eventsSent = 0;
        waitTimeRecalculations = 0;
    }

    public int getWaitTimeRecalculations() {
        return  waitTimeRecalculations;
    }

    public int getEventsSent() {
        return eventsSent;
    }

    @Override
    public void run() {
        synchronized (queue) {
            long startTime = System.currentTimeMillis();
            while (!queue.isEmpty()) {
                while (!queue.peekEvent().hasPassed()) {
                    try {
                        long waitTime = queue.waitTime();
                        if (waitTime >= MINIMUM_WAIT_TIME) {
                            queue.wait(waitTime);
                        } else {
                            queue.wait(MINIMUM_WAIT_TIME);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                eventsSent++;
                queue.poll();
            }
        }
    }

}

