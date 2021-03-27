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
		} else {
			lastStop = path.peekLast();
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
		/** make the appropriate changes to floorsToGoThroughQueues by extending
		 *  the intersection floors in path to floor */
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
				"\n remaining floor intersections: " + floorsToGoThroughQueues[elevatorNumber]);
		if (expectedArrivalFloor != arrivalFloor) {
			throw new ElevatorPositionException("Unexpected elevator position, assumed sensor failure\n" +
					fae.toString() + " " + floorsToGoThroughQueues[elevatorNumber],
					ElevatorPositionException.Type.PATH_MISMATCH);
		}
		if (!doorsClosed) {
			if (arrivalFloor != currentDestinations[elevatorNumber]) {
				throw new ElevatorPositionException("Elevator stopped at wrong floor\n" +
						fae.toString()  + " " + floorsToGoThroughQueues[elevatorNumber],
						ElevatorPositionException.Type.WRONG_ARRIVAL_FLOOR);
			}
			stopQueues[elevatorNumber].pollFirst(); // elevator has stopped on the next floor in the stop queue
			if (stopQueues[elevatorNumber].isEmpty()) return; // elevator reached destination floor
			currentDestinations[elevatorNumber] = stopQueues[elevatorNumber].pollFirst();
			sendMotorEvent(elevatorNumber, currentDestinations[elevatorNumber]);;
		} else if (arrivalFloor ==  currentDestinations[elevatorNumber]) {
			throw new ElevatorPositionException("Elevator is not stopped at the floor its supposed to be\n" +
					fae.toString() + " " + floorsToGoThroughQueues[elevatorNumber],
					ElevatorPositionException.Type.NOT_STOPPED);
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
		Integer elevatorTimes[][] = new Integer[Floor.NUM_ELEVATORS][2];
		// pick the elevator where the source floor is along the path the earliest
		boolean stopped[] = new boolean[Floor.NUM_ELEVATORS];
		for (int i = 0; i < Floor.NUM_ELEVATORS; i++) {
			LinkedList<Integer> throughQueue = floorsToGoThroughQueues[i];
			elevatorTimes[i][0] = 0;
			elevatorTimes[i][1] = 0;
			boolean doneFirst = false;  // done calculating time for first index of elevatorTimes[i]
			boolean doneSecond = false; // done calculating time for second index of elevatorTimes[i]
			if (throughQueue.size() > 0) {
				int lastFloor = throughQueue.get(throughQueue.size() - 1);
				for (int j = throughQueue.size() - 1; j > -1; j--) {
					if (!doneFirst && lastFloor > throughQueue.get(j) && !goingUp ||
							lastFloor < throughQueue.get(j) && goingUp ) {
						// floor intersection is in the desired direction.
						lastFloor = throughQueue.get(j);
						if (sourceFloor != throughQueue.get(j)) {
							elevatorTimes[i][0] += 1;
							doneFirst = true;
						}
					} else if (!doneSecond){
						// floor intersection is not in the desired direction.
						if (sourceFloor != throughQueue.get(j)) {
							elevatorTimes[i][1] += 1;
							doneSecond = true;
						}
					}
					if (doneFirst && doneSecond) break;
				}
				elevatorTimes[i][0] += Math.abs(sourceFloor - elevatorFloors[i]);
				elevatorTimes[i][1] += Math.abs(sourceFloor - elevatorFloors[i]);
			} // if throughQueue.size() > 0
			stopped[i] = true; // elevator is stopped
		}
		int fastestElevators[] = new int[2];
		int minPressDirection = elevatorTimes[0][0];
		int minNonPressDirection = elevatorTimes[0][0];
		for (int i=0; i < elevatorTimes.length; i++) {
			if (elevatorTimes[i][0] < minPressDirection) {
				minPressDirection = elevatorTimes[i][0];
				fastestElevators[0] = i;
			}
			if (elevatorTimes[i][1] < minNonPressDirection) {
				minNonPressDirection = elevatorTimes[i][1];
				fastestElevators[1] = i;
			}
		}
		int fastestElevator = fastestElevators[0];
		if (fastestElevators[1] + (Floor.NUM_FLOORS) / 2 < fastestElevators[0]) {
			fastestElevator = fastestElevators[1];
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
		int currentFloor = cbpe.getSourceFloor();
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
			} catch (ElevatorPositionException | InterruptedException e) {
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
