package com.tftp;

import java.util.Scanner;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

import com.tftp.core.SRSocket;
import com.tftp.core.protocol.BlockNumber;
import com.tftp.core.protocol.Packet;
import com.tftp.core.protocol.TFTPError;
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

    public static final int ERRORSIMULATOR_PORT = 23;
    public static boolean verbose;

    private int serverPort = 69;
    private int dataBlock = 1;
    private int ackBlock = 0;
    private boolean isNormal = true;
    private FileTransfer fileTransfer;
    private int connectionTID;

    public Client() throws IOException {
        super("Client");
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

    /**
     * Sets the instance state of the client regarding where the packet should be sent to.
     * A normal state would send the initial packet to the main server (port id: 69).
     * A testing state would send the initial packet to the error simulator (port id: 23).
     * @param normal
     */
    public void setNormal(boolean normal) {
        this.isNormal = normal;
    }


    /**
     * Attempts to establish the transfer with the destination. Once a response has been received, the cycle
     * logic converges to the necessary method (i.e. rrq() or wrq()).
     *
     * @param filename the operator-provided filename
     * @param mode the operator-provided mode
     * @param requestType the operator-provided request type (i.e. RRQ or WRQ)
     *
     * @throws IOException
     * @throws UnknownIOModeException
     */
    private void transfer(byte[] filename, byte[] mode, String requestType) throws IOException, UnknownIOModeException {
        int port = serverPort;
        if (!this.isNormal) {
            port = ERRORSIMULATOR_PORT;
        }

        DatagramPacket packet;

        if (requestType.toLowerCase().equals("r")){
            packet = new RRQPacket(mode, filename, InetAddress.getLocalHost(), port).getDatagram();

            if (!FileTransfer.isWritable()) {
                send(new ERRORPacket(packet, TFTPError.ACCESS_VIOLATION, ("Access violation").getBytes()).getDatagram());
                System.out.println("File Access Violation, Terminating Transfer.");
                return;
            }

            inform(packet, "Sending RRQ packet", true);
            send(packet);
            fileTransfer = new FileTransfer(new String(filename), FileTransfer.WRITE);
            rrq();
        } else {
            packet = new WRQPacket(mode, filename, InetAddress.getLocalHost(), port).getDatagram();

            if (!FileTransfer.isReadable()) {
                send(new ERRORPacket(packet, TFTPError.ACCESS_VIOLATION, ("Access violation").getBytes()).getDatagram());
                System.out.println("File Access Violation, Terminating Transfer.");
                return;
            }

            if (!FileTransfer.isFileExisting(new String(filename))) {
                System.out.println("The File you wish to write does not exist.");
                return;
            }

            inform(packet, "Sending WRQ packet", true);
            send(packet);

            fileTransfer = new FileTransfer(new String(filename), FileTransfer.READ);
            wrq();
        }
    }


    /**
     * Completes the read (RRQ) cycle that has been requested by the operator.
     * Ideally, ACK packets are sent to the destination and DATA packets are received from the destination until
     * the last DATA packet has been read.
     *
     * The client is equipped with the knowledge to handle abnormal packets and take the necessary measures. Not
     * all measures are fatal and the client does attempt to recover the authenticity of the connection, if possible.
     *
     * @throws IOException
     */
    private void rrq() throws IOException {
        DatagramPacket response;
        response = receive();
        connectionTID = response.getPort();

        while (true) {

            serverPort = response.getPort();
            inform(response, "Packet Received", true);

            if (Packet.getPacketType(response) == Packet.PacketTypes.ERROR) {
                troubleshoot(response);
                break;
            }

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

                if (FileTransfer.getFreeSpace() < data.length) {
                    System.out.println("Disk Full or Allocation Exceeded, Terminating Transfer.");
                    fileTransfer.delete();
                    send(new ERRORPacket(response, TFTPError.DISK_FULL, ("Disk Full or Allocation Exceeded").getBytes()).getDatagram());
                    return;
                }

                fileTransfer.write(data);

                DatagramPacket ackPacket = new ACKPacket(response, BlockNumber.getBlockNumber(ackBlock)).getDatagram();
                ackPacket.setPort(serverPort);

                inform(ackPacket, "Sending ACK Packet", true);
                send(ackPacket);
            } else {
                troubleshoot(response);
                System.out.println("Terminating Client...");

                break;
            }

            if (fileTransfer.isComplete()) {
                break;
            }

            response = receive();
        }

        System.out.println("[IMPORTANT] Transfer complete!");

    }


    /**
     * Completes the write (WRQ) cycle that has been requested by the operator.
     * Ideally, DATA packets are sent to the destination and ACK packets are received from the destination until
     * the last DATA packet has been reached.
     *
     * The client is equipped with the knowledge to handle abnormal packets and take the necessary measures. Not
     * all measures are fatal and the client does attempt to recover the authenticity of the connection, if possible.
     *
     * @throws IOException
     */
    private void wrq() throws IOException {
        DatagramPacket response;
        response = receive();
        connectionTID = response.getPort();

        while(true) {

            serverPort = response.getPort();
            inform(response, "Packet Received", true);
            byte[] data = fileTransfer.read();

            if (Packet.getPacketType(response) == Packet.PacketTypes.ERROR) {
                troubleshoot(response);
                break;
            }

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
                DatagramPacket dataPacket = new DATAPacket(response, BlockNumber.getBlockNumber(dataBlock), data).getDatagram();
                dataPacket.setData(shrink(dataPacket.getData(), fileTransfer.lastBlockSize() + 4));
                dataPacket.setPort(serverPort);

                inform(dataPacket, "Sending DATA Packet", true);
                send(dataPacket);
                dataBlock++;
            } else {
                troubleshoot(response);
                System.out.println("Terminating Client...");

                break;
            }

            if (fileTransfer.isComplete()) {
                break;
            }

            response = receive();
        }

        System.out.println("[IMPORTANT] Transfer complete!");
    }


    /**
     * Troubleshoots the response received for the abnormality.
     *
     * @param response the abnormal DatagramPacket
     * @throws IOException
     */
    private void troubleshoot(DatagramPacket response) throws IOException {
        if (Packet.getPacketType(response) == Packet.PacketTypes.ERROR) {
            byte[] errorMsg = new byte[response.getLength() - 4];
            System.arraycopy(response.getData(), 4, errorMsg, 0, response.getData().length - 4);
            System.out.println("Error Packet Received: Error Code: 0" + response.getData()[3] + ", Error Message: " + new String(errorMsg));

            if (response.getData()[3] == 1) //File Not Found Error
                fileTransfer.delete();

        } else {

            //Received something other than an ACK or ERROR Packet, return an error 4 Packet to indicate corrupted stream
            String errorMsg = "Incorrect Packet Received";
            send(new ERRORPacket(response, TFTPError.ILLEGAL_TFTP_OPERATION, errorMsg.getBytes()).getDatagram());
        }
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

            String newTransfer = "y";
            while (newTransfer.equalsIgnoreCase("y")) {
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

                newTransfer = client.getInput("Would you like to start a new transfer? (y/N) ");
            }
        } catch (IOException | UnknownIOModeException e) {
            e.printStackTrace();
        }
    }
}
