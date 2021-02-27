import common.Parser;
import elevator.Elevator;
import floor.Floor;
import floor.Lamp;
import scheduler.Scheduler;

import java.io.File;
import java.util.ArrayList;

/**
 * This is the main class that starts all the threads
 * 
 * @version Iteration 1
 */
public class Main {
    static final int NUMBER_OF_FLOORS = 10;
    private static final String REQUEST_FILE = "src/requestsFile.txt";

	public static void main(String[] args) {
	    startThreads(NUMBER_OF_FLOORS, REQUEST_FILE);
	}

	public static void startThreads(int floors, String fileName) {
	    File requestFile = new File(fileName);
        Parser.getRequestFromFile(requestFile);
        Scheduler scheduler = new Scheduler();
        Thread schedulerThread = new Thread(scheduler, "Scheduler");
        Elevator elevator = new Elevator(scheduler);
        Thread elevatorThread = new Thread(elevator, "Elevator");

        schedulerThread.start();
        elevatorThread.start();

        ArrayList<Thread> floorThreads = new ArrayList<>();

        for (int i=0; i < floors; i++) {
            Lamp lamp = new Lamp(false);
            if (i==0) lamp.turnOn();
            Floor floor = new Floor(i+1, lamp);
            Thread thread = new Thread(floor, "Floor " + i + 1);
            floorThreads.add(thread);
            if (i == 0) floor.setScheduler(scheduler);
            thread.start();
        }
    }
}
