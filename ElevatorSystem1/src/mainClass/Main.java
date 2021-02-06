package mainClass;

import elevatorSubSystem.Elevator;
import floorSubSystem.Floor;
import scheduler.Scheduler;

/**
 * This is the main class that starts all the threads
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
