package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import com.tftp.core.protocol.Packet;

public class ACKPacket extends Packet {

    private DatagramPacket packet;
    private DatagramPacket ackPacket;

    public ACKPacket() {
        super();
    }

    public ACKPacket(DatagramPacket packet) {
        super(packet);
    }

    public ACKPacket(DatagramPacket packet, byte[] blockNumber) {
        super(packet);
        ackPacket = createPacket(ACK(blockNumber));
    }

    public ACKPacket(byte[] blockNumber, InetAddress address, int port) {
        super();
        ackPacket = createPacket(ACK(blockNumber), address, port);
    }

    @Override
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
