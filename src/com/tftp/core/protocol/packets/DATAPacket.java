package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import com.tftp.core.protocol.Packet;

public class DATAPacket extends Packet {

    private DatagramPacket packet;

    public DATAPacket(DatagramPacket packet, byte[] blockNumber, byte[] data) {
        super(packet);
        this.packet = createPacket(prepare(blockNumber, data));
    }

    @Override
    public DatagramPacket getDatagram() {
        return packet;
    }

    private byte[] prepare(byte[] blockNumber, byte[] data) {
        byte[] request = new byte[2 + blockNumber.length + data.length];
        int counter = 2;

        request[1] = 3;
        System.arraycopy(blockNumber, 0, request, counter, blockNumber.length);

        counter += blockNumber.length;

        System.arraycopy(data, 0, request, counter, data.length);
        return request;
    }
}
