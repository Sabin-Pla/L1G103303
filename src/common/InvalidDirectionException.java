package common;

/**
 * An exception for if "going down" to a higher floor or "going up" to a lower floor
 *
 * @author Sabin Plaiasu
 */
public class InvalidDirectionException extends Exception {

    /**
     * Create an InvalidDirectionException
     *
     * @param message the exception message
     */
    public InvalidDirectionException(String message) {
        super(message);
    }
}
