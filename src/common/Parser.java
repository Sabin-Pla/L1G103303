package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import actor_events.CarButtonEvent;
import actor_events.RequestElevatorEvent;
import floor.InvalidDirectionException;

/**
 * This class parses the requests from the requests file and converts them into the appropriate Event objects
 * 
 * @author John Afolayan 
 * @version Iteration 2
 */
public class Parser {
    private Scanner scanner;
    private ArrayList<RequestElevatorEvent> events;
    private SimulationClock clock;
    private int days;

    public ArrayList<RequestElevatorEvent> getEvents() {
        return events;
    }

    public SimulationClock getClock() {
        if (clock == null) {
            String simulationParameters = scanner.nextLine();
            String startTime = simulationParameters.split(" ")[0];
            int compressionFactor = Integer.parseInt(simulationParameters.split(" ")[1]);
            Instant simulationStart = parseInstant(startTime, days);
            clock = new SimulationClock(simulationStart, compressionFactor);
        }
        return clock;
    }

    public void close() {
        scanner.close();
    }

    /**
     *
     * @param requestFile The file to read requests and configuration info from
     * @throws FileNotFoundException if the file is not found
     */
    public Parser(File requestFile) throws FileNotFoundException {
        this.scanner = new Scanner(requestFile);
        this.events = new ArrayList<>();
    }

	/**
     * This method reads requests from the file and converts the requests to TimeEvent objects.
     * The items in the returned arraylist should be in order by consequence of the fact that events in the request file
     * are entered in order
     *
     */
    public void parseEvents() throws InvalidDirectionException {
        events = new ArrayList<>();
        days = 0;
        getClock();

        //if the file has a new line, store that new line in the queue
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            line = line.strip();
            String pressTime = line.split(" ")[0]; // time the floor button is pressed

            int sourceFloor = Integer.parseInt(line.split(" ")[1]);
            boolean goingUp = line.split(" ")[2].equals("Up");
            int destinationFloor = Integer.parseInt(line.split(" ")[3]);
            
            boolean doorError = false;
            if (line.contains("DoorOpenFailure")) doorError = true;

            Instant pressInstant = parseInstant(pressTime, days);

            CarButtonEvent carButtonEvent = new CarButtonEvent(pressInstant,
                    destinationFloor);

            RequestElevatorEvent requestElevatorEvent = new RequestElevatorEvent(
                    sourceFloor,
                    goingUp,
                    doorError,
                    carButtonEvent);

            events.add(requestElevatorEvent);
        }
    }

    private Instant parseInstant(String timeStamp, int days) {
        timeStamp = fixFormatting(timeStamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, days);
        String dateTimestamp = new SimpleDateFormat("yyyy-dd-MM").
                format(calendar.getTime()) + 'T' + timeStamp;
        Date pressDate = new SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss.SSS'Z'").
                parse(dateTimestamp, new ParsePosition(0));

        // if the last event added occurs before the time of the current event, assume a day has passed
        if (events.size() != 0 &&
                events.get(events.size() - 1).getEventInstant().isAfter( pressDate.toInstant() )) {
            days++;
            calendar.setTime(pressDate);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            pressDate = calendar.getTime();
        }

        return pressDate.toInstant();
    }

    /**
     * Converts a string representing a point in time to a string formatted in a similar fashion, but with exactly
     * 1 ms precision. This means the given string may be truncated or appended to depending on whether or not the given
     * string represents a point in time with more or less than 1 ms of precision.
     *
     * @param pressTime the time of an elevator floor press event. Must be in the form "HH:MM:ss", and may also contain
     *                  the fraction of a second portion with or without 'Z' at the end.
     * @return String in the form HH:MM:ss.SSSZ
     */
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
