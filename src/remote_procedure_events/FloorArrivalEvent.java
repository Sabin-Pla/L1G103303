package remote_procedure_events;

import common.TimeEvent;

public class FloorArrivalEvent extends TimeEvent {
    public static final int SCHEDULER_LISTEN_PORT = 60 + 1024;
    public static final int FLOOR_LISTEN_PORT = 61 + 1024;
    private int arrivalFloor;
    private int elevatorNumber;

    public FloorArrivalEvent(long eventTime, int elevatorNumber, int arrivalFloor) {
        super(eventTime);
        this.arrivalFloor = arrivalFloor;
        this.elevatorNumber = elevatorNumber;
    }

    public int getArrivalFloor() {
        return arrivalFloor;
    }

    public int getElevatorNumber() {
        return elevatorNumber;
    }
}
