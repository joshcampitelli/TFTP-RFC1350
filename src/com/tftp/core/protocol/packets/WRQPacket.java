package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;


/**
 * WRQPacket is one type of TFTP packet that is used by the client to initiate a WRITE transfer to the server.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Brian Zhang, Ahmed Sakr, Josh Campitelli, Ahmed Khattab, Dario Luzuriaga
 * @since May the 18th, 2017.
 */
public class WRQPacket extends ReadWritePacket {


    public WRQPacket(DatagramPacket packet) {
        super(packet);
    }

    public WRQPacket(byte[] mode, byte[] filename, InetSocketAddress address) {
        super(new DatagramPacket(new byte[516], 516, address));
        setData(form(mode, filename));
    }


    @Override
    public PacketTypes getType() {
        return PacketTypes.WRQ;
    }

    @Override
    public boolean isBlockNumbered() {
        return false;
    }

    @Override
    protected byte[] form(byte[] mode, byte[] filename) {
        return prepare((byte) 2, mode, filename);
    }

}
