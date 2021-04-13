package tests;

import actor_events.CarButtonEvent;
import floor.InvalidDirectionException;
import actor_events.RequestElevatorEvent;
import org.junit.Test;

import java.time.Instant;

public class RequestElevatorEventTest {

    @Test
    public void RequestElevatorEvent() throws InvalidDirectionException {
        Instant now = Instant.now();
        CarButtonEvent carButtonEvent = new CarButtonEvent(now, 7);
        RequestElevatorEvent requestElevatorEvent = new RequestElevatorEvent(4, true, false, carButtonEvent);
        assert (requestElevatorEvent.getFloor() == 4);
        assert (requestElevatorEvent.isGoingUp());
        assert (requestElevatorEvent.getEventInstant().equals(now));
    }
}
