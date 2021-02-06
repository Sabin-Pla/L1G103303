package floor;


/**
 * This class stores the requests from the request document
 * 
 * @author Aayush Mallya
 * @version Iteration 1
 */
public class DataStorage {
	private String requestTime; //request time
	private int currentFloor;
	private boolean goingUp; //true if direction is up, false if down
	private int destinationFloor;
	
	/**
	 * Constructor that directly initializes variables with arguments
	 * 
	 * @param requestTime is the time of the request
	 * @param currentFloor is the floor the request is sent from
	 * @param goingUp is true if direction is up and false if down
	 * @param destinationFloor is the floor the user is going
	 */
	public DataStorage(String requestTime, int currentFloor, boolean goingUp, int destinationFloor) {
		this.requestTime = requestTime;
		this.currentFloor = currentFloor;
		this.goingUp = goingUp;
		this.destinationFloor = destinationFloor;
	}
	
	/**
	 * Constructor with string as argument
	 * This string is split to initialize the corresponding variables 
	 * 
	 * @param s is the request string
	 */
	public DataStorage(String s) {
		String[] request = s.split(" ");
        requestTime = request[0];
        currentFloor = Integer.parseInt(request[1]);
        goingUp = request[2].contains("Up");
        destinationFloor = Integer.parseInt(request[3]);
	}
	
	/**
	 * Default constructor
	 */
	public DataStorage() {
        requestTime = "";
        currentFloor = 0;
        goingUp = false;
        destinationFloor = 0;
    }
	
			//GETTERS//
	public String getRequestTime() {
		return requestTime;
	}
	public int getCurrentFloor() {
		return currentFloor;
	}
	public boolean getGoingUp() {
		return goingUp;
	}
	public int getDestinationFloor() {
		return destinationFloor;
	}
	
	public String toString() {
		return "[" + requestTime + "] move " + (goingUp ? "up" : "down") + 
				" from floor " + currentFloor + " to " + destinationFloor; 
	}
}
