package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;


/**
 * RRQPacket is one type of TFTP packet that is used by the client to initiate a READ transfer from the server.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Brian Zhang, Ahmed Sakr, Josh Campitelli, Ahmed Khattab, Dario Luzuriaga
 * @since May the 18th, 2017.
 */
public class RRQPacket extends ReadWritePacket {

    public RRQPacket(DatagramPacket packet) {
        super(packet);
    }

    public RRQPacket(byte[] mode, byte[] filename, InetAddress address, int port) {
        super(new DatagramPacket(new byte[516], 516, address, port));
        setData(form(mode, filename));
    }


    @Override
    public PacketTypes getType() {
        return PacketTypes.RRQ;
    }

    @Override
    public boolean isBlockNumbered() {
        return false;
    }

    @Override
    protected byte[] form(byte[] mode, byte[] filename) {
        return prepare((byte) 1, mode, filename);
    }
}