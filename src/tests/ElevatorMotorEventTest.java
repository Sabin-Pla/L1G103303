package tests;

import common.TimeEvent;
import org.junit.Test;
import remote_procedure_events.ElevatorMotorEvent;

import java.time.Instant;
import java.util.Date;

public class ElevatorMotorEventTest {



    @Test
    public void ElevatorMotorTest() {
    	Instant eventInstant = Instant.now();
        int elevatorNumber = 5;
        int destinationFloor = 10;
        ElevatorMotorEvent e = new ElevatorMotorEvent(eventInstant, elevatorNumber, destinationFloor, false);
        assert (e != null);
        assert (e.getArrivalFloor() == destinationFloor);
        assert (e.getElevatorNumber() == elevatorNumber);
        assert (e.toString() != null);
    }
}
