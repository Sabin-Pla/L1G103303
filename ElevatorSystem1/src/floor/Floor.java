package floor;

import java.util.HashMap;

import elevator.Elevator;
import events.ElevatorEvent;
import events.FloorEvent;
import events.RequestEvent;
import scheduler.Scheduler;

/**
 * The floor class sends a request to the scheduler and 
 * asks the scheduler for a new request
 * 
 * @author Sabin Plaiasu
 * @version Iteration 1
 */
public class Floor implements Runnable {
	private static Scheduler scheduler;
	private static Elevator elevator;
	private HashMap<FloorEvent, Integer> destinationMap  = new HashMap<FloorEvent, Integer>(); // used to get car button for when elevator arrives
	private int floorNumber;
	
	/**
	 * Constructor initializes all variables
	 */
	public Floor(int floorNumber) {
		this.floorNumber = floorNumber;
	}
	
	public void setScheduler(Scheduler scheduler) {
		Floor.scheduler = scheduler;
	}
	
	public void setElevator(Elevator elevator) {
		Floor.elevator = elevator;
	}
	
	/**
	 * Runs the floor thread
	 */
	@Override
	public void run() {
		try {
			RequestPairing pairing = Parser.getNextRequest(floorNumber);
			while (pairing != null) {
				FloorEvent event = pairing.getFloorRequest();
				destinationMap.put(event, pairing.getDestinationFloor());
				//send the first request in the queue to the scheduler
				scheduler.setRequest(event);
				
				while (elevatorArrived()) {
					wait();
				}
				
				Thread.sleep(100);
				
				int destinationFloor = destinationMap.get(scheduler.getNewRequest());
				ElevatorEvent elevatorEvent = new ElevatorEvent(floorNumber, destinationFloor);
				try {
					scheduler.setRequest(elevatorEvent);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * returns the floor at which an elevator arrived
	 * If the elevator arrived to drop off a passenger 
	 * (i,e, if the top of the request queue is an ElevatorEvent,
	 * then prints a message to the console and returns 0 
	 * @return floor at which an el
	 */
	public boolean elevatorArrived() {
		RequestEvent event = scheduler.peekRequests(); // get the event scheduler just fulfilled
		if (event instanceof FloorEvent) {
			FloorEvent floorEvent = (FloorEvent) event;
			Integer destinationFloor = destinationMap.get(floorEvent);
			if (destinationFloor != null) {
				return destinationFloor == floorNumber;
			}
		} else if (event instanceof ElevatorEvent) {
			System.out.println("Passenger has reached their destination floor. Event: \n" + event.toString());
		}
		return false;
	}
	
	public void elevatorArrived(FloorEvent floorEvent) {
		// elevator has arrived at floor to fulfill request (accept a destination floor)
		int destinationFloor = destinationMap.get(floorEvent);
		ElevatorEvent elevatorEvent = new ElevatorEvent(floorNumber, destinationFloor);
		try {
			scheduler.setRequest(elevatorEvent);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
