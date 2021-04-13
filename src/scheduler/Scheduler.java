package scheduler;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.LinkedList;

import common.*;
import floor.Floor;
import floor.SimulationEndEvent;
import floor.TimeEventListener;
import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.FloorArrivalEvent;
import remote_procedure_events.ElevatorMotorEvent;
import remote_procedure_events.FloorButtonPressEvent;

/**
 * This scheduler is the middle man between the elevator and
 * the floor sub systems. It schedules requests for both subsystems
 *
 * @author Mmedara Josiah, Sabin Plaiasu
 * @version Iteration 4
 */
public class Scheduler implements Runnable {

	private int[] elevatorFloors;
	private TimeQueue timeQueue;
	private StopQueue stopQueues[];
	private SimulationClock clock;
	private int currentDestinations[];
	public static final int DATA_SIZE = 256;
	private DatagramSocket floorSocketReceiver, carButtonSocket, elevatorResponseSocket, sendSocket;
	private Integer nextErrorFloor;
	private boolean carButtonsPressed[][];

	/**
	 *
	 * @param elevatorFloors the current floor of each elevator
	 */
	public Scheduler(int[] elevatorFloors, SimulationClock clock) {
		nextErrorFloor = null;
		timeQueue = new TimeQueue();
		this.clock = clock;
		stopQueues = new StopQueue[Floor.NUM_ELEVATORS];
		this.elevatorFloors = new int[Floor.NUM_ELEVATORS];
		currentDestinations = new int[Floor.NUM_ELEVATORS];
		carButtonsPressed = new boolean[Floor.NUM_ELEVATORS][Floor.NUM_FLOORS];
		for (int i=0; i < Floor.NUM_ELEVATORS; i++) {
			stopQueues[i] = new StopQueue();
			this.elevatorFloors[i] = elevatorFloors[i];
			currentDestinations[i] = 1;
		}
		try {
			floorSocketReceiver = new DatagramSocket(FloorButtonPressEvent.SCHEDULER_LISTEN_PORT);
			carButtonSocket = new DatagramSocket(CarButtonPressEvent.SCHEDULER_LISTEN_PORT);
			elevatorResponseSocket = new DatagramSocket(FloorArrivalEvent.SCHEDULER_LISTEN_PORT);
			sendSocket = new DatagramSocket();
		} catch(SocketException e) {
			System.out.println("Error: SchedulerSubSystem cannot be initialized.");
			System.exit(1);
		}
	}

	/**
	 * Routine to send a DatagramPacket to the ElevatorSubsystem. This
	 * DatagramPacket will contain information that the ElevatorSubsystem will use
	 * to decide which Elevator should receive the packet.
	 *
	 */
	private void sendMotorEvent(int elevator, int destFloor) {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		boolean doorError = false;
		if (nextErrorFloor != null && destFloor == nextErrorFloor) {
			doorError = true;
			nextErrorFloor = null;
		} 
		ElevatorMotorEvent eme = new ElevatorMotorEvent(clock.instant(),
				elevator, destFloor, doorError);
		try {
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(dataStream);
			out.writeObject(eme);
			byte[] data = dataStream.toByteArray();
			DatagramPacket packet = new DatagramPacket(data,
					data.length, InetAddress.getLocalHost(), ElevatorMotorEvent.ELEVATOR_RECEIVE_PORT);
			printPacketInfo(packet);
			System.out.println("Sending Motor Event\n" + eme.toString());
			sendSocket.send(packet); // Send the packet
		} catch (IOException e) {
			// Display an error message if the packet cannot be sent.
			System.out.println("Error: Scheduler could not send the packet.");
			System.exit(1);
		}
	}

	/**
	 * Print the information contained within a particular DatagramPacket.
	 *
	 */
	private void printPacketInfo(DatagramPacket packet) {
		boolean sending = false;
		if (Thread.currentThread().getName().equals("worker")) {
				sending = true;
		}
		String symbol = sending ? "->" : "<-";
		String title = sending ? "sending" : "receiving";
		System.out.println("\n" + symbol + " Scheduler: " + title + " Packet");
		System.out.println(symbol + " Address: " + packet.getAddress());
		System.out.println(symbol + " Port: " + packet.getPort());
	}

	/**
	 * calls index of stopQueues to set next stop for an elevator
	 *
	 * @param elevatorNumber the elevator to set the next stop of
	 * @param floor what floor to stop at
	 * @return true if the elevator's current destination changed since adding the stop
	 */
	private boolean setNextStop(int elevatorNumber, int floor) {
		System.out.println("Setting stop for elevator " + elevatorNumber + " to floor " + floor);
		stopQueues[elevatorNumber].addStop(floor, elevatorFloors[elevatorNumber]);
		System.out.print("Stops in stop queue: ");
		stopQueues[elevatorNumber].printQueue();
		System.out.print("remaining floors until next destination: ");
		System.out.println(stopQueues[elevatorNumber].getRemainingFloors());
		int nextDestination = stopQueues[elevatorNumber].peekNext(); 
		if (nextDestination != currentDestinations[elevatorNumber]) {
			System.out.println("Destination changed from " + currentDestinations[elevatorNumber] + " to " + nextDestination);
			currentDestinations[elevatorNumber] = nextDestination;
			return true;
		}
		return false;
	}

	/**
	 * Handles events sent by the elevator when it arrives at a floor by sending that elevator
	 * Sends
	 *
	 * @param fae event to handle
	 * @throws ElevatorPositionException if the elevator is in an unexpected position
	 */
	private int handleFloorArrival(FloorArrivalEvent fae) throws ElevatorPositionException {
		int elevatorNumber = fae.getElevatorNumber();
		int arrivalFloor = fae.getArrivalFloor();
		boolean doorsClosed = fae.getDoorsClosed();
		
		if (!doorsClosed) carButtonsPressed[elevatorNumber][arrivalFloor - 1] = false;
		boolean done = true;
		for (int i = 0; i < carButtonsPressed.length; i++) {
			for (int j = 0; j < carButtonsPressed[i].length; j++) {
				if (carButtonsPressed[i][j]) done = false;
			}
		}
		if (done) { 
			SimulationEndEvent sme = new SimulationEndEvent(Instant.now(), false); // send event saying scheduler is done simulating all the events sent to it thus far
			sme.forwardEventToListener(TimeEventListener.SME_HEADER);
		}
		
		elevatorFloors[elevatorNumber] = arrivalFloor;
		System.out.println("Processing floor arrival for elevator " + elevatorNumber + " at floor " + arrivalFloor +
				"\nremaining floor intersections: " + stopQueues[elevatorNumber].getRemainingFloors() + " current destination: " + currentDestinations[elevatorNumber]);
		System.out.print("stops : ");
		stopQueues[elevatorNumber].printQueue();
		int expected = 0;
		try {
			expected = stopQueues[elevatorNumber].nextFloor();
		} catch (EmptyStackException | NullPointerException e) {
			// elevator reported movement without actually moving. Transient error ignored
		}
		
		if (expected != arrivalFloor && expected != 0) {
			throw new ElevatorPositionException("Unexpected elevator position, assumed sensor failure\n" +
					fae.toString() + " " + stopQueues[elevatorNumber].getRemainingFloors(),
					ElevatorPositionException.Type.PATH_MISMATCH, elevatorNumber);
		} else if (!doorsClosed) {
			if (arrivalFloor != currentDestinations[elevatorNumber]) {
				throw new ElevatorPositionException("Elevator stopped at wrong floor\n" +
						fae.toString()  + " " + stopQueues[elevatorNumber].getRemainingFloors(),
						ElevatorPositionException.Type.WRONG_ARRIVAL_FLOOR, elevatorNumber);
			}
			if (expected == currentDestinations[elevatorNumber]) {
				System.out.println("elevator " + elevatorNumber + " reached destination floor");
				currentDestinations[elevatorNumber] = stopQueues[elevatorNumber].pollNext();
				return -1;
			} else if (expected != 0) {
				System.out.println("Not at destination floor. Current Destination is  " + currentDestinations[elevatorNumber]);
				return elevatorNumber;
			}
		} else if (arrivalFloor ==  currentDestinations[elevatorNumber]) {
			/**if (expected == currentDestinations[elevatorNumber]) {
				stopQueues[elevatorNumber].pollNext();
				
			}*/
			stopQueues[elevatorNumber].reportDoorFailure(expected);
			throw new ElevatorPositionException("Elevator doors are not open when they are supposed to be\n" +
					fae.toString() + " " + stopQueues[elevatorNumber].getRemainingFloors(),
					ElevatorPositionException.Type.NOT_STOPPED, elevatorNumber);
		}
		return -1;
	}

	/**
	 * Handles floor button presses placed in work queue
	 *
	 * @param fbpe event to handle
	 * @return the number of the elevator to send to the destination floor if an elevator must be started in order to
	 * 	fulfill the request
	 * @throws ElevatorPositionException if the elevator is in an unexpected position
	 */
	private int handleFloorButtonPress(FloorButtonPressEvent fbpe) {
		int sourceFloor = fbpe.getFloor();
		boolean goingUp = fbpe.isGoingUp();
		boolean error = fbpe.doorError();
		if (error) this.nextErrorFloor = sourceFloor;

		int serviceTimes[] = new int[Floor.NUM_ELEVATORS];
		boolean stopped[] = new boolean[Floor.NUM_ELEVATORS];
		for (int i = 0; i < Floor.NUM_ELEVATORS; i++) {
			if (stopQueues[i].isMoving()) stopped[i] = true;
			serviceTimes[i] = stopQueues[i].calculateStopTime(sourceFloor, elevatorFloors[i]);
		}
		
		int fastestElevator = 0;
		int fastestTime = serviceTimes[0];
		for (int i=1; i < Floor.NUM_ELEVATORS; i++) {
			int serviceTime = serviceTimes[i];
			if (serviceTime < fastestTime) {
				fastestTime = serviceTime;
				fastestElevator = i;
			}
		}
		
		if (setNextStop(fastestElevator, sourceFloor)) {
			return fastestElevator;
		}

		if (stopped[fastestElevator]) {
			System.out.println("scheduling elevator " + fastestElevator + " to floor " + sourceFloor);
			return fastestElevator;
		}
		return -1; // return an elevator number if its motor must be started
	}

	/**
	 * Handles car button presses placed in work queue
	 *
	 * @param cbpe event to handle
	 * @return the number of the elevator to send to the destination floor if an elevator must be started in order to
	 * 	fulfill the request
	 * @throws ElevatorPositionException if the elevator is in an unexpected position
	 */
	private int handleCarButtonPress(CarButtonPressEvent cbpe) throws ElevatorPositionException {
		int elevatorNumber = cbpe.getElevatorNumber();
		int destinationFloor = cbpe.getDestinationFloor();
		carButtonsPressed[elevatorNumber][destinationFloor - 1] = true;
		int currentFloor = cbpe.getSourceFloor(); // TODO: Perform validation on this
		if (setNextStop(elevatorNumber, destinationFloor)) {
			return elevatorNumber;	
		}
		return -1;
	}

	/**
	 * Schedule the request by determining the best elevator to send the request to and modifying the
	 * floorsToGoThroughQueues and stopQueues
	 *
	 * @param work The current request that is being scheduled.
	 * @return the number of the elevator that should fulfill the request if it's to respond to a floor arrival event
	 * and the elevator is currently stopped. If the elevator is not stopped, this method should return a negative value
	 *
	 */
	private int schedule(TimeEvent work) throws ElevatorPositionException {
		if (work instanceof FloorArrivalEvent) {
				FloorArrivalEvent fae = (FloorArrivalEvent) work;
				return handleFloorArrival(fae);
			} else if (work instanceof FloorButtonPressEvent) {
				FloorButtonPressEvent fbpe = (FloorButtonPressEvent) work;
				return handleFloorButtonPress(fbpe);
			} else if (work instanceof CarButtonPressEvent) {
				CarButtonPressEvent cbpe = (CarButtonPressEvent) work;
				return handleCarButtonPress(cbpe);
			}
		return -1;
	}

	/**
	 * Perform the main loop of scheduler operations
	 *
	 */
	@Override
	public void run() {
		if (Thread.currentThread().getName().equals("floor_listener")) {
			// Main routine to receive request information from the FloorSubsystem.
			while (true) {
				byte[] request = new byte[DATA_SIZE];
				DatagramPacket receivePacket = null;
				try {
					receivePacket = new DatagramPacket(request, request.length);
					floorSocketReceiver.receive(receivePacket);
				} catch (IOException e) {
					// Display an error if the packet cannot be received
					System.out.println("Error: Scheduler cannot receive packet.");
					System.exit(1);
				}
				printPacketInfo(receivePacket);
				try {
					setRequest(receivePacket, 0);
				} catch (InterruptedException  e) {
					e.printStackTrace();
				}
			}
		} else if (Thread.currentThread().getName().equals("elevator_status_listener")) {
			while (true) {
				listenForElevator(true);
			}
		} else if (Thread.currentThread().getName().equals("elevator_button_listener")) {
			while (true) {
				listenForElevator(false);
			}
		} else if (Thread.currentThread().getName().equals("worker")) {
			while (true) {
				checkWork();
			}
		}
	}

	/**
	 * Waits until there is a request to schedule, then schedules it. Will also start the elevator motor if its
	 * currently stopped
	 *
	 */
	private void checkWork() {
		synchronized (this) {
			try {
				while (timeQueue.isEmpty()) {
					wait();
				}
				TimeEvent work = timeQueue.nextEvent();
				int selectedElevator = schedule(work);
				if (selectedElevator >= 0) {
					System.out.println("Current destination: " + currentDestinations[selectedElevator]);
					sendMotorEvent(selectedElevator, currentDestinations[selectedElevator]);
				} // no work needs to be done
				notifyAll();
			} catch (ElevatorPositionException epe) {
				if (epe.getType() == ElevatorPositionException.Type.NOT_STOPPED) {
					epe.printStackTrace();
					System.out.println("Transient error. Elevator doors closed. Opening them...");
					// Doors were not opened when they were supposed to be. Open them.
					int elevatorNumber = epe.getElevator();
					sendMotorEvent(elevatorNumber, currentDestinations[elevatorNumber]);;
				} else {
					epe.printStackTrace();
					System.exit(1);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	/**
	 * Listens for events sent by the elevator subsystem
	 *
	 * @param carButtonListener True if listening for car button presses. False if listening for floor button presses.
	 */
	private void listenForElevator(boolean carButtonListener) {
		byte[] request = new byte[DATA_SIZE];
		DatagramPacket receivePacket = null;
		try {
			// Main routine to receive confirmation from
			receivePacket = new DatagramPacket(request, request.length);
			if (carButtonListener) {
				carButtonSocket.receive(receivePacket);
			} else {
				elevatorResponseSocket.receive(receivePacket);
			}
		} catch (IOException e) {
			System.out.println("Error: Scheduler cannot receive packet.");
			System.exit(1);
		}
		printPacketInfo(receivePacket);
		try {
			if (carButtonListener) {
				setRequest(receivePacket, 2);
			} else {
				setRequest(receivePacket, 1);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Serializes the received event packet and adds it to the work queue.
	 *
	 * @param packet the packet as sent by the floor or elevator subsystems
	 * @param type the remote_procedure_event type
	 *             0: FloorButtonPressEvent, 1: FloorArrivalEvent, 2: CarButtonPressEvent
	 *             // TODO: consider replacing type with an enum
	 * @throws InterruptedException
	 */
	public synchronized void setRequest(DatagramPacket packet, int type)
			throws InterruptedException {
		ByteArrayInputStream bainStream = new ByteArrayInputStream(packet.getData());
		TimeEvent event;
		try {
			ObjectInputStream oinStream = new ObjectInputStream(bainStream);
			System.out.print("Received event from ");
			if (type == 0) {
				System.out.println("floor : FloorButtonPressEvent");
				event = (FloorButtonPressEvent) oinStream.readObject();
				System.out.println(((FloorButtonPressEvent) event).toString());
			} else {
				System.out.print("elevator :");
				if (type == 1) {
					System.out.println(" FloorArrivalEvent");
					event = (FloorArrivalEvent) oinStream.readObject();
					System.out.println(((FloorArrivalEvent) event).toString());
				} else if (type == 2){
					System.out.println(" CarButtonPressEvent");
					event = (CarButtonPressEvent) oinStream.readObject();
					System.out.println(((CarButtonPressEvent) event).toString());
				} else {
					throw new IllegalArgumentException("unrecognized event type : " + type);
				}
			}
			// TODO: Throw error if event is too far in past
		}
		catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		synchronized (this) {
			while (!timeQueue.isEmpty()) {
				wait();
			}
			timeQueue.addNoValidate(event);
			notifyAll();
		}
	}

	/**
	 * Entry point for the application.
	 *
	 * @param args The command-line arguments that are passed when compiling the
	 *             application.
	 */
	public static void main(String[] args) throws FileNotFoundException {
		File requestFile = new File(Floor.REQUEST_FILE);
		Parser p = new Parser(requestFile);
		SimulationClock clock = p.getClock();
		p.close();
		int elevatorFloors[] = new int[Floor.NUM_ELEVATORS];
		for (int i=0; i < Floor.NUM_ELEVATORS; i++) elevatorFloors[i] = 1;
		Scheduler scheduler = new Scheduler(elevatorFloors, clock);
		Thread elevatorStatusListener = new Thread(scheduler, "elevator_status_listener");
		Thread elevatorButtonListener = new Thread(scheduler, "elevator_button_listener");
		Thread floorListener = new Thread(scheduler, "floor_listener");
		Thread worker = new Thread(scheduler, "worker");
		floorListener.start();
		elevatorStatusListener.start();
		elevatorButtonListener.start();
		worker.start();
	}
}
