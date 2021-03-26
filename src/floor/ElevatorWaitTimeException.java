package floor;

/**
 * Exception class for there being too long a wait time between the time an elevator is requested and arrives
 *
 */
public class ElevatorWaitTimeException extends Exception {

    /**
     * Create new wait time exception
     *
     * @param message message to send
     */
    public ElevatorWaitTimeException(String message) {
        super(message);
    }
}
