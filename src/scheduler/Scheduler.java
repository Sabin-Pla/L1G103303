package scheduler;

import java.util.ArrayDeque;
import java.util.Deque;

import events.ElevatorEvent;
import events.FloorEvent;
import events.RequestEvent;

/**
 * This scheduler is the middle man between the elevator and
 * the floor sub systems. It schedules requests for both subsystems
 * 
 * @author Harshil Verma
 * @version Iteration 1
 */
public class Scheduler implements Runnable {
	private Deque<RequestEvent> requestQueue; // TODO: find a better data structure. Double ended queue should not be used
	
	/**
	 * Constructor
	 */
	public Scheduler() {
		requestQueue = new ArrayDeque<>();
	}

	/**
	 * Runs the scheduler thread
	 */
	@Override
	public synchronized void run() {
		while (true) {
			while (requestQueue.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {}
				fulfillNextRequest();
			}
		}
	}
	
	
	private void fulfillNextRequest() {
		RequestEvent event = requestQueue.pop();
		// scheduler is given FloorEvents by floor
		// scheduler returns ElevatorEvents to floor when 
		
		if (event instanceof FloorEvent) {
			
		} else if (event instanceof ElevatorEvent) {
			// Should move elevator when implemented. For now simply sends data back to floor
		}
		
	}

	/**
	 * return the first request in the queue if the queue is not empty
	 * else, wait
	 * 
	 * @return the first request in the queue
	 * @throws InterruptedException
	 */
	public synchronized RequestEvent getNewRequest() throws InterruptedException {
		//wait till the request queue has at least one request
		if(requestQueue.isEmpty()) {
			wait();
		}
		notifyAll();		
		return requestQueue.pop();
	}
	
	public RequestEvent peekRequests() {
		return requestQueue.peek();
	}
	
	/**
	 * Stores an incoming request in the scheduler's queue
	 * 
	 * @param request
	 * @throws InterruptedException
	 */
	public synchronized void setRequest(RequestEvent request) throws InterruptedException {
		requestQueue.add(request);
		System.out.println("Scheduler has received a request from " + Thread.currentThread().getName() + ":\n" + request);
		notifyAll();
	}

	public void processedEvent(ElevatorEvent event) {
		System.out.println("Elevator has processed event:\n" + event.toString());
	}

}