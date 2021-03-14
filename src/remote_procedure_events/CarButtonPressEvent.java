package remote_procedure_events;

import common.InvalidDirectionException;
import common.TimeEvent;

import java.util.Date;

public class CarButtonPressEvent extends TimeEvent {
    public static final int SCHEDULER_LISTEN_PORT = 63 + 1024;

    private int sourceFloor;
    private int elevatorNumber;
    private int destinationFloor;

    /**
     * Creates a FloorButtonPressEvent for when the floor button is pressed
     *
     */
    public CarButtonPressEvent(long eventTime, int sourceFloor, int elevatorNumber, int destinationFloor) {
        super(eventTime);
        this.sourceFloor = sourceFloor;
        this.elevatorNumber = elevatorNumber;
        this.destinationFloor = destinationFloor;
    }

    public  int getElevatorNumber() { return elevatorNumber;}

    public int getSourceFloor() {
        return sourceFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    /**
     * Converts object to human readable string
     *
     * @return object in form of human readable string
     */
    public String toString() {
        return new Date(this.getEventTime()) + " From " + sourceFloor + " To " + destinationFloor;
    }
}
