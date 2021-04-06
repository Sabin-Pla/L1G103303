package scheduler;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;

import common.*;
import floor.Floor;
import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.FloorArrivalEvent;
import remote_procedure_events.ElevatorMotorEvent;
import remote_procedure_events.FloorButtonPressEvent;

/**
 * This scheduler is the middle man between the elevator and
 * the floor sub systems. It schedules requests for both subsystems
 *
 * @author Harshil Verma, Mmedara Josiah, Sabin Plaiasu
 * @version Iteration 4
 */
public class Scheduler implements Runnable {

	private int[] elevatorFloors;
	private TimeQueue timeQueue;
	private LinkedList<Integer> floorsToGoThroughQueues[];
	private LinkedList<Integer> stopQueues[];
	private SimulationClock clock;
	private int currentDestinations[];
	private static final int DATA_SIZE = 256;
	private DatagramSocket floorSocketReceiver, carButtonSocket, elevatorResponseSocket, sendSocket;

	/**
	 *
	 * @param elevatorFloors the current floor of each elevator
	 */
	public Scheduler(int[] elevatorFloors, SimulationClock clock) {
		timeQueue = new TimeQueue();
		this.clock = clock;
		floorsToGoThroughQueues = new LinkedList[Floor.NUM_ELEVATORS];
		stopQueues = new LinkedList[Floor.NUM_ELEVATORS];
		this.elevatorFloors = new int[Floor.NUM_ELEVATORS];
		currentDestinations = new int[Floor.NUM_ELEVATORS];
		for (int i=0; i < Floor.NUM_ELEVATORS; i++) {
			floorsToGoThroughQueues[i] = new LinkedList<>();
			stopQueues[i] = new LinkedList<>();
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
		ElevatorMotorEvent eme = new ElevatorMotorEvent(clock.instant(),
				elevator, destFloor);
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
	 * modifies attributes stopQueues and floorsToGoThroughQueues to set next stop for an elevator
	 *
	 * @param elevatorNumber the elevator to se the next stop of
	 * @param floor what flor to stop at
	 */
	private void setNextStop(int elevatorNumber, int floor) {
		LinkedList<Integer> stops = stopQueues[elevatorNumber];
		/* try to insert the destination stop between the earliest 2 stops */
		for (int i = 0; i < stops.size() - 1; i++) {
			if (floor > stops.get(i) && floor < stops.get(i + 1) ||
					floor < stops.get(i) && floor > stops.get(i + 1)) {
				stops.add(i, floor);
				return;
			}
		}
		LinkedList<Integer> path = floorsToGoThroughQueues[elevatorNumber];
		int lastStop;
		int currentFloor = elevatorFloors[elevatorNumber];
		if (stops.size() == 0) {
			stops.add(floor);
			lastStop = currentFloor;
			if (path.size() != 0) {
				/* Cancelling the current elevator movement request and moving it to a new location.
				 add the last stop back int he stop queue */
				lastStop = currentDestinations[elevatorNumber];
				if ((floor > lastStop && lastStop > currentFloor) || (floor < lastStop && lastStop < currentFloor)) {
					// if floor will be serviced after last stop
					System.out.println("...");
				} else {
					stops.add(1, lastStop);
					return;
				}
			}
		} else {
			lastStop = path.peekLast();
			/* insert the destination stop somewhere between the current floor and the next stop */
			if (floor > lastStop) {
				if (currentFloor < floor) { // floor > currentFloor > 'lastStop'
					stops.addLast(floor); // go back up to 'floor' after arriving at 'lastStop'
				} else { // floor > nextStop > currentFloor
					stops.addLast(floor); // go to 'floor' through 'lastStop'
					return; // no changes made to intersecting floors, floor is already in that stack
				}
			} else if (currentFloor < floor) { // 'lastStop' > floor > currentFloor
				stops.addFirst(floor); // go to 'lastStop' through floor from below
				return; // no changes made to intersecting floors, floor is already in that stack
			} else { // nextStop > currentFloor > floor
				stops.addLast(floor); // go back down to 'floor' after going up to next stop
			}
		}
		/*
		  If the method has not returned at this point, the destination floor is not along the elevator's path.
		  make the appropriate changes to floorsToGoThroughQueues by extending
		   the intersection floors in path to floor */
		if (floor > lastStop) { // going to 'floor' from below
			for (int i=lastStop + 1; i <= floor; i++) {
				path.addLast(i);
			}
		} else { // going to 'floor' from above
			for (int i=lastStop - 1; i >= floor; i--) {
				path.addLast(i);
			}
		}
	}

	/**
	 * Handles events sent by the elevator when it arrives at a floor by sending that elevator
	 * Sends
	 *
	 * @param fae event to handle
	 * @throws ElevatorPositionException if the elevator is in an unexpected position
	 */
	private void handleFloorArrival(FloorArrivalEvent fae) throws ElevatorPositionException {
		int elevatorNumber = fae.getElevatorNumber();
		int arrivalFloor = fae.getArrivalFloor();
		boolean doorsClosed = fae.getDoorsClosed();
		elevatorFloors[elevatorNumber] = arrivalFloor;
		int expectedArrivalFloor = floorsToGoThroughQueues[elevatorNumber].pollFirst();
		System.out.println("Processing floor arrival for elevator " + elevatorNumber + " at floor " + arrivalFloor +
				"\n remaining floor intersections: " + floorsToGoThroughQueues[elevatorNumber] + "\n stops: " +
				stopQueues[elevatorNumber]);
		if (expectedArrivalFloor != arrivalFloor) {
			throw new ElevatorPositionException("Unexpected elevator position, assumed sensor failure\n" +
					fae.toString() + " " + floorsToGoThroughQueues[elevatorNumber],
					ElevatorPositionException.Type.PATH_MISMATCH, elevatorNumber);
		} else if (!doorsClosed) {
			if (arrivalFloor != currentDestinations[elevatorNumber]) {
				throw new ElevatorPositionException("Elevator stopped at wrong floor\n" +
						fae.toString()  + " " + floorsToGoThroughQueues[elevatorNumber],
						ElevatorPositionException.Type.WRONG_ARRIVAL_FLOOR, elevatorNumber);
			}
			if (stopQueues[elevatorNumber].isEmpty()) return; // elevator reached destination floor
			currentDestinations[elevatorNumber] = stopQueues[elevatorNumber].pollFirst();
			sendMotorEvent(elevatorNumber, currentDestinations[elevatorNumber]);;
		} else if (arrivalFloor ==  currentDestinations[elevatorNumber]) {
			throw new ElevatorPositionException("Elevator doors are not open when they are supposed to be\n" +
					fae.toString() + " " + floorsToGoThroughQueues[elevatorNumber],
					ElevatorPositionException.Type.NOT_STOPPED, elevatorNumber);
		}
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
		Integer elevatorTimes[] = new Integer[Floor.NUM_ELEVATORS];
		// pick the elevator where the source floor is along the path the earliest
		/* "stopped" means the elevator has no moor floors to move through. Does not necessarily mean that the doors
		* are closed in this context. */
		boolean stopped[] = new boolean[Floor.NUM_ELEVATORS];
		boolean prefDirection[] = new boolean[Floor.NUM_ELEVATORS];
		for (int i = 0; i < Floor.NUM_ELEVATORS; i++) {
			LinkedList<Integer> throughQueue = floorsToGoThroughQueues[i];
			int currentFloor = elevatorFloors[i];
			elevatorTimes[i] = Math.abs(sourceFloor - currentFloor);
			if (throughQueue.size() > 0) {
				if (throughQueue.size() > 1) {
					int sourceFloorIndex = throughQueue.indexOf(sourceFloor);
					if (sourceFloorIndex >= 0) {
						// elevator is crossing through floor where button was pressed
						if (throughQueue.get(sourceFloorIndex - 1) > throughQueue.get(sourceFloorIndex)) {
							if (goingUp) {
								elevatorTimes[i] = sourceFloorIndex;
								prefDirection[i] = true;
							}
						} else if (throughQueue.get(sourceFloorIndex - 1) < throughQueue.get(sourceFloorIndex)) {
							if (!goingUp) {
								elevatorTimes[i] = sourceFloorIndex;
								prefDirection[i] = true;
							}
						}
					} else { // sourceFloorIndex < 0
						elevatorTimes[i] = Math.abs(throughQueue.peekLast() - sourceFloor);
						prefDirection[i] = false;
					}
				} else { // throughQueue.size() <= 1
					elevatorTimes[i] = Math.abs(throughQueue.peekLast() - sourceFloor);
					if (currentFloor < throughQueue.peekLast() && goingUp
						&& sourceFloor > throughQueue.peekLast()) prefDirection[i] = true;
					if ( currentFloor > throughQueue.peekLast() && !goingUp
						&& sourceFloor < throughQueue.peekLast()) prefDirection[i] = true;
				}
			} else { // if throughQueue.size() == 0
				stopped[i] = true; // elevator is stopped
				prefDirection[i] = true;
			}
		}
		int fastestElevator = 0;
		int minTime = elevatorTimes[0];
		if (!prefDirection[0]) minTime += Floor.NUM_FLOORS;
		for (int i=0; i < elevatorTimes.length; i++) {
			if (elevatorTimes[i] < minTime && prefDirection[i]) {
				minTime = elevatorTimes[i];
				fastestElevator = i;
			} else if (elevatorTimes[i] < minTime - Floor.NUM_FLOORS && !prefDirection[i]) {
				minTime = elevatorTimes[i];
				fastestElevator = i;
			}
		}
		if (sourceFloor == 6) {
			System.out.println("here");
		}
		setNextStop(fastestElevator, sourceFloor);
		if (stopped[fastestElevator]) return fastestElevator;
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
		int currentFloor = cbpe.getSourceFloor(); // TODO: Perform validation on this
		LinkedList<Integer> path = floorsToGoThroughQueues[elevatorNumber];
		for (Integer i : stopQueues[elevatorNumber]) {
			if (i == destinationFloor) {
				return -1; // elevator is already scheduled to stop on requested floor
			}
		}
		/** if the method call has not ended by this point, the destination floor is no where along the elevator
		 * path */
		setNextStop(elevatorNumber, destinationFloor);
		return elevatorNumber;
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
		System.out.println("\nscheduling...");
		if (work instanceof FloorArrivalEvent) {
				FloorArrivalEvent fae = (FloorArrivalEvent) work;
				handleFloorArrival(fae);
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
					currentDestinations[selectedElevator] = stopQueues[selectedElevator].pollFirst();
					sendMotorEvent(selectedElevator, currentDestinations[selectedElevator]);
				} // no work needs to be done
				notifyAll();
			} catch (ElevatorPositionException epe) {
				if (epe.getType() == ElevatorPositionException.Type.NOT_STOPPED) {
					System.out.println("Elevator doors closed. Opening them...");
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
	 * @param packet the packet as sent by the floor or elevator subystems
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
