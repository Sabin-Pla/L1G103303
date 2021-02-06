

import elevator.Elevator;
import floor.Floor;
import scheduler.Scheduler;

/**
 * This is the main class that starts all the threads
 * 
 * @version Iteration 1
 */
public class Main {

	public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        Thread floorThread = new Thread(new Floor(scheduler), "Floor");
        Thread schedulerThread = new Thread(scheduler, "Scheduler");
        Thread elevatorThread = new Thread(new Elevator(scheduler), "Elevator");

        floorThread.start();
        schedulerThread.start();
        elevatorThread.start();
	}
}
