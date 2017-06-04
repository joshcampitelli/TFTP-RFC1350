package com.tftp.core.protocol.packets;

import com.tftp.core.protocol.Packet;

import java.net.DatagramPacket;


/**
 * ReadWritePacket is an abstract TFTP Packet class that engulfs two other packets: RRQPacket and WRQPacket.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Brian Zhang, Ahmed Sakr, Josh Campitelli, Ahmed Khattab, Dario Luzuriaga
 * @since May the 18th, 2017.
 */
public abstract class ReadWritePacket extends Packet {

    public ReadWritePacket(DatagramPacket packet) {
        super(packet);
    }

    protected abstract byte[] form(byte[] mode, byte[] filename);
    protected byte[] prepare(byte rw, byte[] mode, byte[] filename) {
        byte[] request = new byte[4 + mode.length + filename.length];
        int counter = 2; // filename starts at index 2

        request[0] = 0;
        request[1] = rw;

        System.arraycopy(filename, 0, request, counter, filename.length);

        counter += (filename.length + 1); // +1 -> there is a 0 between filename and mode

        System.arraycopy(mode, 0, request, counter, mode.length);

        return request;
    }
}
