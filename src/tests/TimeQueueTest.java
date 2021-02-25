package tests;

import common.Parser;
import common.Time;
import common.TimeEvent;
import common.TimeQueue;
import events.ElevatorEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class TimeQueueTest {

    private ArrayList<TimeEvent> events;

    @Before
    public void getRequests() {
        URL resource = getClass().getResource("parserTest.txt");
        File f =  new File(resource.getFile());
        assert (f != null);
        events = Parser.getRequestFromFile(f);
        assert (events != null);

        TimeEvent firstEvent = events.get(0);
        Time time = new Time(15, firstEvent.getEventTime() - 100000);
        firstEvent.setTime(time);
    }

    @Test
    public void add() {
        TimeQueue queue = new TimeQueue();

        assert (queue != null);

        Collections.shuffle(events);
        for (int i=1; i < events.size(); i++) {
            TimeEvent event = events.get(i);
            assert queue.add(event);
        }

        TimeEvent lastEvent = (TimeEvent) queue.peek();
        while (!queue.isEmpty()) {
            TimeEvent temp = (TimeEvent) queue.poll();
            assert (lastEvent.compareTo(temp) <= 0);
            lastEvent = temp;
        }
    }
}