package tests;

import floor.InvalidDirectionException;
import common.TimeEvent;
import org.junit.Test;
import remote_procedure_events.FloorButtonPressEvent;

import java.time.Instant;
import java.util.Date;

public class FloorButtonPressEventTest {

    @Test
    public void FBPE() {
    	Instant eventInstant = Instant.now();
    	int floor = 5;
    	boolean goingUp = true;
    	FloorButtonPressEvent f = new FloorButtonPressEvent(eventInstant, floor, goingUp, false);
        assert (f != null);
        assert (f.isGoingUp() == true);
        assert (f.getFloor() == floor);
        assert (f.toString() != null);
    }
}
