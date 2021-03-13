package scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

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
	private int lastFloor;

	private int nextDestination;
	private int nextExpectedFloor;

	public int getLastSensor() {
		return lastSensor;
	}

	private enum State {MONITORING_ELEVATOR, IDLING, HANDLING_EVENT};
	private State state;
	
    private static final int FLOOR_SEND_PORT = 23;
    private static final int ELEVATOR_RECEIVE_PORT = 60;
    private static final int ELEVATOR_SEND_PORT = 61;
    private static final int ELEVATOR_REPLY_PORT = 62;
    private static final int DATA_SIZE = 26;
    
    private DatagramPacket sendPacket, receiveFloorPacket, receiveElevatorPacket, receiveElevatorInfo;
    private DatagramSocket floorSocketReceiver, elevatorSocketReceiver, elevatorSocketReplier, sendSocket;

	/**
	 * Constructor
	 */
	public Scheduler(Time time) {
		this.time = time;
		timeQueue = new TimeQueue();
		state = State.IDLING;
		try {
			floorSocketReceiver = new DatagramSocket(FLOOR_SEND_PORT);
            elevatorSocketReceiver = new DatagramSocket(ELEVATOR_SEND_PORT);
            elevatorSocketReplier = new DatagramSocket(ELEVATOR_REPLY_PORT);
            sendSocket = new DatagramSocket();
		} catch(SocketException e) {
			System.out.println("Error: SchedulerSubSystem cannot be initialized.");
            System.exit(1);
		}
	}
	
	/**
     * Advances the scheduler to the next state.
     */
    private void goToNextState() {
        if (getState().ordinal() == 1)
            state = State.IDLING;
        else
            state = State.HANDLING_EVENT;
        System.out.println("State has been updated to: " + getState());
    }
    
    /**
     * Get the current state of the scheduler.
     * 
     * @return The current state of the scheduler.
     */
    private State getState() {
        return state;
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
     * Receive a DatagramPacket from either the ElevatorSubsystem or the
     * FloorSubsystem.
     * 
     * @param fromFloor True if the DatagramPacket is originating from the
     *                  FloorSubsystem, false otherwise.
     */
	private void receivePacket(boolean fromFloor) {
		byte[] request = new byte[DATA_SIZE];
        try {
            // Receive a packet
            if (fromFloor) {
                receiveFloorPacket = new DatagramPacket(request, request.length);
                floorSocketReceiver.receive(receiveFloorPacket);
            }
            else {
                receiveElevatorPacket = new DatagramPacket(request, request.length);
                elevatorSocketReceiver.receive(receiveElevatorPacket);
            }

        } catch (IOException e) {
            // Display an error if the packet cannot be received
            // Terminate the program
            System.out.println("Error: Scheduler cannot receive packet.");
            System.exit(1);
        }
	}
	
	/**
     * Routine to create a DatagramPacket that will be sent.
     * 
     * @param message The byte[] data the DatagramPacket will contain.
     */
    private void createPacket(byte[] message) {
        try {
            // Initialize and create a send packet
            sendPacket = new DatagramPacket(message, message.length, InetAddress.getLocalHost(), ELEVATOR_RECEIVE_PORT);
        } catch (UnknownHostException e) {
            // Display an error message if the packet cannot be created.
            // Terminate the program.
            System.out.println("Error: Scheduler could not create packet.");
            System.exit(1);
        }
    }
    
    /**
     * Routine to send a DatagramPacket to the ElevatorSubsystem. This
     * DatagramPacket will contain information that the ElevatorSubsystem will use
     * to decide which Elevator should receive the packet.
     */
    private void sendPacketToElevator() {
    	System.out.println("-> Sending elvator number");
        printPacketInfo(true, 3);
        try {
            // Send the packet
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            // Display an error message if the packet cannot be sent.
            // Terminate the program.
            System.out.println("Error: Scheduler could not send the packet.");
            System.exit(1);
        }
    }

    /**
     * Print the information contained within a particular DatagramPacket.
     * 
     * @param sending   True if we are sending the DatagramPacket, false if we
     *                  received the DatagramPacket.
     * @param fromWhere 1 if the DatagramPacket originated from the
     *                  ElevatorSubsystem, 2 if the DatagramPacket originated from
     *                  the FloorSubsystem, 3 if the DatagramPacket is being sent
     *                  (sending must also be true).
     */
    private void printPacketInfo(boolean sending, int fromWhere) {
        String symbol = sending ? "->" : "<-";
        String title = sending ? "sending" : "receiving";
        DatagramPacket packetInfo = null;
        switch (fromWhere) {
        case 1:
            packetInfo = receiveElevatorPacket;
            break;
        case 2:
            packetInfo = receiveFloorPacket;
            break;
        case 3:
            packetInfo = sendPacket;
        }
        System.out.println(symbol + " Scheduler: " + title + " Packet");
        System.out.println(symbol + " Address: " + packetInfo.getAddress());
        System.out.println(symbol + " Port: " + packetInfo.getPort());
        System.out.print(symbol + " Data (byte): ");
        for (byte b : packetInfo.getData())
            System.out.print(b);

        System.out.print("\n" + symbol + " Data (String): " + new String(packetInfo.getData()) + "\n\n");
    }
    
    /**
     * Check to see if there are any requests that must be scheduled.
     * 
     * @return The next request to be scheduled, as a DatagramPacket.
     */
    private synchronized DatagramPacket checkWork() {
        if (timeQueue.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return (DatagramPacket) timeQueue.poll();
    }

    /**
     * Asks the elevator for its status and receives the reply from the elevator
     * 
     * @return A DatagramPacket containing the information for all Elevators.
     */
    private DatagramPacket sendStatusRequest() {
        byte[] request = "Status".getBytes();
        System.out.println("-> Sending a request for Status to the ElevatorSubsystem\n");

        createPacket(request);
        try {
        	this.printPacketInfo(true, 3);
            // Send the packet
            sendSocket.send(sendPacket);
            // Wait for a reply
            byte[] longMessage = new byte[1000];
            receiveElevatorInfo = new DatagramPacket(longMessage, longMessage.length, InetAddress.getLocalHost(),
                    ELEVATOR_REPLY_PORT);
            elevatorSocketReplier.receive(receiveElevatorInfo);
            return receiveElevatorInfo;
        } catch (IOException e) {
            // Display an error message if the packet cannot be sent.
            // Terminate the program.
            System.out.println("Error: Scheduler could not send the packet.");
            System.exit(1);
        }
        return null;
    }
    
    /**
     * Schedule the request by determining the best elevator to send the request to.
     * 
     * @param work         The current request that is being scheduled.
     * @param elevatorInfo The statuses of all the elevators.
     * @return A DatagramPacket that contains the request, along with the
     *         information as to which Elevator the request will be added to, and if
     *         it should do at the front of the back of the workQueue.
     */
    private void schedule(DatagramPacket work, DatagramPacket elevatorInfo) {
        // Progress the state of the Scheduler to indicate that we are currently
        // scheduling a request.
        goToNextState();

        ArrayList<Integer> elevatorScores = new ArrayList<>();

        String nextReq = new String(work.getData());
        String[] requestInfo = new String(work.getData()).split(" ");
        String[] elevatorStatuses = new String(elevatorInfo.getData()).split("-");
        int numElevators = elevatorStatuses.length - 1;

        boolean requestDirection = requestInfo[2].equals("Up") ? true : false;
        int startFloor = Integer.parseInt(requestInfo[1]);
        int destinationFloor = Integer.parseInt(requestInfo[3]);

        int numIdle = 0;

        for (String s : elevatorStatuses) {
            String[] temp = s.split("\\|");
            if (temp[0].trim().equals("IDLE"))
                numIdle++;
        }


        if (numIdle == numElevators) {
            int i = 0;
            for (String s : elevatorStatuses) {
                if (i < numElevators) {
                    String[] temp = s.split("\\|");
                    elevatorScores.add(i, Math.abs(startFloor - Integer.parseInt(temp[1])));
                    i++;

                }   
            }
            int min = elevatorScores.indexOf(Collections.min(elevatorScores));
            String newData = String.valueOf(min) + "|0|" +  nextReq;
            
            createPacket(newData.getBytes());                

        } else {

        }

        // We are done scheduling, so the Scheduler state should indicate that it is no
        // longer scheduling.
        goToNextState();
    }

    
    /**
     * Thread execution routine.
     */
    @Override
    public void run() {
        if (Thread.currentThread().getName().equals("F2E"))
            // Main routine to receive request information from the FloorSubsystem.
            while (true) {
                receivePacket(true);
                printPacketInfo(false, 2);
                try {
					setRequest(receiveFloorPacket);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ElevatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        else if (Thread.currentThread().getName().equals("E2S"))
            while (true) {
                // Main routine to receive confirmation from
                receivePacket(false);
                printPacketInfo(false, 1);
                System.out.println("---------------------------------------------------------------------");
            }
        else
            while (true) {
                DatagramPacket work = checkWork();
                // If we get here, we have work we can do!
                DatagramPacket elevatorInfo = this.sendStatusRequest();
                schedule(work, elevatorInfo);
                sendPacketToElevator();
            }
    }

	/**
	 * Stores an incoming request in the scheduler's queue
	 *
	 * @param request
	 * @throws InterruptedException
	 */
	public synchronized void setRequest(DatagramPacket request) throws InterruptedException, ElevatorException {
		if (!timeQueue.add(request)) {
			throw new ElevatorException("Cannot schedule event in the past!");
		}
		if (state != State.HANDLING_EVENT) {
			state = State.HANDLING_EVENT;
			notifyAll();
		}
	}
	
	
	//	MAIN IS NOT COMPLETE.   //
	/**
     * Entry point for the application.
     *
     * @param args The command-line arguments that are passed when compiling the
     *             application.
     */
    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        System.out.println("---- SCHEDULER SUB SYSTEM ----- \n");
        Thread elevatorToScheduler = new Thread(scheduler);
        Thread floorToElevator = new Thread(scheduler);
        Thread workThread = new Thread(scheduler);
        floorToElevator.setName("F2E");
        elevatorToScheduler.setName("E2S");
        workThread.setName("Worker");
        floorToElevator.start();
        elevatorToScheduler.start();
        workThread.start();
    }
}
