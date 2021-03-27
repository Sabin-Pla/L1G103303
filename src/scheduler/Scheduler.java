package scheduler;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Date;
import java.util.Stack;

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
	private TimeQueue timeQueue;

	private static int elevatorListenerNumber = 0;

	private static int[] elevatorFloors;
	private Stack<Integer> floorsToGoThroughStacks[];
	private Stack<Integer> stopStacks[];
	private static SimulationClock clock;

	private static final int DATA_SIZE = 256;
	private DatagramSocket floorSocketReceiver, carButtonSocket, elevatorResponseSocket, sendSocket;

	public Scheduler() {
		timeQueue = new TimeQueue();
		floorsToGoThroughStacks = new Stack[Floor.NUM_ELEVATORS];
		stopStacks = new Stack[Floor.NUM_ELEVATORS];
		for (int i=0; i < Floor.NUM_ELEVATORS; i++) {
			floorsToGoThroughStacks[i] = new Stack<>();
			stopStacks[i] = new Stack<>();
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
		System.out.print(symbol + " Data (byte): ");
		for (byte b : packet.getData())
			System.out.print(b);

		System.out.print("\n" + symbol + " Data (String): " + new String(packet.getData()));
	}

	private void setNextStop(int elevatorNumber, int floor) {
		Stack<Integer> stops = stopStacks[elevatorNumber];
		for (int i=0; i < stops.size() - 1; i++) {
			if (floor > stops.get(i) && floor < stops.get(i + 1) ||
					floor < stops.get(i) && floor > stops.get(i + 1)
			) {
				stops.insertElementAt(i, floor);
				return;
			}
		}
		stops.push(floor);
	}

	/**
	 * Schedule the request by determining the best elevator to send the request to.
	 *
	 * @param work         The current request that is being scheduled.
	 * @return A DatagramPacket that contains the request, along with the
	 *         information as to which Elevator the request will be added to, and if
	 *         it should do at the front of the back of the workQueue.
	 */
	private int schedule(TimeEvent work) throws ElevatorPositionException {
		if (work instanceof FloorArrivalEvent) {
				return 0;
			} else if (work instanceof FloorButtonPressEvent) {
				FloorButtonPressEvent fbe = (FloorButtonPressEvent) work;
				int sourceFloor = fbe.getFloor();
				boolean goingUp = fbe.isGoingUp();
				Integer elevatorTimes[][] = new Integer[Floor.NUM_ELEVATORS][2];
				// pick the elevator where the source floor is along the path the earliest
			for (int i = 0; i < Floor.NUM_ELEVATORS; i++) {
				elevatorTimes[i][0] = 0;
				elevatorTimes[i][1] = 0;
				boolean doneFirst = false; // done calculating time for first index of elevatorTimes[i]
				boolean doneSecond = false;// done calculating time for second index of elevatorTimes[i]
				Stack<Integer> stopStack = floorsToGoThroughStacks[i];
				if (stopStack.size() > 0) {
					int lastFloor = stopStack.get(stopStack.size() - 1);
					for (int j = stopStack.size() - 1; j > -1; j--) {
						if (!doneFirst && lastFloor > stopStack.get(j) && !goingUp ||
								lastFloor < stopStack.get(j) && goingUp ) {
							// floor intersection is in the desired direction.
							lastFloor = stopStack.get(j);
							if (sourceFloor != stopStack.get(j)) {
								elevatorTimes[i][0] += 1;
								doneFirst = true;
							}
						} else if (!doneSecond){
							// floor intersection is not in the desired direction.
							if (sourceFloor != stopStack.get(j)) {
								elevatorTimes[i][1] += 1;
								doneSecond = true;
							}
						}
						if (doneFirst && doneSecond) break;
					}
					elevatorTimes[i][0] += Math.abs(sourceFloor - elevatorTimes[i][0]);
					elevatorTimes[i][1] += Math.abs(sourceFloor - elevatorTimes[i][1]);
				}
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
			return fastestElevator;
		} else if (work instanceof CarButtonPressEvent) {
			CarButtonPressEvent cbe = (CarButtonPressEvent) work;
			int elevatorNumber = cbe.getElevatorNumber();
			int destinationFloor = cbe.getDestinationFloor();
			int currentFloor = cbe.getSourceFloor();
			Stack<Integer> path = floorsToGoThroughStacks[elevatorNumber];
			if (currentFloor != path.peek()) {
				throw new ElevatorPositionException("wrong elevator position");
			}
			for (Integer i : stopStacks[elevatorNumber]) {
				if (i == destinationFloor) {
					return -1; // elevator is already scheduled to stop on requested floor
				}
			}
			/** if the method call has not ended by this point, the destination floor is no where along the elevator
			 * path */
			setNextStop(elevatorNumber, destinationFloor);
			if (path.get(0) < destinationFloor) {
				for (Integer i = path.get(0) + 1; i <= destinationFloor; i++) {
					path.push(i);
				}
			} else {
				for (Integer i = path.get(0) - 1; i >= destinationFloor; i--) {
					path.push(i);
				}
			}
		}
		return -1;
	}

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
				} catch (InterruptedException | TimeException e) {
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

	private void checkWork() {
		synchronized (this) {
			try {
				while (timeQueue.isEmpty()) {
					wait();
				}
				TimeEvent work = timeQueue.nextEvent();
				int selectedElevator = schedule(work);
				if (selectedElevator >= 0) {
					sendMotorEvent(selectedElevator,  stopStacks[selectedElevator].pop());
					/** Block until the elevator has reached the designated floor */
					while (elevatorFloors[selectedElevator] != stopStacks[selectedElevator].peek()) {
						// TODO: check to make sure elevator is going in expected direction, throw error if its not
						wait();
						if (timeQueue.isEmpty()) return;
					}
				}// no work needs to be done
			} catch (ElevatorPositionException | InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

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
				ByteArrayInputStream bainStream = new ByteArrayInputStream(receivePacket.getData());
				FloorArrivalEvent arrivalEvent = null;
				int elevatorNumber;
				try {
					ObjectInputStream oinStream = new ObjectInputStream(bainStream);
					arrivalEvent = (FloorArrivalEvent) oinStream.readObject();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				elevatorNumber = arrivalEvent.getElevatorNumber();
				synchronized (this) {
					if (stopStacks[elevatorNumber].isEmpty()) return; // elevator reached destination floor
					elevatorFloors[elevatorNumber] = arrivalEvent.getArrivalFloor();
					sendMotorEvent(elevatorNumber, stopStacks[elevatorNumber].pop());
					floorsToGoThroughStacks[elevatorNumber].pop();
					// TODO: throw error if the value in the floor intersection stack and stop stack are not the same.
					notifyAll();
				}
			}
		} catch (IOException e) {
			// Display an error if the packet cannot be received
			System.out.println("Error: Scheduler cannot receive packet.");
			System.exit(1);
		}

		printPacketInfo(receivePacket);
		try {
			if (carButtonListener) {
				setRequest(receivePacket, 1);
			} else {
				setRequest(receivePacket, 2);
			}
		} catch (InterruptedException | TimeException e) {
			e.printStackTrace();
		}
	}

	public synchronized void setRequest(DatagramPacket packet, int type)
			throws InterruptedException, TimeException {
		System.out.println("\nSet Request: ");
		ByteArrayInputStream bainStream = new ByteArrayInputStream(packet.getData());
		TimeEvent event;
		try {
			ObjectInputStream oinStream = new ObjectInputStream(bainStream);
			System.out.print("Received event from ");
			if (type == 0) {
				System.out.println("floor : FloorButtonPressEvent");
				event = (FloorButtonPressEvent) oinStream.readObject();
				System.out.println("Going up : " + ((FloorButtonPressEvent) event).isGoingUp());
				System.out.println("Floor    : " + ((FloorButtonPressEvent) event).getFloor());
			} else {
				System.out.print("elevator :");
				if (type == 1) {
					System.out.println(" FloorArrivalEvent");
					event = (FloorArrivalEvent) oinStream.readObject();
					System.out.println("Elevator #   : " + ((FloorArrivalEvent) event).getElevatorNumber());
					System.out.println("Arrived at   : " + ((FloorArrivalEvent) event).getArrivalFloor());
					System.out.println("Doors closed : " + ((FloorArrivalEvent) event).getDoorsClosed());
				} else if (type == 2){
					System.out.println(" CarButtonPressEvent");
					event = (CarButtonPressEvent) oinStream.readObject();
					System.out.println("Destination Floor : " + ((CarButtonPressEvent) event).getDestinationFloor());
					System.out.println("Elevator #        : " + ((CarButtonPressEvent) event).getElevatorNumber());
				} else {
					throw new IllegalArgumentException("unrecognized event type : " + type);
				}
			}
			System.out.println("Time: " + Date.from(event.getEventInstant()));
			// TODO: Throw error if event is too far in past
		}
		catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

		timeQueue.addNoValidate(event);
		notifyAll();
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
		clock = p.getClock();
		p.close();
		elevatorFloors = new int[Floor.NUM_ELEVATORS];
		Scheduler scheduler = new Scheduler();
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
