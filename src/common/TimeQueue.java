package common;

import java.util.concurrent.PriorityBlockingQueue;

public class TimeQueue extends PriorityBlockingQueue {

    private void validateEvent(Object o) throws Exception {
        if (!(o instanceof TimeEvent)) {
            throw new IllegalArgumentException("Objects inserted in TimeQueue must be TimeEvent objects");
        } else if (((TimeEvent) o).hasPassed() ) {
            throw new TimeException("Past events may not be inserted into TimeQueue");
        }
    }

    /**
     * Calculates the amount of real-world time in MS that it will take for the next event to occur. This amount
     * may be off by several milliseconds due to conversions.
     *
     * @return the amount of time before the next event according to events time object
     */
    public long waitTime() {
        Time time = peekEvent().getTime();
        long realEventTime = peekEvent().getEventTime(); // the 'real world' time the next event will occur
        return (long) ((realEventTime - time.now()) / (time.getCompressionFactor()));
    }

    @Override
    public boolean add(Object o) {
        try {
            validateEvent(o);
            return super.add(o);
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public boolean offer(Object o) {
        try {
            validateEvent(o);
            return super.offer(o);
        } catch (Exception ignored) {
            return false;
        }
    }

    public TimeEvent peekEvent() {
        return (TimeEvent) this.peek();
    }
}
