package com.tftp.core;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.io.IOException;

import com.tftp.Client;
import com.tftp.Server;

/**
 * SRSocket is a wrapper class of DatagramSocket that allows for easier use of the networking interface by abstracting
 * much of the exhaustive setup. Moreover, it also adds helpful and constantly used methods on top of the interface.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 1st, 2017.
 */
public class SRSocket extends DatagramSocket {

    private String name;

    public SRSocket(String name) throws IOException {
        super();
        this.name = name;
    }

    public SRSocket(String name, int port) throws IOException {
        this(name, port, InetAddress.getLocalHost());
    }

    public SRSocket(String name, int port, InetAddress address) throws IOException {
        super(port, address);
        this.name = name;
    }

    protected String getName() {
        return this.name;
    }

    public DatagramPacket receive() throws IOException {
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        this.receive(packet);

        // reduce the buffer to the size of the data received, if possible
        data = shrink(packet.getData(), packet.getLength());
        packet.setData(data);

        return packet;
    }

    public void send(DatagramPacket packet) throws IOException {
        super.send(packet);
    }
    public void send(InetAddress host, int port, byte[] data) throws IOException {
        this.send(new DatagramPacket(data, data.length, host, port));
    }

    public void inform(DatagramPacket packet, String event, boolean extra) {
        if (!Client.verbose && !Server.verbose) {
            return;
        }

        int len = packet.getLength();
        System.out.printf("%s: %s:\n", this.name, event);
        System.out.printf("%s Host Address: %s, Host port: %d, Length: %d\n",
                        event.contains("Send") ? "To" : "From", packet.getAddress(), packet.getPort(), len);
        System.out.printf("Data (as string): %s\n", new String(packet.getData(), 0, packet.getData().length));

        if (extra) {
            System.out.printf("Data (as bytes): %s\n\n", Arrays.toString(packet.getData()));
        }
    }

    public void inform(DatagramPacket packet, String event) {
        inform(packet, event, false);
    }


    /**
     * Shrinks the provided array to a new, specified length. If the new length provided is bigger
     * than the array's existing size, then the method returns without doing anything.
     *
     * @return the shrunk array
     */
    protected byte[] shrink(byte[] arr, int newLength) {
        if (newLength > arr.length) {
            return arr;
        }

        byte[] arr1 = new byte[newLength];
        for (int i = 0; i < arr1.length; i++) {
            arr1[i] = arr[i];
        }

        return arr1;
    }

    /**
     *
     * @param received DatagramPacket the Client received
     * @param expectedTID The TID that the packet is expected to come from
     * @return DatagramPacket
     *
     * parseUnknownPacket parses the packet given to it and determines which type of error it is.
     *
     * Error Packet Type 4: Incorrect Packet ie. Opcode error, Packet Size Error, AckPacket Error.
     *                      Client Shall Terminate.
     *
     * Error Packet Type 5: Incorrect TID, meaning the Connection the Client sent a Packet to was not
     *                      expecting a Packet from and returned an Error Packet Type 5 to said Client.
     *                      In such case the Client shall terminate.
     *
     * If any Client has received an Error Packet this means that either the data received from the Server
     * was corrupt or the Server was not expecting Packets from this Client.
     */
    public DatagramPacket parseUnknownPacket(DatagramPacket received, int expectedTID, int blockNumber) { //Can also check block numbers as well.
        byte[] data = received.getData();
        String errorMsg = "";
        DatagramPacket errorPacket;
        Packet packet = new Packet(received);

        //System.out.println("Expected TID: " + expectedTID + ", actual TID: " + received.getPort());
        if (received.getPort() != expectedTID) { //Incorrect TID
            errorMsg = "Incorrect TID";
            errorPacket = packet.ERRORPacket(Packet.ERROR_UNKNOWN_TRANSFER_ID, errorMsg.getBytes());
        } else if (data.length > 516) {             //Error type 4: corrupt data
            errorMsg = "Data greater than 512";
            errorPacket = packet.ERRORPacket(Packet.ERROR_ILLEGAL_TFTP_OPERATION, errorMsg.getBytes());
        } else if (data[1] > 5) {                   //Opcode 06 or greater is an undefined opCode
            errorMsg = "Undefined OpCode";
            errorPacket = packet.ERRORPacket(Packet.ERROR_ILLEGAL_TFTP_OPERATION, errorMsg.getBytes());
        } else if (blockNumber != -1 && BlockNumber.getBlockNumber(received.getData()) != blockNumber) {
            errorMsg = "Incorrect Block Number";
            errorPacket = packet.ERRORPacket(Packet.ERROR_ILLEGAL_TFTP_OPERATION, errorMsg.getBytes());
        } else {                                    //Unknown Packet was received, send back fatal Error Packet 4
            errorPacket = null;
        }
        if (errorPacket != null)
            System.out.println("Error Packet Detected: Error Code: 0" + errorPacket.getData()[3] + ", Error Message: " + errorMsg);

        return errorPacket;
    }

}
