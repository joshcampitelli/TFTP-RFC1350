package com.tftp.core;

import com.tftp.Server;
import com.tftp.core.protocol.Authentication;
import com.tftp.core.protocol.BlockNumber;
import com.tftp.core.protocol.Packet;
import com.tftp.core.protocol.Packet.PacketTypes;
import com.tftp.core.protocol.TFTPError;
import com.tftp.core.protocol.packets.ACKPacket;
import com.tftp.core.protocol.packets.DATAPacket;
import com.tftp.core.protocol.packets.ERRORPacket;
import com.tftp.exceptions.AccessViolationException;
import com.tftp.exceptions.InvalidPacketException;
import com.tftp.exceptions.UnknownIOModeException;
import com.tftp.io.FileTransfer;

import java.net.DatagramPacket;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Connection allows for the dedicated thread birth for a new transfer request to be serviced. Connection containsKey
 * all the logic and power to fulfill all the needs of the transfer.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Ahmed Sakr, Josh Campitelli, Brian Zhang, Ahmed Khattab, Dario Luzuriaga
 * @since May the 1st, 2017.
 */
public class Connection extends SRSocket implements Runnable {

    private DatagramPacket request;
    private FileTransfer fileTransfer;
    private Authentication authenticator;
    private Server server;
    private int TID, clientTID;
    private int block;
    private boolean active = true;

    public Connection(Server server, DatagramPacket packet) throws IOException {
        super(String.format("Connection (Client TID: %d)", packet.getPort()), 0, InetAddress.getLocalHost());
        this.server = server;
        this.request = packet;
        this.TID = getPort();
        this.clientTID = packet.getPort();
        this.authenticator = new Authentication(this.clientTID);
    }


    //Removes the opcode & block number (first 4 bytes) from the data
    //used only for DATA Packets
    private byte[] extractData(byte[] msg) {
        byte[] data = new byte[msg.length - 4];
        System.arraycopy(msg, 4, data, 0, msg.length - 4);
        return data;
    }

    //Handles the different types of packets sent to the server, returns the returnPacket to go back to client (ACK/DATA)
    private Packet handlePacket(DatagramPacket received) throws UnknownIOModeException, IOException, InvalidPacketException {
        if (!authenticator.verify(received, block)) {
            Packet result = authenticator.getResult();

            if (authenticator.isDuplicate()) {
                return duplicateReceived(result);
            } else if (result.getType() == PacketTypes.ERROR && result.getDatagram().getData()[3] != 5) {
                active = false;
            }

            return result;
        }

        PacketTypes type = Packet.getPacketType(received);
        Packet response;
        if (type == PacketTypes.RRQ) {
            response = rrqReceived(received);
        } else if (type == PacketTypes.WRQ) {
            response = wrqReceived(received);
        } else if (type == PacketTypes.ACK) {
            response = ackReceived(received);
        } else if (type == PacketTypes.DATA) {
            response = dataReceived(received);
        } else if (type == PacketTypes.ERROR) {
            response = errorReceived(received);
        } else {
            throw new InvalidPacketException("Illegal packet parsed!!!");
        }

        if (active) {
            active = fileTransfer != null && !fileTransfer.isComplete();
        }

        return response;
    }

    //Read Request Received initializes the FileTransfer for mode READ, and sends DATA1 Packet
    private Packet rrqReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        authenticator.setMode("reading");
        String[] parameters = authenticator.extractTransferParameters(packet);
        String filename = parameters[0];

        if (server.getTransferController().isFileLocked(filename)) {
            return new ERRORPacket(packet, TFTPError.ACCESS_VIOLATION, ("Access violation").getBytes());
        }

        try {
            fileTransfer = new FileTransfer(filename, FileTransfer.READ);
        } catch (AccessViolationException ex) {
            return new ERRORPacket(packet, TFTPError.ACCESS_VIOLATION, ("Access violation").getBytes());
        }

        server.getTransferController().registerTransfer(fileTransfer);

        byte[] data = fileTransfer.read();
        data = shrink(data, fileTransfer.lastBlockSize());

        this.block = 1;
        return new DATAPacket(packet, BlockNumber.getBlockNumber(block), data);
    }

    //Write Request Received initializes the FileTransfer for mode WRITE, then sends ACK0 Packet
    private Packet wrqReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        authenticator.setMode("writing");
        String[] parameters = authenticator.extractTransferParameters(packet);
        String filename = parameters[0];

        if (!FileTransfer.isWritable(filename)) {
            return new ERRORPacket(packet, TFTPError.ACCESS_VIOLATION, ("Access violation").getBytes());
        }

        try {
            fileTransfer = new FileTransfer(filename, FileTransfer.WRITE);
        } catch (AccessViolationException ex) {
            return new ERRORPacket(packet, TFTPError.ACCESS_VIOLATION, ("Access violation").getBytes());
        }

        server.getTransferController().registerTransfer(fileTransfer);

        this.block = 0;
        return new ACKPacket(packet, BlockNumber.getBlockNumber(block++));
    }

    //Ack Received gets the bytes from the FileTransfer Object then sends DATA1 Packet
    private Packet ackReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        //Send Data from the file
        byte[] data = fileTransfer.read();
        data = shrink(data, fileTransfer.lastBlockSize());
        this.block++;

        return new DATAPacket(packet, BlockNumber.getBlockNumber(block), data);

    }

    //Data Received extracts the data (removed opcode/block#) then uses FileTransfer Object to Write the data
    private Packet dataReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        byte[] msg = extractData(packet.getData());
        fileTransfer.write(msg);
        return new ACKPacket(packet, BlockNumber.getBlockNumber(block++));
    }

    //Error Received handles the error packets which are sent to the server, different from detecting errors
    //No matter which error the Connection RECEIVES it shuts down. This is because if it receives an error
    //Packet that means the client has sent it an error, if its TID then the Connection is communicating with
    //The incorrect client.
    private Packet errorReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        //If the Server receives an invalid TID it must terminate, this means the Server is communicating with an incorrect Client
        byte[] errorMsg = new byte[packet.getLength() - 4];
        System.arraycopy(packet.getData(), 4, errorMsg, 0, packet.getData().length - 4);
        System.out.println("Error Packet Received: Error Code: 0" + packet.getData()[3] + ", Error Message: " + new String(errorMsg));

        active = packet.getData()[3] == (byte) 5;
        return null;
    }

    private Packet duplicateReceived(Packet duplicatePacket) throws IOException {
        if (duplicatePacket.getType() == Packet.PacketTypes.ACK){
            System.out.println("Duplicate ACK Received: Ignoring Packet.");
            return null;
        } else if (duplicatePacket.getType() == Packet.PacketTypes.DATA) {
            byte[] bn = new byte[2];
            bn[0] = duplicatePacket.getDatagram().getData()[2];
            bn[1] = duplicatePacket.getDatagram().getData()[3];

            System.out.println("Duplicate DATA Received. Sending corresponding ACK packet!");
            return new ACKPacket(duplicatePacket.getDatagram(), bn);
        } else {
            return null;
        }
    }

    private void process(DatagramPacket request) throws IOException, InvalidPacketException, UnknownIOModeException {
        Packet packet;

        while (true) {
            packet = handlePacket(request);

            if (packet != null){
                inform(packet, "Sending Packet");
                send(packet);
            }

            // transfer complete
            if (!active) {
                if (fileTransfer != null) {
                    server.getTransferController().deregisterTransfer(fileTransfer);
                    fileTransfer.close();
                }

                break;
            }

            request = waitForPacket(packet);
            if (request == null) {
                System.out.println("Packet was never Received.");

                if (fileTransfer != null) {
                    server.getTransferController().deregisterTransfer(fileTransfer);
                    fileTransfer.close();
                }
                break;
            }
            inform(request, "Received Packet", true);
        }

    }

    @Override
    public void run() {
        try {
            process(request);
            System.out.printf("%s terminated and is closing...\n", getName());
        } catch (IOException  | UnknownIOModeException e) {
            System.out.printf("A un-handled I/O error has been thrown by %s. Terminating thread...\n", getName());
        } catch (InvalidPacketException e) {
            System.out.printf("%s has sent an un-handled packet. Terminating thread...\n", getName());
        }
    }
}
