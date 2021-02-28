# Project Iteration 1 : SYSC3303 L1, GROUP 10

# THE READ ME IN ITS CURRENT STATE IS NOT CURRENTLY COMPLETE. THIS DOCUMENT CONTAINS ALL THE INFORMATION I INTEND TO IMPART ON CODE COLLABORATORS WITH THIS MR. UPON SUBMITTING A MERGE REQUEST, IT MIGHT BE A GOOD IDEA TO UPDATE THIS README YOURSELF WITH WHATEVER BEHAVIOUR YOU ADDED. REMOVE THIS NOTICE ONCE THE README IS COMPLETE.

Simulation of a real time elevator system. 


# Breakdown of Responsibilty
 
## Common Package:
 
 CarButtonEvent - Harshil Verma
 
 InvalidDirectionException - Sabin Plaiasu
 
 Parser - John Afolayan
 
 RequestElevatorEvent - Aayush Mallya
 
 Time - Sabin Plaiasu
 
 TimeEvent - Harshil Verma
 
 TimeException - Sabin Plaiasu
 
 TimeQueue - John Afolayan
 
## Elevator Package:
 
 Door - Aayush Mallya
 Elevator - Mmedara Josiah, Sabin Plaiasu
 Sensor - Aayush Mallya
 
## Floor Package:
 ElevatorException - Mmedara Josiah
 Floor - Sabin Plaiasu
 Lamp - John Afolayan
 
## Scheduler Package:
 Scheduler - Harshil Verma, Mmedara Josiah
 
## Test Package:
 
 CarButtonEventTest - Harshil Verma
 FloorTest - Sabin Plaiasu
 IntegratedEventTest/Test Runner - Mmedara Josiah
 ParserTest - John Afolayan
 RequestElevatorEventTest - Aayush Mallya
 TimeEventTest - Aayush Mallya
 TimeQueueTest - John Afolayan, Mmedara Josiah
 
# Names of Files

### Package Common

The common package contains much of the miscellaneous boilerplate behaviour common to all subsystems. This includes classes which facilate the behaviour of time based events.

Classes:
#### CarButtonEvent
This class handles the events related to when a button is pressed
#### InvalidDirectionException
An exception for attempting to go to a non accessible floor
#### Parser
Parses a file with strings delimited by new lines in the form `10:15:59.0 1 Up 4` denoting floor button press time, source floor, desired direction, and destination floor (which car button should be hit when the elevator arrives). 
#### RequestElevatorEvent
This class handles the request events to be processed by the elevator
#### Time
Used to redefine timescales for simulating series of events sent over a long period of time. For instance, the user may want to simulate 3 days worth of events in 3 minutes.
#### TimeEvent
An event that has a start time (i.e, a time it was or should be sent). TimeEvents have a hasPassed() method, indicating whether or not an event has passed with respect to a given [Time](#Time) object.
#### TimeQueue
An implementation of a PriorityQueue that contains TimeEvents. It will reject the addition of any events that have passed (according to the timescale set by a Tie object). Priority is given to earlier events.

### Package Elevator
The elevator package contains the classes pertaining to the elevator itself 

Classes:
#### Door
This class contains the various states for the door
#### Elevator
This class models the elevator
#### Sensor
This class models the sensor used to track the elevator


### Package Scheduler
Contains the scheduler class

Classes:
#### Scheduler
Schedules requests for both the elevator and floor subsystem

### Package Tests
The tests package contains unit tests to validate the behaviour of each class.

One integrated test, IntegratedEventTest exists to demonstrate threads sequentially sending the events in a TimeQueue as they pass according to a simulated time scale of 60x speed.

The events all expire exactly when they are supposed to. However, I was unable to get thread runners to send events as events in the queue expired. 
