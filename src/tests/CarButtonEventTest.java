package tests;

import actor_events.CarButtonEvent;
import floor.InvalidDirectionException;
import actor_events.RequestElevatorEvent;
import org.junit.Test;

import java.time.Instant;

public class CarButtonEventTest {

    @Test(expected = InvalidDirectionException.class)
    public void CarButton() throws InvalidDirectionException {
        Instant now = Instant.now();
        CarButtonEvent carButtonEvent = new CarButtonEvent(now, 2);
        assert (carButtonEvent != null);
        assert (carButtonEvent.getDestinationFloor() == 2);
        assert (carButtonEvent.getEventInstant().equals(now));
        RequestElevatorEvent requestElevatorEvent = new RequestElevatorEvent( 7, false, false, carButtonEvent);
        assert (requestElevatorEvent != null);
        RequestElevatorEvent invalidEvent = new RequestElevatorEvent( 7, true, false, carButtonEvent);
    }
}
