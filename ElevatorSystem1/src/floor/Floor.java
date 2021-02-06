package floor;

import java.util.ArrayDeque;
import java.util.Deque;

import scheduler.Scheduler;

/**
 * The floor class sends a request to the scheduler and 
 * asks the scheduler for a new request
 * 
 * @author Sabin Plaiasu
 * @version Iteration 1
 */
public class Floor implements Runnable {
	
	private Deque<DataStorage> requestQueue; //this queue stores all the requests to be fulfilled by the floor
	private static Scheduler scheduler;
	
	/**
	 * Constructor initializes all variables
	 */
	public Floor(Scheduler scheduler) {
		requestQueue = new ArrayDeque<>();
		this.scheduler = scheduler;
	}
	
	/**
	 * Runs the floor thread
	 */
	@Override
	public void run() {
		int numberOfRequests = getNumberOfRequests();
		int requestCount = 0;
		
		while(true) {
			try {
				if(requestCount < numberOfRequests) {
					System.out.println("\nRequest " + ++requestCount);
					//send the first request in the queue to the scheduler
					scheduler.setRequest(requestQueue.pop());
					//sleep for a bit to avoid asking for the same request that was just sent
					Thread.sleep(100);
					//ask for a new request from the scheduler and print it out
					System.out.println("Floor has received a request from the Scheduler: " + "\n" + scheduler.getNewRequest().toString() + "\nDone");
				}
				else {
					System.exit(0);
				}
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Gets request from file and adds them to the floor's request queue 
	 * returns the number of requests
	 * 
	 * @return the number of requests
	 */
	public int getNumberOfRequests() {
		Parser p = new Parser();
		requestQueue = p.getRequestFromFile();
		return requestQueue.size();
	}
	
	/**
	 * Print all the requests in the request queue
	 */
	public void printRequests() {
		for(DataStorage request: requestQueue) {
			System.out.println(request);
		}
	}

}
