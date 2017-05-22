package com.tftp.core.protocol;

import java.net.DatagramPacket;
import java.net.InetAddress;

import java.util.Arrays;

public class AckPacket extends Packet {

    private DatagramPacket packet;
    private DatagramPacket ackPacket;

    public AckPacket() {
        super();
    }
    
    public AckPacket(DatagramPacket packet) {
        super(packet);
    }

    public AckPacket(DatagramPacket packet, byte[] blockNumber) {
        super(packet);
        ackPacket = createPacket(ACK(blockNumber));
    }

    public AckPacket(byte[] blockNumber, InetAddress address, int port) {
        super();
        ackPacket = createPacket(ACK(blockNumber), address, port);
    }

    public DatagramPacket get() {
        return ackPacket;
    }

    public void set(byte[] blockNumber) {
        ackPacket = createPacket(ACK(blockNumber));
    }
    
    public void set(byte[] blockNumber, InetAddress address, int port) {
        ackPacket = createPacket(ACK(blockNumber), address, port);
    }

    private byte[] ACK(byte[] blockNumber) {
        byte[] ack = new byte[ACK_SIZE];
        ack[1] = 4;
        System.arraycopy(blockNumber, 0, ack, 2, 2);
        return ack;
    }
}
