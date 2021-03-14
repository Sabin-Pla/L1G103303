package elevator;

import common.CarButtonEvent;
import common.Parser;
import floor.ElevatorException;
import floor.Floor;
import remote_procedure_events.ArrivedFloorEvent;
import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.FloorButtonPressEvent;
import common.Time;
import remote_procedure_events.LeftFloorEvent;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


/**
 * This class models the elevator
 *
 * @author Mmedara Josiah, Sabin Plaiasu, John Afolayan
 * @version Iteration 3
 *
 */
public class Elevator extends Thread {

	private final long MOVE_ONE_FLOOR_TIME = 10000;

	private int currentFloor;
	static private Time time;
	private int destinationFloor;
	private DatagramPacket sendPacket, receiveCBPE, receiveMoveEvent;
	private DatagramSocket sendSocket, floorSocketReceiver, schedulerSocketReceiver;
	private boolean doorsClosed;
	private int elevatorNumber;
	private int lastSensor;

	/**
	 * Constructor
	 */
	public Elevator(int elevatorNumber, int currentFloor, Time time) throws SocketException {
		this.elevatorNumber = elevatorNumber;
		this.currentFloor = currentFloor;
		this.destinationFloor = currentFloor;
		this.time = time;
		this.lastSensor = 0;
		this.doorsClosed = true;
		this.carButtonPressEvent = new DatagramSocket(CarButtonPressEvent.SCHEDULER_LISTEN_PORT);
		this.leftFloorEvent = new DatagramSocket(CarButtonPressEvent.SCHEDULER_LISTEN_PORT);
		this.arrivedFloorEvent = new DatagramSocket(CarButtonPressEvent.SCHEDULER_LISTEN_PORT);
		this.leftFloorEvent = new DatagramSocket(61);
	}

	@Override
	public void run() {
		while (true) {
			while (currentFloor == destinationFloor) {
				doorsClosed = false;
				try {
					lastSensor = destinationFloor;
					notifyAll(); // let sensors know elevator has stopped
					wait();
				} catch (InterruptedException e) {
					doorsClosed = true;
				}
			}

			while (lastSensor != currentFloor) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("\nElevator: moving...");

			try {
				sleep((long) (MOVE_ONE_FLOOR_TIME / time.getCompressionFactor()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (currentFloor > destinationFloor) {
				currentFloor -= 1;
				System.out.println("\nElevator: Down 1 floor, now at " + currentFloor);
			} else {
				currentFloor += 1;
				System.out.println("\nElevator: Up 1 floor, now at " + currentFloor);
			}
			reportArrival();
		}
	}

	/**
	 *
	 * @return Current floor
	 */
	public Integer getCurrentFloor(){
		return currentFloor;
	}

	/**
	 * This method represents the floor which elevator is moving to.
	 * @param destFloor Destination Floor
	 */
	public void move(int destFloor) {
		this.destinationFloor = destFloor;
		System.out.println("\nElevator: new destination floor " + destFloor);
		notifyAll();
	}

	private int getFloor() {
		return currentFloor;
	}

	private void sendCarButtonPress(CarButtonEvent event) {
		try {
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(dataStream);
			out.writeObject(event);
			out.close();

			byte[] data = dataStream.toByteArray();
			DatagramPacket  sendPacket = new DatagramPacket(data,
					data.length, InetAddress.getLocalHost(), CarButtonPressEvent.SCHEDULER_LISTEN_PORT);
			sendSocket.send(sendPacket);
		} catch (InterruptedException | ElevatorException | IOException e) {
			e.printStackTrace();
		}
	}

	private void sendLeftFloorEvent(LeftFloorEvent event) {
		try {
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(dataStream);
			out.writeObject(event);
			out.close();

			byte[] data = dataStream.toByteArray();
			DatagramPacket  sendPacket = new DatagramPacket(data,
					data.length, InetAddress.getLocalHost(), LeftFloorEvent.SCHEDULER_LISTEN_PORT);
			sendSocket.send(sendPacket);
		} catch (InterruptedException | ElevatorException | IOException e) {
			e.printStackTrace();
		}
	}

	private void reportArrival(ArrivedFloorEvent event) {
		try {
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(dataStream);
			out.writeObject(event);
			out.close();

			byte[] data = dataStream.toByteArray();
			DatagramPacket  sendPacket = new DatagramPacket(data,
					data.length, InetAddress.getLocalHost(), ArrivedFloorEvent.SCHEDULER_LISTEN_PORT);
			sendSocket.send(sendPacket);
		} catch (InterruptedException | ElevatorException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receive a DatagramPacket from either the SchedulerSubsystem or the FloorSubsystem.
	 *
	 * @param fromFloor True if the DatagramPacket is originating from the FloorSubsystem, false if it is from Scheduler.
	 */
	private void receivePacket(boolean fromFloor) {
		byte[] request = new byte[256];
		try {
			// Receive a packet
			if (fromFloor) {
				receiveCBPE = new DatagramPacket(request, request.length);
				floorSocketReceiver.receive(receiveCBPE);
			}
			else {
				receiveMoveEvent = new DatagramPacket(request, request.length);
				schedulerSocketReceiver.receive(receiveMoveEvent);
			}
		} catch (IOException e) {
			// Display an error if the packet cannot be received
			// Terminate the program
			System.out.println("Error: Elevator cannot receive packet.");
			System.exit(1);
		}
	}

	private boolean doorsClosed() {
		return doorsClosed;
	}

	public static void main(String[] args) throws SocketException, FileNotFoundException {

		File requestFile = new File(Floor.REQUEST_FILE);
		Time time = new Time(Parser.getStartTime(requestFile));
		for(int i=0; i<4; i++) {
			Elevator elevator = new Elevator(i, 1, time);
			Thread elevatorToScheduler = new Thread(elevator);
			elevatorToScheduler.setName("E2S");
			Thread elevatorToFloor = new Thread(elevator);
			elevatorToFloor.setName("E2F");
			elevator.start();
		}
	}
}