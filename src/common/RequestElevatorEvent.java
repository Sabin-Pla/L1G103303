package common;

public class RequestElevatorEvent extends TimeEvent {

    private boolean goingUp;
    private int floor;

    public RequestElevatorEvent(int floor, long requestTime, boolean goingUp) {
        super(requestTime);
        this.goingUp = goingUp;
        this.floor = floor;
    }

    public int getFloor() {
        return floor;
    }

    public boolean isGoingUp() {
        return goingUp;
    }
}
