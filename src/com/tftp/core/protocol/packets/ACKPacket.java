package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import com.tftp.core.protocol.Packet;


/**
 * ACKPacket is one type of TFTP packet that is used in acknowledging requests, mainly DATA packets.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Brian Zhang, Ahmed Sakr, Josh Campitelli, Ahmed Khattab, Dario Luzuriaga
 * @since May the 18th, 2017.
 */
public class ACKPacket extends Packet {

    public ACKPacket(DatagramPacket packet) {
        super(packet);
    }

    public ACKPacket(DatagramPacket packet, byte[] blockNumber) {
        super(packet);
        setData(prepare(blockNumber));
    }

    private byte[] prepare(byte[] blockNumber) {
        byte[] ack = new byte[ACK_SIZE];
        ack[1] = 4;
        System.arraycopy(blockNumber, 0, ack, 2, 2);
        return ack;
    }

    @Override
    public PacketTypes getType() {
        return PacketTypes.ACK;
    }

    @Override
    public boolean isBlockNumbered() {
        return true;
    }
}
