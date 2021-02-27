import common.*;
import elevator.Elevator;
import elevator.Sensor;
import floor.Floor;
import floor.Lamp;
import scheduler.Scheduler;

import java.io.File;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.Date;

/**
 * This is the main class that starts all the threads
 * 
 * @version Iteration 2
 */
public class Main {
    static final int NUMBER_OF_FLOORS = 10;
    private static final String REQUEST_FILE = "src/requestsFile.txt";

	public static void main(String[] args) {
	    startThreads(NUMBER_OF_FLOORS, REQUEST_FILE);
	}

	public static void startThreads(int numfloors, String fileName) {
	    File requestFile = new File(fileName);
        ArrayList<RequestElevatorEvent> events = Parser.getRequestFromFile(requestFile);

        System.out.println("Events to be sent: ");
        for (RequestElevatorEvent event : events) {
            System.out.println(event);
        }

        Time time = new Time(
                Time.SECOND_TO_MINUTE,
                events.get(0).getEventTime() - (long) (1000 * Time.SECOND_TO_MINUTE));

        events.get(0).setTime(time);

        Scheduler scheduler = new Scheduler(time);
        Thread schedulerThread = new Thread(scheduler, "Scheduler");
        Elevator elevator = new Elevator(scheduler, 1, time);

        scheduler.setElevator(elevator);

        schedulerThread.start();

        Sensor[] sensors = new Sensor[numfloors];
        Floor[] floors = new Floor[numfloors];

        for (int i=0; i < numfloors; i++) {
            TimeQueue floorEvents = new TimeQueue();
            for (RequestElevatorEvent event : events) {
                if (event.getFloor() == i + 1)  {
                    if (!floorEvents.add(event)) {
                        System.out.println("error, simulation with expired time added");
                        return;
                    }
                }
            }

            Lamp lamp = new Lamp(false);
            if (i==0) lamp.turnOn();
            Floor floor = new Floor(i+1, floorEvents, lamp);
            Sensor sensor = new Sensor(elevator, floor);
            floors[i] = floor;

            if (i == 0) {
                floor.setScheduler(scheduler);
                sensor.setScheduler(scheduler);
                floor.setElevator(elevator);
            }

            floor.start();
            sensor.start();
        }
        elevator.setFloors(floors);

        System.out.println("Starting simulation. Current time " + new Date(time.now()));
        time.restart();
    }
}
