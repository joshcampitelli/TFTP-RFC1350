Course: Real Time Concurrent Systems
Course code: SYSC3303A
School: Carleton University
Season: Summer 2017
Group number: 2
Assignment: Term project
Description: Server-client/s file transfer system. 
Protocol: Trivial file transfer protocol.
Protocol specification: RFC1350. 
Programming language: Java. 
Iteration number: 1
Date: 09/05/2017
Students (by alphabetical order):
         Josh Campitelli (101010050)
         Ahmed Khattab (100994398)
         Dario Luzuriaga (100911067)
         Ahmed Sakr (101018695)
         Brian Zhang (101008207)

Breakdown of responsibilities:
------------------------------
Josh Campitelli: Helped with the design, writing, and debugging of the project, specifically the handling of packets in the Client and Connection Classes.
Ahmed Khattab: Worked towards the overall progress of the project including the shutting down of the server and created the UMC and UML diagrams. 
Dario Luzuriaga: Contributed writing the README.txt instructions, reviewing code and suggesting the users’ interaction with the program. 
Ahmed Sakr:
Brian Zhang:

Introduction:
-------------
This work consists on the first iteration (or submission) of the term communications project TFTP server-client. It defines a file transfer system based on specification RFC1350. It applies to one server and different client hosts.  
The project is planned to have 5 steps, submissions or iterations, being this one the initial one.

Iteration 1:
------------
In future submissions some communication errors between the server and clients will be introduced using an error simulation. For this first submission, no errors are assumed to happen, while that possibility is contemplated starting on the second iteration of the project.  
The general communication procedure is the following. The client specifies an operation mode for the system (to be explained: normal, test, quiet, verbose). The client enters a file name with an extension. The client defines if the file is going to be read from the server or written on it. In this last case the client will transmit to the server the specified file from its default location. Otherwise, it will receive the file from the server. 
As mentioned before, it is assumed that no eventualities will take place: if the client specifies a file name to be read from the server, it is supposed to exist in the default locations; the files involved will not excess 512 bytes; the communication capacity of the server will not be acceded by the number of clients; no long waiting will occur. 
The server could be shout down by responding to a request to do so on the server's interface. In that case if any file is being interchanged between client/s and server, they will continue the process until the end of the transfer, but the server will not accept any new request (port 69 or equivalent would be closed). 

 

Implementation:
---------------
The project is developed in Java language and Eclipse programming environment. 
It defines three main classes: 
1)- Client.java: 
This is the program to be installed in the server's side. It provides the user interface with the option to read or write files from or to the server. Permanently, it listens on port 23 (or equivalent) to obtain new incoming data, messages or notifications from the server. 
2)- ErrorSimulator.java: 
This is the program to be installed on a host acting as a link between the client and the server. For this first iteration it is not used. For the moment it only shows the main structure to be developed in future submissions. 
3)- Server.java:
This is the program to be installed on the server's host. It receives read or write requests and responds to the client accordantly. The server submits the specified file to the client or writes the file transmitted by the client in its default directory. It permanently listens to the port 69 for new requests, unless the server's user requires it to be shout down.  
4)- There are other multiples classes that divide the tasks to be completed by the server, like transferring files, establishing connections, defining data packets, defining written or read instances, dealing with operation modes, defining invalid packets, etcetera. 

 Modes:
 ------

 The client's user can define different modes to appreciate the communication process with the server, as follows. 
 - Normal mode: the basic interaction between client and server is displayed in the client's interface. 
 - Test Mode: it will be used in future iterations, when the error simulator will be implemented to test errors. 
 - Quiet Mode: minimal information is displayed in the client's interface. 
 - Verbose Mode: all the details of the processes are displayed to the client. 

Setup:
------
The file iteration_01.zip contains all the information to import the developing project into several versions of Eclipse, even older versions than the used in programming instances. Such compressed file should be expanded into a folder to be imported in the Eclipse interface by choosing: 

	File -> Import -> General -> Existing Projects into Workspace. 

Running programs: 
-----------------
The main Client.java and Server.java classes commented do not have parameters. The Server needs to be run first. The class ErrorSimulator.java could be run secondly or not run at all (since it is not yet fully developed). The Client needs to be run later. Multiple clients could be run simultaneously or sequentially. When the client starts running, all the other classes will interact with it. It is suggested to open two console windows to appreciate the role of these two main classes. 

Demo: 
-----
There is a second way to run the execution of classes referred before, via the following files that run on Windows or DOS operating systems: 
1)- Server compile&run.bat: 
It runs an execution of class the Server.java. 
2)- ErrorSimulator compile&run.bat:
It runs an execution of the class ErrorSimulator.java, which is not used yet to interact between server and client/s. 
3)- Client compile&run.bat:
It runs an execution of the class Client.java. This file con be executed several times in order to run several clients' connections and requests. 

Issues:
---------
There are not erroneous issues discovered yet. But there could be exception messages shown from the Java code executions, in case the ideal circumstances described in the "Iteration 1" section do not take place. 

Diagrams:
---------
This iteration packet includes also some diagrams to illustrate de communication process between the hosts. They are compiled in the attached file diagrams.pdf. 
