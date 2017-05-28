package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import com.tftp.core.protocol.Packet;

public class ACKPacket extends Packet {

    private DatagramPacket packet;

    public ACKPacket(DatagramPacket packet, byte[] blockNumber) {
        super(packet);
        this.packet = createPacket(prepare(blockNumber));
    }

    @Override
    public DatagramPacket getDatagram() {
        return packet;
    }

    private byte[] prepare(byte[] blockNumber) {
        byte[] ack = new byte[ACK_SIZE];
        ack[1] = 4;
        System.arraycopy(blockNumber, 0, ack, 2, 2);
        return ack;
    }
}
