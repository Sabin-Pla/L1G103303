package floor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Scanner;

/**
 * This class parses the requests from the requests file
 * 
 * @author John Afolayan 
 * @version Iteration 1
 */
public class Parser {
	private static final String REQUESTFILE = "src/requestsFile.txt";
	
	/**
     * This method reads requests from the file and stores it in the DataStorage class
     * 
     * @return An array queue of all the requests from the file.
     */
    public ArrayDeque<DataStorage> getRequestFromFile() {
    	//make a new array queue
        ArrayDeque<DataStorage> requestQueue = new ArrayDeque<>();
        //store the request text file in a file variable
        File file = new File(REQUESTFILE);
        Scanner scanner;
        try {
        	//scan through the request file
            scanner = new Scanner(file);
            //if the file has a new line, store that new line in the queue
            while (scanner.hasNext()) {
                requestQueue.add(new DataStorage(scanner.nextLine()));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("There was a problem while reading the request.");
            return null;
        }
        return requestQueue;
    }
}
