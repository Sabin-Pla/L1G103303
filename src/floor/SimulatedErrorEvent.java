package floor;

import java.time.Instant;

import common.TimeEvent;
import scheduler.ElevatorPositionException;

public class SimulatedErrorEvent extends TimeEvent {
	
	private String exceptionMessage;
	
	public SimulatedErrorEvent(Instant eventInstant, String exceptionMessage) {
		super(eventInstant);
		this.exceptionMessage = exceptionMessage;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}
}
