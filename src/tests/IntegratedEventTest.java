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
        ArrayList<TimeEvent> events = Parser.getRequestFromFile(f);

        assert (events.size() == NUM_EVENTS * 2);

        long simulationStart = events.get(0).getEventTime();
        /*
            The simulation takes 3 minutes to complete,
            but we want it to be done in 3 seconds.
         */
        Time time = new Time(Time.SECOND_TO_MINUTE, simulationStart - 500);
        events.get(0).setTime(time);

        TimeQueue queueElevator = new TimeQueue();
        TimeQueue queueFloor = new TimeQueue();

        for (TimeEvent event : events) {
            if (event instanceof CarButtonEvent) {
                assert queueElevator.add(event);
            } else if (event instanceof RequestElevatorEvent) {
                assert queueFloor.add(event);
            }
        }

        TestRunner elevatorRunner = new TestRunner(queueElevator);
        TestRunner floorRunner = new TestRunner(queueFloor);
        Thread elevatorThread = new Thread(elevatorRunner);
        Thread floorThread = new Thread(floorRunner);

        assert (elevatorRunner.getEventsSent() == 0);
        assert (floorRunner.getEventsSent() == 0);

        elevatorThread.start();
        //floorThread.start();

        time.restart();

        assert (!queueElevator.peekEvent().hasPassed());
        assert (!events.get(0).hasPassed());
        //assert (elevatorRunner.getEventsSent() == 0);
        Thread.sleep(500);

        assert (elevatorRunner.peek().hasPassed());
        queueElevator.poll();
        assert (!elevatorRunner.peek().hasPassed());
        assert (events.get(0).hasPassed());
        assert (!events.get(2).hasPassed());
        //assert (elevatorRunner.getEventsSent() == 1);
        Thread.sleep(1000);

        assert (elevatorRunner.peek().hasPassed());
        queueElevator.poll();
        assert (!elevatorRunner.peek().hasPassed());
        assert (events.get(2).hasPassed());
        assert (!events.get(4).hasPassed());
        //assert (elevatorRunner.getEventsSent() == 2);
        Thread.sleep(1000);

        assert (elevatorRunner.peek().hasPassed());
        queueElevator.poll();
        assert (!elevatorRunner.peek().hasPassed());
        assert (events.get(4).hasPassed());
        assert (!events.get(6).hasPassed());
        //assert (elevatorRunner.getEventsSent() == 3);
        Thread.sleep(1000);

        assert (events.get(6).hasPassed());
        //assert (elevatorRunner.getEventsSent() == 4);

        //assert (elevatorRunner.done());
        //assert (floorRunner.done());
    }
}

class TestRunner implements Runnable {

    private TimeQueue queue;
    private int eventsSent;

    public TestRunner(TimeQueue queue) {
        this.queue = queue;
        eventsSent = 0;
    }

    public TimeEvent peek() {
        return (TimeEvent) queue.peek();
    }

    public int getEventsSent() {
        return eventsSent;
    }

    public boolean done() {
        return queue.isEmpty();
    }

    @Override
    public synchronized void run() {
        while (!queue.isEmpty()) {
            while (!queue.peekEvent().hasPassed()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            eventsSent++;
            queue.poll();
        }
    }
}

