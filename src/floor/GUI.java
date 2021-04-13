package floor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;

import floor.TimeEventListener;

/**
 * This class models the view of this system
 * 
 * @author Mmedara Josiah 101053887
 * @version 1.0
 */
public class GUI extends TimeEventListener{
	private Queue<String>[] expectedArrivals;
	private JFrame frame;
	private JPanel mainPanel, allInfoPanel, allCarButtonsPanel;
	private ArrayList<JPanel> allInfoPanelList, allCarButtonsPanelList;
	private ArrayList<JLabel> floorErrorLabelList, floorNumberLabelList;
	private ArrayList<JButton> floorUpButtonList, floorDownButtonList, floorButtonLampList,
		elevator1ButtonList, elevator2ButtonList, elevator3ButtonList, elevator4ButtonList;
	private ArrayList<ArrayList<JButton>> allElevatorsList, allCarButtonList;
	
	public GUI() {
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
		
		frame = new JFrame("Elevator System Simulation");
		mainPanel = new JPanel();
		mainPanel.setLayout(new FlowLayout(2));
		allElevatorsList = new ArrayList<>();
		
		mainPanel.add(allInfoPanel());
		mainPanel.add(allCarButtonsPanel());
		
		frame.add(mainPanel);
		frame.setTitle("Elevator System Simulation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.pack();
		frame.setVisible(true);
		frame.toFront();
	}
	
	/**
	 * Creates the whole floor info panel 
	 * @return the floor info panel
	 */
	public JPanel allInfoPanel() {
		allInfoPanelList();
		allInfoPanel = new JPanel();
		allInfoPanel.setLayout(new BoxLayout(allInfoPanel, BoxLayout.PAGE_AXIS));
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			allInfoPanel.add(allInfoPanelList.get(i));
		}
		return allInfoPanel;
	}
	
	/**
	 * Creates the info panel for each floor
	 * @return the list of floor info
	 */
	public ArrayList<JPanel> allInfoPanelList() {
		allInfoPanelList = new ArrayList<>();
		floorNumberLabelList();
		floorErrorLabelList();
		floorUpButtonList();
		floorDownButtonList();
		floorButtonLampList();
		elevator1ButtonList();
		elevator2ButtonList();
		elevator3ButtonList();
		elevator4ButtonList();
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			JPanel panel = new JPanel();
			panel.setLayout(new FlowLayout(9));
			panel.add(floorErrorLabelList.get(i));
			panel.add(floorUpButtonList.get(i));
			panel.add(floorDownButtonList.get(i));
			panel.add(floorButtonLampList.get(i));
			panel.add(elevator1ButtonList.get(i));
			panel.add(elevator2ButtonList.get(i));
			panel.add(elevator3ButtonList.get(i));
			panel.add(elevator4ButtonList.get(i));
			panel.add(floorNumberLabelList.get(i));
			allInfoPanelList.add(panel);
		}
		return allInfoPanelList;
	}
	
	/**
	 * Creates all the labels for floor errors
	 * @return the list of labels
	 */
	private ArrayList<JLabel> floorNumberLabelList() {
		floorNumberLabelList = new ArrayList<>();
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			JLabel label;
			if(i==Floor.NUM_FLOORS) {
				label = new JLabel("       ");
			}
			else {
				label = new JLabel("floor " + (Floor.NUM_FLOORS+1-1-i));
			}
			floorNumberLabelList.add(label);
		}
		return floorNumberLabelList;
	}

	/**
	 * Creates all the labels for floor errors
	 * @return the list of labels
	 */
	private ArrayList<JLabel> floorErrorLabelList() {
		floorErrorLabelList = new ArrayList<>();
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			JLabel label;
			if(i==Floor.NUM_FLOORS) {
				label = new JLabel("     ");
			}
			else {
				label = new JLabel("fault");
				label.setForeground(Color.RED);
				label.setFont(new Font("Sanserif", Font.ITALIC, 10));
			}
			floorErrorLabelList.add(label);
		}
		return floorErrorLabelList;
	}
	
	/**
	 * Creates the up button for each floor 
	 * @return the list of up buttons
	 */
	private ArrayList<JButton> floorUpButtonList() {
		floorUpButtonList = new ArrayList<>();
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			JButton button;
			if(i==Floor.NUM_FLOORS) {
				button = new JButton(" ");
				button.setOpaque(true);
				button.setBorderPainted(false);
			}
			else {
				button = new JButton("^");
				button.setOpaque(true);
				button.setBorderPainted(true);
			}
			floorUpButtonList.add(button);
		}
		return floorUpButtonList;
	}
	
	/**
	 * Creates the down button for each floor
	 * @return the list of down buttons
	 */
	private ArrayList<JButton> floorDownButtonList() {
		floorDownButtonList = new ArrayList<>();
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			JButton button;
			if(i==Floor.NUM_FLOORS) {
				button = new JButton(" ");
				button.setOpaque(true);
				button.setBorderPainted(false);
			}
			else {
				button = new JButton("v");
				button.setOpaque(true);
				button.setBorderPainted(true);
			}
			floorDownButtonList.add(button);
		}
		return floorDownButtonList;
	}
	
	/**
	 * Creates the lamp button for each floor
	 * @return the list of lamp buttons
	 */
	private ArrayList<JButton> floorButtonLampList() {
		floorButtonLampList = new ArrayList<>();
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			JButton button;
			if(i==Floor.NUM_FLOORS) {
				button = new JButton("    ");
				button.setOpaque(true);
				button.setBorderPainted(false);
			}
			else {
				button = new JButton("Lamp");
				button.setOpaque(true);
				button.setBorderPainted(false);
			}
			floorButtonLampList.add(button);
		}
		return floorButtonLampList;
	}
	
	/**
	 * Creates the buttons to represent elevator 1 stops at each floor
	 * @return the list of buttons
	 */
	private ArrayList<JButton> elevator1ButtonList(){
		elevator1ButtonList = new ArrayList<>();
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			JButton button;
			if(i==Floor.NUM_FLOORS) {
				button = new JButton("   E1");
				button.setOpaque(true);
				button.setBorderPainted(false);
			}
			else {
				button = new JButton("");
				button.setOpaque(true);
				button.setBorderPainted(true);
			}
			elevator1ButtonList.add(button);
		}
		allElevatorsList.add(elevator1ButtonList);
		return elevator1ButtonList;
	}
	
	/**
	 * Creates the buttons to represent elevator 2 stops at each floor
	 * @return the list of buttons
	 */
	private ArrayList<JButton> elevator2ButtonList(){
		elevator2ButtonList = new ArrayList<>();
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			JButton button;
			if(i==Floor.NUM_FLOORS) {
				button = new JButton("   E2");
				button.setOpaque(true);
				button.setBorderPainted(false);
			}
			else {
				button = new JButton("");
				button.setOpaque(true);
				button.setBorderPainted(true);
			}
			elevator2ButtonList.add(button);
		}
		allElevatorsList.add(elevator2ButtonList);
		return elevator2ButtonList;
	}
	
	/**
	 * Creates the buttons to represent elevator 3 stops at each floor
	 * @return the list of buttons
	 */
	private ArrayList<JButton> elevator3ButtonList(){
		elevator3ButtonList = new ArrayList<>();
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			JButton button;
			if(i==Floor.NUM_FLOORS) {
				button = new JButton("   E3");
				button.setOpaque(true);
				button.setBorderPainted(false);
			}
			else {
				button = new JButton("");
				button.setOpaque(true);
				button.setBorderPainted(true);
			}
			elevator3ButtonList.add(button);
		}
		allElevatorsList.add(elevator3ButtonList);
		return elevator3ButtonList;
	}
	
	/**
	 * Creates the buttons to represent elevator 4 stops at each floor
	 * @return the list of buttons
	 */
	private ArrayList<JButton> elevator4ButtonList(){
		elevator4ButtonList = new ArrayList<>();
		for(int i=0; i<Floor.NUM_FLOORS+1; i++) {
			JButton button;
			if(i==Floor.NUM_FLOORS) {
				button = new JButton("   E4");
				button.setOpaque(true);
				button.setBorderPainted(false);
			}
			else {
				button = new JButton("");
				button.setOpaque(true);
				button.setBorderPainted(true);
			}
			elevator4ButtonList.add(button);
		}
		allElevatorsList.add(elevator4ButtonList);
		return elevator4ButtonList;
	}
	
	/**
	 * all panels for elevator cars
	 * @return
	 */
	private JPanel allCarButtonsPanel() {
		allCarButtonList();
		allCarButtonsPanel = new JPanel();
		allCarButtonsPanel.setLayout(new GridLayout(0,2));
		for(int i=0; i<Floor.NUM_ELEVATORS; i++) {
			allCarButtonsPanel.add(allCarButtonsPanelList.get(i));
		}
		return allCarButtonsPanel;
	}
	
	/**
	 * stores all car buttons
	 * @return
	 */
	private ArrayList<ArrayList<JButton>> allCarButtonList(){
		allCarButtonList = new ArrayList<>();
		allCarButtonsPanelList = new ArrayList<>();
		for(int i=0; i<Floor.NUM_ELEVATORS; i++) {
			ArrayList<JButton> carButtonList = new ArrayList<>();
			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(0, 2));
			panel.setBorder(BorderFactory.createTitledBorder("E" + (i+1) + " Car"));
			for(int j=0; j<Floor.NUM_FLOORS; j++) {
				JButton button = new JButton("" + (j+1));
				button.setOpaque(true);
				button.setBorderPainted(true);
				carButtonList.add(button);
				panel.add(button);
			}
			allCarButtonsPanelList.add(panel);
			allCarButtonList.add(carButtonList);
		}
		return allCarButtonList;
	}

	@Override
	public void floorButtonPressed(int sourceFloor, boolean up) {
		System.out.println("Floor " + sourceFloor + " button pressed, up: " + up);
		if(up) {
			floorUpButtonList.get(Floor.NUM_FLOORS-sourceFloor).setBackground(Color.GREEN);
		}
		else {
			floorDownButtonList.get(Floor.NUM_FLOORS-sourceFloor).setBackground(Color.GREEN);
				
		}
	}

	@Override
	public void lampOff(int floorNumber) {
		System.out.println("Floor " + floorNumber + " lamp off");
		floorButtonLampList.get(Floor.NUM_FLOORS-floorNumber).setBackground(null);
	}

	@Override
	public void lampOn(int floorNumber) {
		System.out.println("Floor " + floorNumber + " lamp on");
		floorButtonLampList.get(Floor.NUM_FLOORS-floorNumber).setBackground(Color.GREEN);
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
		
		if(elevatorNumber==0) {
			allElevatorsList.get(0).get(Floor.NUM_FLOORS-floor).setBackground(Color.ORANGE);
			allCarButtonList.get(elevatorNumber).get(floor-1).setBackground(null);
		}
		else if(elevatorNumber==1) {
			allElevatorsList.get(1).get(Floor.NUM_FLOORS-floor).setBackground(Color.MAGENTA);
			allCarButtonList.get(elevatorNumber).get(floor-1).setBackground(null);
		}
		else if(elevatorNumber==2) {
			allElevatorsList.get(2).get(Floor.NUM_FLOORS-floor).setBackground(Color.BLUE);
			allCarButtonList.get(elevatorNumber).get(floor-1).setBackground(null);
		}
		else if(elevatorNumber==3) {
			allElevatorsList.get(3).get(Floor.NUM_FLOORS-floor).setBackground(Color.CYAN);
			allCarButtonList.get(elevatorNumber).get(floor-1).setBackground(null);
		}
		if(!doorsClosed) {
			floorUpButtonList.get(Floor.NUM_FLOORS-floor).setBackground(null);
			floorDownButtonList.get(Floor.NUM_FLOORS-floor).setBackground(null);
			
		}
	}

	@Override
	protected void elevatorDeparted(int elevatorNumber, int floor) {
		System.out.println("Elevator " + elevatorNumber + " departed floor " + floor);
		allElevatorsList.get(elevatorNumber).get(Floor.NUM_FLOORS-floor).setBackground(null);
	}

	@Override
	public void simulatedErrorOccurred(String errorMessage) {
		System.out.println(errorMessage);
	}

	@Override
	public void carButtonPressed(int elevatorNumber, int floorNumber) {
		System.out.println("Car button " + floorNumber + " pressed for elevator " + elevatorNumber);
		if(elevatorNumber==0) {
			allCarButtonList.get(elevatorNumber).get(floorNumber-1).setBackground(Color.ORANGE);
		}
		else if(elevatorNumber==1) {
			allCarButtonList.get(elevatorNumber).get(floorNumber-1).setBackground(Color.MAGENTA);
		}
		else if(elevatorNumber==2) {
			allCarButtonList.get(elevatorNumber).get(floorNumber-1).setBackground(Color.BLUE);
		}
		else if(elevatorNumber==3) {
			allCarButtonList.get(elevatorNumber).get(floorNumber-1).setBackground(Color.CYAN);
		}
	}

	@Override
	public void simulationEnd() {
		System.out.println("The simulation is now over.");
		int a=JOptionPane.showConfirmDialog(frame, "The simulation is now over.\nDo you want to exit?");  
		if(a==JOptionPane.YES_OPTION){  
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
			System.exit(0);
		}  
	}
}
