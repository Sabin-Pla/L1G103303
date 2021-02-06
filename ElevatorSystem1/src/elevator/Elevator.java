package elevator;

import java.util.*;
import events.ElevatorEvent;
import scheduler.Scheduler;

/**
 * This class models the elevator
 * in iteration 1, the elevator receives info from the scheduler,
 * uses it and sends it back to the scheduler
 * 
 * @author Mmedara Josiah, Sabin Plaiasu
 * @version Iteration 1
 *
 */
public class Elevator implements Runnable {
    private Scheduler scheduler;
    private Queue<ElevatorEvent> requests;
    // requests must contain a valid queue of inputs
    // i.e, the destination floor in one event must be the source floor in the next
    private int currentFloor;
    
    /**
     * Constructor
     */
    public Elevator(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    /**
     * This method runs the elevator thread
     */
    @Override
	public synchronized void run() {
		while (true) {
			try {
				while (requests.isEmpty()) {
						wait();
				}
				ElevatorEvent next = requests.poll();
				System.out.println("Elevator has received a request from the Scheduler:\n" + 
						next.toString()); 
				fulfillRequest(next);
				notifyAll();
			} catch (InterruptedException e) {}
        }
	}
    
    private void fulfillRequest(ElevatorEvent request) throws InterruptedException {
    	ElevatorEvent next = requests.poll();
    	Thread.sleep(100); //simulate delay, remove next iteration
    	currentFloor = next.getDestinationFloor();
    	scheduler.processedEvent(next);
    }
}
