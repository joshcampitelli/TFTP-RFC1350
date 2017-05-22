package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class RRQPacket extends ReadWritePacket {

    private DatagramPacket packet;
    private DatagramPacket readPacket;

    public RRQPacket() {
        super();
    }

    public RRQPacket(DatagramPacket packet) {
        super(packet);
    }

    public RRQPacket(DatagramPacket packet, byte[] mode, byte[] filename) {
        super(packet);
        readPacket = createPacket(RRQ(mode, filename));
    }

    public RRQPacket(byte[] mode, byte[] filename, InetAddress address, int port) {
        super();
        readPacket = createPacket(RRQ(mode, filename), address, port);
    }

    public DatagramPacket get() {
        return readPacket;
    }

    public void set(byte[] mode, byte[] filename) {
        readPacket = createPacket(RRQ(mode, filename));
    }

    public void set(byte[] mode, byte[] filename, InetAddress address, int port) {
        readPacket = createPacket(RRQ(mode, filename), address, port);
    }

    private byte[] RRQ(byte[] mode, byte[] filename) {
        return createRequest((byte) 1, mode, filename);
    }
}
