package tests;

import org.junit.Test;
import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.FloorArrivalEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static floor.Floor.receiveSocket;

public class ElevatorTest {

    public DatagramPacket dp;

    @Test
    private void forwardButtonPress() throws IOException, InterruptedException {
        DatagramSocket receiveSocket = new DatagramSocket(FloorArrivalEvent.FLOOR_LISTEN_PORT);
        DatagramSocket sendSocket = new DatagramSocket();
        DatagramPacket dp = null;
        Listener l = new Listener(dp);
        l.start();

        byte[] someData = new byte[256]; // make sure this is not all null

        //send someData to elevator on the port Elevator.setFloorSocketReceiver listens to (81 + 1024)
        dp = new DatagramPacket(someData,
                someData.length, InetAddress.getLocalHost(), CarButtonPressEvent.SCHEDULER_LISTEN_PORT);
        sendSocket.send(dp);

        synchronized (dp) {
            while (dp == null) {
                dp.wait();
            }
        }

        // we have a datagram packet returned by elevator
        assert (someData == dp.getData());
    }
}


class Listener extends Thread {

    private DatagramPacket dp;

    public Listener(DatagramPacket dp) {
        this.dp = dp;
    }

    @Override
    public void run() {
        synchronized (dp) {
            while (dp != null) {
                try {
                    dp.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            try {
                receiveSocket.receive(dp); // listen for the event which the elevator sends.
            } catch (IOException e) {
                e.printStackTrace();
            }
            dp.notifyAll();
        }
    }
}