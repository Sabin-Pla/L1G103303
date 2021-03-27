package remote_procedure_events;

import common.TimeEvent;
import java.time.Instant;
import java.util.Date;

public class FloorArrivalEvent extends TimeEvent {
    public static final int SCHEDULER_LISTEN_PORT = 60 + 1024;
    public static final int FLOOR_LISTEN_PORT = 61 + 1024;
    
    private int arrivalFloor;
    private int elevatorNumber;
    private boolean doorsClosed;

    public FloorArrivalEvent(Instant eventInstant, int elevatorNumber, int arrivalFloor, boolean doorsClosed) {
        super(eventInstant);
        this.arrivalFloor = arrivalFloor;
        this.elevatorNumber = elevatorNumber;
        this.doorsClosed = doorsClosed;
    }

    public int getArrivalFloor() {
        return arrivalFloor;
    }

    public int getElevatorNumber() {
        return elevatorNumber;
    }

    public boolean getDoorsClosed() {
        return doorsClosed;
    }

    /**
     * Converts object to human readable string
     *
     * @return object in form of human readable string
     */
    public String toString() {
        return new Date(this.getEventInstant().toEpochMilli()) + " Elevator: " + elevatorNumber +
                " Arrival Floor: " + arrivalFloor + " Doors Closed: " + doorsClosed;
    }
}
