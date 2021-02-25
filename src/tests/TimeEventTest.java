package tests;

import common.Time;
import common.TimeEvent;
import org.junit.Before;
import org.junit.Test;

public class TimeEventTest {

    Time time;
    long now, later;
    TimeEvent eventNow, eventLater;

    @Before
    public void createTime() {
        now = System.currentTimeMillis();
        time = new Time(15, now);
        eventNow = new TimeEvent(now);
        eventNow.setTime(time);
        later = now + 10000;
        eventLater = new TimeEvent(later);
    }

    @Test
    public void compareTo() throws InterruptedException {
        assert (eventNow != null);
        assert (eventNow.getEventTime() == now);
        assert (eventLater != null);
        assert (eventLater.getEventTime() == later);

        assert (eventNow.compareTo(eventLater) < 0);
        assert (eventLater.compareTo(eventNow) > 0);
        assert (eventNow.compareTo(eventNow) == 0);

        Thread.sleep(4500);
        assert (eventNow.hasPassed());
    }
}