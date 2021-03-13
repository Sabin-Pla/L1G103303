package common;

import java.util.Date;

public class CarButtonEvent extends TimeEvent {

    private long eventTime;
    private int destinationFloor;

    /**
     * Creates a CarButtonEvent containing the destination floor of whoever would have requested the elevator
     * in elevatorEvent
     *
     * @param eventTime the time the floor event was sent
     * @param destinationFloor the floor at which the passenger wants to leave
     * @throws InvalidDirectionException if the elevator is requested up to go to a higher floor, or down to a
     * lower floor
     */
    public CarButtonEvent(long eventTime, int destinationFloor) throws InvalidDirectionException {
        super(eventTime);

        this.eventTime = eventTime;
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
        return new Date(this.getEventTime()) + " boarding. going to floor " + this.getDestinationFloor();
    }
}
