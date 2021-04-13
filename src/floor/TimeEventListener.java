package floor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import remote_procedure_events.CarButtonPressEvent;
import remote_procedure_events.FloorArrivalEvent;
import remote_procedure_events.FloorButtonPressEvent;
import scheduler.Scheduler;

/**
 * An interface to listen to various events sent by all 3 subsytems. The GUI extends this.
 * 
 * @author Sabin Plaiasu
 * @version Iteration 5
 */
public abstract class TimeEventListener extends Thread {
	
	public static final int LISTENER_RECEIVE_PORT = 90 + 1024;
	private DatagramSocket receiveSocket;
	private boolean floorDoneSimulation;
	public static final int SME_HEADER = 4;
	public static final int LAMP_HEADER = 3;
	public static final int CAR_BUTTON_HEADER = 2;
	public static final int FLOOR_ARRIVAL_HEADER = 1;
	public static final int FLOOR_BUTTON_HEADER = 0;
	private int[] elevatorFloors;
	
	public TimeEventListener() {
		elevatorFloors = new int[Floor.NUM_ELEVATORS];
		for (int i=0; i < Floor.NUM_ELEVATORS; i++) {
			elevatorFloors[i] = 1;
		}
		floorDoneSimulation = false;
		try {
			receiveSocket = new DatagramSocket(LISTENER_RECEIVE_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (true) {
			timeEventOccured();
		}
	}
	
	public void timeEventOccured() {
		byte[] request = new byte[Scheduler.DATA_SIZE];
		DatagramPacket receivePacket = new DatagramPacket(request, request.length);
		try {
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			System.out.println("Error: TimeEventListener cannot receive packet.");
			System.exit(1);
		}
		byte[] packetData = new byte[receivePacket.getData().length - 1];
		for (int i=1; i < receivePacket.getData().length; i++) {
			packetData[i - 1] = receivePacket.getData()[i];
		}
		byte headerByte = receivePacket.getData()[0];
		
		try {
			ByteArrayInputStream bainStream = new ByteArrayInputStream(packetData);
			ObjectInputStream oinStream = new ObjectInputStream(bainStream);
			
			if (headerByte == Integer.valueOf(FLOOR_BUTTON_HEADER).byteValue()) {
				FloorButtonPressEvent event = (FloorButtonPressEvent) oinStream.readObject();
				floorButtonPressed(event.getFloor(), event.isGoingUp());
			} else if (headerByte == Integer.valueOf(FLOOR_ARRIVAL_HEADER).byteValue()) {
				FloorArrivalEvent event = (FloorArrivalEvent) oinStream.readObject();
				int elevatorNumber = event.getElevatorNumber();
				elevatorDeparted(elevatorNumber, elevatorFloors[elevatorNumber]);
				elevatorFloors[elevatorNumber] = event.getArrivalFloor();
				elevatorArrived(elevatorNumber, event.getArrivalFloor(), event.getDoorsClosed());
			} else if (headerByte == Integer.valueOf(CAR_BUTTON_HEADER).byteValue()) {
				CarButtonPressEvent event = (CarButtonPressEvent) oinStream.readObject();
				carButtonPressed(event.getElevatorNumber(), event.getDestinationFloor());
			} else if (headerByte == Integer.valueOf(LAMP_HEADER).byteValue()) {
				FloorLampEvent fle = (FloorLampEvent) oinStream.readObject();
				if (fle.getOn()) {
					lampOn(fle.getFloor());
				} else {
					lampOff(fle.getFloor());
				}
			} else if (headerByte == Integer.valueOf(SME_HEADER).byteValue()) {
				SimulationEndEvent sme = (SimulationEndEvent) oinStream.readObject();
				if (sme.isFloor() && !floorDoneSimulation) {
					System.out.println("Floor done sending events (including car presses)");
					floorDoneSimulation = true;
				}
				// if both the floor and scheduler subsystems signaled that they have reached the end of their simulation
				if (floorDoneSimulation && !sme.isFloor()) simulationEnd();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	protected abstract void floorButtonPressed(int sourceFloor, boolean up);
	
	protected abstract void lampOff(int floorNumber);
	
	protected abstract void lampOn(int floorNumber);
	
	protected abstract void elevatorArrived(int elevatorNumber, int floor, boolean doorsClosed);
	
	protected abstract void elevatorDeparted(int elevatorNumber, int floor);
	
	protected abstract void carButtonPressed(int elevatorNumber, int floorNUmber);
	
	// called when all requests have been fulfilled and the simulation ends
	protected abstract void simulationEnd();
}
