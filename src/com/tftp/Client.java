package com.tftp;

import java.util.Scanner;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

import com.tftp.core.SRSocket;
import com.tftp.core.protocol.BlockNumber;
import com.tftp.core.protocol.Packet;
import com.tftp.core.protocol.packets.*;
import com.tftp.exceptions.UnknownIOModeException;
import com.tftp.io.FileTransfer;

/**
 * Client defines and builds the logic for a user capable of transferring files to a pre-defined server port
 * through the agreed upon TFTP protocol.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 1st, 2017.
 */
public class Client extends SRSocket {

    public static final byte REQUEST_READ = 1, REQUEST_WRITE = 2;
    public static final int ERRORSIMULATOR_PORT = 23;
    public static boolean verbose;

    private int TID;
    private int serverPort = 69;
    private int dataBlock = 1;
    private int ackBlock = 0;
    private boolean isNormal = true;
    private FileTransfer fileTransfer;
    private int connectionTID;

    public Client() throws IOException {
        super("Client");
        this.TID = getLocalPort();
    }

    /**
     * Scans the console for user input.
     *
     * @param text the text to display when prompting the user for input
     *
     * @return the user input as a string
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

    private void transfer(byte[] filename, byte[] mode, String requestType) throws IOException, UnknownIOModeException {
        int port = serverPort;
        if (!this.isNormal) {
            port = ERRORSIMULATOR_PORT;
        }

        if (checkFilename(new String(filename), "\\data\\client")) {
            System.out.println("The File Already Exists");
        } else {
            System.out.println("The File Does Not Already Exist");
        }

        DatagramPacket packet;

        if (requestType.toLowerCase().equals("r")){
            packet = new RRQPacket(mode, filename, InetAddress.getLocalHost(), port).get();
            inform(packet, "Sending RRQ packet", true);
            send(packet);
            fileTransfer = new FileTransfer(new String(filename), FileTransfer.WRITE);
            rrq();
        } else {
            packet = new WRQPacket(mode, filename, InetAddress.getLocalHost(), port).get();
            inform(packet, "Sending WRQ packet", true);
            send(packet);

            fileTransfer = new FileTransfer(new String(filename), FileTransfer.READ);
            wrq();
        }
    }

    private void rrq() throws IOException {
        DatagramPacket response;

        do {
            response = receive();

            serverPort = response.getPort();
            inform(response, "Packet Received", true);

            if (ackBlock == 0)
                connectionTID = response.getPort();

            DatagramPacket errorPacket = parseUnknownPacket(response, this.connectionTID, ackBlock + 1);
            if (errorPacket != null && errorPacket.getData()[3] == 4) {
                inform(errorPacket, "Sending Packet");
                send(errorPacket);
                System.out.println("Terminating Client...");
                break;
            } else if (errorPacket != null && errorPacket.getData()[3] == 5) {
                System.out.println("Ignoring Packet, Continuing Execution.");
                continue;
            }

            if (Packet.getPacketType(response) == Packet.PacketTypes.DATA) {
                ackBlock++;

                // unpack the data portion and write it to the file
                int length = response.getData().length;
                byte[] data = new byte[length - 4];
                System.arraycopy(response.getData(), 4, data, 0, length - 4);
                fileTransfer.write(data);

                DatagramPacket ackPacket = new ACKPacket(response, BlockNumber.getBlockNumber(ackBlock)).get();
                ackPacket.setPort(serverPort);

                inform(ackPacket, "Sending ACK Packet", true);
                send(ackPacket);
            } else if (Packet.getPacketType(response) == Packet.PacketTypes.ERROR) {
                byte[] errorMsg = new byte[response.getLength() - 4];
                System.arraycopy(response.getData(), 4, errorMsg, 0, response.getData().length - 4);
                System.out.println("Error Packet Received: Error Code: 0" + response.getData()[3] + ", Error Message: " + new String(errorMsg));
                System.out.println("Terminating Client...");
                break;
            } else {
                String errorMsg = "Incorrect Packet Received";
                send(new ERRORPacket(response, Packet.ERROR_ILLEGAL_TFTP_OPERATION, errorMsg.getBytes()).get());    //Send error packet with error code 4.
                System.out.println("Terminating Client...");
                break;
            }
        } while (!fileTransfer.isComplete());

        System.out.println("[IMPORTANT] Transfer complete!");

    }

    private void wrq() throws IOException {
        DatagramPacket response;

        while(!fileTransfer.isComplete()) {
            response = receive();

            serverPort = response.getPort();
            inform(response, "Packet Received", true);
            byte[] data = fileTransfer.read();

            //Gets the Connection Port which it will be communicating
            if (dataBlock == 1)
                connectionTID = response.getPort();

            DatagramPacket errorPacket = parseUnknownPacket(response, this.connectionTID, dataBlock - 1);
            if (errorPacket != null && errorPacket.getData()[3] == 4) {
                send(errorPacket);
                System.out.println("Terminating Client...");
                break;
            } else if (errorPacket != null && errorPacket.getData()[3] == 5) {
                send(errorPacket);
                System.out.println("Ignoring Packet, Continuing Execution.");
                continue;
            }

            //Ensure the packet received from the server is of type ACK
            if (Packet.getPacketType(response) == Packet.PacketTypes.ACK) {
                DatagramPacket dataPacket = new DATAPacket(response, BlockNumber.getBlockNumber(dataBlock), data).get();
                dataPacket.setData(shrink(dataPacket.getData(), fileTransfer.lastBlockSize() + 4));
                dataPacket.setPort(serverPort);

                inform(dataPacket, "Sending DATA Packet", true);
                send(dataPacket);
                dataBlock++;
            } else if (Packet.getPacketType(response) == Packet.PacketTypes.ERROR) {
                byte[] errorMsg = new byte[response.getLength() - 4];
                System.arraycopy(response.getData(), 4, errorMsg, 0, response.getData().length - 4);
                System.out.println("Error Packet Received: Error Code: 0" + response.getData()[3] + ", Error Message: " + new String(errorMsg));
                System.out.println("Terminating Client...");
                break;
            } else {    //Received something other than an ACK or ERROR Packet, return an error 4 Packet to indicate corrupted stream
                String errorMsg = "Incorrect Packet Received";
                send(new ERRORPacket(response, Packet.ERROR_ILLEGAL_TFTP_OPERATION, errorMsg.getBytes()).get());    //Send error packet with error code 4.
                System.out.println("Terminating Client...");
                break;
            }
        }

        System.out.println("[IMPORTANT] Transfer complete!");
    }
    
    
    public static void main(String[] args) {
        try {
            Client client = new Client();
            FileTransfer.setup(FileTransfer.CLIENT_DIRECTORY);

            String dataMode = client.getInput("The Client is set to normal. Would you like to set it to test? (y/N) ");
            if (dataMode.toLowerCase().equals("y")) {
                client.setNormal(false);
            }


            String verbosity = client.getInput("The Client is set to quiet. Would you like to set it to verbose? (y/N) ");
            if (verbosity.toLowerCase().equals("y")) {
                verbose = true;
            }

            String quit = "";
            while (!quit.equalsIgnoreCase("n")) {
                String requestType = "";
                while (!(requestType.toLowerCase().equals("r") || requestType.toLowerCase().equals("w"))) {
                    requestType = client.getInput("Would you like to Write or Read? (W/R) ");
                }

                byte[] filename = client.getInput("Enter file name: ").getBytes();
                byte[] mode = "octet".getBytes();
                client.transfer(filename, mode, requestType);
                client.close();

                client = new Client();
                client.setNormal(!dataMode.toLowerCase().equals("y"));

                quit = client.getInput("Would you like to start a new transfer? (y/N) ");
            }
        } catch (IOException | UnknownIOModeException e) {
            e.printStackTrace();
        }
    }
}
