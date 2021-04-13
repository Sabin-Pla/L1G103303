package floor;

public class GuiDemo extends TimeEventListener {

	@Override
	public void floorButtonPressed(int sourceFloor, boolean up) {
		System.out.println("Floor " + sourceFloor + " button pressed, up: " + up);
	}

	@Override
	public void lampOff(int floorNumber) {
		System.out.println("Floor " + floorNumber + " lamp off");
	}

	@Override
	public void lampOn(int floorNumber) {
		System.out.println("Floor " + floorNumber + " lamp on");
		
	}

	@Override
	public void elevatorArrived(int elevatorNumber, int floor, boolean doorsClosed) {
		System.out.println("Elevator " + elevatorNumber + " now at floor " + floor + ", Doors closed? " + doorsClosed);
	}

	@Override
	public void carButtonPressed(int elevatorNumber, int floorNumber) {
		System.out.println("Car button " + floorNumber + " pressed for elevator " + elevatorNumber);
		
	}

	@Override
	public void simulationEnd() {
		System.out.println("The simulation is now over.");
	}

	@Override
	protected void elevatorDeparted(int elevatorNumber, int floor) {
		System.out.println("Elevator " + elevatorNumber + " departed floor " + floor);
	}

	@Override
	public void simulatedErrorOccurred(String errorMessage) {
		System.out.println(errorMessage);
	}

}
