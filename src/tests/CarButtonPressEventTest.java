package tests;

import org.junit.Test;
import remote_procedure_events.CarButtonPressEvent;

import java.time.Instant;

public class CarButtonPressEventTest {
    private int sourceFloor = 1;
    private int elevatorNumber = 2;
    private int destinationFloor = 5;
    private Instant eventInstant;

    CarButtonPressEvent c = new CarButtonPressEvent(eventInstant, sourceFloor, elevatorNumber, destinationFloor);

    @Test
    public void CBPE(){
        assert (c != null);
        assert (c.getElevatorNumber() == elevatorNumber);
        assert (c.getSourceFloor() == sourceFloor);
        assert (c.getDestinationFloor() == destinationFloor);
        assert (c.toString() != null);
    }
}
