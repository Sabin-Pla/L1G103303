package floor;

import elevator.Elevator;

/**
 * Exception class for there being too long a wait time between the time an elevator is requested and arrives
 *
 */
public class ElevatorException extends Exception {

    /**
     * Create new wait time exception
     *
     * @param message message to send
     */
    public ElevatorException(String message) {
        super(message);
    }
}
