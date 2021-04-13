package remote_procedure_events;

import floor.InvalidDirectionException;
import common.TimeEvent;

import java.time.Instant;
import java.util.Date;

public class FloorButtonPressEvent extends TimeEvent {
    public static final int SCHEDULER_LISTEN_PORT = 62 + 1024;

    private boolean goingUp;
    private int floor;
    boolean doorError;

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
     * @param doorError true if a door opening error should be simulated
     */    
    public FloorButtonPressEvent(Instant eventInstant, int floor, boolean goingUp, boolean doorError)  {
        super(eventInstant);
        this.goingUp = goingUp;
        this.floor = floor;
        this.doorError = doorError;
    }

    /**
     *
     * @return true if the passenger wants to go to a higher floor than the floor at which the button was pressed
     */
    public boolean isGoingUp() {
        return goingUp;
    }
    
    public boolean doorError() {
    	return doorError;
    }

    /**
     * Converts object to human readable string
     *
     * @return object in form of human readable string
     */
    public String toString() {
        return new Date(this.getEventInstant().toEpochMilli()) + " From " + floor + " Goingup: " + goingUp;
    }
}
