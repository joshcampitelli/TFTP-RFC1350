Course code: SYSC3303A
Group number: 2
Iteration number: 4
Date: May 30, 2017. 
Students (by alphabetical order):
         Josh Campitelli (101010050)
         Ahmed Khattab (100994398)
         Dario Luzuriaga (100911067)
         Ahmed Sakr (101018695)
         Brian Zhang (101008207)

Breakdown of responsibilities:
------------------------------
- Josh Campitelli: Contributed refactoring the client and the connection classes, adding network errors and timeouts on the connection, client and server classes. 
- Ahmed Khattab: Introduced the timing diagrams, helped to create the errors for simulation as an upgrade of iteration 3, added more comments on programming code. 
- Dario Luzuriaga: Contributed to improving the Error Simulator, helped with the interface consistency, and contributed generating internal and external documents and UML diagrams. 
- Ahmed Sakr: Contributed refactoring the error simulator, the client, and some of the packing classes. Introduced new code comments. Fixed some pre-existing errors. 
- Brian Zhang: Helped to integrate the I/O error packets, operated improvements in packet.java and increased the efficiency in handling packets in all instances. 

Iteration 4: 
------------
The innovation of this iteration resides in dealing with network errors. The system is now able to determine when a packet is lost (due to a time out wait period), delayed or duplicated. The client or server is required to retransmit a data packet after a time out. 

Packets lost:
-------------
Since network errors would not occur spontaneously by running the server and client classes in the same computer, the network errors are generated via the error simulator class. From it, it is possible to define: normal operation (0), lose a packet (1), delay a packet (2), duplicate a packet (3). 

Setup:
------
In Eclipse, the project can be set up by choosing the following options in the menu bar:
        File -> Import -> General -> File System

Running programs: 
-----------------
In Eclipse, three simultaneous processes should be executed: Server.java, ErrorSimulator.java and Client.java. Multiple clients can be ran concurrently. 
The Server needs to be run first. The class ErrorSimulator.java should be run secondly. The Client needs to be run at the end. Multiple clients are accepted in the system. It is suggested to open at least two or three console windows to appreciate the role of the classes. 

Testing:
--------
In this case the tests can be run from the interfaces of Client.java, ErrorSimulator.java and Server.java without any other class or special method. The locations of files can be changed on the client and server's sides, allowing the user to induce I/O errors (access denegation, disk full, etcetera). The processes of reading from the server or writing on the server can now be defined from the client's side in a cyclical way, without restarting the Client.java execution. Also, the error simulator allows the user to enter any defined error in an interface to implement the necessary tests in an iterative way. The settings are added to a queue for execution in each new client’s connexion. 

Issues:
-------
There are no erroneous issues discovered yet. 

Diagrams:
---------
This iteration packet includes also some diagrams to illustrate de communication process between the hosts. They are introduced in files with names referring to timing diagrams and UML graphs. They are located in the folder deliverables/iteration 4/. 
