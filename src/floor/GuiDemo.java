package floor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class GuiDemo extends TimeEventListener {

	private Queue<String>[] expectedArrivals;
	
	@SuppressWarnings("unchecked")
	public GuiDemo() {
		super();
		expectedArrivals = new LinkedList[Floor.NUM_ELEVATORS];
		for (int i=0; i < Floor.NUM_ELEVATORS; i++) {
			expectedArrivals[i] = new LinkedList<String>();
		}
		
		File expected = new File("expected.txt");
		Scanner s = null;
		try {
			s = new Scanner(expected);
			for (int i=0; i < Floor.NUM_ELEVATORS; i++) {
				for (String arrival : s.nextLine().split(" ")) {
					expectedArrivals[i].add(arrival);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void floorButtonPressed(int sourceFloor, boolean up) {
		System.out.println("Floor " + sourceFloor + " button pressed, up: " + up);
	}

	@Override
	public void lampOff(int floorNumber) {
		System.out.println("Floor " + floorNumber + " lamp off");
	}

	@Override
	public void lampOn(int floorNumber) {
		System.out.println("Floor " + floorNumber + " lamp on");
		
	}

	@Override
	public void elevatorArrived(int elevatorNumber, int floor, boolean doorsClosed) {
		String arrivalString = String.valueOf(floor);
		if (!doorsClosed) arrivalString += "x";
		String expectedArrivalString = expectedArrivals[elevatorNumber].poll();
		if (!expectedArrivalString.equals(arrivalString)) {
			System.out.println("Simulation test failure");
			System.out.println(arrivalString + " : " + expectedArrivalString);
			System.exit(1);
		}
		System.out.println("Elevator " + elevatorNumber + " now at floor " + floor + ", Doors closed? " + doorsClosed);
	}

	@Override
	public void carButtonPressed(int elevatorNumber, int floorNumber) {
		System.out.println("Car button " + floorNumber + " pressed for elevator " + elevatorNumber);
		
	}

	@Override
	public void simulationEnd() {
		System.out.println("The simulation is now over.");
	}

	@Override
	protected void elevatorDeparted(int elevatorNumber, int floor) {
		System.out.println("Elevator " + elevatorNumber + " departed floor " + floor);
	}

	@Override
	public void simulatedErrorOccurred(String errorMessage) {
		System.out.println(errorMessage);
	}
}
