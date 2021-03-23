package floor;

import actor_events.CarButtonEvent;
import actor_events.RequestElevatorEvent;
import common.*;

import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.FloorArrivalEvent;
import remote_procedure_events.FloorButtonPressEvent;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.Duration;
import java.util.ArrayList;

/**
 * The floor class sends requests to the schedulers and operates its floor lamp.
 *
 * A Floor thread must be notified every time the elevator comes and leaves at its floor
 * 
 * @author Sabin Plaiasu
 * @version Iteration 3
 */
public class Floor extends Thread {

	public static final long MAXIMUM_WAIT_TIME = 120000; // max amount of time (ms) a thread should wait for an elevator
	public static final int NUM_FLOORS = 10;
	public static final int NUM_ELEVATORS = 2;
	public static final String REQUEST_FILE = "src/requestsFile.txt";
	
	private final int floorNumber;
	private static SimulationClock clock;
	private TimeQueue eventQueue;
	private TimeQueue carButtonEvents;
	private boolean floorLamps[];
	private DatagramSocket sendSocket;
	private DatagramSocket receiveSocket;
	private int elevatorFloors[];

	/**
	 * Constructor initializes all variables
	 */
	public Floor(int floorNumber, TimeQueue eventQueue) throws SocketException {
		this.floorNumber = floorNumber;
		this.eventQueue = eventQueue;
		this.floorLamps = new boolean[2];
		this.carButtonEvents = new TimeQueue();
		this.sendSocket = new DatagramSocket();
		this.receiveSocket = new DatagramSocket(FloorArrivalEvent.FLOOR_LISTEN_PORT);
		this.elevatorFloors = new int[NUM_ELEVATORS];
		for (int i=0; i < NUM_ELEVATORS; i++) {
			elevatorFloors[i] = 1;  // assume all elevators are at floor 1 to start
		}
		setName("Floor " + floorNumber);

	}

	/**
	 * Runs the floor thread
	 */
	@Override
	public void run() {
		while (true) {
			try {
				while (!eventQueue.isEmpty()) {
					while (!eventQueue.peekEvent().hasPassed(clock)) {
						waitOnElevator((int) eventQueue.calculateWaitTime());
					}
					TimeEvent nextEvent = eventQueue.nextEvent();
					System.out.println("\nFloor:\nSending event to scheduler from floor " + floorNumber);
					System.out.println(nextEvent);

					sendEvent(nextEvent, 0);
					carButtonEvents.add(((RequestElevatorEvent) nextEvent).getCarButtonEvent());
				}
				waitOnElevator(0);
			} catch (ElevatorWaitTimeException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public void waitOnElevator(int timeout) throws ElevatorWaitTimeException {
		byte data[] = new byte[256];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		try {
			receiveSocket.setSoTimeout(timeout);
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ByteArrayInputStream bainStream = new ByteArrayInputStream(receivePacket.getData());
		FloorArrivalEvent arrivalEvent = null;

		try {
			ObjectInputStream oinStream = new ObjectInputStream(bainStream);
			arrivalEvent = (FloorArrivalEvent) oinStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		elevatorMoved(arrivalEvent.getElevatorNumber(), arrivalEvent.getArrivalFloor());
	}

	/**
	 * Performs the actions needed once an elevator arrives at this floor
	 *
	 * Turn on floor lamp and allow passengers to board if the elevator is stopped
	 *
	 * If there are car button events, dispatch those to the elevator/scheduler.
	 *
	 */
	public void elevatorMoved(int elevatorNumber, int floor) throws ElevatorWaitTimeException {
		if (floor == floorNumber) {
				floorLamps[elevatorNumber] = true;
				System.out.println("Floor " + floorNumber + " turning Lamp on");
				if (!carButtonEvents.isEmpty()) {
					CarButtonEvent event = (CarButtonEvent) carButtonEvents.nextEvent();
					Duration waitTime = Duration.between(event.getEventInstant(), clock.instant());
					if (waitTime.toMillis() > MAXIMUM_WAIT_TIME) {
						throw new ElevatorWaitTimeException("Passenger waited more than " + MAXIMUM_WAIT_TIME + " ms");
					}
					System.out.println("\nFloor: Sending car button press event to elevator. To " +
							event.getDestinationFloor());
					sendEvent(event, elevatorNumber);
				}
		} else if (floorLamps[elevatorNumber]){
			System.out.println("Floor " + floorNumber + "turning lamp off");
			floorLamps[elevatorNumber] = false;
		}
		elevatorFloors[elevatorNumber] = floor;
	}


	private void sendEvent(TimeEvent event, int lastElevator) {
		try {
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(dataStream);

			if (event instanceof CarButtonEvent) {
				CarButtonPressEvent cbpe = new CarButtonPressEvent(event.getEventInstant(),
						elevatorFloors[lastElevator],
						lastElevator,
						((CarButtonEvent) event).getDestinationFloor());

				out.writeObject(cbpe);
				byte[] data = dataStream.toByteArray();
				DatagramPacket sendPacket = new DatagramPacket(data,
						data.length, InetAddress.getLocalHost(), CarButtonPressEvent.ELEVATOR_LISTEN_PORT);
				sendSocket.send(sendPacket);
			} else if (event instanceof FloorButtonPressEvent) {
				out.writeObject(event);
				byte[] data = dataStream.toByteArray();
				DatagramPacket sendPacket = new DatagramPacket(data,
						data.length, InetAddress.getLocalHost(), FloorButtonPressEvent.SCHEDULER_LISTEN_PORT);
				sendSocket.send(sendPacket);
			}

			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) throws SocketException, FileNotFoundException, InvalidDirectionException {
		File requestFile = new File(REQUEST_FILE);
		Parser p = new Parser(requestFile);
		p.parseEvents();
		clock = p.getClock();
		ArrayList<RequestElevatorEvent> events = p.getEvents();
		p.close();

		System.out.println("Events to be sent: ");
		for (RequestElevatorEvent event : events) {
			System.out.println(event);
		}

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

			floorEvents.setClock(clock);
			Floor floor = new Floor(i+1, floorEvents);
			floors[i] = floor;
		}

		clock.start();
		for (Floor f: floors) {
			f.start();
		}
	}
	
}
