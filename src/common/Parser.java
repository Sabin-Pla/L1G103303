package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.CarButtonEvent;
import common.RequestElevatorEvent;
import common.TimeEvent;

/**
 * This class parses the requests from the requests file and converts them into the appropriate Event objects
 * 
 * @author John Afolayan 
 * @version Iteration 1
 */
public class Parser {

	/**
     * This method reads requests from the file and stores it in the DataStorage class
     * 
     * @return An array queue of all the requests from the file.
     */
    public static ArrayList<TimeEvent> getRequestFromFile(File requestFile) {
   
        ArrayList<TimeEvent> events = new ArrayList<>();
        Scanner scanner;

        try {
        	//scan through the request file
            scanner = new Scanner(requestFile);
            int days = 0;
            //if the file has a new line, store that new line in the queue
            while (scanner.hasNext()) {
            	String line = scanner.nextLine();
                line = line.strip();
            	String pressTime = line.split(" ")[0]; // time the floor button is pressed

            	int sourceFloor = Integer.parseInt(line.split(" ")[1]);
            	boolean goingUp = line.split(" ")[2].equals("Up");
            	int destinationFloor = Integer.parseInt(line.split(" ")[3]);

                pressTime = fixFormatting(pressTime);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_MONTH, days);
                String dateTimestamp = new SimpleDateFormat("yyyy-dd-MM").
                        format(calendar.getTime()) + 'T' + pressTime;

                Date pressDate = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss.SSS'Z'").
                        parse(dateTimestamp, new ParsePosition(0));

                // if the last event added occurs before the time of the current event, assume a day has passed
                if (events.size() != 0 &&
                        events.get(events.size() - 1).getEventTime() > pressDate.toInstant().toEpochMilli()) {
                    days++;
                    calendar.setTime(pressDate);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    pressDate = calendar.getTime();
                }

            	RequestElevatorEvent requestElevatorEvent = new RequestElevatorEvent(
            	        sourceFloor,
                        pressDate.toInstant().toEpochMilli(),
                        goingUp);

                events.add(requestElevatorEvent);
                events.add(new CarButtonEvent(requestElevatorEvent, destinationFloor));
            }
            scanner.close();
        } catch (FileNotFoundException | InvalidDirectionException e) {
            e.printStackTrace();
            System.out.println("There was a problem while reading the request.");
            return null;
        }
        return events;
    }

    private static String fixFormatting(String pressTime) {

        Pattern timePattern = Pattern.compile("([0-9]{2}:[0-9]{2}:[0-9]{2})(\\.)?([0-9]+)?(Z)?");
        Matcher matcher = timePattern.matcher(pressTime);

        int groups = -1;
        while (matcher.find()){
            for( int groupIdx = 0; groupIdx < matcher.groupCount() + 1; groupIdx++ ){
                // uncomment to debug
                // System.out.println( "[" + groups + "][" + groupIdx + "] = " + matcher.group(groupIdx));
                if (matcher.group(groupIdx) != null) groups++;
            }
        }

        switch (groups) {
            case (1): // corresponds to a string in the form "20:38:10"
                pressTime += ".000Z";
                break;

            case(2): // corresponds to a string in the form "20:38:10."
                pressTime += "000Z";
                break;

            case(3): // corresponds to a string in the form "20:38:10.1" "20:38:10.1234"
                if (pressTime.length() > 12) {
                    pressTime = pressTime.substring(0, 12);
                }
                while (pressTime.length() < 12) {
                    pressTime += '0';
                }
                pressTime += 'Z';
                break;

            case(4): // has 'Z' at the end "20:38:10.1234Z"
                pressTime = pressTime.substring(0, pressTime.length() - 1);
                System.out.println(pressTime);
                while (pressTime.length() < 12) {
                    pressTime += "0";
                }
                pressTime = pressTime.substring(0, 12);
                pressTime += 'Z';
                break;

            default:
                throw new IllegalArgumentException("Invalid floor press time " + pressTime + " group count " + groups);
        }
        return pressTime;
    }
}
