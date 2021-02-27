package tests;

import common.CarButtonEvent;
import common.InvalidDirectionException;
import common.RequestElevatorEvent;
import org.junit.Test;

public class RequestElevatorEventTest {
    @Test
    public void RequestElevatorEvent() throws InvalidDirectionException {
        long now = System.currentTimeMillis();
        CarButtonEvent carButtonEvent = new CarButtonEvent(now, 7);
        RequestElevatorEvent requestElevatorEvent = new RequestElevatorEvent(4, true, carButtonEvent);
        assert (requestElevatorEvent.getFloor() == 4);
        assert (requestElevatorEvent.isGoingUp());
        assert (requestElevatorEvent.getEventTime() == now);
    }
}
