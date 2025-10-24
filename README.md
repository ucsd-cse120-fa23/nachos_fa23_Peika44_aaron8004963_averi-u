# Nachos Distribution for CSE120 FA23

Please do not change the direcory structure! (i.e. do not remove the top-level `nachos` direcotry.)

# Project 1

Members: Peike Xu, Averi Yu, Arron Li 
Project Overview  
This project aims to provide a basic operating system with Nachos. We implemented various algorithms and methods such as waitFor.

Code Description  
The main functionalities are divided among several Python files:

Alarm.java: Uses the hardware timer to provide preemption, and to allow threads to sleep until a certain time.
Condition2.java: An implementation of condition variables that disables interrupt()s for synchronization.
...

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


# Project 2   
This project aims to provide a basic operating system with Nachos. We implemented various algorithms and methods such as waitFor.

Code Description  
The main functionalities are divided among several Python files to achieve file system calls, multiprogramming, and other system calls like exec. 

How Well It Worked
The project was largely successful. We passed all basic tests and the methods implemented are all functional. We also implemented more tests to make sure all conditions are handled. 

Testing  
We tested our code using a variety of methods:

Unit tests to ensure individual functions work as expected.
Manually running the program with different datasets/scenarios.

Contributions  
[Peike Xu]: Mainly focused on implementation of Part1 by implementing creat, open, read, write, close, and unlink.Also Colaborate on debugging other parts.
[Averi Yu]: I worked mainly on part 2 of this project and helped debug others. I worked on UserKernal and UserProcess classes with functions UserKernal.initialize(), UserProcess.readVirtualMemory, etc. I debugged them with the given tests and Gradescope, further adding tests. 
[Arron Li ]: Implements part3 of this project and Colaborate on debugging other parts. Mainly modified the handle exit, halt, join and exec.
(random order)  

# Project 3 
Members: Peike Xu, Averi Yu, Arron Li 
  
This project aims to provide a basic operating system with Nachos. We implemented various algorithms and methods such as Initialize.

Code Description  
The main functionalities are divided among several Java files to achieve functions like Demand Pageing, Lazy Loading, and Page Pinning. 

How Well It Worked
The project was largely successful. We passed all basic tests and the methods implemented are all functional. We also implemented more tests to make sure all conditions are handled. 

Testing  
We tested our code using a variety of methods:

Unit tests to ensure individual functions work as expected.
Manually running the program with different datasets/scenarios.

Contributions  
[Peike Xu]: Mainly focused on implementation of Part1 and Part 4 by implementing readVirtualMemory, and handlePageFault.Also Colaborate on debugging other parts.
[Averi Yu]: For project 3, I worked on part 2 and part 3 of this project and helped debug others. I worked on VMKernal and VMProcess with functions like Iniitalize(), terminate,writeVirtualMemory, etc. I debugged them with the given tests and Gradescope, further adding tests. 
[Arron Li ]: Implements part1 and Part 3 of this project like handlePageFault and Colaborate on debugging other parts. Mainly modified handelException.
(random order)

