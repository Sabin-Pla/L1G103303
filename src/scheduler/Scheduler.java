package scheduler;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Stack;

import common.*;

import floor.ElevatorException;
import floor.Floor;
import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.FloorArrivalEvent;
import remote_procedure_events.ElevatorMotorEvent;
import remote_procedure_events.FloorButtonPressEvent;

/**
 * This scheduler is the middle man between the elevator and
 * the floor sub systems. It schedules requests for both subsystems
 *
 * @author Harshil Verma, Mmedara Josiah
 * @version Iteration 2
 */
public class Scheduler implements Runnable {
	// maximum amount of time (ms) an elevator should take to fulfill any request
	private TimeQueue timeQueue;

	private static int elevatorListenerNumber = 0;

	private Stack<Integer> intersectingFloors[];
	private Stack<Integer> stopStack[];
	private Time time;

	private static final int DATA_SIZE = 256;

	private DatagramPacket sendPacket, receiveElevatorInfo;
	private DatagramSocket floorSocketReceiver, carButtonSocket, elevatorResponseSocket, sendSocket;


	/**
	 * Constructor
	 */
	public Scheduler() {
		timeQueue = new TimeQueue();
		intersectingFloors = new Stack[Floor.NUM_ELEVATORS];
		stopStack = new Stack[Floor.NUM_ELEVATORS];
		for (int i=0; i < Floor.NUM_ELEVATORS; i++) {
			intersectingFloors[i] = new Stack<Integer>();
			stopStack[i] = new Stack<Integer>();
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

	private void setTime(Time time) {
		this.time = time;
	}

	/**
	 * Routine to create a DatagramPacket that will be sent.
	 *
	 * @param message The byte[] data the DatagramPacket will contain.
	 */
	private void createPacket(byte[] message) {
		try {
			// Initialize and create a send packet
			sendPacket = new DatagramPacket(message, message.length, InetAddress.getLocalHost(),
					ElevatorMotorEvent.ELEVATOR_RECEIVE_PORT);
		} catch (UnknownHostException e) {
			// Display an error message if the packet cannot be created.
			System.out.println("Error: Scheduler could not create packet.");
			System.exit(1);
		}
	}

	/**
	 * Routine to send a DatagramPacket to the ElevatorSubsystem. This
	 * DatagramPacket will contain information that the ElevatorSubsystem will use
	 * to decide which Elevator should receive the packet.
	 *
	 */
	private void sendPacketToElevator(ElevatorMotorEvent eme) {
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
		if (Thread.currentThread().getName().equals("WorkerThread")) {
				sending = true;
		}
		String symbol = sending ? "->" : "<-";
		String title = sending ? "sending" : "receiving";

		System.out.println(symbol + " Scheduler: " + title + " Packet");
		System.out.println(symbol + " Address: " + packet.getAddress());
		System.out.println(symbol + " Port: " + packet.getPort());
		System.out.print(symbol + " Data (byte): ");
		for (byte b : packet.getData())
			System.out.print(b);

		System.out.print("\n" + symbol + " Data (String): " + new String(packet.getData()) + "\n\n");
	}

	/**
	 * Check to see if there are any requests that must be scheduled.
	 *
	 * @return The next request to be scheduled, as a DatagramPacket.
	 */
	private synchronized Object checkWork() {
		if (timeQueue.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return timeQueue.poll();
	}

	private void setNextStop(int elevatorNumber, int floor) {
		Stack<Integer> stack = stopStack[elevatorNumber];
		for (int i=0; i < stack.size() - 1; i++) {
			if (floor > stack.get(i) && floor < stack.get(i + 1) ||
					floor < stack.get(i) && floor > stack.get(i + 1)
			) {
				stack.insertElementAt(i, floor);
			}
		}
		stack.push(floor);
	}

	/**
	 * Schedule the request by determining the best elevator to send the request to.
	 *
	 * @param work         The current request that is being scheduled.
	 * @return A DatagramPacket that contains the request, along with the
	 *         information as to which Elevator the request will be added to, and if
	 *         it should do at the front of the back of the workQueue.
	 */
	private void schedule(Object work) {

		if (work instanceof FloorArrivalEvent) {
			return;
		} else if (work instanceof  FloorButtonPressEvent) {
			FloorButtonPressEvent fbe = (FloorButtonPressEvent) work;
			int sourceFloor = fbe.getFloor();
			boolean goingUp = fbe.isGoingUp();

			int elevatorTimes[] = new int[Floor.NUM_ELEVATORS];
			// pick the elevator where the source floor is along the path the earliest
			for (int i = 0; i < Floor.NUM_ELEVATORS; i++) {
				for (int j=0; j < intersectingFloors[i].size(); j++) {
					elevatorTimes[i] = j;
					if (sourceFloor == intersectingFloors[i].get(j)) {
						break;
					}
				}
			}

			int fastestElevator = 0;
			int min = elevatorTimes[0];
			for (int i=0; i < elevatorTimes.length; i++) {
				if (elevatorTimes[i] < min) {
					min = elevatorTimes[i];
					fastestElevator = i;
				}
			}

			setNextStop(fastestElevator, sourceFloor);
		} else if (work instanceof  CarButtonPressEvent) {
			CarButtonPressEvent cbe = (CarButtonPressEvent) work;
			int elevatorNumber = cbe.getElevatorNumber();
			int destinationFloor = cbe.getDestinationFloor();
			int currentFloor = cbe.getSourceFloor();

			Stack<Integer> path = intersectingFloors[elevatorNumber];
			if (currentFloor != path.peek()) {
				try {
					throw new ElevatorException("wrong elevator position");
				} catch (ElevatorException e) {
					e.printStackTrace();
				}
			}

			for (Integer i : stopStack[elevatorNumber]) {
				if (i == destinationFloor) {
					return; // will already stop at this floor
				}
			}

			for (Integer i = 0; i < path.size(); i++) {
				int floor = path.get(i);
				if (floor == destinationFloor) {
					setNextStop(elevatorNumber, floor);
					return;
				}
			}

			// if the method call has not ended by this point, the destination floor is no where along the elevator path

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
	}


	/**
	 * Thread execution routine.
	 */
	@Override
	public void run() {
		if (Thread.currentThread().getName().matches("Floor")) {
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
		} else if (Thread.currentThread().getName().matches("Elevator")) {
			if (elevatorListenerNumber == 0 ) {
				Thread elevatorListener2 = new Thread(this, "ElevatorListener2");
				elevatorListenerNumber++;
				elevatorListener2.start();
			} else {
				if (Thread.currentThread().getName().equals("ElevatorListener2")) {
					listenForElevator(true);
				}
			}
			listenForElevator(false);
		} else {
			while (true) {
				Object work = checkWork(); // blocks until rpc places something in queue
				schedule(work);
			}
		}
	}


	private void listenForElevator(boolean carButtonListener) {
		while (true) {
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

					try {
						ObjectInputStream oinStream = new ObjectInputStream(bainStream);
						arrivalEvent = (FloorArrivalEvent) oinStream.readObject();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}

					ElevatorMotorEvent eme = new ElevatorMotorEvent(time.now(),
							arrivalEvent.getElevatorNumber(), stopStack[arrivalEvent.getElevatorNumber()].pop());
					sendPacketToElevator(eme);
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
	}


	/**
	 * Stores an incoming request in the scheduler's queue
	 *
	 * @throws InterruptedException
	 */
	public synchronized void setRequest(DatagramPacket packet, int type)
			throws InterruptedException, TimeException {

		ByteArrayInputStream bainStream = new ByteArrayInputStream(packet.getData());
		TimeEvent event = null;
		try {
			ObjectInputStream oinStream = new ObjectInputStream(bainStream);
			if (type == 0) {
				event = (FloorButtonPressEvent) oinStream.readObject();
			} else if (type == 1) {
				event = (FloorArrivalEvent) oinStream.readObject();
			} else if (type == 2) {
				event = (CarButtonPressEvent) oinStream.readObject();
			}
		}
		catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

		if (!timeQueue.add(event)) {
			throw new TimeException("Cannot schedule event in the past!");
		}
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
		Time time = new Time(Time.SECOND_TO_MINUTE / 2, Parser.getStartTime(requestFile));
		Scheduler scheduler = new Scheduler();
		System.out.println("---- SCHEDULER SUB SYSTEM ----- \n");
		Thread elevatorListener = new Thread(scheduler, "ElevatorListener");
		Thread floorListener = new Thread(scheduler, "FloorListener");
		Thread workerThread = new Thread(scheduler, "Worker");
		scheduler.setTime(time);
		floorListener.start();
		elevatorListener.start();
		workerThread.start();
	}
}
