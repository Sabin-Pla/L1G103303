package tests;

import floor.InvalidDirectionException;
import common.TimeEvent;
import org.junit.Test;
import remote_procedure_events.FloorButtonPressEvent;

import java.time.Instant;
import java.util.Date;

public class FloorButtonPressEventTest {
    Instant eventInstant;
    private int floor = 5;
    boolean goingUp = true;
    FloorButtonPressEvent f = new FloorButtonPressEvent(eventInstant, floor, goingUp);

    @Test
    public void FBPE() {
        assert (f != null);
        assert (f.isGoingUp() == true);
        assert (f.getFloor() == floor);
        assert (f.toString() != null);
    }
}
