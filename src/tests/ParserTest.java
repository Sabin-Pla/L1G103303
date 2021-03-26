package tests;

import actor_events.RequestElevatorEvent;
import org.junit.Test;

import common.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;


public class ParserTest {

    static final int NUM_EVENTS = 10;

    @Test
    public void parseFile() throws FileNotFoundException, InvalidDirectionException {
        URL resource = getClass().getResource("parserTest.txt");
        File f =  new File(resource.getFile());
        assert (f != null);
        Parser p = new Parser(f);
        p.parseEvents();
        p.close();
        SimulationClock clock = p.getClock();
        clock.start();
        ArrayList<RequestElevatorEvent> events = p.getEvents();
        assert (events != null);
        assert (events.size() == NUM_EVENTS);
        for (int i=0; i < NUM_EVENTS; i++) assert (!events.get(i).hasPassed(clock));
    }
}