package floor;

import events.FloorEvent;

public class RequestPairing {
	private FloorEvent floorRequest;
	private int destinationFloor;
	
	public RequestPairing(FloorEvent floorRequest, int destinationFloor) {
		this.floorRequest = floorRequest;
		this.destinationFloor = destinationFloor;
	}
	
	public FloorEvent getFloorRequest() {
		return floorRequest;
	}
	
	public int getDestinationFloor() {
		return destinationFloor;
	}
}
