package floor;

import java.util.HashMap;
import java.util.Queue;

import common.Parser;
import common.TimeQueue;
import elevator.Elevator;
import events.ElevatorEvent;
import events.FloorEvent;
import events.RequestEvent;
import scheduler.Scheduler;

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
public class Floor implements Runnable {
	private static Scheduler scheduler;
	private static Elevator elevator;
	private int floorNumber;
	private TimeQueue eventQueue;
	private Lamp lamp;
	public enum State {OPERATING_LAMP, SENDING_EVENTS}
	private State state;

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
		state = State.OPERATING_LAMP;
		this.lamp = lamp;
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
	 * Runs the floor thread
	 */
	@Override
	public void run() {
		while (true) {
			switch (state) {
				case OPERATING_LAMP:
					operateLamp();
					if (!eventQueue.isEmpty()) switchState();
					break;
				case SENDING_EVENTS:
					try {
						sendEvents();
						switchState();
					} catch (InterruptedException elevatorInterrupt) {
						operateLamp();
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

	public void sendEvents() throws InterruptedException {
		synchronized (eventQueue) {
			while (!eventQueue.isEmpty()) {
				while (!eventQueue.isEmpty() && !eventQueue.peekEvent().hasPassed()) {
					eventQueue.peekEvent().getEventTime();
					eventQueue.wait(eventQueue.waitTime());
				}
				scheduler.setRequest(eventQueue.poll());
			}
		}
	}

	public void operateLamp() {
		if (elevator.getFloor() == floorNumber) {
			lamp.turnOn();
		} else {
			lamp.turnOff();
		}
	}
}
