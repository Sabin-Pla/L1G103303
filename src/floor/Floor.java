package floor;

import actor_events.CarButtonEvent;
import actor_events.RequestElevatorEvent;
import common.*;

import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.FloorArrivalEvent;
import remote_procedure_events.FloorButtonPressEvent;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

/**
 * The floor class sends requests to the schedulers and operates its floor lamp.
 *
 * A Floor thread must be notified every time the elevator comes and leaves at its floor
 * 
 * @author Sabin Plaiasu
 * @version Iteration 5
 */
public class Floor {

	public static final long MAXIMUM_WAIT_TIME = 1200000; // max amount of time (ms) a thread should wait for an elevator
	public static final int NUM_FLOORS = 10;
	public static final int NUM_ELEVATORS = 2;
	public static final String REQUEST_FILE = "requestsFile.txt";
	
	private final int floorNumber;
	private static SimulationClock clock;
	private static TimeQueue actorEventQueue;
	private TimeQueue carButtonEventQueue;
	private boolean floorLamps[];
	private DatagramSocket sendSocket;
	private static DatagramSocket receiveSocket;
	private static int elevatorFloors[];
	private static Floor[] floors;
	private static int lastArrivalFloors[];
	private boolean simulationDone[];

	/**
	 * Constructor initializes all variables
	 */
	public Floor(int floorNumber) throws SocketException {
		this.floorNumber = floorNumber;
		this.floorLamps = new boolean[2];
		this.carButtonEventQueue = new TimeQueue();
		this.sendSocket = new DatagramSocket();
		this.simulationDone = new boolean[NUM_FLOORS]; 
		for (int i=0; i < NUM_FLOORS; i++) simulationDone[i] = true;
		for (int i=0; i < NUM_ELEVATORS; i++) {
			elevatorFloors[i] = 1;  // assume all elevators are at floor 1 to start
		}
	}

	public static void setReceiveSocket(int listenPort) throws SocketException {
		receiveSocket = new DatagramSocket(listenPort);
	}

	public static void waitOnElevator(Duration timeout) throws ElevatorWaitTimeException {
		byte data[] = new byte[256];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);
		try {
			if (timeout == null) {
				// out of actor events to send
				receiveSocket.setSoTimeout(0);
			} else {
				receiveSocket.setSoTimeout(Math.abs((int) timeout.toMillis()));
			}
			receiveSocket.receive(receivePacket);
		} catch (SocketTimeoutException e) {
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		// the elevator has arrived
		ByteArrayInputStream bainStream = new ByteArrayInputStream(receivePacket.getData());
		FloorArrivalEvent arrivalEvent = null;
		try {
			ObjectInputStream oinStream = new ObjectInputStream(bainStream);
			arrivalEvent = (FloorArrivalEvent) oinStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		int elevatorNumber = arrivalEvent.getElevatorNumber();
		int arrivalFloor = arrivalEvent.getArrivalFloor();
		if (!arrivalEvent.getDoorsClosed()) {
			floors[arrivalFloor - 1].elevatorArrived(elevatorNumber);
		}
		
		floors[lastArrivalFloors[elevatorNumber] - 1].elevatorDeparted(elevatorNumber);
		lastArrivalFloors[elevatorNumber] = arrivalFloor;
	}

	/**
	 * Performs the actions needed once an elevator arrives at this floor
	 *
	 * Turn on floor lamp and allow passengers to board if the elevator is stopped
	 *
	 * If there are car button events, dispatch those to the elevator/scheduler.
	 *
	 */
	public void elevatorArrived(int elevatorNumber) throws ElevatorWaitTimeException {
		boolean elevatorWasAlreadyHere = false;
		for (int f : elevatorFloors)  {
			if (f == this.floorNumber) elevatorWasAlreadyHere = true;
		}
		
		if (!elevatorWasAlreadyHere) {
			System.out.println("Floor " + floorNumber + ": turning lamp on");
			floorLamps[elevatorNumber] = true;
			FloorLampEvent fle = new FloorLampEvent(Instant.now(), this.floorNumber, true);
			fle.forwardEventToListener(TimeEventListener.LAMP_HEADER);
		}
		
		elevatorFloors[elevatorNumber] = this.floorNumber;
		
		boolean sentCarButtonPresses = false;
		while (!carButtonEventQueue.isEmpty()) {
			sentCarButtonPresses = true;
			System.out.println("Sending car button event to elevator...");
			CarButtonEvent event = (CarButtonEvent) carButtonEventQueue.nextEvent();
			Duration waitTime = Duration.between(event.getEventInstant(), clock.instant());
			if (waitTime.toMillis() > MAXIMUM_WAIT_TIME) {
				throw new ElevatorWaitTimeException("Passenger waited more than " + MAXIMUM_WAIT_TIME + " ms");
			}
			System.out.println("Sending car button press event to elevator. To " +
					event.getDestinationFloor());
			sendEvent(event, elevatorNumber);
		}
		
		if (sentCarButtonPresses) {
			simulationDone[floorNumber - 1] = false;
		}
		
		if (actorEventQueue.isEmpty()) {
			boolean done = true;
			for (int i = 0; i < simulationDone.length; i++) {
				if (simulationDone[i] == false) done = false;
			}
			if (done) {
				SimulationEndEvent sme = new SimulationEndEvent(Instant.now(), true);
				sme.forwardEventToListener(TimeEventListener.END_HEADER);
			}
		}
	}

	public void elevatorDeparted(int elevatorNumber) {
		if (floorLamps[elevatorNumber]) {
			System.out.println("Floor " + floorNumber + ": turning lamp off");
			floorLamps[elevatorNumber] = false;
			FloorLampEvent fle = new FloorLampEvent(Instant.now(), this.floorNumber, false);
			fle.forwardEventToListener(TimeEventListener.LAMP_HEADER);
		}
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
				cbpe.forwardEventToListener(TimeEventListener.CAR_BUTTON_HEADER);
			} else if (event instanceof RequestElevatorEvent) {
				RequestElevatorEvent reev = (RequestElevatorEvent) event;
				FloorButtonPressEvent fbpe = new FloorButtonPressEvent(reev.getEventInstant(),
						reev.getFloor(), reev.isGoingUp(), reev.getDoorError());
				out.writeObject(fbpe);
				byte[] data = dataStream.toByteArray();
				DatagramPacket sendPacket = new DatagramPacket(data,
						data.length, InetAddress.getLocalHost(), FloorButtonPressEvent.SCHEDULER_LISTEN_PORT);
				sendSocket.send(sendPacket);
				fbpe.forwardEventToListener(TimeEventListener.FLOOR_BUTTON_HEADER);
				carButtonEventQueue.addNoValidate(reev.getCarButtonEvent());
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) throws SocketException, FileNotFoundException, InvalidDirectionException {
		elevatorFloors = new int[NUM_ELEVATORS];
		lastArrivalFloors = new int[NUM_ELEVATORS];
		for (int i=0; i < NUM_ELEVATORS; i++) {
			elevatorFloors[i] = 1;
			lastArrivalFloors[i] = 1;
 		}

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
		floors = new Floor[NUM_FLOORS];
		actorEventQueue = new TimeQueue();
		actorEventQueue.setClock(clock);

		for (int i=0; i < NUM_FLOORS; i++) {
			for (RequestElevatorEvent event : events) {
				if (event.getFloor() == i + 1)  {
					if (!actorEventQueue.add(event)) {
						System.out.println("error, simulation with expired time added");
						return;
					}
				}
			}
			Floor floor = new Floor(i+1);
			floors[i] = floor;
		}

		setReceiveSocket(FloorArrivalEvent.FLOOR_LISTEN_PORT);
		clock.start();
		GuiDemo guiDemo = new GuiDemo();
		guiDemo.start();
		
		System.out.println("Simulation started...");

		while (true) {
			try {
				while (!actorEventQueue.isEmpty()) {
					while (!actorEventQueue.peekEvent().hasPassed(clock)) {
						Duration waitTime = actorEventQueue.calculateWaitTime();
						waitOnElevator(waitTime);
					}
					TimeEvent nextEvent = actorEventQueue.nextEvent();
					System.out.println((RequestElevatorEvent) nextEvent);
					floors[((RequestElevatorEvent) nextEvent).getFloor() - 1].sendEvent(nextEvent, 0);
				}
				waitOnElevator(null);
			} catch (ElevatorWaitTimeException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	
}
