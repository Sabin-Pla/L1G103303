package tests;

import common.TimeEvent;
import org.junit.Test;
import remote_procedure_events.FloorArrivalEvent;

import java.time.Instant;
import java.util.Date;

public class FloorArrivalEventTest {

    @Test
    public void FloorArrivalTest() {
    	Instant eventInstant = Instant.now();
        int elevatorNumber = 5;
        int arrivalFloor = 10;
        boolean doorsClosed = true;
        FloorArrivalEvent f = new FloorArrivalEvent(eventInstant, elevatorNumber, arrivalFloor, doorsClosed);
  
        assert (f != null);
        assert (f.getArrivalFloor() == arrivalFloor);
        assert (f.getElevatorNumber() == elevatorNumber);
        assert (f.getDoorsClosed() == doorsClosed);
        assert (f.toString() != null);
    }
}
