package floor;

import java.time.Instant;

import common.TimeEvent;

public class FloorLampEvent extends TimeEvent {
	
	private int floor;
	private boolean on;
	
	public FloorLampEvent(Instant eventInstant, int floor, boolean on) {
		super(eventInstant);
		this.floor = floor;
		this.on = on;
	}

	public int getFloor() {
		return floor;
	}
	
	public boolean getOn() {
		return on;
	}
}
