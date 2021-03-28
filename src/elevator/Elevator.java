package elevator;

import common.Parser;
import common.SimulationClock;
import floor.Floor;
import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.ElevatorMotorEvent;
import remote_procedure_events.FloorArrivalEvent;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.Duration;


/**
 * This class contains the entry point for the elevator subsystem.
 *
 * @author Mmedara Josiah, Sabin Plaiasu, John Afolayan
 * @version Iteration 4
 *
 */
public class Elevator extends Thread {

	private final long MOVE_ONE_FLOOR_TIME = 10000;

	private static SimulationClock clock;
	private int currentFloor;
	private int destinationFloor;
	private DatagramSocket sendSocket;
	private static  DatagramSocket floorSocketReceiver;
	private static DatagramSocket schedulerSocketReceiver;
	private boolean doorsClosed;
	private int elevatorNumber;
	private static Elevator[] elevators;

	public Elevator(int elevatorNumber, int currentFloor) throws SocketException {
		this.elevatorNumber = elevatorNumber;
		this.currentFloor = currentFloor;
		this.destinationFloor = currentFloor;
		this.doorsClosed = true;
		this.sendSocket = new DatagramSocket();
	}

	public static void setSchedulerSocketReceiver(int port) throws SocketException {
		schedulerSocketReceiver = new DatagramSocket(port);
	}

	public static void setFloorSocketReceiver(int port) throws SocketException {
		floorSocketReceiver = new DatagramSocket(port);
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
		FloorArrivalEvent fae = new FloorArrivalEvent(clock.instant(), elevatorNumber,  currentFloor, doorsClosed);
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

	public void engageMotor() throws InterruptedException {
		System.out.println("Elevator: moving...");
		try {
			Duration d = Duration.ofMillis(MOVE_ONE_FLOOR_TIME).dividedBy(clock.getCompressionFactor());
			sleep(d.toMillis());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (currentFloor > destinationFloor) {
			currentFloor -= 1;
			System.out.println("Elevator: Down a floor, now at " + currentFloor);
		} else {
			currentFloor += 1;
			System.out.println("Elevator: Up a floor, now at " + currentFloor);
		}
		if (currentFloor == destinationFloor) {
			doorsClosed = false;
			System.out.println("Reached destination floor: " + currentFloor);
		}
		reportMovement();
	}

	@Override
	public void run() {
		while (Thread.currentThread().getName().equals("motor")) {
			synchronized (this) {
				while (destinationFloor == currentFloor) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (destinationFloor == currentFloor) {
						System.out.println("Redundant motor event to current floor");
					}
				}
				notifyAll();
			}
			doorsClosed = true;
			try {
				engageMotor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		while (Thread.currentThread().getName().equals("button_handler")) {
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


	private static void listenToScheduler() {
		try {
			byte data[] = new byte[256];
			DatagramPacket moveEventPacket = new DatagramPacket(data, data.length);;
			schedulerSocketReceiver.receive(moveEventPacket);
			try {
				ByteArrayInputStream bainStream = new ByteArrayInputStream(moveEventPacket.getData());
				ObjectInputStream oinStream = new ObjectInputStream(bainStream);
				ElevatorMotorEvent eme = (ElevatorMotorEvent) oinStream.readObject();
				Elevator elevator = elevators[eme.getElevatorNumber()];
				System.out.println("New destination floor: " + eme.getArrivalFloor());
				synchronized (elevator) {
					elevator.destinationFloor = eme.getArrivalFloor();
					System.out.println("Destination updated");
					elevator.notifyAll();
				}
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

	public static void main(String[] args) throws SocketException, FileNotFoundException {
		elevators = new Elevator[Floor.NUM_ELEVATORS];
		File requestFile = new File(Floor.REQUEST_FILE);
		Parser p = new Parser(requestFile);
		clock = p.getClock();
		setSchedulerSocketReceiver(ElevatorMotorEvent.ELEVATOR_RECEIVE_PORT);
		setFloorSocketReceiver(CarButtonPressEvent.ELEVATOR_LISTEN_PORT);
		for(int i=0; i < Floor.NUM_ELEVATORS; i++) elevators[i] = new Elevator(i, 1);
		Thread motorThread = new Thread(elevators[0], "motor");
		Thread buttonThread = new Thread(elevators[0], "button_handler");
		motorThread.start();
		buttonThread.start();
		while (true) {
			listenToScheduler();
		}
	}
}