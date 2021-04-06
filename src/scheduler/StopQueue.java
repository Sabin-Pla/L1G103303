package scheduler;

import java.util.ArrayList;

public class StopQueue {

    private ArrayList<Stop> stops;
    private boolean goingUp;
    private final int STOP_TIME_WRT_FLOOR_MOVE = 2; // every stop in the queue adds this many stops worth of time

    public StopQueue() {
        stops = new ArrayList<>();
    }

    public StopQueue(StopQueue sq) {
        this.stops = (ArrayList<Stop>) sq.stops.clone();
        this.goingUp = sq.goingUp;
    }

    public Integer peekNext() {
        if (stops.size() == 0) return null;
        return stops.get(0).floor;
    }

    public Integer pollNext() {
        if (stops.size() == 0) return null;
        return stops.remove(0).floor;
    }

    public int[] floorsToNext(int floor) {
        int index = stops.indexOf(new Stop(floor));
        return stops.get(index).floorsToNext();
    }


    class Stop {
        int floor;

        public Stop(int floor) {
            this.floor = floor;
        }

        public boolean isBetween(Stop s1, Stop s2) {
            return (floor >= s1.floor && floor <= s2.floor) || (floor >= s2.floor && floor  <= s1.floor);
        }

        public boolean equals(Object o) {
            return ((Stop) o).floor == floor;
        }

        /**
         * returns an array containing all the floors between this (floor and the floor of the next stop]
         * @return
         */
        public int[] floorsToNext() {
            int index = stops.indexOf(this);
            if (index == stops.size() - 1) {
                return null;
            }
            int numFloors = floor - stops.get(index + 1).floor;

            int intersectionFloors[];
            if (numFloors > 0) {
                intersectionFloors = new int[numFloors];
                for (int i = 1; i <= numFloors; i ++) {
                    intersectionFloors[i - 1] = floor - i;
                }
            } else {
                numFloors *= -1;
                intersectionFloors = new int[numFloors];
                for (int i = 1; i <= numFloors; i ++) {
                    intersectionFloors[i - 1] = floor + i;
                }
            }
            return intersectionFloors;
        }
    }

    public int calculateStopTime(int floorNumber, int currentFloor) {
        StopQueue cloneQueue = new StopQueue(this);
        addStop(floorNumber, currentFloor, cloneQueue);
        int stopTime = Math.abs(floorNumber -  currentFloor);
        for (Stop s : cloneQueue.stops) {
            if (s.floor == floorNumber) break;
            stopTime += STOP_TIME_WRT_FLOOR_MOVE + s.floorsToNext().length;
        }
        return stopTime;
    }

    public void addStop(int floorNumber, int currentFloor) {
        addStop(floorNumber, currentFloor, this);

    }
    /**
     * At a departure stop (no corresponding FBPE)
     *
     * @param floorNumber the floor number to stop at
     */
    private void addStop(int floorNumber, int currentFloor, StopQueue sq) {
        ArrayList<Stop> stops = sq.stops;
        Stop s = new Stop(floorNumber);
        // first ensure the stop isn't already in the queue
        if (stops.indexOf(s) > -1) return;

        Stop currentStop = new Stop(currentFloor);
        if (stops.size() == 1) {
            Stop nextStop = stops.get(0);
            if (s.isBetween(currentStop, nextStop)) {
                stops.add(0, s);
            } else if (currentStop.isBetween(s, nextStop)) {
                stops.add(s);
            } else if (nextStop.isBetween(s, currentStop)) {
                stops.add(s);
            }
            return;
        }

        // loop needs at least 2 elements in stop queue to work
        for (int i = 0; i < stops.size(); i++) {
            Stop s2 = stops.get(i);
            if (i != stops.size() - 1) {
                Stop s3 = stops.get(i + 1);
                if (s.isBetween(s2, s3)) {
                    stops.add(i + 1, s);
                    return;
                } else if (s2.isBetween(s, s3) && !(currentStop.isBetween(s, s2))) {
                    stops.add(i, s);
                    return;
                }
            } else {
                Stop secondLast = stops.get(i - 1);
                Stop last = s2;
                if (secondLast.isBetween(s, last)) {
                    stops.add(s);
                } else if (last.isBetween(s, secondLast)) {
                    stops.add(s);
                } else {
                    stops.add(i - 1, s);
                }
                return;
            }

        }
        // no return by this line -> queue is empty or the given floor should be the last stop
        stops.add(new Stop(floorNumber));
    }
}
