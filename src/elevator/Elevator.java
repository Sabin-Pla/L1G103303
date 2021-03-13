package elevator;

import common.CarButtonEvent;
import floor.ElevatorException;
import floor.Floor;
import remote_procedure_events.CarButtonPressEvent;
import scheduler.Scheduler;
import common.Time;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


/**
 * This class models the elevator
 *
 * @author Mmedara Josiah, Sabin Plaiasu
 * @version Iteration 2
 *
 */
public class Elevator extends Thread {

	private final long MOVE_ONE_FLOOR_TIME = 10000;

	private Scheduler scheduler;
	private int currentFloor;
	static private Time time;
	private int destinationFloor;
	private DatagramSocket sendSocket;
	private boolean doorsClosed;
	private int elevatorNumber;

	/**
	 * Constructor
	 */
	public Elevator(int elevatorNumber, int currentFloor, Time time) throws SocketException {
		this.elevatorNumber = elevatorNumber;
		this.currentFloor = currentFloor;
		this.destinationFloor = currentFloor;
		this.time = time;

		this.doorsClosed = true;
		this.sendSocket = new DatagramSocket();
	}

	@Override
	public void run() {
		while (true) {
			while (currentFloor == destinationFloor) {
				doorsClosed = false;
				try {
					notifyAll(); // let sensors know elevator has stopped
					wait();
				} catch (InterruptedException e) {
					doorsClosed = true;
				}
			}

			while (scheduler.getLastSensor() != currentFloor) {
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

	public int getFloor() {
		return currentFloor;
	}

	public void sendCarButtonPress(CarButtonEvent event) {
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

	public void reportArrival() {
		// reports arrival to Floor and Scheduler
	}

	public boolean doorsClosed() {
		return doorsClosed;
	}
}