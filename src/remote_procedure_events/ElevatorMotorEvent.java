package remote_procedure_events;

import common.TimeEvent;

import java.time.Instant;

public class ElevatorMotorEvent extends TimeEvent {
    public static final int ELEVATOR_RECEIVE_PORT = 70 + 1024;

    private int destinationFloor;
    private int elevatorNumber;

    public ElevatorMotorEvent(Instant eventInstant, int elevatorNumber, int destinationFloor) {
        super(eventInstant);
        this.elevatorNumber = elevatorNumber;
        this.destinationFloor = destinationFloor;
    }

    public int getArrivalFloor() {
        return destinationFloor;
    }

    public int getElevatorNumber() {
        return elevatorNumber;
    }
}
