package common;

public class CarButtonEvent extends TimeEvent {

    private RequestElevatorEvent elevatorEvent;
    private int destinationFloor;

    public CarButtonEvent(RequestElevatorEvent elevatorEvent, int destinationFloor) throws InvalidDirectionException {
        super(elevatorEvent);

        if (elevatorEvent.isGoingUp() && destinationFloor < elevatorEvent.getFloor()) {
            throw new InvalidDirectionException("Going down to a higher floor");
        } else if (!elevatorEvent.isGoingUp() && destinationFloor > elevatorEvent.getFloor())  {
            throw new InvalidDirectionException("Going up to a lower floor");
        } else if (destinationFloor == elevatorEvent.getFloor()) {
            throw new InvalidDirectionException("Source and destination floor cannot be equal");
        }

        this.elevatorEvent = elevatorEvent;
        this.destinationFloor = destinationFloor;
    }

    public int getDestinationFloor() {
        return destinationFloor;
    }

    public RequestElevatorEvent getElevatorEvent() {
        return elevatorEvent;
    }
}
