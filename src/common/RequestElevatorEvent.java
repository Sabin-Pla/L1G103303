package common;

import java.util.Date;

public class RequestElevatorEvent extends TimeEvent {

    private boolean goingUp;
    private int floor;
    private CarButtonEvent carButtonEvent;

    /**
     * Creates a RequestElevatorEvent for when the floor button is pressed
     *
     * @param floor the floor at which the button was pressed
     * @param goingUp true if the passenger wants to go to a higher floor, otherwise false
     * @param carButtonEvent the corresponding elevator button press event (i.e, where the passenger intends to go)
     */
    public RequestElevatorEvent(int floor, boolean goingUp, CarButtonEvent carButtonEvent)
            throws InvalidDirectionException {
        super(carButtonEvent.getEventTime());
        this.goingUp = goingUp;
        this.floor = floor;
        this.carButtonEvent = carButtonEvent;

        if (goingUp && carButtonEvent.getDestinationFloor() < floor) {
            throw new InvalidDirectionException("Going down to a higher floor");
        } else if (!goingUp && carButtonEvent.getDestinationFloor() > floor)  {
            throw new InvalidDirectionException("Going up to a lower floor");
        } else if (carButtonEvent.getDestinationFloor() == floor) {
            throw new InvalidDirectionException("Source and destination floor cannot be equal");
        }

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

    public CarButtonEvent getCarButtonEvent() {
        return this.carButtonEvent;
    }

    /**
     * Converts object to human readable string
     *
     * @return object in form of human readable string
     */
    public String toString() {
        return new Date(this.getEventTime()) + " From " + floor + " To " +  carButtonEvent.getDestinationFloor();
    }
}
