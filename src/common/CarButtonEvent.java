package common;

public class CarButtonEvent extends TimeEvent {

    private RequestElevatorEvent elevatorEvent;
    private int destinationFloor;

    /**
     * Creates a CarButtonEvent containing the destination floor of whoever would have requested the elevator
     * in elevatorEvent
     *
     * @param elevatorTime the time the floor event was sent
     * @param destinationFloor the floor at which the passenger wants to leave
     * @throws InvalidDirectionException if the elevator is requested up to go to a higher floor, or down to a
     * lower floor
     */
    public CarButtonEvent(long eventTime, int destinationFloor) throws InvalidDirectionException {
        super(eventTime);

        this.elevatorEvent = elevatorEvent;
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
     * gets the elevator-request event
     *
     * @return the event sent when the passenger pressed the floor button
     */
    public RequestElevatorEvent getElevatorEvent() {
        return elevatorEvent;
    }
}
