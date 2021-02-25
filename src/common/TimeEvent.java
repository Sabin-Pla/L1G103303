package common;

public class TimeEvent implements Comparable {

    protected static Time time;
    private long eventTime;
    private static long PAST_EVENT_LENIENCY = 4000; // any events younger than this many MS are not in the past

    public TimeEvent(long eventTime) {
        this.eventTime = eventTime;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public long getEventTime() {
        return eventTime;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof TimeEvent) {
            long difference = eventTime - ((TimeEvent) o).getEventTime();
            if (difference > 0) {
                return 1;
            } else if (difference < 0) {
                return - 1;
            } else {
                return 0;
            }
        } else {
            throw new IllegalArgumentException("Object cannot be compared against a non-TimeEvent object");
        }
    }

    public boolean hasPassed() {
        return eventTime + PAST_EVENT_LENIENCY < time.now();
    }
}
