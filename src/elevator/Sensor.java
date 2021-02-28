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
        int floorNumber = floor.getFloorNumber();
        while (true) {
            synchronized (elevator) {
                while (elevator.getFloor() != floorNumber) {
                    try {
                        elevator.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                synchronized (floor.getEventQueue()) {
                    floor.getEventQueue().notify();
                }

                System.out.println("Sensor interrupted " + floor.getFloorNumber());

                scheduler.sensorActivated(floorNumber); // for arrival
                elevator.notifyAll();

                while (elevator.getFloor() == floorNumber) {
                    try {
                        elevator.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
