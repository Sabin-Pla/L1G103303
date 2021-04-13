package tests;

import common.Parser;
import common.SimulationClock;
import common.TimeEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

public class TimeEventTest {

    private SimulationClock clock;
    private TimeEvent eventNow, eventLater;

    @Before
    public void createTime() {
        Instant now = Instant.now();
        clock = new SimulationClock(now, 10);
        eventNow = new TimeEvent(now);
        eventLater = new TimeEvent(now.plusSeconds(10));
        clock.start();
    }

    @Test
    public void compareTo() {
        assert (eventNow != null);
        assert (eventLater != null);
        assert (eventNow.compareTo(eventLater) < 0);
        assert (eventLater.compareTo(eventNow) > 0);
    }

    @Test
    public void hasPassed() throws InterruptedException {
        Thread.sleep(200);
        assert (eventNow.hasPassed(clock));
        assert (!eventLater.hasPassed(clock));
        Thread.sleep(500);
        assert (eventLater.hasPassed(clock));
    }

}