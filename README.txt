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
- Josh Campitelli: Contributed handling errors 1 to 6, categorizing all I/O errors in connection.java, as well as improving the codes in client and server sides.  
- Ahmed Khattab: Introduced the timing diagrams, specified messages for each error to be displayed to the client's user and helped defining the interaction between Client.java and its user. 
- Dario Luzuriaga: Contributed to improving the Error Simulator, in particular, making it easier for users to introduce errors in packets, helped to define new procedures of writing files, and contributed generating documents and UML diagrams. 
- Ahmed Sakr: Improved the error simulator and introduced new documentation and comments inside the programming codes, adjusted the coding style and accessibility to methods in order to make debugging easier. 
- Brian Zhang: Helped to define and integrate all I/O error packets, operated improvements in packet.java and increased the efficiency in handling packets in all instances. 

Iteration 3: 
------------
Apart of the errors 4 and 5 from previous iteration, the system is able now to handle errors 1, 2, 3 and 6. 

Errors:
-------
At present time the client, error simulator and server are able to operate simulating and dealing with the following errors: 
0: Unknown error.  
1: File not found . 
2: Access violation. 
3: Disk full. 
4: Illegal TFTP operation. 
5: Unknown transfer ID. 
6: File already exists. 

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
In this case the tests can be run from the interfaces of Client.java, ErrorSimulator.java and Server.java without any other class or special method. The locations of files can now be changed on the client and server's sides. The processes of reading from the server or writing on the server can now be defined from the client's side in a cyclical way, without restarting the Client.java execution. Also, the error simulator allows the user to enter any defined error in an interactive interface to implement all necessary tests. 

Issues:
-------
There are no erroneous issues discovered yet. 

Diagrams:
---------
This iteration packet includes also some diagrams to illustrate de communication process between the hosts. They contain the names “Timing Diagrams” and “UML”, and are located in the folder deliverables/iteration 3/. 
