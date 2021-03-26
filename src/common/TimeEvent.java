package common;

import java.time.Instant;

public class TimeEvent implements Comparable, java.io.Serializable {

    private Instant eventInstant;
    public static final long PAST_EVENT_LENIENCY = 5000; // any events younger than this many MS are not in the past

    /**
     * Creates an object for an event that happens at a certain time
     *
     * @param eventTime epoch ms time at which event occurs or will occur
     */
    public TimeEvent(Instant eventTime) {
        this.eventInstant = eventTime;
    }

    public Instant getEventInstant() {
        return eventInstant;
    }

    /**
     * Compares 2 event objects for which one is set to occur at a later date than the other
     *
     * @param o the TimeEvent object ot compare
     * @return 1 if the provided event is set to occur at an earlier date than this event. 0 if the events occur
     * simultaneously. Otherwise, 0.
     * @throws IllegalArgumentException if attempting to compare against a non-TimeEvent object
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof TimeEvent) {
            boolean after = eventInstant.minusMillis(PAST_EVENT_LENIENCY).isAfter(((TimeEvent) o).getEventInstant());
            if (after) {
                return 1;
            } else {
                return -1;
            }
        } else {
            throw new IllegalArgumentException("Object cannot be compared against a non-TimeEvent object");
        }
    }

    /**
     * determine whether or not the event has passed according to timescale provided by time object in setTime()
     *
     * @return true if the event occurrence time is more than PAST_EVENT_LENIENCY ms in the past
     */
    public boolean hasPassed(SimulationClock clock) {
        return eventInstant.isBefore(clock.instant().plusMillis(PAST_EVENT_LENIENCY));
    }
}
