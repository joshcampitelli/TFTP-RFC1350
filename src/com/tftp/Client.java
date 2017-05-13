// Import resources
package com.tftp;
import java.util.Scanner;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import com.tftp.core.SRSocket;
import com.tftp.core.Packet;
import com.tftp.exceptions.UnknownIOModeException;
import com.tftp.io.FileTransfer;

/**
 * 
 * Project: Server-client/s file transfer system
 * Course: Real Time Concurrent Systems
 * Course code: SYSC3303A
 * School: Carleton University
 * Term: Summer 2017
 * Campitelli, Khattab, Luzuriaga, Sakr, Zhang
 * Group: 2
 * Iteration: 2
 * Date: 16/05/2017
 */

// Define class Client
public class Client extends SRSocket {

	// Set up variables
    public static final byte REQUEST_READ = 1, REQUEST_WRITE = 2;
    public static final int ERRORSIMULATOR_PORT = 23;
    public static boolean verbose;
    private int TID;
    private int serverPort = 69;
    private int dataBlock = 1;
    private int ackBlock = 0;
    private boolean isNormal = true;
    private FileTransfer fileTransfer;

    // Define constructor
    public Client() throws IOException {
        super("Client");
        this.TID = getPort();
    } // end constructor

    /**
     * Method: getInout
     * Description: It scans the console for user input.
     *
     * @InputParametre: String
     *  The text to display when prompting the user for input
     *
     * @return: String
     *  The user's input as a string
     */
    
    // Define getInout method
    public String getInput(String text) {
        Scanner scanner = new Scanner(System.in);
        System.out.printf(text);
        return scanner.nextLine(); // return
    } // end getInput method

    // Define getTID method
    public int getTID() { 
        return this.TID; // return integer
    } // end getTID method

    // Define method for normal mode of operation
    public void setNormal(boolean normal) {
        this.isNormal = normal;
    } // end method setNormal

    // Define a method to send a request to a host
    private void sendRequest(byte[] filename, byte[] mode, String requestType) throws IOException, UnknownIOModeException {
        int port = serverPort;
        if (!this.isNormal) {
            port = ERRORSIMULATOR_PORT;
        } // end sendRequest method

        // Define a Datagram packet
        DatagramPacket packet;

        // Set conditions to read file from server or
        // set conditions to write file into the server
        if (requestType.toLowerCase().equals("r")){
            packet = new Packet().RRQPacket(mode, filename, InetAddress.getLocalHost(), port);
            inform(packet, "Sending RRQ packet", true);
            send(packet);
    	      	// Read file
            fileTransfer = new FileTransfer(FileTransfer.CLIENT_DIRECTORY + new String(filename), FileTransfer.WRITE);
            rrq();
        } else {
            packet = new Packet().WRQPacket(mode, filename, InetAddress.getLocalHost(), port);
            inform(packet, "Sending WRQ packet", true);
            send(packet);
            // Write file
            fileTransfer = new FileTransfer(FileTransfer.CLIENT_DIRECTORY + new String(filename), FileTransfer.READ);
            wrq();
        } // end if condition
    }

    // Define method of reading file request
    private void rrq() throws IOException {
        DatagramPacket response = receive();

        serverPort = isNormal ? response.getPort() : ERRORSIMULATOR_PORT;
        inform(response, "Packet Received", true);

        // unpack the data portion and write it to the file
        int length = response.getData().length;
        byte[] data = new byte[length - 4];
        System.arraycopy(response.getData(), 4, data, 0, length - 4);
        fileTransfer.write(data);

        if (response.getData().length == Packet.DATA_SIZE) {
            DatagramPacket ackPacket = new Packet(response).ACKPacket(getBlockNumber(ackBlock));
            ackPacket.setPort(serverPort);

            inform(ackPacket, "Sending ACK Packet", true);
            send(ackPacket);
            ackBlock++;
            rrq();
        } // end if condition
    } // end the method rrq

    // Define method of writing file request
    private void wrq() throws IOException {
        DatagramPacket response = receive();

        serverPort = isNormal ? response.getPort() : ERRORSIMULATOR_PORT;
        inform(response, "Packet Received", true);

        Packet packet = new Packet();
        byte[] data = fileTransfer.read();

        if (packet.checkPacketType(response) == Packet.PacketTypes.ACK) {
            DatagramPacket dataPacket = new Packet(response).DATAPacket(getBlockNumber(dataBlock), data);
            dataPacket.setData(shrink(dataPacket.getData(), fileTransfer.lastBlockSize() + 4));
            dataPacket.setPort(serverPort);

            inform(dataPacket, "Sending DATA Packet", true);
            send(dataPacket);
            dataBlock++; // move through the blacks of data

            if (!fileTransfer.isComplete()) {
                wrq();
            } else {
                System.out.println("[IMPORTANT] Transfer complete!");
            } // end if condition
        } // end if condition
    } // end method wqr

    // Define main program
    public static void main(String[] args) {
        try {
            Client client = new Client();

            // Allow the user to choose between normal or test modes
            String dataMode = client.getInput("The Client is set to normal. Would you like to set it to test? (y/N) ");
            if (dataMode.toLowerCase().equals("y")) {
                client.setNormal(false);
            } // end try

            // Allow the user to choose between quiet or verbose modes
            String verbosity = client.getInput("The Client is set to quiet. Would you like to set it to verbose? (y/N) ");
            if (verbosity.toLowerCase().equals("y")) {
                verbose = true;
            } // end if condition

            // Allow the user to read from the server or write a file to the server
            String requestType = "";
            while (!(requestType.toLowerCase().equals("r") || requestType.toLowerCase().equals("w"))) {
                requestType = client.getInput("Would you like to Write (W) or Read (R)? (W/R) ");
            } // end while statement
            
            // Allow the user to enter the file nname to read or write
            byte[] filename = client.getInput("Enter file name: ").getBytes();
            byte[] mode = "octet".getBytes();
            client.sendRequest(filename, mode, requestType);
        } catch (IOException | UnknownIOModeException e) {
            e.printStackTrace();
        } // end exceptions 
        
    } // end main program
    
} // end class Client
