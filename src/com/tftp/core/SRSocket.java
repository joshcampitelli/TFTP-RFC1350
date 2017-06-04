package com.tftp.core;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.io.IOException;

import com.tftp.Client;
import com.tftp.Server;
import com.tftp.core.protocol.BlockNumber;
import com.tftp.core.protocol.Packet;

/**
 * SRSocket is a wrapper class of DatagramSocket that allows for easier use of the networking interface by abstracting
 * much of the exhaustive setup. Moreover, it also adds helpful and constantly used methods on top of the interface.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Ahmed Sakr, Brian Zhang, Josh Campitelli, Ahmed Khattab, Dario Luzuriaga
 * @since May the 1st, 2017.
 */
public class SRSocket extends DatagramSocket {

    private String name;
    private static int TIMEOUT_TIME = 5000; //MilliSeconds to wait before retransmitting a packet
    private static int RETRANSMIT_NUM = 3; //Number of times to retransmit a packet

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

        // disable timeout if exists
        setSoTimeout(0);

        // reduce the buffer to the size of the data received, if possible
        data = shrink(packet.getData(), packet.getLength());
        packet.setData(data);

        return packet;
    }

    /**
     * Receive a packet but with a set timeout.
     * PLEASE NOTE: this method still *throws* the timeout exception, even though a try-catch block is in place.
     * The reason the try-catch block exists is to disable the timeout before throwing it.
     *
     * @param timeout the time (in milliseconds) to wait before timing out
     *
     * @return the packet
     * @throws IOException importantly SocketTimeoutException
     */
    public DatagramPacket receive(int timeout) throws IOException {
        setSoTimeout(timeout);

        try {
            return receive();
        } catch (SocketTimeoutException ex) {

            setSoTimeout(0);
            throw ex;
        }
    }

    /**
     * The waitForPacket method is simply a receive method with the timeout and retransmit implemented
     * the variables TIMEOUT_TIME & RETRANSMIT_NUM correspond to the time which it takes for the socket to
     * timeout initialized to 5000ms, and the number of times which you want to resend the packet before
     * giving up initialized to 3.
     *
     * @param retransmitPacket the packet to be resent if the Socket Times Out
     * @return DatagramPacket, the packet which is received
     * @throws IOException although catches the SocketTimeoutException
     */
    protected DatagramPacket waitForPacket(Packet retransmitPacket) throws IOException {
        DatagramPacket response = null;
        for (int i = 0; i < RETRANSMIT_NUM; i++) {
            try {
                response = receive(TIMEOUT_TIME);
                break;
            } catch (SocketTimeoutException e) {
                if (retransmitPacket != null && retransmitPacket.getType() == Packet.PacketTypes.DATA) {
                    inform(retransmitPacket, "Resending DATA Packet");
                    send(retransmitPacket);
                }
            }
        }

        //Will return null if the packet never arrives. Which Will lead to a NullPointerException in Connection
        return response;
    }

    public void send(Packet packet) throws IOException {
        super.send(packet.getDatagram());
    }

    public void inform(DatagramPacket packet, String event, boolean extra) {
        if (!Client.verbose && !Server.verbose) {
            return;
        }

        int len = packet.getLength();
        System.out.printf("%s: %s:\n", this.name, event);
        System.out.printf("Packet type: %s, Block Number: %d\n", Packet.getPacketType(packet), BlockNumber.getBlockNumber(packet.getData()));
        System.out.printf("%s Host: %s:%d, Length: %d\n",
                        event.contains("Send") ? "To" : "From", packet.getAddress(), packet.getPort(), len);
        System.out.printf("Data (as string): %s\n", new String(packet.getData(), 0, packet.getData().length));

        if (extra) {
            System.out.printf("Data (as bytes): %s\n\n", Arrays.toString(packet.getData()));
        }
    }

    public void inform(Packet packet, String event, boolean extra) {
        inform(packet.getDatagram(), event, extra);
    }

    public void inform(Packet packet, String event) {
        inform(packet.getDatagram(), event, false);
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
        System.arraycopy(arr, 0, arr1, 0, newLength);

        return arr1;
    }
}
