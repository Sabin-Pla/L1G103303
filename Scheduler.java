package scheduler;

import java.util.ArrayDeque;
import java.util.Deque;

import elevator.Elevator;
import events.ElevatorEvent;
import events.FloorEvent;
import events.RequestEvent;

/**
 * This scheduler is the middle man between the elevator and
 * the floor sub systems. It schedules requests for both subsystems
 * 
 * @author Harshil Verma, Mmedara Josiah
 * @version Iteration 2
 */
public class Scheduler implements Runnable {
	private Deque<RequestEvent> requestEventQueue;
	private RequestEvent requestEvent;
	private Elevator elevator;
	/**
	 * Constructor
	 */
	public Scheduler() {
		requestEventQueue = new ArrayDeque<>();
		requestEvent = null;
		elevator = null;
	}
	
	public Scheduler(Elevator elevator) {
		requestEventQueue = new ArrayDeque<>();
		requestEvent = null;
		this.elevator = elevator;
	}

	/**
	 * Runs the scheduler thread
	 */
	@Override
	public synchronized void run() {
		while (true) {
			while (requestEventQueue.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {}
				fulfillNextRequest();
			}
		}
	}
	
	
	private void fulfillNextRequest() {
		RequestEvent event = requestEventQueue.pop();
		// scheduler is given FloorEvents by floor
		// scheduler returns ElevatorEvents to floor when 
		if (event instanceof FloorEvent) {
			
		} else if (event instanceof ElevatorEvent) {
			// Should move elevator when implemented. For now simply sends data back to floor
		}
		
	}

	/**
	 * Get all notified requests from the elevator
	 * 
	 * @return the request passed by the elevator once it reached destination floor
	 */
	public synchronized RequestEvent getNotifiedRequest() {
		if(requestEvent != null) {
			RequestEvent request = requestEvent;
			requestEvent = null;
			return request;
		}
		return requestEvent;
	}
	
	/**
	 * Notifies the scheduler
	 * @param requestData request being sent from the elevator or from the floor
	 * @throws InterruptedException
	 */
	public synchronized void notifyScheduler(RequestEvent requestData) throws InterruptedException {
		requestEvent = requestData;
		System.out.print("Scheduler received information from " + Thread.currentThread().getName() + ": " + requestData);
		notifyAll();
	}
	
	public RequestEvent peekRequests() {
		return requestEventQueue.peek();
	}
	
	/**
	 * Stores an incoming request in the scheduler's queue
	 * 
	 * @param request
	 * @throws InterruptedException
	 */
	public synchronized void setRequest(RequestEvent request) throws InterruptedException {
		if(!this.requestEventQueue.isEmpty()) {
			this.wait();
		}
		System.out.println("Scheduler has received a request from " + Thread.currentThread().getName() + ":\n" + request);
		elevator.addToQueue(request);
		notifyAll();
	}

	public void processedEvent(ElevatorEvent event) {
		System.out.println("Elevator has processed event:\n" + event.toString());
	}

}
