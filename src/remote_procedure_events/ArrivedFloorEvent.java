package remote_procedure_events;

import common.InvalidDirectionException;
import common.TimeEvent;

import java.util.Date;

public class ArrivedFloorEvent extends TimeEvent {
    public static final int SCHEDULER_LISTEN_PORT = 66 + 1024;

    private boolean goingUp;
    private int floor;

    /**
     *
     * @return The floor at which the button was pressed
     */
    public int getFloor() {
        return floor;
    }

    /**
     * Creates a FloorButtonPressEvent for when the floor button is pressed
     */
    public ArrivedFloorEvent(long eventTime) throws InvalidDirectionException {
        super(eventTime);
    }

    /**
     * Converts object to human readable string
     *
     * @return object in form of human readable string
     */
    public String toString() {
        return new Date(this.getEventTime()) + " From " + floor;
    }
}
