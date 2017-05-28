package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class RRQPacket extends ReadWritePacket {

    private DatagramPacket packet;

    public RRQPacket(byte[] mode, byte[] filename, InetAddress address, int port) {
        super();
        this.packet = createPacket(form(mode, filename), address, port);
    }

    @Override
    public DatagramPacket getDatagram() {
        return packet;
    }

    @Override
    protected byte[] form(byte[] mode, byte[] filename) {
        return prepare((byte) 1, mode, filename);
    }
}