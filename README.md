# Project Iteration 4 : SYSC3303 L1, GROUP 10

Simulation of a real time elevator system. 


# Breakdown of Responsibilty Iteration 2
 
### Common Package:
 
 CarButtonEvent - Harshil Verma
 
 InvalidDirectionException - Sabin Plaiasu
 
 Parser - John Afolayan
 
 RequestElevatorEvent - Aayush Mallya
 
 Time - Sabin Plaiasu
 
 TimeEvent - Harshil Verma
 
 TimeException - Sabin Plaiasu
 
 TimeQueue - John Afolayan
 
### Elevator Package:
 
 Door - Aayush Mallya
 
 Elevator - Mmedara Josiah, Sabin Plaiasu
 
 Sensor - Aayush Mallya
 
### Floor Package:

 ElevatorException - Mmedara Josiah
 
 Floor - Sabin Plaiasu
 
 Lamp - John Afolayan
 
### Scheduler Package:

 Scheduler - Harshil Verma, Mmedara Josiah
 
### Test Package:
 
 CarButtonEventTest - Harshil Verma
 
 FloorTest - Sabin Plaiasu
 
 IntegratedEventTest/Test Runner - Mmedara Josiah
 
 ParserTest - John Afolayan
 
 RequestElevatorEventTest - Aayush Mallya
 
 TimeEventTest - Aayush Mallya
 
 TimeQueueTest - John Afolayan, Mmedara Josiah
 
 # Breakdown of Responsibilty Iteration 3
 
 ### Common Package:
 
 TimeEvent - Sabin Plaiasu
 
 ### Elevator Package:
 
 Elevator - John Afolayan, Mmedara Josiah
 
 ### Floor Package:
 
 Floor - Sabin Plaiasu, Aayush Mallya
 
 ### remote_procedure_events Package:
 
 CarButtonPressEvent - Harshil Verma
 
 ElevatorFloorArrival - John Afolayan
 
 FloorButtonPressEvent - Aayush Mallya
 
 ### Scheduler Package:
 
 Scheduler - Mmedara Josiah, Harshil Verma
 
 # Breakdown of Responsibilty Iteration 4
 
### actor_events Package: 

 CarButtonEvent - Harshil Verma
 
 RequestElevatorEvent - Aayush Mallya
 
### Common Package:
 
 Parser - John Afolayan
 
 TimeException - Sabin Plaiasu
 
 TimeEvent - Harshil Verma
 
 TimeException - Sabin Plaiasu
 
 TimeQueue - John Afolayan
 
### Elevator Package:
 
 Elevator - Mmedara Josiah, Aayush Mallya
 
### Floor Package:

 ElevatorWaitTimeException - Mmedara Josiah
 
 Floor - Sabin Plaiasu
 
 InvalidDirectionException - John Afolayan
 
 ### remote_procedure_events Package:
 
 CarButtonPressEvent - Harshil Verma
 
 ElevatorMototEvent - Sabin Plaiasu
 
 FloorArrivalEvent - John Afolayan
 
 FloorButtonPressEvent - Aayush Mallya
 
### Scheduler Package:

 ElevatorPositionException - Aayush Mallya
 
 Scheduler - Harshil Verma, Mmedara Josiah
 
### Test Package:
 
 CarButtonEventTest - Harshil Verma
 
 ParserTest - Sabin Plaiasu
 
 RequestElevatorEventTest - Aayush Mallya
 
 TimeEventTest - Aayush Mallya
 
 TimeQueueTest - John Afolayan, Mmedara Josiah
 
# Names of Files

### Package actor_events 

The actor_events package contains the events that will be triggered by the actor

Classes:
#### CarButtonEvent
This class handles the events related to when a button is pressed
#### RequestElevatorEvent
This class handles the request events to be processed by the elevator

### Package Common

The common package contains much of the miscellaneous boilerplate behaviour common to all subsystems. This includes classes which facilate the behaviour of time based events.

Classes:
#### Parser
Parses a file with strings delimited by new lines in the form `10:15:59.0 1 Up 4` denoting floor button press time, source floor, desired direction, and destination floor (which car button should be hit when the elevator arrives). 
#### SimulationClock
Used to redefine timescales for simulating series of events sent over a long period of time. For instance, the user may want to simulate 3 days worth of events in 3 minutes.
#### TimeEvent
An event that has a start time (i.e, a time it was or should be sent). TimeEvents have a hasPassed() method, indicating whether or not an event has passed with respect to a given [Time](#Time) object.
#### TimeException
An exception for when time-based events are set to occur at invalid times
#### TimeQueue
An implementation of a PriorityQueue that contains TimeEvents. It will reject the addition of any events that have passed (according to the timescale set by a Tie object). Priority is given to earlier events.

### Package Elevator
The elevator package contains the classes pertaining to the elevator itself 

Classes:
#### Elevator
This class models the elevator

### Package Floor:
This package contains classes pertaining to the floor subsystem

Classes:
#### ElevatorWaitTimeException
Exception class for there being too long a wait time between the time an elevator is requested and arrives
#### Floor
The floor class sends requests to the schedulers and operates its floor lamp.
#### InvalidDirectionException
An exception for if "going down" to a higher floor or "going up" to a lower floor

### Package remote_procedure_events 
This package contains the events that transpire when the car button is pressed, when a floor button is spressed, and when the elevator arrives at a floor.

Classes:
#### CarButtonPressEvent
This class handles the evnts when a car button is pressed.
#### ElevatorMotorEvent
This class handles the elevator motor and its functions
#### FloorArrivalEvent
This class handles the time event and floor/elevator information when an elevator arrives at a floor.
#### FloorButtonPressEvent
This class handles the port listener and the events triggered when a floor button is pressed.

### Package Scheduler
Contains the scheduler class

Classes:
#### Scheduler
Schedules requests for both the elevator and floor subsystem
#### ElevatorPositionException
Exception class for the reported elevator floor not matching up with where the scheduler expects it to be

### Package Tests
The tests package contains unit tests to validate the behaviour of each class.

One integrated test, IntegratedEventTest exists to demonstrate threads sequentially sending the events in a TimeQueue as they pass according to a simulated time scale of 60x speed.

# Setup & Test Instructions

1. Extract the zip file to your desktop.
2. In eclipse: File -> Import -> General -> Existing Projects into Workspace -> Select root directory -> Browse
3. Select the extracted folder and then click 'Finish'
4. Navigate to the 'tests' package 
5. You will now see all of the test files and are able to run them at your discretion 


