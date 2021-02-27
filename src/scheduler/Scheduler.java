package scheduler;

import common.*;
import elevator.Elevator;
import floor.ElevatorException;

/**
 * This scheduler is the middle man between the elevator and
 * the floor sub systems. It schedules requests for both subsystems
 *
 * @author Harshil Verma, Mmedara Josiah
 * @version Iteration 2
 */
public class Scheduler implements Runnable {

	// maximum amount of time (ms) an elevator should take to fulfill any request
	private final long MAXIMUM_ACCEPTABLE_WAIT_TIME = 60 * 5 * 1000;

	private TimeQueue timeQueue;
	private TimeEvent timeEvent;
	private Elevator elevator;
	private Time time;

	private int lastSensor; // the floor at which the last sensor was activated

	private int nextDestination;
	private int nextExpectedFloor;

	/**
	 * Constructor
	 */
	public Scheduler(Time time) {
		this.time = time;
		timeQueue = new TimeQueue();
	}

	/**
	 * Sets the elevator
	 *
	 * @param elevator the elevator
	 */
	public void setElevator(Elevator elevator) {
		this.elevator = elevator;
		this.lastSensor = elevator.getCurrentFloor();
	}

	/**
	 * Runs the scheduler thread
	 */
	@Override
	public synchronized void run() {
		while (true) {
			while (timeQueue.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}
			fulfillNextRequest();
			try {
				monitorElevator();
			} catch (ElevatorException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void monitorElevator() throws ElevatorException {
		long timeoutTime = timeEvent.getEventTime() + MAXIMUM_ACCEPTABLE_WAIT_TIME;
		while (!(time.now() > timeoutTime )) {
			try {
				wait();
			} catch (InterruptedException elevatorMoved) {
				int currentFloor = elevator.getCurrentFloor();
				if (currentFloor == nextDestination) {
					return;
				}
				if (currentFloor != nextExpectedFloor) {
					throw new ElevatorException("Elevator not going in dispatched direction");
				}
				if (nextDestination > currentFloor) {
					nextExpectedFloor++;
				} else {
					nextExpectedFloor--;
				}
			}
		}
		throw new ElevatorException("Elevator took more than 3 minutes to fulfill request");
	}

	/**
	 * Scheduler moves the elevator the the appropriate floor depending on the request type
	 */
	private void fulfillNextRequest() {
		//select and remove the request at the head of the time queue
		timeEvent = (TimeEvent) timeQueue.poll();

		System.out.println("\nScheduler:");

		//if the event is a requestElevatorEvent, move the elevator to the floor to pick up the passenger
		if (timeEvent instanceof RequestElevatorEvent) {
			System.out.println("Elevator should pick up passenger on floor " + ((RequestElevatorEvent) timeEvent).getFloor());

			int currentFloor = elevator.getCurrentFloor();
			int nextFloor = ((RequestElevatorEvent) timeEvent).getFloor();
			if (nextFloor > currentFloor) {
				nextExpectedFloor = currentFloor + 1;
			} else {
				nextExpectedFloor = currentFloor - 1;
			}

			elevator.move(nextFloor);
		}
		//if the event is a CarButtonEvent, move the elevator to the floor to drop off the passenger
		else if (timeEvent instanceof CarButtonEvent) {
			System.out.println("Elevator should drop off passenger on floor " + ((CarButtonEvent) timeEvent).getDestinationFloor());

			int currentFloor = elevator.getCurrentFloor();
			int nextFloor = ((CarButtonEvent) timeEvent).getDestinationFloor();
			if (nextFloor > currentFloor) {
				nextExpectedFloor = currentFloor + 1;
			} else {
				nextExpectedFloor = currentFloor - 1;
			}

			elevator.move(nextFloor);
		}
	}

	public synchronized void sensorActivated(int floorNumber) {
		if (floorNumber != lastSensor) {
			System.out.println("Scheduler:  Elevator at floor " + floorNumber);
			notifyAll();
		}
	}

	/**
	 * Stores an incoming request in the scheduler's queue
	 *
	 * @param request
	 * @throws InterruptedException
	 */
	public synchronized void setRequest(TimeEvent request) throws InterruptedException, ElevatorException {
		if (!timeQueue.add(request)) {
			throw new ElevatorException("Cannot schedule event in the past!");
		}
		notifyAll();
	}
}