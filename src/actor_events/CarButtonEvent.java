package actor_events;

import common.TimeEvent;

import java.time.Instant;
import java.util.Date;

public class CarButtonEvent extends TimeEvent {

    private Instant eventInstant;
    private int destinationFloor;

    /**
     * Creates a CarButtonEvent containing the destination floor of whoever would have requested the elevator
     * in elevatorEvent
     *
     * lower floor
     */
    public CarButtonEvent(Instant eventInstant, int destinationFloor) {
        super(eventInstant);

        this.eventInstant = eventInstant;
        this.destinationFloor = destinationFloor;
    }

    /**
     * gets the destination floor
     *
     * @return the floor at which the passenger wants to leave
     */
    public int getDestinationFloor() {
        return destinationFloor;
    }

    /**
     * Converts object to human readable string
     *
     * @return object in form of human readable string
     */
    public String toString() {
        return new Date(getEventInstant().toEpochMilli()) + " boarding. going to floor " + this.getDestinationFloor();
    }
}
