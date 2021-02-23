

import elevator.Elevator;
import floor.Floor;
import floor.Parser;
import scheduler.Scheduler;

/**
 * This is the main class that starts all the threads
 * 
 * @version Iteration 1
 */
public class Main {

	public static void main(String[] args) {
		Parser.getRequestFromFile();
        Scheduler scheduler = new Scheduler();
        Floor floor1 = new Floor(1);
        floor1.setScheduler(scheduler);
        Thread floorThread1 = new Thread(floor1, "Floor1");
        Thread floorThread2 = new Thread(new Floor(2), "Floor2");
        Thread schedulerThread = new Thread(scheduler, "Scheduler");
        Thread elevatorThread = new Thread(new Elevator(scheduler), "Elevator");

        floorThread1.start();
        floorThread2.start();
        schedulerThread.start();
        elevatorThread.start();
	}
}
