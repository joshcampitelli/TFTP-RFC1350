package com.tftp;

import java.net.DatagramPacket;
import java.io.IOException;

import com.tftp.core.Socket;
import com.tftp.core.protocol.*;
import com.tftp.core.protocol.packets.*;
import com.tftp.exceptions.AccessViolationException;
import com.tftp.exceptions.UnknownIOModeException;
import com.tftp.io.FileTransfer;

/**
 * Client defines and builds the logic for a user capable of transferring files to a pre-defined server port
 * through the agreed upon TFTP protocol.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Sakr, Brian Zhang, Ahmed Khattab, Dario Luzuriaga
 * @since May the 1st, 2017.
 */
public class Client extends Socket {

    private int block;
    private boolean isNormal = true;
    private FileTransfer fileTransfer;
    private Authentication authenticator;
    private int connectionTID;

    private enum ErrorStatus {FATAL_ERROR, NON_FATAL_ERROR, NO_ERROR, DUPLICATE}

    public Client() throws IOException {
        super("Client");
    }

    /**
     * Sets the instance state of the client regarding where the packet should be sent to.
     * A normal state would send the initial packet to the main server (port id: 69).
     * A testing state would send the initial packet to the error simulator (port id: 23).
     *
     * @param normal
     */
    public void setNormal(boolean normal) {
        this.isNormal = normal;
    }


    /**
     * Attempts to establish the transfer with the destination. Once a response has been received, the cycle
     * logic converges to the necessary method (i.e. rrq() or wrq()).
     *
     * @param filename    the operator-provided filename
     * @param mode        the operator-provided mode
     * @param requestType the operator-provided request type (i.e. RRQ or WRQ)
     * @throws IOException
     * @throws UnknownIOModeException
     */
    public void transfer(byte[] filename, byte[] mode, String ip, String requestType) throws IOException, UnknownIOModeException {
        int port = TFTPConfig.SERVER_PORT;
        if (!this.isNormal) {
            port = TFTPConfig.SIMULATOR_PORT;
        }

        Packet packet;

        if (requestType.toLowerCase().equals("r")) {
            packet = new RRQPacket(mode, filename, IPAddress.toInetSocketAddress(ip, port));

            try {
                fileTransfer = new FileTransfer(new String(filename), FileTransfer.WRITE);
            } catch (AccessViolationException ex) {
                System.out.println("Access violation: File not detectable or writable.");
                return;
            }

            inform(packet, "Sending Packet", true);
            send(packet);
            System.out.printf("[IMPORTANT] Sent Read request to server for file '%s'!\n", new String(filename));

            DatagramPacket response = waitForPacket(packet);
            if (response == null) {
                System.out.println("[IMPORTANT] Server never responded to request.");
                return;
            }

            rrq(response);
        } else {
            packet = new WRQPacket(mode, filename, IPAddress.toInetSocketAddress(ip, port));

            try {
                if (!FileTransfer.isFileExisting(new String(filename))) {
                    System.out.println("The File you wish to write does not exist.");
                    return;
                } else if (!FileTransfer.isReadable(new String(filename))) {
                    System.out.println("File Access Violation, Terminating Transfer.");
                    return;
                }

                fileTransfer = new FileTransfer(new String(filename), FileTransfer.READ);
            } catch (AccessViolationException ex) {
                System.out.println("Access violation: File not detectable or readable.");
                return;
            }

            inform(packet, "Sending Packet", true);
            send(packet);
            System.out.printf("[IMPORTANT] Sent Write request to server for file '%s'!\n", new String(filename));

            DatagramPacket response = waitForPacket(packet);
            if (response == null) {
                System.out.println("[IMPORTANT] Server never responded to request.");
                return;
            }

            wrq(response);
        }
    }

    /**
     * Completes the read (RRQ) cycle that has been requested by the operator.
     * Ideally, ACK packets are sent to the destination and DATA packets are received from the destination until
     * the last DATA packet has been read.
     * <p>
     * The client is equipped with the knowledge to handle abnormal packets and take the necessary measures. Not
     * all measures are fatal and the client does attempt to recover the authenticity of the connection, if possible.
     *
     * @throws IOException
     */
    private void rrq(DatagramPacket response) throws IOException {
        connectionTID = response.getPort();
        authenticator = new Authentication(connectionTID);
        authenticator.setMode("writing");
        authenticator.setFilename(fileTransfer.getFileName());

        Packet ackPacket;
        this.block = 0;

        while (true) {
            this.block++;

            if (response == null) {
                System.out.println("DATA Packet was never Received.");
                break;
            }

            connectionTID = response.getPort();
            inform(response, "Packet Received", true);

            if (Packet.getPacketType(response) == Packet.PacketTypes.DATA) {
                ErrorStatus status = checkPacket(response, block);
                if (status == ErrorStatus.FATAL_ERROR) {
                    break;
                } else if (status == ErrorStatus.NON_FATAL_ERROR) {
                    if (fileTransfer.isComplete())
                        break;

                    response = receive(); //Wont timeout if there is an incorrect TID
                    continue;
                } else if (status == ErrorStatus.DUPLICATE) {
                    // must acknowledge the DATA packet
                    Packet packet = new ACKPacket(response, new byte[]{response.getData()[2], response.getData()[3]});
                    inform(packet, "Sending Packet");
                    send(packet);

                    this.block--; //CRITICAL
                    response = waitForPacket(null);
                    continue;
                }

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

                ackPacket = new ACKPacket(response, BlockNumber.getBlockNumber(block));
                ackPacket.getDatagram().setPort(connectionTID);

                inform(ackPacket, "Sending Packet", true);
                send(ackPacket);

                if (fileTransfer.isComplete()) {
                    break;
                }

                response = waitForPacket(ackPacket);
            } else {
                if (Packet.getPacketType(response) != Packet.PacketTypes.ERROR)
                    checkPacket(response, block); //Packet Was Modified and No longer identifies as an DATA even though it is.
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
     * <p>
     * The client is equipped with the knowledge to handle abnormal packets and take the necessary measures. Not
     * all measures are fatal and the client does attempt to recover the authenticity of the connection, if possible.
     *
     * @throws IOException
     */
    private void wrq(DatagramPacket response) throws IOException {
        connectionTID = response.getPort();
        authenticator = new Authentication(connectionTID);
        authenticator.setMode("reading");
        authenticator.setFilename(fileTransfer.getFileName());

        Packet dataPacket;
        this.block = 0;
        while (true) {
            if (response == null) {
                System.out.println("ACK Packet was never Received.");
                break;
            }

            connectionTID = response.getPort();
            inform(response, "Packet Received", true);
            //Ensure the packet received from the server is of type ACK
            if (Packet.getPacketType(response) == Packet.PacketTypes.ACK) {
                ErrorStatus status = checkPacket(response, block);
                if (status == ErrorStatus.FATAL_ERROR) {
                    break;
                } else if (status == ErrorStatus.NON_FATAL_ERROR) {
                    System.out.println();
                    if (fileTransfer.isComplete())
                        break;

                    response = receive();
                    continue;
                } else if (status == ErrorStatus.DUPLICATE) {
                    //If response is a duplicate, that indicates that the connection never received the original
                    //ack and must resend the original ack.
                    response = receive();
                    continue;
                }

                if (fileTransfer.isComplete()) {
                    break;
                }

                this.block++;
                byte[] data = fileTransfer.read();
                data = shrink(data, fileTransfer.lastBlockSize());

                dataPacket = new DATAPacket(response, BlockNumber.getBlockNumber(block), data);
                dataPacket.getDatagram().setPort(connectionTID);

                inform(dataPacket, "Sending Packet", true);
                send(dataPacket);

                response = waitForPacket(dataPacket);
            } else {
                if (Packet.getPacketType(response) != Packet.PacketTypes.ERROR)
                    checkPacket(response, block); //Packet Was Modified and No longer identifies as an ACK even though it is.
                else
                    troubleshoot(response);

                System.out.println("Terminating Client...");
                break;
            }
        }

        System.out.println("[IMPORTANT] Transfer finished!");
    }

    /**
     * The checkPacket method calls the validatePacket on Socket which detects packet errors, this method then
     * determines whether there was an error and returns the type. It also makes a call to the errorDetected method.
     *
     * @param received    the DatagramPacket which is being tested for errors.
     * @param blockNumber the expected value for the Block Number.
     * @return ErrorStatus enum {FATAL_ERROR, NON_FATAL_ERROR, NO_ERROR}
     * @throws IOException
     */
    private ErrorStatus checkPacket(DatagramPacket received, int blockNumber) throws IOException {
        //Parses Received ACK & DATA Packets to check for Unknown TID, DATA size > 512, undefined opcodes, & incorrect block numbers.
        if (authenticator.verify(received, blockNumber)) {
            return ErrorStatus.NO_ERROR;
        } else if (authenticator.isDuplicate()) {
            System.out.println("Duplicate Packet Received: Ignoring Packet.");
            return ErrorStatus.DUPLICATE;
        } else {
            Packet result = authenticator.getResult();
            inform(result, "Sending Error Packet");
            send(result);

            if (authenticator.getError() != TFTPError.UNKNOWN_TRANSFER_ID) {
                System.out.println("Terminating Client...");
                return ErrorStatus.FATAL_ERROR;
            } else {
                System.out.println("Ignoring Packet, Continuing Execution.");
                return ErrorStatus.NON_FATAL_ERROR;
            }
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
}
