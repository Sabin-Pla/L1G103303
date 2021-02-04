package elevatorSubSystem;

import java.util.*;

import floorSubSystem.DataStorage;
import scheduler.Scheduler;

/**
 * This class models the elevator
 * in iteration 1, the elevator receives info from the scheduler,
 * uses it and sends it back to the scheduler
 * 
 * @author Mmedara Josiah 101053887
 * @version 1.0
 *
 */
public class Elevator implements Runnable {
    private Scheduler scheduler;
    private Deque<DataStorage> requestQueue; //a queue containing all requests for the elevator ..
    private DataStorage currentRequest;

    /**
     * Constructor
     */
    public Elevator(Scheduler scheduler) {
        requestQueue = new ArrayDeque<DataStorage>();
        this.scheduler = scheduler;
    }
    
    /**
     * This method runs the elevator thread
     */
    @Override
	public void run() {
		while (true) {
            try {
                Thread.sleep(100);
                //add a request to the elevators request queue
                requestQueue.add(scheduler.getRequest());
                //pop the request queue to return the info received from the scheduler
                currentRequest = requestQueue.pop();
                //print confirmation of data received
                System.out.println("Elevator received information from Scheduler: " + currentRequest.toString()); 
                System.out.println(toString());
                //give request information back to the scheduler
                scheduler.setRequest(currentRequest);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
	}

    /**
     * This method returns the current movement of the elevator
     * 
     * @return the current movement of the elevator
     */
    @Override
    public String toString() {
        return "At time " + currentRequest.getRequestTime() + ", the elevator is moving "
                + (currentRequest.getGoingUp() ? "up" : "down") + " from floor " + currentRequest.getCurrentFloor()
                + " to floor " + currentRequest.getDestinationFloor();
    }
}
