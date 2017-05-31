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
    private enum ErrorStatus {FATAL_ERROR, NON_FATAL_ERROR, NO_ERROR, DUPLICATE}

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
    private String getInput(String text) {
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
        DatagramPacket ackPacket = null;

        while (true) {
            if (response == null) {
                System.out.println("DATA Packet was never Received.");
                break;
            }

            serverPort = response.getPort();
            inform(response, "Packet Received", true);

            if (Packet.getPacketType(response) == Packet.PacketTypes.DATA) {
                ErrorStatus status = checkPacket(response, this.connectionTID, ackBlock + 1);
                if (status == ErrorStatus.FATAL_ERROR) {
                    break;
                } else if (status == ErrorStatus.NON_FATAL_ERROR) {
                    if (fileTransfer.isComplete())
                        break;

                    response = receive(); //Wont timeout if there is an incorrect TID
                    continue;
                } else if (status == ErrorStatus.DUPLICATE) {
                    //If response is a duplicate, that indicates that the connection never received the original
                    //ack and must resend the original ack.
                    System.out.println();
                    inform(ackPacket, "Resending ACK Packet", true);
                    send(ackPacket);
                    response = this.waitForPacket(ackPacket);
                    continue;
                }

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

                ackPacket = new ACKPacket(response, BlockNumber.getBlockNumber(ackBlock)).getDatagram();
                ackPacket.setPort(serverPort);

                inform(ackPacket, "Sending ACK Packet", true);
                send(ackPacket);

                if (fileTransfer.isComplete()) {
                    break;
                }

                response = waitForPacket(ackPacket);
            } else {
                if (Packet.getPacketType(response) != Packet.PacketTypes.ERROR)
                    checkPacket(response, this.connectionTID, dataBlock - 1); //Packet Was Modified and No longer identifies as an DATA even though it is.
                else
                    troubleshoot(response);

                break;
            }
        }

        System.out.println("[IMPORTANT] Transfer finished!");

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
        DatagramPacket dataPacket = null;

        while(true) {
            if (response == null) {
                System.out.println("ACK Packet was never Received.");
                break;
            }

            serverPort = response.getPort();
            inform(response, "Packet Received", true);
            byte[] data = fileTransfer.read();

            //Ensure the packet received from the server is of type ACK
            if (Packet.getPacketType(response) == Packet.PacketTypes.ACK) {
                ErrorStatus status = checkPacket(response, this.connectionTID, dataBlock - 1);
                if (status == ErrorStatus.FATAL_ERROR) {
                    System.out.println("FATAL ERROR DETECTED");
                    break;
                } else if (status == ErrorStatus.NON_FATAL_ERROR) {
                    System.out.println("NON-FATAL ERROR DETECTED");
                    if (fileTransfer.isComplete())
                        break;

                    response = receive();
                    continue;
                } else if (status == ErrorStatus.DUPLICATE) {
                    System.out.println("DUPLICATE PACKET DETECTED");
                    //If response is a duplicate, that indicates that the connection never received the original
                    //ack and must resend the original ack.
                    inform(dataPacket, "Resending ACK Packet", true);
                    send(dataPacket);
                    response = this.waitForPacket(dataPacket);


                    continue;
                }

                dataPacket = new DATAPacket(response, BlockNumber.getBlockNumber(dataBlock), data).getDatagram();
                dataPacket.setData(shrink(dataPacket.getData(), fileTransfer.lastBlockSize() + 4));
                dataPacket.setPort(serverPort);

                inform(dataPacket, "Sending DATA Packet", true);
                send(dataPacket);
                dataBlock++;

                if (fileTransfer.isComplete()) {
                    break;
                }

                response = waitForPacket(dataPacket);
            } else {
                if (Packet.getPacketType(response) != Packet.PacketTypes.ERROR)
                    checkPacket(response, this.connectionTID, dataBlock - 1); //Packet Was Modified and No longer identifies as an ACK even though it is.
                else
                    troubleshoot(response);

                System.out.println("Terminating Client...");
                break;
            }
        }

        System.out.println("[IMPORTANT] Transfer finished!");
    }

    /**
     * The checkPacket method calls the parseUnknownPacket on SRSocket which detects packet errors, this method then
     * determines whether there was an error and returns the type. It also makes a call to the errorDetected method.
     *
     * @param received the DatagramPacket which is being tested for errors.
     * @param expectedTID the expected value for the TID.
     * @param blockNumber the expected value for the Block Number.
     * @return ErrorStatus enum {FATAL_ERROR, NON_FATAL_ERROR, NO_ERROR}
     * @throws IOException
     */
    private ErrorStatus checkPacket(DatagramPacket received, int expectedTID, int blockNumber) throws IOException {
        //Parses Received ACK & DATA Packets to check for Unknown TID, DATA size > 512, undefined opcodes, & incorrect block numbers.
        DatagramPacket temp = super.parseUnknownPacket(received, expectedTID, blockNumber);
        //If temp is a duplicate packet...
        if (temp == null) {
            return ErrorStatus.NO_ERROR;
        }
        if (Packet.getPacketType(temp) == Packet.PacketTypes.ACK || Packet.getPacketType(temp) == Packet.PacketTypes.DATA) {
            System.out.println("DUPLICATE PACKET RECEIVED");
            return ErrorStatus.DUPLICATE;
        }
        
        inform(temp, "Sending Error Packet");
        send(temp);
        if (temp.getData()[3] == 4) {
            System.out.println("Terminating Client...");
            return ErrorStatus.FATAL_ERROR;    //Fatal Error Detected (Error Code: 04)
        } else {
            System.out.println("Ignoring Packet, Continuing Execution.");
            return ErrorStatus.NON_FATAL_ERROR; //Non-Fatal Error Detected (Error Code: 05)
        }
    }

    /**
     * Troubleshoots the response received for the abnormality. Received an Error Packet from the Connection.
     *
     * @param errorPacket the abnormal DatagramPacket
     * @throws IOException
     */
    private void troubleshoot(DatagramPacket errorPacket) throws IOException {
        if (Packet.getPacketType(errorPacket) == Packet.PacketTypes.ERROR) {
            byte[] errorMsg = new byte[errorPacket.getLength() - 4];
            System.arraycopy(errorPacket.getData(), 4, errorMsg, 0, errorPacket.getData().length - 4);
            System.out.println("Error Packet Received: Error Code: 0" + errorPacket.getData()[3] + ", Error Message: " + new String(errorMsg));

            if (errorPacket.getData()[3] == 1) //File Not Found Error
                fileTransfer.delete();

        } else {

            //Received something other than an ACK or ERROR Packet, return an error 4 Packet to indicate corrupted stream
            String errorMsg = "Incorrect Packet Received";
            send(new ERRORPacket(errorPacket, TFTPError.ILLEGAL_TFTP_OPERATION, errorMsg.getBytes()).getDatagram());
        }
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();
            FileTransfer.setup(FileTransfer.CLIENT_DIRECTORY);
            if (FileTransfer.parentDirectory.equalsIgnoreCase("q"));
            System.out.println("Input 'q' during any instruction to quit.");
            while (true) {
                String dataMode = client.getInput("The Client is set to normal. Would you like to set it to test? (y/N) ");
                if (dataMode.toLowerCase().equals("y")) {
                    client.setNormal(false);
                } else if (dataMode.toLowerCase().equals("q")) {
                    break;
                }

                String verbosity = client.getInput("The Client is set to quiet. Would you like to set it to verbose? (y/N) ");
                if (verbosity.toLowerCase().equals("y")) {
                    verbose = true;
                } else if (verbosity.toLowerCase().equals("q")) {
                    break;
                }

                String newTransfer = "y";
                String requestType = "";
                while (newTransfer.equalsIgnoreCase("y")) {
                    requestType = "";
                    while (!(requestType.toLowerCase().equals("r") || requestType.toLowerCase().equals("w") || requestType.toLowerCase().equals("q"))) {
                        requestType = client.getInput("Would you like to Write or Read? (W/R) ");
                    }
                    if (requestType.toLowerCase().equals("q")) {
                        break;
                    }

                    byte[] filename = client.getInput("Enter file name: ").getBytes();
                    byte[] mode = "octet".getBytes();
                    client.transfer(filename, mode, requestType);
                    client.close();

                    client = new Client();
                    client.setNormal(!dataMode.toLowerCase().equals("y"));

                    newTransfer = client.getInput("Would you like to start a new transfer? (y/N) ");
                }
                if (requestType.toLowerCase().equals("q") || !newTransfer.equalsIgnoreCase("y")) {
                    break;
                }
            }
            client.close();
            System.out.println("Client closed");
        } catch (IOException | UnknownIOModeException e) {
            e.printStackTrace();
        }
    }
}