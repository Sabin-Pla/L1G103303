package tests;

import common.TimeEvent;
import org.junit.Test;
import remote_procedure_events.FloorArrivalEvent;

import java.time.Instant;
import java.util.Date;

public class FloorArrivalEventTest {
    private Instant eventInstant;
    private int elevatorNumber = 5;
    private  int arrivalFloor = 10;
    private boolean doorsClosed = true;

    FloorArrivalEvent f = new FloorArrivalEvent(eventInstant, elevatorNumber, arrivalFloor, doorsClosed);

    @Test
    public void FloorArrivalTest(){
        assert (f != null);
        assert (f.getArrivalFloor() == arrivalFloor);
        assert (f.getElevatorNumber() == elevatorNumber);
        assert (f.getDoorsClosed() == doorsClosed);
        assert (f.toString() != null);
    }
}
