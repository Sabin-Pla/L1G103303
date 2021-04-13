package common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.Instant;

import floor.TimeEventListener;
import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.ElevatorMotorEvent;

public class TimeEvent implements Comparable, java.io.Serializable {

    private Instant eventInstant;
    private static DatagramSocket sendSocket;
    public static final long PAST_EVENT_LENIENCY = 5000; // any events younger than this many MS are not in the past

    /**
     * Creates an object for an event that happens at a certain time
     *
     * @param eventTime epoch ms time at which event occurs or will occur
     */
    public TimeEvent(Instant eventTime) {
        this.eventInstant = eventTime;
        if (sendSocket == null) {
 	        try {
 				sendSocket = new DatagramSocket();
 			} catch (SocketException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         }
    }

    public Instant getEventInstant() {
        return eventInstant;
    }
    
    public void forwardEventToListener(Integer header) {
    	ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
    	dataStream.write(header.byteValue());
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(dataStream);
			out.writeObject(this);
			byte[] data = dataStream.toByteArray();
			DatagramPacket packet = new DatagramPacket(data,
				data.length, InetAddress.getLocalHost(), TimeEventListener.LISTENER_RECEIVE_PORT);
			sendSocket.send(packet); // Send the packet
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Compares 2 event objects for which one is set to occur at a later date than the other
     *
     * @param o the TimeEvent object ot compare
     * @return 1 if the provided event is set to occur at an earlier date than this event. 0 if the events occur
     * simultaneously. Otherwise, 0.
     * @throws IllegalArgumentException if attempting to compare against a non-TimeEvent object
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof TimeEvent) {
            boolean after = eventInstant.plusMillis(PAST_EVENT_LENIENCY).isAfter(((TimeEvent) o).getEventInstant());
            if (after) {
                return 1;
            } else {
                return -1;
            }
        } else {
            throw new IllegalArgumentException("Object cannot be compared against a non-TimeEvent object");
        }
    }

    /**
     * determine whether or not the event has passed according to timescale provided by time object in setTime()
     *
     * @return true if the event occurrence time is more than PAST_EVENT_LENIENCY ms in the past
     */
    public boolean hasPassed(SimulationClock clock) {
        return eventInstant.isBefore(clock.instant().minusMillis(PAST_EVENT_LENIENCY));
    }
}
