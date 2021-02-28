package elevator;

import common.CarButtonEvent;
import floor.ElevatorException;
import floor.Floor;
import scheduler.Scheduler;
import common.Time;


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
	private Floor[] floors;
	private Door door;
	static private Time time;
	private int destinationFloor;

	/**
	 * Constructor
	 */
	public Elevator(Scheduler scheduler, int currentFloor, Time time) {
		this.scheduler = scheduler;
		this.time = time;
		this.currentFloor = currentFloor;
		this.door = new Door();
		this.destinationFloor = currentFloor;

	}

	@Override
	public synchronized void run() {
		while (true) {
			while (currentFloor == destinationFloor) {
				door.open();
				try {
					notifyAll(); // let sensors know elevator has stopped
					wait();
				} catch (InterruptedException e) {
					door.open();
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

			notifyAll();
		}
	}

	public void setFloors(Floor[] floors) {
		this.floors = floors;
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
	public synchronized void move(int destFloor) {
		this.destinationFloor = destFloor;
		System.out.println("\nElevator: new destination floor " + destFloor);
		notifyAll();
	}

	public int getFloor() {
		return currentFloor;
	}

	public void sendEvent(CarButtonEvent event) {
		try {
			scheduler.setRequest(event);
		} catch (InterruptedException | ElevatorException e) {
			e.printStackTrace();
		}
	}

	public boolean isStopped() {
		return door.isOpen();
	}
}