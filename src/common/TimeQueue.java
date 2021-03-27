package common;

import java.time.Duration;
import java.time.Instant;
import java.util.PriorityQueue;

public class TimeQueue extends PriorityQueue {

    private static SimulationClock clock;

    private void validateEvent(Object o) throws Exception {
        if (!(o instanceof TimeEvent)) {
            throw new IllegalArgumentException("Objects inserted in TimeQueue must be TimeEvent objects");
        } else if (((TimeEvent) o).hasPassed(clock) ) {
            throw new TimeException("Past events may not be inserted into TimeQueue");
        }
    }

    public void setClock(SimulationClock clock) {
        this.clock = clock;
    }

    /**
     * Calculates the amount of real-world time in MS that it will take for the next event to occur. This amount
     * may be off by several milliseconds due to losing nanosecond precision.
     *
     * @return the amount of time in miliseconds before the next event according to events time object
     */
    public Duration calculateWaitTime() {
        Instant now = clock.instant();
        Instant nextEventInstant = peekEvent().getEventInstant();
        Duration d = Duration.between(now, nextEventInstant);
        return d.dividedBy(clock.getCompressionFactor());
    }

    @Override
    public boolean add(Object o) {
        try {
            validateEvent(o);
            return super.add(o);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return false;
    }

    public void addNoValidate(Object o) {
        super.offer(o);
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

    public TimeEvent nextEvent() {
        return (TimeEvent) this.poll();
    }

    public TimeEvent peekEvent() {
        return (TimeEvent) this.peek();
    }
}
