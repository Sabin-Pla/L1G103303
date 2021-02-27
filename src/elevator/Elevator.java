package elevator;

import common.CarButtonEvent;
import floor.ElevatorException;
import floor.Floor;
import scheduler.Scheduler;
import common.Time;

import static java.lang.Thread.sleep;


/**
 * This class models the elevator
 *
 * @author Mmedara Josiah, Sabin Plaiasu
 * @version Iteration 2
 *
 */
public class Elevator {

	private final long MOVE_ONE_FLOOR_TIME = 10000;

	private Scheduler scheduler;
	private Integer currentFloor;
	private Floor[] floors;
	private Door door;
	static private Time time;

	/**
	 * Constructor
	 */
	public Elevator(Scheduler scheduler, Integer currentFloor, Time time) {
		this.scheduler = scheduler;
		this.time = time;
		this.currentFloor = currentFloor;
		this.door = new Door();

		synchronized (currentFloor) {
			this.currentFloor = currentFloor;
			currentFloor.notifyAll();
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
		try {
			System.out.println("\nElevator : destination " + destFloor);
			Floor floor = floors[currentFloor];
			synchronized (floor.getEventQueue()) {
				floor.getEventQueue().notify();
			}
			door.close();
			while (currentFloor != destFloor) {
				sleep((long) (MOVE_ONE_FLOOR_TIME / time.getCompressionFactor()));
				if (currentFloor > destFloor) {
					currentFloor -= 1;
					System.out.println("Moving down a floor");
				} else {
					currentFloor += 1;
					System.out.println("Moving up a floor");
				}
			}
			door.open();
			synchronized (floor.getEventQueue()) {
				floor.getEventQueue().notify();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
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
}