package scheduler;

import java.util.ArrayDeque;
import java.util.Deque;

import floorSubSystem.DataStorage;

/**
 * This scheduler is the middle man between the elevator and
 * the floor sub systems. It schedules requests for both subsystems
 * 
 * @author
 * @version Iteration 1
 */
public class Scheduler implements Runnable {
	private Deque<DataStorage> requestQueue;
	
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
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * return the first request in the queue if the queue is not empty
	 * else, wait
	 * 
	 * @return the first request in the queue
	 * @throws InterruptedException
	 */
	public synchronized DataStorage getNewRequest() throws InterruptedException {
		//wait till the request queue has at least one request
		if(requestQueue.isEmpty()) {
			wait();
		}
		notifyAll();		
		return requestQueue.pop();
	}
	
	/**
	 * Stores an incoming request in the scheduler's queue
	 * 
	 * @param request
	 * @throws InterruptedException
	 */
	public synchronized void setRequest(DataStorage request) throws InterruptedException {
		if(!requestQueue.isEmpty()) {
			wait();
		}
		requestQueue.add(request);
		System.out.println("Scheduler has received a request from " + Thread.currentThread().getName() + ":\n" + request);
		notifyAll();
	}

}
