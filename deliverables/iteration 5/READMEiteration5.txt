Course code: SYSC3303A
Group number: 2
Iteration number: 5
Date: June 7, 2017. 
Students (by alphabetical order):
         Josh Campitelli (101010050)
         Ahmed Khattab (100994398)
         Dario Luzuriaga (100911067)
         Ahmed Sakr (101018695)
         Brian Zhang (101008207)

Introduction: 
-------------
This work consists on a TFTP server-client project. It defines a file transfer system based on specification RFC1350. It applies to one server and different client hosts. This system allows a user to read files from the server and to write files in the server. The server is able to provide access to different client hosts in a simultaneous way. The project was conceived to run properly in Eclipse IDE for Java Developers under Microsoft Windows 10 operating system. 

Errors:
-------
Since network errors would not occur spontaneously by running the server and client classes in a safe and controlled environment, the network and I/O errors are generated via an error simulator class. From it, it is possible to define: network normal operation (0), lose a packet (1), delay a packet (2), duplicate a packet (3). The error simulator also handles operating system (I/O) errors. In summery, the simulator is able to artificially generate the following code errors:
0: Unknown error.  
1: File not found. 
2: Access violation. 
3: Disk full. 
4: Illegal TFTP operation. 
5: Unknown transfer ID. 
6: File already exists.

Setup:
------
The project is contained in different folders whose order should be kept. 
In Eclipse, the project can be set up by choosing the following options in the menu bar:
        File -> Import -> General -> File System
The necessary classes are located in Eclipse’s tree (generally in the left hand side): 
	  SYSC3303-project-master – scr – com.ftp -- Server.java
	  SYSC3303-project-master – scr – com.ftp.simulation – ErrorSimulator.java
	  SYSC3303-project-master – scr – com.ftp -- Client.java
By following the ‘Running programs’ section, once a class is chosen in the tree mentioned, that class should be executed using:
        Run -> Run Configurations… -> Java Application -> (Class name) -> Run

Running programs: 
-----------------
In Eclipse, three simultaneous processes should be executed: Server.java, ErrorSimulator.java and Client.java. Multiple clients can be run concurrently. 
The Server needs to be run first. The class ErrorSimulator should be run secondly. The Client needs to be run at the end. Multiple clients are accepted in the system.
	Run -> Run History -> Server
	Run -> Run History -> ErrorSimulator
	Run -> Run History -> Client  (multiple times if desired) 
It is suggested to open at least two console windows to appreciate the role of the classes. 

Modes:
------
The system can run in four modes: 
 - Normal Mode: the basic interaction between client and server is displayed. 
 - Test Mode: when activated, the error simulator is operative. 
 - Quiet Mode: minimal information is displayed in the user's interface. 
 - Verbose Mode: all the details of the processes are displayed to the user.
The different host extremes of the system can use some of them as follows:
- Server: quiet or verbose mode.
- Client: normal or test, quiet or verbose mode. 

Testing files:
--------------
In order to facilitate the tests of the system, files of different lengths are already provided to be expanded and copied to the client and server’s folders. 
If the client wants to read files from the server, the sample files should be in the directory indicated by Eclipse’s tree:
	SYSC3303-project-master – data – server
For doing so, the following compressed files should be expanded and copied to the previous directory:
	SYSC3303-project-master – data – server – testfiles.zip
On the other hand, if the client wants to write files to the server, the sample files should be in the directory indicated by Eclipse’s tree:
	SYSC3303-project-master – data – client
For doing so, the following compressed files should be expanded and copied to the previous directory:
	SYSC3303-project-master – data – client – testfiles.zip
Also, in order to avoid name conflicts, the server’s folder should be emptied:
	SYSC3303-project-master – data – server
The default directory locations can be changed on the interfaces of the client and the server. 

Tests:
------
The tests can be run from the interfaces of Client.java, ErrorSimulator.java and Server.java without any other class or special method. The locations of files can be changed on the client and server's sides, allowing the user to induce I/O errors (access denegation, disk full, etcetera). The processes of reading from the server or writing on the server can be defined from the client's side in a cyclical way, without restarting the Client.java execution. Also, the error simulator allows the user to enter any defined error in an interface to implement the necessary tests in an iterative way. The settings are added to a queue for execution in each new file transfer. 

Tests on Server:
----------------
In order to run tests, the following options should be chosen on the server:
ENTER key to keep the default directory
Y key to use the verbose mode.
Type Quit and ENTER to end the server. 

Tests on Error Simulator:
-------------------------
When one cycle of the queue mentioned before is completed, it starts over again. This way the next file transfer would use the oldest setting in the queue. 
In each cycle the user in the simulator can choose what kind of error to generate, by altering the following information: 
Read request RRQ, write request WRQ, data content DATA, reception acknowledgement ACK, Error. 
An interactive menu guides the user to define the block number of the packet to change and specific details according to the option chosen. 

Tests on Client:
----------------
The following options should be chosen on the client to run the tests:
ENTER key to keep the default directory. 
Y to set the test mode. 
Y to choose the verbose mode. 
The IP address of the server and ENTER (for example, 134.117.58.17 ENTER). 
W to write to the server, R to read from the server.
File name and ENTER (for example, 4KB.txt ENTER). 
Q at any time to quit the client. 
Once a transfer is completed a transfer-finished message should be displayed. 

Issues:
-------
There are no erroneous issues discovered yet. 

Diagrams:
---------
In order to understand the functionalities of the system, different diagrams were created: UML class diagrams, UCM graphs, and communications timing diagrams. 
This project was developed in 5 steps or iterations. The diagrams of each iteration from 1 to 5 can be found in the project directories:
deliverables/iteration 1/
deliverables/iteration 2/
deliverables/iteration 3/
deliverables/iteration 4/
deliverables/iteration 5/. 


