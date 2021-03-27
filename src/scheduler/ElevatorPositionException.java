package scheduler;

/**
 * Exception class for the reported elevator floor not matching up with where the scheduler expects it to be
 *
 */
public class ElevatorPositionException extends Exception {

    public enum Type {NOT_STOPPED, PATH_MISMATCH, WRONG_ARRIVAL_FLOOR};
    Type type;
    /**
     * Create new elevator position exception
     *
     * @param message message to send
     */
    public ElevatorPositionException(String message, Type type) {
        super(message);
        this.type = type;
    }
}

