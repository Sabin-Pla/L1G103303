package tests;

import org.junit.Before;
import org.junit.Test;
import scheduler.StopQueue;

import java.util.Arrays;

public class StopQueueTest {

    private StopQueue sq;

    @Before
    public void StopQueueTest() {
        sq = new StopQueue();
        assert (sq != null);
    }

    @Test
    public void addTest() {
        // ensure no stops have been added yet
        assert (sq.pollNext() == null );

        // ensure adding 1 stop works and polling on an empty queue returns null
        sq.addStop(10, 1);
        assert ( sq.pollNext() == 10);
        assert ( sq.pollNext() == null);

        // ensure the stop between this floor and floor 10 is added before floor 10
        sq.addStop(10, 1);
        sq.addStop(5, 1);
        assert ( sq.pollNext() == 5);
        assert ( sq.pollNext() == 10);
        assert ( sq.pollNext() == null);

        // ensure the stop after this floor and floor 10 is added after floor 10
        sq.addStop(10, 6);
        sq.addStop(5, 6);
        assert ( sq.pollNext() == 10);
        assert ( sq.pollNext() == 5);
        assert ( sq.pollNext() == null);

        // ensure adding a stop between stops functions as intended
        sq.addStop(10, 1);
        sq.addStop(5, 2);
        sq.addStop(7, 3);
        sq.addStop(8, 6);
        sq.addStop(1, 2);
        sq.addStop(2, 2);
        assert ( sq.pollNext() == 5);
        assert ( sq.pollNext() == 7);
        assert ( sq.pollNext() == 8);
        assert ( sq.pollNext() == 10);
        assert ( sq.pollNext() == 2);
        assert ( sq.pollNext() == 1);
        assert ( sq.pollNext() == null);

        // ensure adding a stop between stops functions as intended
        sq.addStop(2, 8);
        sq.addStop(5, 8);
        sq.addStop(3, 8);
        sq.addStop(1, 8);
        sq.addStop(7, 8);
        sq.addStop(6, 8);
        assert ( sq.pollNext() == 7);
        assert ( sq.pollNext() == 6);
        assert ( sq.pollNext() == 5);
        assert ( sq.pollNext() == 3);
        assert ( sq.pollNext() == 2);
        assert ( sq.pollNext() == 1);
        assert ( sq.pollNext() == null); // sq must be empty before exiting method call
    }

    @Test
    public void floorToNextTest() {
        sq.addStop(5, 1);
        assert (sq.floorsToNext(sq.peekNext()) == null);
        sq.addStop(6, 1);
        assert (Arrays.equals(sq.floorsToNext(sq.peekNext()), new int[] {6}));
        sq.addStop(2, 4);
        sq.pollNext();
        assert (sq.peekNext() == 6);
        assert (Arrays.equals(sq.floorsToNext(sq.peekNext()), new int[] {5, 4, 3, 2}));
        while (sq.pollNext() != null); // clear stop queue
        sq.addStop(2, 1);
        sq.addStop(8, 1);
        assert (Arrays.equals(sq.floorsToNext(sq.peekNext()), new int[] {3, 4, 5, 6, 7, 8}));
        while (sq.pollNext() != null); // clear stop queue
    }

    @Test
    public void CalculateStopTimeTest() {
        int lastStopTime = sq.calculateStopTime(1, 1);
        assert (lastStopTime == 0); // ensure the stop time is 0 if the elevator is already at the floor
        for (int i = 2; i < 10; i++) {
            int stopTime = sq.calculateStopTime(i, 1);
            assert (stopTime > lastStopTime);
            lastStopTime = stopTime;
        }
    }
}
