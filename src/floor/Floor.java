package floor;

import common.*;
import elevator.Elevator;

import scheduler.Scheduler;

import java.util.Date;

/**
 * The floor class sends requests to the schedulers and operates its floor lamp.
 *
 * Modelled by a state machine with 2 states:
 *  OPERATING_LAMP and SENDING_EVENTS
 *
 * A Floor thread must be notified every time the elevator comes and leaves at its floor
 * 
 * @author Sabin Plaiasu
 * @version Iteration 2
 */
public class Floor extends Thread {

	// the minimum amount of time between which a floor thread should check to see if it should send events
	private final long MINIMUM_WAIT_TIME = 5;
	private final long MAXIMUM_WAIT_TIME = 120000; // max amount of time (ms) a thread should wait for an elevator
	private static Scheduler scheduler;
	private static Elevator elevator;
	private int floorNumber;
	private TimeQueue eventQueue;
	private Lamp lamp;

	public enum State {OPERATING_LAMP, SENDING_EVENTS}
	private State state;
	private TimeQueue carButtonEvents;
	private static int numFloors;

	/**
	 * Constructor initializes all variables
	 */
	public Floor(int floorNumber, TimeQueue eventQueue, Lamp lamp) {
		this.floorNumber = floorNumber;
		this.eventQueue = eventQueue;
		this.lamp = lamp;
		if (eventQueue.isEmpty()) {
			state = State.OPERATING_LAMP;
		} else {
			state = State.SENDING_EVENTS;
		}
		this.carButtonEvents = new TimeQueue();
		this.setName("Floor " + floorNumber);
	}

	/**
	 * A Floor with a state machine for operating its floor lamp and sending requests to the scheduler.
	 * A Floor thread must be notified every time the elevator comes and leaves at its floor
	 *
	 * @param floorNumber The number of floor (counting from floor being ground)
	 * @param lamp The lamp at floor
	 */
	public Floor(int floorNumber, Lamp lamp) {
		this.floorNumber = floorNumber;
		this.eventQueue = new TimeQueue();
		this.state = State.OPERATING_LAMP;
		this.lamp = lamp;
		this.carButtonEvents = new TimeQueue();
	}

	/**
	 * gets the elevator
	 *
	 * @return the elevator
	 */
	public Elevator getElevator() {
		return  this.elevator;
	}

	/**
	 * Returns the event queue
	 *
	 * @return the event queue
	 */
	public TimeQueue getEventQueue() {
		return eventQueue;
	}

	/**
	 * Gets the number of the floor
	 *
	 * @return the floor number
	 */
	public int getFloorNumber() {
		return floorNumber;
	}

	/**
	 * Sets the scheduler to be used by all Floors
	 *
	 * @param scheduler the scheduler to be used by all floors
	 */
	public void setScheduler(Scheduler scheduler) {
		Floor.scheduler = scheduler;
	}

	/**
	 * Sets the elevator to be used by all floors
	 *
	 * @param elevator the elevator to be used all floors
	 */
	public void setElevator(Elevator elevator) {
		Floor.elevator = elevator;
	}

	/**
	 * Sets the number of floors
	 *
	 * @param numFloors number of floors in the building
	 */
	public void setNumFloors(int numFloors) {
		this.numFloors = numFloors;
	}

	/**
	 * Runs the floor thread
	 */
	@Override
	public void run() {
		while (true) {
			switch (state) {
				case OPERATING_LAMP:
					elevatorArrival();
					if (!eventQueue.isEmpty()) switchState();
					break;
				case SENDING_EVENTS:
					try {
						sendEvents();
						switchState(); // reached if sendEvents() naturally returned by condition !eventQueue.isEmpty()
					} catch (InterruptedException elevatorInterrupt) {
						elevatorArrival();
					}
			}
		}
	}

	public void switchState() {
		if (state == State.OPERATING_LAMP) {
			state = State.SENDING_EVENTS;
		} else {
			state = State.OPERATING_LAMP;
		}
	}

	/**
	 * Sends requestElevatorEvents as they expire off the event queue.
	 * Once the events are said, add their corresponding CarButtonPressEvent to the car button event queue
	 *
	 * @throws InterruptedException if the elevator has arrived
	 */
	public void sendEvents() throws InterruptedException {
		synchronized (eventQueue) {
			while (!eventQueue.isEmpty()) {
				while (!eventQueue.peekEvent().hasPassed()) {
					eventQueue.peekEvent().getEventTime();
					long waitTime = eventQueue.waitTime();
					if (waitTime <= MINIMUM_WAIT_TIME * eventQueue.peekEvent().getTime().getCompressionFactor()) {
						break;
					} else {
						eventQueue.wait(MINIMUM_WAIT_TIME);
					}
				}
				TimeEvent nextEvent = eventQueue.nextEvent();
				System.out.println("\nFloor:\nSending event to scheduler from floor " + floorNumber);
				System.out.println(nextEvent);
				try {
					scheduler.setRequest(nextEvent);
				} catch (ElevatorException e) {
					System.out.println(new Date(nextEvent.getTime().now()));
					e.printStackTrace();
				}
				carButtonEvents.add(((RequestElevatorEvent) nextEvent).getCarButtonEvent());
			}
		}
	}

	/**
	 * Performs the actions needed once an elevator arrives.
	 *
	 * Turn on floor lamp if elevator arrived at floor
	 *
	 * If there are car button events, dispatch those to the elevator/scheduler.
	 *
	 */
	public void elevatorArrival() {
		if (elevator.getFloor() == floorNumber && elevator.isStopped()) {
			lamp.turnOn();
			if (!carButtonEvents.isEmpty()) {
				CarButtonEvent event = (CarButtonEvent) carButtonEvents.nextEvent();
				if (event.getEventTime() - event.getTime().now() > MAXIMUM_WAIT_TIME) {
					try {
						throw new ElevatorException("Passenger waited more than " + MAXIMUM_WAIT_TIME + " ms");
					} catch (ElevatorException e) {
						e.printStackTrace();
						return;
					}
 				}
				elevator.sendEvent(event);
			}
		} else {
			lamp.turnOff();
		}
	}
}
