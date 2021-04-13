package tests;

import org.junit.Test;
import remote_procedure_events.CarButtonPressEvent;

import java.time.Instant;

public class CarButtonPressEventTest {

    @Test
    public void CBPE() {
    	
    	int sourceFloor = 1;
        int elevatorNumber = 2;
        int destinationFloor = 5;
        Instant eventInstant = Instant.now();

        CarButtonPressEvent c = new CarButtonPressEvent(eventInstant, sourceFloor, elevatorNumber, destinationFloor);
        
        assert (c != null);
        assert (c.getElevatorNumber() == elevatorNumber);
        assert (c.getSourceFloor() == sourceFloor);
        assert (c.getDestinationFloor() == destinationFloor);
        assert (c.toString() != null);
    }
}
