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

public class Client extends SRSocket {

    public static final byte ERROR_ILLEGAL_TFTP_OPERATION = 04;
    public static final byte ERROR_UNKNOWN_TRANSFER_ID = 05;
    public static final String[] ERROR_MESSAGES = { "Illegal TFTP Operation","Unknown transfer ID"};	
    public static final byte REQUEST_READ = 1, REQUEST_WRITE = 2;
    public static final byte ERROR_ILLEGAL_TFTP_OPERATION = 04;
    public static final byte ERROR_UNKNOWN_TRANSFER_ID = 05;
    public static final int ERRORSIMULATOR_PORT = 23;
    public static boolean verbose;
    private int TID;
    private int serverPort = 69;
    private int dataBlock = 1;
    private int ackBlock = 0;
    private boolean isNormal = true;
    private FileTransfer fileTransfer;

    public Client() throws IOException {
        super("Client");
        this.TID = getPort();
    }

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
    public String getInput(String text) {
        Scanner scanner = new Scanner(System.in);
        System.out.printf(text);
        return scanner.nextLine();
    }

    public int getTID() { 
        return this.TID;
    }

    public void setNormal(boolean normal) {
        this.isNormal = normal;
    }

    /**
     * Define a method to send a request to a host
     */
    private void sendRequest(byte[] filename, byte[] mode, String requestType) throws IOException, UnknownIOModeException {
        int port = serverPort;
        if (!this.isNormal) {
            port = ERRORSIMULATOR_PORT;
        }

        DatagramPacket packet;

        // Set conditions for transfer to server
        if (requestType.toLowerCase().equals("r")){
            packet = new Packet().RRQPacket(mode, filename, InetAddress.getLocalHost(), port);
            inform(packet, "Sending RRQ packet", true);
            send(packet);
            fileTransfer = new FileTransfer(FileTransfer.CLIENT_DIRECTORY + new String(filename), FileTransfer.WRITE);
            rrq();
        } else {
            packet = new Packet().WRQPacket(mode, filename, InetAddress.getLocalHost(), port);
            inform(packet, "Sending WRQ packet", true);
            send(packet);
            fileTransfer = new FileTransfer(FileTransfer.CLIENT_DIRECTORY + new String(filename), FileTransfer.READ);
            wrq();
        }
    }

    /**
     * Fulfils the complete life cycle of the read request, by continuously receiving DATA packets
     * and dispatching corresponding ACK packets.
     *
     * @throws IOException may be thrown if the file is inaccessible.
     */
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
        }
    }

    /**
     * Fulfils the complete life cycle of the write request, by continuously receiving ACK packets
     * and dispatching corresponding DATA packets.
     *
     * @throws IOException may be thrown if the file is inaccessible.
     */
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
            }
        }
    }

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
        }   
    }
    
}
