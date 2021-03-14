package remote_procedure_events;

import common.TimeEvent;

public class ElevatorFloorArrivalEvent extends TimeEvent {
    public static final int SCHEDULER_LISTEN_PORT = 60 + 1024;
    public static final int FLOOR_LISTEN_PORT = 61 + 1024;
    private int arrivalFloor;
    private int elevatorNumber;

    public ElevatorFloorArrivalEvent(int floorNumber, long eventTime) {
        super(eventTime);
        arrivalFloor = floorNumber;
    }

    public int getArrivalFloor() {
        return arrivalFloor;
    }

    public int getElevatorNumber() {
        return elevatorNumber;
    }
}
