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
There is an additional way to test the system, by running the method presetModifications() inside the class ErrorSimulator.java. 

addModification() allows for the packet modification process. An example is shown below:

addModification(23, PacketTypes.ACK, Packet.ERROR_UNKNOWN_TRANSFER_ID, Packet.NO_SPECIAL_ERROR);

where the parameters are as follows:

"23": block size number, this specifies what block number the packet must be
"PacketsTypes.ACK": packet type, this specifies what type of packet it must be
"Packet.ERROR_UNKNOWN_TRANSFER_ID": error packet id to generate, this specifies what error packet id the error simulator should produce
"Packet.NO_SPECIAL_ERROR": error packet type, in this case unknown TID doesn't have one, but Illegal TFTP operation has illegal block number, illegal opcode, etc.

PLEASE NOTE: the error simulator hosts a list of all the modifications added. Once they are carried out, they are popped from the list so that the others
may get a chance to also be executed. This is great because it allows you to run multiple clients and check all possible modifications.

This is the presetModifications() as of submission:

        addModification(3, PacketTypes.ACK, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_OPCODE);
        addModification(7, PacketTypes.DATA, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_BLOCK_NUMBER);
        addModification(15, PacketTypes.DATA, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_PACKET_SIZE);
        addModification(23, PacketTypes.ACK, Packet.ERROR_UNKNOWN_TRANSFER_ID, Packet.NO_SPECIAL_ERROR);
        addModification(29, PacketTypes.ACK, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_BLOCK_NUMBER);
        addModification(222, PacketTypes.DATA, Packet.ERROR_UNKNOWN_TRANSFER_ID, Packet.NO_SPECIAL_ERROR);

So when you run the error simulator and attempt to test, it will tamper those specific packets specified in the presetModifications() present in ErrorSimulator.java.
If you wish to change or add more modifications, you can easily do so.

TEST CASES:

        addModification(3, PacketTypes.ACK, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_OPCODE);
	RESULT:
		Client: Packet Received:
		From Host Address: /192.168.1.169, Host port: 65035, Length: 4
 		Data (as string):
		Data (as bytes): [0, 13, 0, 3]

		Error Packet Detected: Error Code: 04, Error Message: Undefined OpCode
		Terminating Client...
	PASS

	addModification(7, PacketTypes.DATA, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_BLOCK_NUMBER);
	RESULT:
		Error Packet Detected: Error Code: 04, Error Message: Incorrect Block Number
		Connection (Client TID: 51346): Sending Packet:
		To Host Address: /192.168.1.169, Host port: 51346, Length: 27
		Data (as string):   Incorrect Block Number
		Connection (Client TID: 51346) terminated and is closing...
	PASS
	addModification(15, PacketTypes.DATA, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_PACKET_SIZE);
	RESULT:
		Error Packet Detected: Error Code: 04, Error Message: Data greater than 512
		Connection (Client TID: 57748): Sending Packet:
		To Host Address: /192.168.1.169, Host port: 57748, Length: 26
		Data (as string):   Data greater than 512
		Connection (Client TID: 57748) terminated and is closing...
	PASS

	addModification(23, PacketTypes.ACK, Packet.ERROR_UNKNOWN_TRANSFER_ID, Packet.NO_SPECIAL_ERROR);
	RESULT:
		Error Packet Detected: Error Code: 05, Error Message: Incorrect TID
		Ignoring Packet, Continuing Execution.
	PASS
	
	addModification(29, PacketTypes.ACK, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_BLOCK_NUMBER);
	RESULT:

		Client: Packet Received:
		From Host Address: /192.168.1.169, Host port: 52659, Length: 4
		Data (as string):  ÿ
		Data (as bytes): [0, 4, -1, 28]

		Error Packet Detected: Error Code: 04, Error Message: Incorrect Block Number
		Terminating Client...
	PASS

	addModification(222, PacketTypes.DATA, Packet.ERROR_UNKNOWN_TRANSFER_ID, Packet.NO_SPECIAL_ERROR);
	RESULT:
	
		Error Packet Detected: Error Code: 05, Error Message: Incorrect TID
		Connection (Client TID: 52383): Sending Packet:
		To Host Address: /192.168.1.169, Host port: 52385, Length: 24
		Data (as string):   Unknown transfer ID
	PASS

Issues:
-------
There are no erroneous issues discovered yet, apart of the known limitations of this iteration (like the impossibility of writing the same server's file by two different clients, for example). 

Diagrams:
---------
This iteration packet includes also some diagrams to illustrate de communication process between the hosts. They contain the names “Timing Diagrams” and “UML”, and are located in the folder deliverables/iteration 2/. 
