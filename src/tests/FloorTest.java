package tests;

import actor_events.CarButtonEvent;
import actor_events.RequestElevatorEvent;
import common.*;

import floor.ElevatorWaitTimeException;
import floor.Floor;
import org.junit.Before;
import org.junit.Test;
import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.FloorArrivalEvent;
import remote_procedure_events.FloorButtonPressEvent;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.ArrayList;

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
        p = new Parser(new File("requestsFile.txt"));
        resource = getClass().getResource("parserTest.txt");
        file =  new File(resource.getFile());
        data = new byte[256];
        receivePacket = new DatagramPacket(data, data.length);
        receiveSocket = new DatagramSocket(FloorArrivalEvent.FLOOR_LISTEN_PORT);
    }

    @Test
    public void FloorCheck() throws SocketException, ElevatorWaitTimeException, FileNotFoundException {
        //Check if file is valid
        assert (p != null);

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

    /**
     * This test method is incorrect since I was unable to send an event through this test class...
     * Will check back later.
     */
    @Test
    public void sendEvent(){
        assert ((event instanceof CarButtonEvent) || (event instanceof RequestElevatorEvent));
    }
}
