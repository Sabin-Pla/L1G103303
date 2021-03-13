package tests;

import org.junit.Test;

import common.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;


public class ParserTest {

    static final int NUM_lINES = 10;

    @Test
    public void parseFile() {
        URL resource = getClass().getResource("parserTest.txt");
        File f =  new File(resource.getFile());
        assert (f != null);

        ArrayList<RequestElevatorEvent> events = Parser.getRequestFromFile(f);
        assert (events != null);
        assert (events.size() == NUM_lINES);

        for (int i=1; i < NUM_lINES; i++) {
            TimeEvent event = events.get(i);
            assert (event.compareTo(events.get(i - 1)) >= 0 );
        }
    }
}