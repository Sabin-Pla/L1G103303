package events;

import java.time.Instant;

/**
 * This class for the events which floor passes to scheduler
 * (for the 1st iteration these events must be passed back to floor)
 * 
 * @author Aayush Mallya, Sabin Plaiasu
 * @version Iteration 1
 */
public class FloorEvent extends RequestEvent {
	private long requestTime; //request epoch time (ms)
	private int sourceFloor;  //the floor from which the event is sent
	private boolean goingUp;  //true if direction is up, false if down
	
	/**
	 * Constructor that directly initializes variables with arguments
	 * 
	 * @param requestTime is the time of the request
	 * @param currentFloor is the floor the request is sent from
	 * @param goingUp is true if direction is up and false if down
	 * @param destinationFloor is the floor the user is going
	 */
	public FloorEvent(String requestTime, int sourceFloor, boolean goingUp) {
		// for now assume all requests occur on the date 2020-12-03
		Instant instant = Instant.parse("2020-12-03T" + requestTime);
		this.requestTime = instant.now().toEpochMilli();
		this.sourceFloor = sourceFloor;
		this.goingUp = goingUp;
	}
	
	public long getRequestTime() {
		return requestTime;
	}
	public int getSourceFloor() {
		return sourceFloor;
	}
	public boolean getGoingUp() {
		return goingUp;
	}
	
	public String toString() {
		return "[" + requestTime + "] move " + (goingUp ? "up" : "down") + 
				" from floor " + sourceFloor; 
	}
}
