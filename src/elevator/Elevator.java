package elevator;

import java.util.*;

import common.CarButtonEvent;
import common.InvalidDirectionException;
import common.TimeQueue;
import events.ElevatorEvent;
import floor.Floor;
import scheduler.Scheduler;
import common.Time;
import common.InvalidDirectionException;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;


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
    private HashMap<Integer, Floor> ThreadMap;
    private Time time;
	double compressionFactor;
	long startTime;

    /**
     * Constructor
     */
    public Elevator(Scheduler scheduler, Integer currentFloor) {
        this.scheduler = scheduler;
        this.currentFloor = currentFloor;
        this.time = new Time(compressionFactor, startTime);

        synchronized (currentFloor){
        	this.currentFloor = currentFloor;
        	notifyAll();
		}
    }

	/**
	 *
	 * @return Current floor
	 */
	public Integer getCurrentFloor(){
    	return currentFloor;
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

	/**
	 * This method represents the floor which elevator is moving to.
	 * @param destFloor Destination Floor
	 */
	public synchronized void move(int destFloor) throws InvalidDirectionException {
		while(true) {
			try {
				System.out.println("Moving to floor " + destFloor);
				sleep(1);
				currentFloor = destFloor;
				currentThread().notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//Elevator receives request from scehdular to move to a floor from Schedular. Throw in delay after.
    
    private boolean isStalled() {
    	if (scheduler.peekRequests() instanceof ElevatorEvent) {
    		return true;
    	}
		return false;
    }
    
    private void fulfillRequest(ElevatorEvent request) throws InterruptedException {
    	sleep(100); //simulate delay
    	currentFloor = request.getDestinationFloor();
    	scheduler.processedEvent(request);
    }

	public int getFloor() {
    	return currentFloor;
	}

	public void sendEvent(CarButtonEvent event) {
		try {
			scheduler.setRequest(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
