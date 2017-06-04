package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import com.tftp.core.protocol.Packet;


/**
 * DATAPacket is one type of TFTP packet that is used for transmitting data blocks with a maximum size of 512 bytes
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Brian Zhang, Ahmed Sakr, Josh Campitelli, Ahmed Khattab, Dario Luzuriaga
 * @since May the 18th, 2017.
 */
public class DATAPacket extends Packet {

    public DATAPacket(DatagramPacket packet) {
        super(packet);
    }

    public DATAPacket(DatagramPacket packet, byte[] blockNumber, byte[] data) {
        super(packet);
        setData(prepare(blockNumber, data));
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

    @Override
    public PacketTypes getType() {
        return PacketTypes.DATA;
    }

    @Override
    public boolean isBlockNumbered() {
        return true;
    }
}
