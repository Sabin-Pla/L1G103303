package common;

public class RequestElevatorEvent extends TimeEvent {

    private boolean goingUp;
    private int floor;

    /**
     * Creates a RequestElevatorEvent for when the floor button is pressed
     *
     * @param floor the floor at which the button was pressed
     * @param requestTime the epoch MS time at which the event was sent
     * @param goingUp true if the passenger wants to go to a higher floor, otherwise false
     */
    public RequestElevatorEvent(int floor, long requestTime, boolean goingUp) {
        super(requestTime);
        this.goingUp = goingUp;
        this.floor = floor;
    }

    /**
     *
     * @return The floor at which the button was pressed
     */
    public int getFloor() {
        return floor;
    }

    /**
     *
     * @return true if the passenger wants to go to a higher floor than the floor at which the button was pressed
     */
    public boolean isGoingUp() {
        return goingUp;
    }
}
