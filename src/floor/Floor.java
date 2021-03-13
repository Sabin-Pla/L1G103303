package floor;

import common.*;
import elevator.Elevator;

import elevator.Sensor;
import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.ElevatorFloorArrival;
import remote_procedure_events.FloorButtonPressEvent;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * The floor class sends requests to the schedulers and operates its floor lamp.
 *
 * Modelled by a state machine with 2 states:
 *  OPERATING_LAMP and SENDING_EVENTS
 *
 * A Floor thread must be notified every time the elevator comes and leaves at its floor
 * 
 * @author Sabin Plaiasu
 * @version Iteration 2
 */
public class Floor extends Thread {

	// the minimum amount of time between which a floor thread should check to see if it should send events
	private final long MINIMUM_WAIT_TIME = 5;
	private final long MAXIMUM_WAIT_TIME = 120000; // max amount of time (ms) a thread should wait for an elevator
	private static final int NUM_FLOORS = 10;
	private static final String REQUEST_FILE = "src/requestsFile.txt";
	
	private int floorNumber;
	private TimeQueue eventQueue;
	private TimeQueue carButtonEvents;
	private boolean floorLamps[];
	private DatagramSocket sendSocket;
	private DatagramSocket receiveSocket;

	/**
	 * Constructor initializes all variables
	 */
	public Floor(int floorNumber, TimeQueue eventQueue) throws SocketException {
		this.floorNumber = floorNumber;
		this.eventQueue = eventQueue;
		this.floorLamps = new boolean[2];
		this.carButtonEvents = new TimeQueue();
		this.sendSocket = new DatagramSocket();
		this.receiveSocket = new DatagramSocket(ElevatorFloorArrival.FLOOR_LISTEN_PORT);
		setName("Floor " + floorNumber);

	}

	/**
	 * Returns the event queue
	 *
	 * @return the event queue
	 */
	public TimeQueue getEventQueue() {
		return eventQueue;
	}

	/**
	 * Gets the number of the floor
	 *
	 * @return the floor number
	 */
	public int getFloorNumber() {
		return floorNumber;
	}

	/**
	 * Runs the floor thread
	 */
	@Override
	public void run() {
		while (true) {
			while (!eventQueue.isEmpty()) {
				while (!eventQueue.peekEvent().hasPassed()) {
					eventQueue.peekEvent().getEventTime();
					long waitTime = eventQueue.waitTime();
					if (waitTime <= MINIMUM_WAIT_TIME * eventQueue.peekEvent().getTime().getCompressionFactor()) {
						break;
					} else {
						waitOnElevator((int) waitTime);
					}
				}
				TimeEvent nextEvent = eventQueue.nextEvent();
				System.out.println("\nFloor:\nSending event to scheduler from floor " + floorNumber);
				System.out.println(nextEvent);

				sendEvent(nextEvent);
				carButtonEvents.add(((RequestElevatorEvent) nextEvent).getCarButtonEvent());
			}
			waitOnElevator(0);
		}
	}

	public void waitOnElevator(int timeout) {
		byte data[] = new byte[256];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);;
		try {
			receiveSocket.setSoTimeout(timeout);
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ByteArrayInputStream bainStream = new ByteArrayInputStream(receivePacket.getData());
		ElevatorFloorArrival arrivalEvent = null;

		try {
			ObjectInputStream oinStream = new ObjectInputStream(bainStream);
			arrivalEvent = (ElevatorFloorArrival) oinStream.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			if (arrivalEvent.getArrivalFloor() == floorNumber) {
					elevatorMoved(arrivalEvent.getElevatorNumber(), true);

			} else {
				elevatorMoved(arrivalEvent.getElevatorNumber(), false);
			}
		} catch (ElevatorException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs the actions needed once an elevator arrives at this floor
	 *
	 * Turn on floor lamp and allow passengers to board if the elevator is stopped
	 *
	 * If there are car button events, dispatch those to the elevator/scheduler.
	 *
	 */
	public void elevatorMoved(int elevatorNumber, boolean arrived) throws ElevatorException {
		if (arrived) {
			floorLamps[elevatorNumber] = true;
			System.out.println("\n Floor: Lamp on");
			if (!carButtonEvents.isEmpty()) {
				CarButtonEvent event = (CarButtonEvent) carButtonEvents.nextEvent();
				if (event.getEventTime() - event.getTime().now() > MAXIMUM_WAIT_TIME) {
					throw new ElevatorException("Passenger waited more than " + MAXIMUM_WAIT_TIME + " ms");
 				}
				System.out.println("\nFloor: Sending car button press event to elevator. To " +
						event.getDestinationFloor());
				sendEvent(event);
			}
		} else {
			floorLamps[elevatorNumber] = false;
		}
	}


	private void sendEvent(TimeEvent event) {
		try {
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(dataStream);
			out.writeObject(event);
			out.close();

			byte[] data = dataStream.toByteArray();

			if (event instanceof CarButtonEvent) {
				DatagramPacket sendPacket = new DatagramPacket(data,
						data.length, InetAddress.getLocalHost(), CarButtonPressEvent.SCHEDULER_LISTEN_PORT);
				sendSocket.send(sendPacket);
			} else if (event instanceof FloorButtonPressEvent) {
				DatagramPacket sendPacket = new DatagramPacket(data,
						data.length, InetAddress.getLocalHost(), FloorButtonPressEvent.SCHEDULER_LISTEN_PORT);
				sendSocket.send(sendPacket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws SocketException {
		File requestFile = new File(REQUEST_FILE);
		ArrayList<RequestElevatorEvent> events = Parser.getRequestFromFile(requestFile);

		System.out.println("Events to be sent: ");
		for (RequestElevatorEvent event : events) {
			System.out.println(event);
		}

		Time time = new Time(
				Time.SECOND_TO_MINUTE / 2,
				events.get(0).getEventTime());

		events.get(0).setTime(time);

		Floor[] floors = new Floor[NUM_FLOORS];

		for (int i=0; i < NUM_FLOORS; i++) {
			TimeQueue floorEvents = new TimeQueue();
			for (RequestElevatorEvent event : events) {
				if (event.getFloor() == i + 1)  {
					if (!floorEvents.add(event)) {
						System.out.println("error, simulation with expired time added");
						return;
					}
				}
			}

			Floor floor = new Floor(i+1, floorEvents);
			floors[i] = floor;
			floor.start();
		}
	}
	
}
