Course code: SYSC3303A
Group number: 2
Iteration number: 2
Date: May 16, 2017. 
Students (by alphabetical order):
         Josh Campitelli (101010050)
         Ahmed Khattab (100994398)
         Dario Luzuriaga (100911067)
         Ahmed Sakr (101018695)
         Brian Zhang (101008207)

Breakdown of responsibilities:
------------------------------
Josh Campitelli: Contributed with the error handling in the client hub and connection class, helping with the interaction with the error simulations as well. 
Ahmed Khattab: Introduced the timing diagrams and helped to develop the verbose mode in the server's side. 
Dario Luzuriaga: Contributed to develop the interaction with the user in the error simulator, developed the UML diagrams and wrote documentation like README.txt. 
Ahmed Sakr: Introduced the multi-thread capacities in the error simulator, helped to develop methods to add errors in packets and organize their block numbers. 
Brian Zhang: Created error packets, helped with error handling on the client and connection classes. 

Iteration 2: 
------------
In this iteration, the error simulator was modified to produce error codes 4 (illegal TFTP operation) and 5 (unknown transfer ID) in the datagram packets being sent between the client and server. The simulator handles all the potential errors at this step.

Modes:
------
Four modes of operation are defined in the system (Normal or Test, Quiet or Verbose): 
 - Normal mode: the basic interaction between client and server is displayed in the interface. 
 - Test Mode: the error simulator acts as a middle man and received datagram packets from both the client and server.  
 - Quiet Mode: minimal information is displayed to the user. 
 - Verbose Mode: all the details of the processes are shown in the interface.


Setup:
------
In Eclipse, the project can be set up by choosing the following options in the menu bar:
        File -> Import -> General -> Existing Projects into Workspace. 

Running programs: 
-----------------
In Eclipse, three simultaneous processes should be executed: Server.java, ErrorSimulator.java and Client.java. Multiple clients can be ran concurrently. 
In the Eclipse interface, the Server needs to be run first. The class ErrorSimulator.java should be run secondly. The Client needs to be run at the end. Multiple clients are accepted in the system. It is suggested to open at least two or three console windows to appreciate the role of the classes. 

Demo: 
-----
There is a second way to run the TFTP server, via executing these files for Windows: Server compile&run.bat, ErrorSimulator compile&run.bat, Client compile&run.bat. 

Testing:
--------
Errors can be introduced when running the Error Simulator class. By asking simple questions, it allows the user to personalize the error codes desired (4 or 5), packet block to be changed and type of packet (data or acknowledgement ACK). Running the client and server in verbose mode would allow a tester to identify how the packets are modified. 
There is an additional way to test the system, by running the method presentModifications() inside the class ErrorSimulator.java. 

Issues:
-------
There are no erroneous issues discovered yet, apart of the known limitations of this iteration (like the impossibility of writing the same server's file by two different clients, for example). 

Diagrams:
---------
This iteration packet includes also some diagrams to illustrate de communication process between the hosts. They contain the names “Timing Diagrams” and “UML”, and are located in the folder deliverables/iteration 2/. 
