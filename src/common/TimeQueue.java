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
