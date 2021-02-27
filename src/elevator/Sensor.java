package elevator;

import floor.Floor;
import scheduler.Scheduler;

public class Sensor extends Thread {

    private Elevator elevator;
    private static Scheduler scheduler;
    private Floor floor;

    public Sensor(Elevator elevator, Floor floor) {
        this.scheduler = scheduler;
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
        while (true) {
            synchronized (elevator.getFloor()) {
                int currentFloor = elevator.getFloor();
                int floorNumber = floor.getFloorNumber();
                try {
                    while (!(currentFloor == floorNumber + 1) || !(currentFloor == floorNumber - 1)) {
                        elevator.getFloor().wait();
                        while (!(elevator.getFloor() == floorNumber)) {
                            elevator.getFloor().wait();
                        }
                    }
                } catch (InterruptedException elevatorArrived) {}
                floor.getEventQueue().notify();
                scheduler.sensorActivated(floorNumber); // for arrival
            }
        }
    }


}
