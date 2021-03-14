package remote_procedure_events;

import common.TimeEvent;

public class ElevatorMotorEvent extends TimeEvent {
    public static final int ELEVATOR_RECEIVE_PORT = 70 + 1024;

    private int destinationFloor;
    private int elevatorNumber;

    public ElevatorMotorEvent(long eventTime, int elevatorNumber, int destinationFloor) {
        super(eventTime);
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
