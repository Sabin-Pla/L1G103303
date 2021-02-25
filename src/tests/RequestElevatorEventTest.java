package tests;

import common.RequestElevatorEvent;
import org.junit.Test;

public class RequestElevatorEventTest {
    @Test
    public void RequestElevatorEvent() {
        long now = System.currentTimeMillis();
        RequestElevatorEvent event = new RequestElevatorEvent(4, now, true);
        assert (event.getFloor() == 4);
        assert (event.isGoingUp());
        assert (event.getEventTime() == now);
    }
}
