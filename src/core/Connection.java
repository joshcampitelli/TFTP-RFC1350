package core;

import exceptions.InvalidPacketException;
import exceptions.UnknownIOModeException;
import io.FileTransfer;

import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.io.IOException;

/**
 *
 * @author Carleton Summer 2017 SYSC 3303A Group 2
 * @since May the 5th, 2017
 */
public class Connection extends SRSocket implements Runnable {

    private DatagramPacket request;
    private int TID, clientTID;
    private enum PacketTypes { ACK, DATA, RRQ, WRQ}
    private FileTransfer fileTransfer;
    private int ackBlock = 0;
    private int dataBlock = 1;

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

    //converts an integer into a 2 byte array
    private byte[] getBlockNumber(int input) {
        byte[] data = new byte[2]; // <- assuming "in" value in 0..65535 range and we can use 2 bytes only

        data[0] = (byte)(input & 0xFF);
        data[1] = (byte)((input >> 8) & 0xFF);

        return data;
    }

    //extractFilename method only gets called once a RRQ or WRQ Packet has arrived on the server
    //The method extracts the filename from the data portion of the packet
    private String extractFilename(DatagramPacket packet) {
        String file = "";

        //Loop through the data, starting at index position 2 (surpassing the opcode)
        for(int i = 2; i < packet.getData().length; i++) {
            //If byte is 0 break, since the filename is enclosed with 0's
            if(packet.getData()[i] == 0) {
                break;
            } else {
                file += packet.getData()[i];
            }
        }
        return file;
    }

    //Removes the opcode & block number (first 4 bytes) from the data
    //used only for DATA Packets
    private byte[] extractData(byte[] msg) {
        byte[] data = new byte[msg.length - 4];
        System.arraycopy(msg, 4, data, 0, msg.length - 4);
        return data;
    }

    //Handles the different types of packets sent to the server
    // TODO: damn is did'nt know this was a keyword, this method is very messy...
    private void handlePacket(DatagramPacket receivedPacket) throws UnknownIOModeException, IOException, InvalidPacketException {

        Packet packet = new Packet(receivedPacket);

        if (packet.checkPacketType(receivedPacket) == Packet.PacketTypes.RRQ) {
            rrqReceived(receivedPacket);
        } else if (packet.checkPacketType(receivedPacket) == Packet.PacketTypes.WRQ) {
            wrqReceived(receivedPacket);
        } else if (packet.checkPacketType(receivedPacket) == Packet.PacketTypes.ACK) {
            ackReceived(receivedPacket);
        } else if (packet.checkPacketType(receivedPacket) == Packet.PacketTypes.DATA) {
            dataReceived(receivedPacket);
        } else {
            //Throw an error
            throw new InvalidPacketException("Illegal data buffer!!!");
        }
    }

    //Read Request Received initializes the FileTransfer for mode READ, and sends DATA1 Packet
    private void rrqReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        String filename = extractFilename(packet);
        fileTransfer = new FileTransfer(filename, 1);
    }

    //Write Request Received initializes the FileTransfer for mode WRITE, then sends ACK0 Packet
    private void wrqReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        String filename = extractFilename(packet);
        fileTransfer = new FileTransfer(filename, 2);
    }

    //Ack Received gets the bytes from the FileTransfer Object then sends DATA1 Packet
    private void ackReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        //Send Data from the file
        byte[] data = fileTransfer.read();
        //Create DATA Packet using read data, then send
    }

    //Data Received extracts the data (removed opcode/block#) then uses FileTransfer Object to Write the data
    private void dataReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        byte[] msg = extractData(packet.getData());
        fileTransfer.write(msg);
        //send an ACK Packet
        byte[] ackBlock = {0, 4, 0, 0};
        //Zero is a placeholder, block number increments each time an ACK packet gets sent
        //DatagramPacket ackPacket = new DatagramPacket(ackData); //Pass it the data
        //send(ackPacket); //Need to change send and receive to use Packet instead of DatagramPacket
    }

    /**
     * Produce a response from the given byte array.
     */
    public byte[] parse(byte[] data) throws InvalidPacketException {
        if (data[1] == 1) {
            return new byte[]{0, 3, 0, 1};
        } else if (data[1] == 2) {
            return new byte[]{0, 4, 0, 0};
        } else {
            throw new InvalidPacketException("Illegal data buffer!!!");
        }
    }

    private void process(DatagramPacket request) throws IOException, InvalidPacketException {
        byte[] response = parse(request.getData());

        DatagramPacket packet = new DatagramPacket(response, response.length, request.getAddress(), request.getPort());
        inform(packet, "Sending Packet");
        send(packet);
    }

    @Override
    public void run() {
        try {
            while (true) {
                process(request);

                request = receive();
                inform(request, "Received Packet");
            }
        } catch (IOException | InvalidPacketException e) {
            System.out.printf("%s sent an invalid request. Terminating thread...\n", getName());
        }
    }
}
