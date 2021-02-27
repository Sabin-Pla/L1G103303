package tests;

import common.*;
import events.ElevatorEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class TimeQueueTest {

    private ArrayList<RequestElevatorEvent> events;

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

    @Test
    public void addPastEvent() {
        TimeQueue queue = new TimeQueue();

        assert (queue != null);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, 1); // set the time to a time far past any event
        long future = calendar.getTime().getTime();
        Time time = new Time(1, future);
        events.get(0).setTime(time);

        Collections.shuffle(events);
        for (int i=1; i < events.size(); i++) {
            TimeEvent event = events.get(i);
            assert !queue.add(event);
        }
    }
}