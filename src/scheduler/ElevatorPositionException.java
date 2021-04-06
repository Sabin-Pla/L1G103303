package scheduler;

/**
 * Exception class for the reported elevator floor not matching up with where the scheduler expects it to be
 *
 */
public class ElevatorPositionException extends Exception {

    public enum Type {NOT_STOPPED, PATH_MISMATCH, WRONG_ARRIVAL_FLOOR};
    private Type type;
    private int elevator;

    /**
     * Create new elevator position exception
     *
     * @param message message to send
     */
    public ElevatorPositionException(String message, Type type, int elevator) {
        super(message);
        this.type = type;
        this.elevator = elevator;
    }

    public Type getType() {
        return  type;
    }

    public int getElevator() {
        return elevator;
    }
}

