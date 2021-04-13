package floor;

import java.time.Instant;

import common.TimeEvent;

public class SimulationEndEvent extends TimeEvent {
	
	boolean isFloor;
	
	public SimulationEndEvent(Instant eventInstant, boolean isFloor) {
		super(eventInstant);
		this.isFloor = isFloor;
	}
	
	public boolean isFloor() {
		return isFloor;
	}
}
