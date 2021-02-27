package elevator;

import floor.Floor;
import scheduler.Scheduler;

public class Sensor extends Thread {

    private Elevator elevator;
    private static Scheduler scheduler;
    private Floor floor;

    public Sensor(Elevator elevator, Floor floor) {
        this.elevator = elevator;
        this.floor = floor;
    }

    /**
     * Sets the scheduler
     *
     * @param scheduler
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    @Override
    public void run() {
        Integer currentFloor = elevator.getFloor();
        int floorNumber = floor.getFloorNumber();
        while (true) {
            synchronized (currentFloor) {
                while (currentFloor != floorNumber) {
                    try {
                        currentFloor.wait();
                    } catch (InterruptedException elevatorArrived) {}
                }

                synchronized (floor.getEventQueue()) {
                    floor.getEventQueue().notify();
                }
                scheduler.sensorActivated(floorNumber); // for arrival
                currentFloor.notifyAll();
            }
        }
    }


}
