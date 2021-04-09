package tests;

import common.TimeEvent;
import org.junit.Test;
import remote_procedure_events.ElevatorMotorEvent;

import java.time.Instant;
import java.util.Date;

public class ElevatorMotorEventTest {

    private Instant eventInstant;
    private int elevatorNumber = 5;
    private  int destinationFloor = 10;

    @Test
    public void ElevatorMotorTest(){
        ElevatorMotorEvent e = new ElevatorMotorEvent(eventInstant, elevatorNumber, destinationFloor);
        assert (e != null);
        assert (e.getArrivalFloor() == destinationFloor);
        assert (e.getElevatorNumber() == elevatorNumber);
        assert (e.toString() != null);
    }
}
