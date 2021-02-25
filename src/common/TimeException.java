package common;

/**
 * An exception for when time-based events are set to occur at invalid times (i.e, when attempting to place a TimeEvent
 * occurring in the past in a queue that only contains events which have not yet passed)
 *
 * @author Sabin Plaiasu
 */
public class TimeException extends Exception {

    /**
     * Create a TimeException
     *
     * @param message the exception message
     */
    public TimeException(String message) {
        super(message);
    }
}
