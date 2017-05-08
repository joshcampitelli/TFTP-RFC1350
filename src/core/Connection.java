package core;

import exceptions.InvalidPacketException;
import exceptions.UnknownIOModeException;
import io.FileTransfer;

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
        System.out.println("=============" + new String(file) + "==============");
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
            //Throw an error
            System.out.println("Illegal data buffer!!!");
            throw new InvalidPacketException("Illegal data buffer!!!");

        }
    }

    //Read Request Received initializes the FileTransfer for mode READ, and sends DATA1 Packet
    private DatagramPacket rrqReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        String filename = extractFilename(packet);
        fileTransfer = new FileTransfer(filename, FileTransfer.READ);
        byte[] data = fileTransfer.read();
        data = shrink(data, fileTransfer.lastBlockSize());

        DatagramPacket temp = new Packet(packet).DATAPacket(getBlockNumber(dataBlock), data);
        dataBlock++;

        return temp;
    }

    //Write Request Received initializes the FileTransfer for mode WRITE, then sends ACK0 Packet
    private DatagramPacket wrqReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        String filename = extractFilename(packet);
        fileTransfer = new FileTransfer(filename, FileTransfer.WRITE);
        DatagramPacket temp =  new Packet().ACKPacket(getBlockNumber(ackBlock));
        return temp;
    }

    //Ack Received gets the bytes from the FileTransfer Object then sends DATA1 Packet
    private DatagramPacket ackReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        //Send Data from the file
        byte[] data = fileTransfer.read();

        DatagramPacket temp = new Packet().DATAPacket(data, getBlockNumber(dataBlock));
        dataBlock++;
        return temp;
    }

    //Data Received extracts the data (removed opcode/block#) then uses FileTransfer Object to Write the data
    private DatagramPacket dataReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        byte[] msg = extractData(packet.getData());
        fileTransfer.write(msg);
        DatagramPacket temp = new Packet().ACKPacket(getBlockNumber(ackBlock));
        ackBlock++;
        return temp;
    }

    private void process(DatagramPacket request) throws IOException, InvalidPacketException, UnknownIOModeException {
        DatagramPacket packet = handlePacket(request);
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
        } catch (IOException | InvalidPacketException | UnknownIOModeException e) {
            System.out.println("=================================");
            e.printStackTrace();
            System.out.printf("%s sent an invalid request. Terminating thread...\n", getName());
        }
    }
}
