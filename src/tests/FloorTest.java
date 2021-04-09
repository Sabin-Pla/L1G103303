package tests;

import common.Parser;
import common.SimulationClock;
import common.TimeEvent;
import common.TimeQueue;
import floor.ElevatorWaitTimeException;
import floor.Floor;
import org.junit.Before;
import org.junit.Test;
import remote_procedure_events.FloorArrivalEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.URL;

public class FloorTest {

    private Floor f;
    private Parser p;
    public static int floorNum = 5;
    private TimeQueue carButtonEventQueue;
    private static SimulationClock clock;
    private boolean floorLamps[] = new boolean[2];
    byte data[];
    DatagramPacket receivePacket;
    DatagramSocket receiveSocket;
    URL resource;
    File file;
    TimeEvent event;
    int lastElevator;

    @Before
    public void FloorTest() throws SocketException, FileNotFoundException {
        f = new Floor(floorNum);
        resource = getClass().getResource("parserTest.txt");
        file =  new File(resource.getFile());
        data = new byte[256];
        receivePacket = new DatagramPacket(data, data.length);
        receiveSocket = new DatagramSocket(FloorArrivalEvent.FLOOR_LISTEN_PORT);
    }

    @Test
    public void FloorCheck() throws SocketException, ElevatorWaitTimeException, FileNotFoundException {
        assert (f != null);

        //Make sure file is not empty and valid
        assert (file != null);

        //Make sure clock equals the parser clock time
        assert (clock == p.getClock());
        //Make sure packet is not empty/null
        assert (receivePacket != null);
        //Make sure packet is not empty/null
        f.setReceiveSocket(FloorArrivalEvent.FLOOR_LISTEN_PORT);
        assert (receiveSocket != null);
    }
}
