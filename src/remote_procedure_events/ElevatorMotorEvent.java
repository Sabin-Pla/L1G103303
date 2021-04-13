package remote_procedure_events;

import common.TimeEvent;

import java.time.Instant;
import java.util.Date;

public class ElevatorMotorEvent extends TimeEvent {
    public static final int ELEVATOR_RECEIVE_PORT = 70 + 1024;

    private int destinationFloor;
    private int elevatorNumber;
    private boolean doorError;

    public ElevatorMotorEvent(Instant eventInstant, int elevatorNumber, int destinationFloor, boolean doorError) {
        super(eventInstant);
        this.elevatorNumber = elevatorNumber;
        this.destinationFloor = destinationFloor;
        this.doorError = doorError;
    }

    public int getArrivalFloor() {
        return destinationFloor;
    }

    public int getElevatorNumber() {
        return elevatorNumber;
    }

    public String toString() {
        return Date.from(getEventInstant()) + " elevator: " + elevatorNumber + " to floor: " + destinationFloor;
    }

	public boolean getError() {
		return doorError;
	}
}
