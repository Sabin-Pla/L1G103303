# Project Iteration 1 : SYSC3303 L1, GROUP 10

# THE READ ME IN ITS CURRENT STATE IS NOT CURRENTLY COMPLETE. THIS DOCUMENT CONTAINS ALL THE INFORMATION I (SABIN) INTEND TO IMPART ON PROJECT COLLABORATORS. UPON SUBMITTING A MERGE REQUEST, IT MIGHT BE A GOOD IDEA TO UPDATE THIS README YOURSELF WITH WHATEVER BEHAVIOUR YOU ADDED. REMOVE THIS NOTICE ONCE THE README IS COMPLETE.

Simulation of a real time elevator system. 


##Breakdown of Classes

There are 6?? packages contained within the source code of this project.  
These are:  
1. common
2. elevator
3. floor
4. scheduler
5. tests
6. frontend // not actually added yet. I think it would be a good idea to have a package just for the GUI, parser, and class to start the threads. Basically a package for everything needed to get the system up and running.

?. events // perhaps should be deprecated and use common instead?

### Package common

The common package contains much of the miscellaneous boilerplate behaviour common to all subsystems. This includes classes which facilate the behaviour of time based events.

Classes:  
#### Time
Used to redefine timescales for simulating series of events sent over a long period of time. For instance, the user may want to simulate 3 days worth of events in 3 minutes.

#### TimeEvent
An event that has a start time (i.e, a time it was or should be sent). TimeEvents have a hasPassed() method, indicating whether or not an event has passed with respect to a given [Time](#Time) object.

#### TimeQueue
An implementation of a PriorityQueue that contains TimeEvents. It will reject the addition of any events that have passed (according to the timescale set by a Tie object). Priority is given to earlier events.

#### Parser
Parses a file with strings delimited by new lines in the form `10:15:59.0 1 Up 4` denoting floor button press time, source floor, desired direction, and destination floor (which car button should be hit when the elevator arrives).

Each line is converted to a TimeEvent object. Parser returns an arraylist of the objects in sequence. 

The events in the file are assumed to be in chronological order, so the first line contains the first event, so if the time of one line is further in the future than the time of the line before it, the parser will add one day to the event time.

### Package tests

The tests package contains unit tests to validate the behaviour of each class.

One integrated test, IntegratedEventTest exists to demonstrate threads sequentially sending the events in a TimeQueue as they pass according to a simulated time scale of 60x speed.

The events all expire exactly when they are supposed to. However, I was unable to get thread runners to send events as events in the queue expired. 