package com.tftp.core;

import com.tftp.exceptions.InvalidPacketException;
import com.tftp.exceptions.UnknownIOModeException;
import com.tftp.io.FileTransfer;

import java.net.DatagramPacket;
import java.io.IOException;

/**
 * Connection allows for the dedicated thread birth for a new transfer request to be serviced. Connection containsKey
 * all the logic and power to fulfill all the needs of the transfer.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 1st, 2017.
 */
public class Connection extends SRSocket implements Runnable {

    private DatagramPacket request;
    private int TID, clientTID;
    private FileTransfer fileTransfer;
    private int ackBlock = 0;
    private int dataBlock = 1;
    private boolean active = true;

    public Connection(DatagramPacket packet) throws IOException {
        super(String.format("Connection (Client TID: %d)", packet.getPort()));
        this.request = packet;
        this.TID = getPort();
        this.clientTID = packet.getPort();
    }

    public int getTID() {
        return this.TID;
    }

    public int getClientTID() {
        return this.clientTID;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    //extractFilename method only gets called once a RRQ or WRQ Packet has arrived on the server
    //The method extracts the filename from the data portion of the packet
    private String extractFilename(DatagramPacket packet) {
        byte[] file = new byte[100];
        int len = 0;

        //Loop through the data, starting at index position 2 (surpassing the opcode)
        for(int i = 2; i < packet.getData().length; i++) {
            //If byte is 0 break, since the filename is enclosed with 0's
            if(packet.getData()[i] == 0) {
                break;
            } else {
                file[i - 2] = packet.getData()[i];
                len++;
            }
        }

        file = shrink(file, len);
        return new String(file);
    }

    //Removes the opcode & block number (first 4 bytes) from the data
    //used only for DATA Packets
    private byte[] extractData(byte[] msg) {
        byte[] data = new byte[msg.length - 4];
        System.arraycopy(msg, 4, data, 0, msg.length - 4);
        return data;
    }

    //Handles the different types of packets sent to the server, returns the returnPacket to go back to client (ACK/DATA)
    // TODO: damn is did'nt know this was a keyword, this method is very messy...
    private DatagramPacket handlePacket(DatagramPacket receivedPacket) throws UnknownIOModeException, IOException, InvalidPacketException {

        Packet packet = new Packet();
        if (packet.checkPacketType(receivedPacket) == Packet.PacketTypes.RRQ) {
            return rrqReceived(receivedPacket);
        } else if (packet.checkPacketType(receivedPacket) == Packet.PacketTypes.WRQ) {
            return wrqReceived(receivedPacket);
        } else if (packet.checkPacketType(receivedPacket) == Packet.PacketTypes.ACK) {
            return ackReceived(receivedPacket);
        } else if (packet.checkPacketType(receivedPacket) == Packet.PacketTypes.DATA) {
            return dataReceived(receivedPacket); 
        } else {
            throw new InvalidPacketException("Illegal data buffer!!!");
        }
    }

    //Read Request Received initializes the FileTransfer for mode READ, and sends DATA1 Packet
    private DatagramPacket rrqReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        String filename = extractFilename(packet);
        fileTransfer = new FileTransfer(FileTransfer.SERVER_DIRECTORY + filename, FileTransfer.READ);
        byte[] data = fileTransfer.read();
        data = shrink(data, fileTransfer.lastBlockSize());

        DatagramPacket temp = new Packet(packet).DATAPacket(getBlockNumber(dataBlock), data);
        dataBlock++;

        return temp;
    }

    //Write Request Received initializes the FileTransfer for mode WRITE, then sends ACK0 Packet
    private DatagramPacket wrqReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        String filename = extractFilename(packet);
        fileTransfer = new FileTransfer(FileTransfer.SERVER_DIRECTORY + filename, FileTransfer.WRITE);
        DatagramPacket temp =  new Packet(packet).ACKPacket(getBlockNumber(ackBlock));
        ackBlock++;

        return temp;
    }

    //Ack Received gets the bytes from the FileTransfer Object then sends DATA1 Packet
    private DatagramPacket ackReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        if (clientTID != TID) {
            return new Packet().ERRORPacket(Packet.ERROR_UNKNOWN_TRANSFER_ID, "Invalid TID".getBytes());
        } else {
            //Send Data from the file
            byte[] data = fileTransfer.read();

            DatagramPacket temp = new Packet(packet).DATAPacket(getBlockNumber(dataBlock), data);

            // shrink data array to amount of read bytes
            temp.setData(shrink(temp.getData(), fileTransfer.lastBlockSize() + 4));
            dataBlock++;
            return temp;
        }
    }

    //Data Received extracts the data (removed opcode/block#) then uses FileTransfer Object to Write the data
    private DatagramPacket dataReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        if (clientTID != TID) {
            return new Packet().ERRORPacket(Packet.ERROR_UNKNOWN_TRANSFER_ID, "Invalid TID".getBytes());
        } else {
            byte[] msg = extractData(packet.getData());
            fileTransfer.write(msg);
            
            DatagramPacket temp = new Packet(packet).ACKPacket(getBlockNumber(ackBlock));
            ackBlock++;
            return temp;
        }
    }

    private void process(DatagramPacket request) throws IOException, InvalidPacketException, UnknownIOModeException {
        DatagramPacket packet = handlePacket(request);
        inform(packet, "Sending Packet");
        send(packet);

        // transfer complete
        if (fileTransfer != null && fileTransfer.isComplete()) {
            setActive(false);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                process(request);

                if (!isActive()) {
                    break;
                }

                request = receive();
                inform(request, "Received Packet");
            }

            System.out.printf("%s completed successfully and is closing...\n", getName());
        } catch (IOException | InvalidPacketException | UnknownIOModeException e) {
            System.out.printf("%s sent an invalid request. Terminating thread...\n", getName());
        }
    }
}
