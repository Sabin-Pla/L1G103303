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
	public void run() {
    	synchronized (scheduler) {
			while (true) {
				try {
					while (isStalled()) {
							wait();
					}
					ElevatorEvent next = (ElevatorEvent) scheduler.peekRequests();
					System.out.println("Elevator has received a request from the Scheduler:\n" + 
							next.toString()); 
					fulfillRequest(next);
					notifyAll();
				} catch (InterruptedException e) {}
	        }
    	}
	}
    
    private boolean isStalled() {
    	if (scheduler.peekRequests() instanceof ElevatorEvent) {
    		return true;
    	}
		return false;
    }
    
    private void fulfillRequest(ElevatorEvent request) throws InterruptedException {
    	Thread.sleep(100); //simulate delay
    	currentFloor = request.getDestinationFloor();
    	scheduler.processedEvent(request);
    }
}
