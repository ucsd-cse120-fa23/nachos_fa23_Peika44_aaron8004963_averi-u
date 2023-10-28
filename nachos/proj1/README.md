Members: Peike Xu, Averi Yu, Arron Li 
Project Overview  
This project aims to provide a basic operating system with Nachos. We implemented various algorithms and methods such as waitFor.

Code Description  
The main functionalities are divided among several Python files:

Alarm.java: Uses the hardware timer to provide preemption, and to allow threads to sleep until a certain time.
Condition2.java: An implementation of condition variables that disables interrupt()s for synchronization.
[to be added]...

How Well It Worked
The project was largely successful. We passed all basic tests and the methods implemented are all functional. We also implemented more tests to make sure all conditions are handled. 

Testing  
We tested our code using a variety of methods:

Unit tests to ensure individual functions work as expected.
Manually running the program with different datasets/scenarios.

Contributions  
[Peike Xu]: Mainly focused on implementation of Rendezvous function. Creating multiple tests for p5 and p2 questions. Colaborate on debugging other parts.
[Averi Yu]: Implemented parts of Alarm.java and Condition2.java for p1 and p4, specifically the function of timerInterrupt, sleepFor, sleepForTest1, and more. Tested methods according to instructions provided for p1 and p4 questions. Look through other problems and help debugging. 
[Arron Li ]: Implements p2(Condition2)&p3(KThread), writting tests for p1-4.Colaborate on debugging other parts.
(random order)

