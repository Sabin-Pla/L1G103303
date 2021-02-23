package events;

public class ElevatorEvent extends RequestEvent {
	private int currentFloor;
	private int destinationFloor;
	
	public ElevatorEvent(int currentFloor, int destinationFloor) {
		this.currentFloor = currentFloor;
		this.destinationFloor = destinationFloor;
	}
	
	public int getCurrentFloor() {
		return currentFloor;
	}
	
	public int getDestinationFloor() {
		return destinationFloor;
	}
	
	public String toString() {
		return "Go to floor" + destinationFloor + " from floor" + currentFloor;
	}
}
