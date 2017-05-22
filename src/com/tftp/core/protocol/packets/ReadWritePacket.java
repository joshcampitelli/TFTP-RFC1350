package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import com.tftp.core.protocol.Packet;

public abstract class ReadWritePacket extends Packet {

    private DatagramPacket packet;

    public ReadWritePacket() {
        super();
    }

    public ReadWritePacket(DatagramPacket packet) {
        super(packet);
    }

    protected byte[] createRequest(byte rw, byte[] mode, byte[] filename) {
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
