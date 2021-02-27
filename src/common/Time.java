package common;

/**
 *  A class that provides a definition for the current epoch time on its own distorted timescale
 *
 *  @author Sabin Plaiasu
 *  @version Iteration 2
 */
public class Time {
    private double compressionFactor;
    private long actualStartTime;
    private long startTime;

    public static final double SECOND_TO_MINUTE = 60;

    /**
     * Creates a time object with a timescale of compressionFactor
     *
     * @param compressionFactor the multiplicative amount by which time should be distorted
     * @param startTime the time the simulation should start
     */
    public Time(double compressionFactor, long startTime) {
        this.compressionFactor = compressionFactor;
        this.startTime = startTime;
        restart();
    }

    /**
     * Sets the current real-world time to the simulation start time
     */
    public void restart() {
        actualStartTime = System.currentTimeMillis();
    }

    /**
     * Calculates the amount of time that would have passed between now and the last time this object was reset() if
     * a second passed by at a rate of compressionFactor seconds
     *
     * @return the current time according to distorted timescale
     */
    public long now() {
        long actualDifference = System.currentTimeMillis() - actualStartTime;
        return (long) (actualDifference * compressionFactor) + startTime;
    }

    /**
     * Returns the compression factor of the time object
     *
     * @return the multiplicative amount by which time should be distorted
     */
    public double getCompressionFactor() {
        return this.compressionFactor;
    }
}
