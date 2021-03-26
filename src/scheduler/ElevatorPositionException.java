package scheduler;

/**
 * Exception class for the reported elevator floor not matching up with where the scheduler expects it to be
 *
 */
public class ElevatorPositionException extends Exception {

    /**
     * Create new elevator position exception
     *
     * @param message message to send
     */
    public ElevatorPositionException(String message) {
        super(message);
    }
}

