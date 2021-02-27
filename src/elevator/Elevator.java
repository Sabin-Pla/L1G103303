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
	private Integer currentFloor;
	private Floor[] floors;
	private Door door;
	static private Time time;
	private int destFloor;

	/**
	 * Constructor
	 */
	public Elevator(Scheduler scheduler, Integer currentFloor, Time time) {
		this.scheduler = scheduler;
		this.time = time;
		this.currentFloor = currentFloor;
		this.door = new Door();
		this.destFloor = currentFloor;

		synchronized (this.currentFloor) {
			this.currentFloor = currentFloor;
			this.currentFloor.notifyAll();
		}
	}

	@Override
	public void run() {
		while (true) {
			synchronized (currentFloor) {
				while (currentFloor == destFloor) {
					try {
						currentFloor.wait();
					} catch (InterruptedException e) {}
				}

				try {
					sleep((long) (MOVE_ONE_FLOOR_TIME / time.getCompressionFactor()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (currentFloor > destFloor) {
					currentFloor -= 1;
					System.out.println("Moving down a floor, now at " + currentFloor);
				} else {
					currentFloor += 1;
					System.out.println("Moving up a floor, now at " + currentFloor);
				}

				currentFloor.notifyAll();
			}
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
	public void move(int destFloor) {
		this.destFloor = destFloor;
		synchronized (currentFloor) {
			currentFloor.notifyAll();
		}
	}


	public Integer getFloor() {
		synchronized (currentFloor) {
			return currentFloor;
		}
	}

	public void sendEvent(CarButtonEvent event) {
		try {
			scheduler.setRequest(event);
		} catch (InterruptedException | ElevatorException e) {
			e.printStackTrace();
		}
	}

	public boolean isStopped() {
		return door.isOpen;
	}
}