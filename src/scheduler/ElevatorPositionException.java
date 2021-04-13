package scheduler;

import java.io.Serializable;

/**
 * Exception class for the reported elevator floor not matching up with where the scheduler expects it to be
 *
 */
public class ElevatorPositionException extends Exception implements Serializable {

    public enum Type {NOT_STOPPED, PATH_MISMATCH, WRONG_ARRIVAL_FLOOR};
    private Type type;
    private int elevatorNumber;

    /**
     * Create new elevator position exception
     *
     * @param message message to send
     */
    public ElevatorPositionException(String message, Type type, int elevatorNumber) {
        super(message);
        this.type = type;
        this.elevatorNumber = elevatorNumber;
    }

    public Type getType() {
        return  type;
    }

    public int getElevator() {
        return elevatorNumber;
    }
    
    public String toString() {
    	return "Error type: " + type + ", Elevator " + elevatorNumber;
    }
}

