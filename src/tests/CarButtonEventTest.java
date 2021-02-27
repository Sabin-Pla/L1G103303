package tests;

import common.CarButtonEvent;
import common.InvalidDirectionException;
import common.RequestElevatorEvent;
import org.junit.Test;

public class CarButtonEventTest {

    @Test(expected = InvalidDirectionException.class)
    public void CarButton() throws InvalidDirectionException {
        long now = System.currentTimeMillis();
        CarButtonEvent carButtonEvent = new CarButtonEvent(now, 2);
        assert (carButtonEvent != null);
        assert (carButtonEvent.getDestinationFloor() == 2);
        assert (carButtonEvent.getEventTime() == now);
        RequestElevatorEvent requestElevatorEvent = new RequestElevatorEvent( 7, false, carButtonEvent);
        assert (requestElevatorEvent != null);
        RequestElevatorEvent invalidEvent = new RequestElevatorEvent( 7, true, carButtonEvent);
    }
}
