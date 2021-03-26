package tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import actor_events.RequestElevatorEvent;
import common.*;
import org.junit.Before;
import org.junit.Test;

public class TimeQueueTest {

    private ArrayList<RequestElevatorEvent> events;
    private SimulationClock clock;

    @Before
    public void getRequests() throws FileNotFoundException, InvalidDirectionException {
        URL resource = getClass().getResource("parserTest.txt");
        File f =  new File(resource.getFile());
        assert (f != null);
        Parser p = new Parser(f);
        p.parseEvents();
        events = p.getEvents();
        assert (events != null);
        clock = p.getClock();
        assert (clock != null);
        clock.start();
    }

    @Test
    public void add() {
        TimeQueue queue = new TimeQueue();
        assert (queue != null);
        queue.setClock(clock);

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
        SimulationClock clockStartFuture = new SimulationClock(calendar.toInstant(), 1);
        queue.setClock(clockStartFuture);

        Collections.shuffle(events);
        for (int i=1; i < events.size(); i++) {
            TimeEvent event = events.get(i);
            assert !queue.add(event);
        }
    }
}