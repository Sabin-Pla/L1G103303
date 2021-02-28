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

	public int getLastSensor() {
		return lastSensor;
	}

	private enum State {MONITORING_ELEVATOR, IDLING, HANDLING_EVENT};
	private State state;

	/**
	 * Constructor
	 */
	public Scheduler(Time time) {
		this.time = time;
		timeQueue = new TimeQueue();
		state = State.IDLING;
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
			while (state == State.IDLING) {
				try {
					wait();
				} catch (InterruptedException e) {}
			}

			while (state == State.HANDLING_EVENT) {
				fulfillNextRequest();
			}

			while (state == state.MONITORING_ELEVATOR) {
				try {
					monitorElevator();
				} catch (ElevatorException e) {
					e.printStackTrace();
					state = State.IDLING;
				}
			}
		}
	}

	private synchronized void monitorElevator() throws ElevatorException {
		long timeoutTime = timeEvent.getEventTime() + MAXIMUM_ACCEPTABLE_WAIT_TIME;

		while (!(time.now() > timeoutTime )) {
			try {
				wait();
			} catch (InterruptedException elevatorMoved) {}

			int currentFloor = lastSensor;
			System.out.println("\nScheduler: Monitoring... elevator at floor " + currentFloor);
			if (currentFloor == nextDestination) {
				System.out.println("\nScheduler: Destination floor reached");
				if (timeQueue.isEmpty()) {
					state = State.IDLING;
				} else {
					state = State.HANDLING_EVENT;
				}
				return;
			} else if (currentFloor != nextExpectedFloor) {
				throw new ElevatorException("Elevator not going in dispatched direction : " +
						currentFloor + " " + nextExpectedFloor);
			}

			if (nextDestination > currentFloor) {
				nextExpectedFloor = currentFloor + 1;
			} else {
				nextExpectedFloor = currentFloor - 1;
			}
		}
		throw new ElevatorException("Elevator took too long to fulfill request");
	}

	/**
	 * Scheduler moves the elevator the the appropriate floor depending on the request type
	 */
	private void fulfillNextRequest() {
		//select and remove the request at the head of the time queue
		timeEvent = (TimeEvent) timeQueue.poll();

		System.out.print("\nScheduler: ");

		int currentFloor = elevator.getCurrentFloor();
		//if the event is a requestElevatorEvent, move the elevator to the floor to pick up the passenger
		if (timeEvent instanceof RequestElevatorEvent) {
			System.out.println("Elevator should pick up passenger on floor " + ((RequestElevatorEvent) timeEvent).getFloor());
			nextDestination = ((RequestElevatorEvent) timeEvent).getFloor();
		}
		//if the event is a CarButtonEvent, move the elevator to the floor to drop off the passenger
		else if (timeEvent instanceof CarButtonEvent) {
			System.out.println("Elevator should drop off passenger on floor " + ((CarButtonEvent) timeEvent).getDestinationFloor());
			nextDestination = ((CarButtonEvent) timeEvent).getDestinationFloor();
		}

		if (nextDestination > currentFloor) {
			nextExpectedFloor = currentFloor + 1;
		} else {
			nextExpectedFloor = currentFloor - 1;
		}

		System.out.println("next expected floor: " + nextExpectedFloor);

		elevator.move(nextDestination);
		state = State.MONITORING_ELEVATOR;
	}

	public synchronized void sensorActivated(int floorNumber) {
		while (floorNumber != lastSensor) {
			System.out.println("\nScheduler: Sensor at floor " + floorNumber + " activated");
			System.out.println("Current state: " + state);
			lastSensor = floorNumber;
		}
		notifyAll();
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
		if (state != State.HANDLING_EVENT) {
			state = State.HANDLING_EVENT;
			notifyAll();
		}
	}
}