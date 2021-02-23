package floor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import events.FloorEvent;
import events.RequestEvent;

/**
 * This class parses the requests from the requests file
 * 
 * @author John Afolayan 
 * @version Iteration 1
 */
public class Parser {
	private static final String REQUESTFILE = "src/requestsFile.txt";
	private static LinkedList<RequestPairing> requestQueue;
	
	/**
     * This method reads requests from the file and stores it in the DataStorage class
     * 
     * @return An array queue of all the requests from the file.
     */
    public static LinkedList<RequestPairing> getRequestFromFile() {
   
        requestQueue = new LinkedList<RequestPairing>();
        //store the request text file in a file variable
        File file = new File(REQUESTFILE);
        Scanner scanner;
        try {
        	//scan through the request file
            scanner = new Scanner(file);
            //if the file has a new line, store that new line in the queue
            while (scanner.hasNext()) {
            	String line = scanner.nextLine();
            	String time = line.split(" ")[0];
            	int sourceFloor = Integer.parseInt(line.split(" ")[1]);
            	boolean goingUp = line.split(" ")[2].equals("Up");
            	int destinationFloor = Integer.parseInt(line.split(" ")[3]);
            	FloorEvent request = new FloorEvent(time, sourceFloor, goingUp);
                requestQueue.add(new RequestPairing(request, destinationFloor));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("There was a problem while reading the request.");
            return null;
        }
        return requestQueue;
    }
    
    /**
     * gets the next request for an elevator submitted by someone on floor floorNumber
     * @param floorNumber 
     * @return
     */
    public static RequestPairing getNextRequest(int floorNumber) {
    	for (RequestPairing rp : requestQueue) {
    		if (rp.getFloorRequest().getSourceFloor() == floorNumber) return rp;
    	}
    	return null;
    }
    
    public static RequestPairing nextRequest() {
    	return requestQueue.poll();
    }
    
    public static boolean hasNext() {
    	return !requestQueue.isEmpty();
    }
}
