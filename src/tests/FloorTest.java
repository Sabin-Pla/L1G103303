package tests;

import common.*;

import floor.Floor;
import floor.Lamp;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class FloorTest {

    final int NUM_EVENTS = 4;

    @Test
    public void FloorThreadTest() {
        URL resource = getClass().getResource("integratedTestEvents.txt");
        File f =  new File(resource.getFile());
        assert (f != null);
        ArrayList<RequestElevatorEvent> events = Parser.getRequestFromFile(f);

        assert (events.size() == NUM_EVENTS);

        long simulationStart = events.get(0).getEventTime();
        /*
            The simulation takes 3 minutes to complete,
            but we want it to be done in 3 seconds.
         */
        Time time = new Time(Time.SECOND_TO_MINUTE, simulationStart - 500);
        events.get(0).setTime(time);

        ArrayList<Thread> floorThreads = new ArrayList<>();
        ArrayList<Floor> floors = new ArrayList<>();
        for (int i=1; i < 6; i++) {
            TimeQueue queueFloor = new TimeQueue();
            for (RequestElevatorEvent event : events) {
                if (event.getFloor() == i) queueFloor.add(event);
            }

            Floor floor = new Floor(i, queueFloor, new Lamp(false));

            floors.add(floor);
            floorThreads.add(new Thread(floor));
        }

        time.restart();
        for (Thread thread : floorThreads) thread.start();
    }
}

