package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class WRQPacket extends ReadWritePacket {

    private DatagramPacket packet;
    private DatagramPacket writePacket;

    public WRQPacket() {
        super();
    }

    public WRQPacket(DatagramPacket packet) {
        super(packet);
    }

    public WRQPacket(DatagramPacket packet, byte[] mode, byte[] filename) {
        super(packet);
        writePacket = createPacket(WRQ(mode, filename));
    }

    public WRQPacket(byte[] mode, byte[] filename, InetAddress address, int port) {
        super();
        writePacket = createPacket(WRQ(mode, filename), address, port);
    }


    @Override
    public DatagramPacket get() {
        return writePacket;
    }

    public void set(byte[] mode, byte[] filename) {
        writePacket = createPacket(WRQ(mode, filename));
    }

    public void set(byte[] mode, byte[] filename, InetAddress address, int port) {
        writePacket = createPacket(WRQ(mode, filename), address, port);
    }

    private byte[] WRQ(byte[] mode, byte[] filename) {
        return createRequest((byte) 2, mode, filename);
    }
}
