package elevator;

import common.Parser;
import common.Time;
import floor.Floor;
import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.ElevatorMotorEvent;
import remote_procedure_events.FloorArrivalEvent;

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

	private Time time;
	private int currentFloor;
	private int destinationFloor;
	private DatagramSocket sendSocket, floorSocketReceiver, schedulerSocketReceiver;
	private boolean doorsClosed;
	private int elevatorNumber;

	/**
	 * Constructor
	 */
	public Elevator(int elevatorNumber, int currentFloor) throws SocketException {
		this.elevatorNumber = elevatorNumber;
		this.currentFloor = currentFloor;
		this.destinationFloor = currentFloor;
		this.doorsClosed = true;
		this.schedulerSocketReceiver = new DatagramSocket(ElevatorMotorEvent.ELEVATOR_RECEIVE_PORT);
		this.floorSocketReceiver = new DatagramSocket(CarButtonPressEvent.ELEVATOR_LISTEN_PORT);
		this.sendSocket = new DatagramSocket();
	}

	private boolean doorsClosed() {
		return doorsClosed;
	}

	private void setTime(Time time) {
		this.time = time;
	}

	/**
	 * This method represents the floor which elevator is moving to.
	 * @param destFloor Destination Floor
	 */
	public void move(int destFloor) {
		this.destinationFloor = destFloor;
		System.out.println("Elevator: new destination floor " + destFloor);
		notifyAll();
	}

	private int getFloor() {
		return currentFloor;
	}

	private void forwardButtonPress(byte[] data) {
		try {
			DatagramPacket packet = new DatagramPacket(data,
					data.length, InetAddress.getLocalHost(), CarButtonPressEvent.SCHEDULER_LISTEN_PORT);
			sendSocket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void reportMovement() {
		FloorArrivalEvent fae = new FloorArrivalEvent(time.now(), elevatorNumber,  currentFloor);
		try {
			ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(dataStream);
			out.writeObject(fae);
			out.close();

			byte[] data = dataStream.toByteArray();
			DatagramPacket schedulerPacket = new DatagramPacket(data,
					data.length, InetAddress.getLocalHost(), FloorArrivalEvent.SCHEDULER_LISTEN_PORT);
			sendSocket.send(schedulerPacket);
			DatagramPacket floorPacket = new DatagramPacket(data,
					data.length, InetAddress.getLocalHost(), FloorArrivalEvent.FLOOR_LISTEN_PORT);
			sendSocket.send(floorPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (Thread.currentThread().getName().equals("main")) {
			while (currentFloor == destinationFloor) {
				doorsClosed = false;
				try {
					byte data[] = new byte[256];
					DatagramPacket moveEventPacket = new DatagramPacket(data, data.length);;
					schedulerSocketReceiver.receive(moveEventPacket);
					try {
						ByteArrayInputStream bainStream = new ByteArrayInputStream(moveEventPacket.getData());
						ObjectInputStream oinStream = new ObjectInputStream(bainStream);
						ElevatorMotorEvent eme = (ElevatorMotorEvent) oinStream.readObject();
						destinationFloor = eme.getArrivalFloor();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						return;
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			doorsClosed = false;
			System.out.println("Elevator: moving...");

			try {
				sleep((long) (MOVE_ONE_FLOOR_TIME / time.getCompressionFactor()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (currentFloor > destinationFloor) {
				currentFloor -= 1;
				System.out.println("Elevator: Down 1 floor, now at " + currentFloor);
			} else {
				currentFloor += 1;
				System.out.println("Elevator: Up 1 floor, now at " + currentFloor);
			}

			reportMovement();
		}

		while (Thread.currentThread().getName().equals("FloorListener")) {
			byte data[] = new byte[256];
			DatagramPacket cbpePacket = new DatagramPacket(data, data.length);;
			try {
				floorSocketReceiver.receive(cbpePacket);
				forwardButtonPress(cbpePacket.getData());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	public static void main(String[] args) throws SocketException, FileNotFoundException {
		File requestFile = new File(Floor.REQUEST_FILE);
		Time time = new Time(Time.SECOND_TO_MINUTE / 2, Parser.getStartTime(requestFile));
		for(int i=0; i<4; i++) {
			Elevator elevator = new Elevator(i, 1);
			Thread mainThread = new Thread(elevator, "main");
			Thread floorListener = new Thread(elevator, "FloorListener");
			mainThread.start();
			floorListener.start();
			elevator.setTime(time);
		}
	}
}