package remote_procedure_events;

import common.InvalidDirectionException;
import common.TimeEvent;

import java.util.Date;

public class FloorButtonPressEvent extends TimeEvent {
    public static final int SCHEDULER_LISTEN_PORT = 62 + 1024;

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
     *
     * @param floor the floor at which the button was pressed
     * @param goingUp true if the passenger wants to go to a higher floor, otherwise false
     */
    public FloorButtonPressEvent(int floor, boolean goingUp, long eventTime) throws InvalidDirectionException {
        super(eventTime);
        this.goingUp = goingUp;
        this.floor = floor;
    }

    /**
     *
     * @return true if the passenger wants to go to a higher floor than the floor at which the button was pressed
     */
    public boolean isGoingUp() {
        return goingUp;
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
