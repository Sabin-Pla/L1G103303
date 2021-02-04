package floorSubSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Scanner;

/**
 * This class parses the requests from the requests file
 * 
 * @author Mmedara Josiah 101053887
 * @version 1.0
 */
public class Parser {
	private static final String REQUESTFILE = "src/requestsFile.txt";
	
	/**
     * This method reads requests from the file and stores it in the DataStorage class
     * 
     * @return An array dequeue of all the requests from the file.
     */
    public ArrayDeque<DataStorage> getRequestFromFile() {
        ArrayDeque<DataStorage> request = new ArrayDeque<>();
        File file = new File(REQUESTFILE);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNext()) {
                request.add(new DataStorage(scanner.nextLine()));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("There is a problem with reading the request.");
            return null;
        }
        return request;
    }
}
