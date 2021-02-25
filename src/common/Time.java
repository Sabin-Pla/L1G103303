package common;

import java.util.Date;

public class Time {
    private double compressionFactor;
    private long actualStartTime;
    private long startTime;

    public static final double SECOND_TO_MINUTE = 60;

    public Time(double compressionFactor, long startTime) {
        this.compressionFactor = compressionFactor;
        this.startTime = startTime;
        restart();
    }

    public void restart() {
        actualStartTime = System.currentTimeMillis();
    }

    public long now() {
        long actualDifference = System.currentTimeMillis() - actualStartTime;
        return (long) (actualDifference * compressionFactor) + startTime;
    }
}
