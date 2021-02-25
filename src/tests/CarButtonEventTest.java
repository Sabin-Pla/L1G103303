package tests;

import common.CarButtonEvent;
import common.InvalidDirectionException;
import common.RequestElevatorEvent;
import org.junit.Before;
import org.junit.Test;

public class CarButtonEventTest {

    private RequestElevatorEvent requestElevatorEvent;

    @Before
    public void prepareRequestElevatorEvent() {
        long now = System.currentTimeMillis();
        requestElevatorEvent = new RequestElevatorEvent(
                5, now, false);
    }

    @Test(expected = InvalidDirectionException.class)
    public void CarButton() throws InvalidDirectionException {
        CarButtonEvent carButtonEvent = new CarButtonEvent(requestElevatorEvent, 2);
        assert (carButtonEvent != null);
        assert (carButtonEvent.getDestinationFloor() == 2);
        assert (carButtonEvent.getElevatorEvent() == requestElevatorEvent);
        CarButtonEvent exceptionEvent = new CarButtonEvent(requestElevatorEvent, 7);
    }
}
