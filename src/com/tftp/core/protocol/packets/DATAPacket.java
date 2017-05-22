package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import com.tftp.core.protocol.Packet;

public class DATAPacket extends Packet {

    private DatagramPacket packet;
    private DatagramPacket dataPacket;

    public DATAPacket() {
        super();
    }

    public DATAPacket(DatagramPacket packet) {
        super(packet);
    }

    public DATAPacket(DatagramPacket packet, byte[] blockNumber, byte[] data) {
        super(packet);
        dataPacket = createPacket(DATA(blockNumber, data));
    }

    public DATAPacket(byte[] blockNumber, byte[] data, InetAddress address, int port) {
        super();
        dataPacket = createPacket(DATA(blockNumber, data), address, port);
    }

    @Override
    public DatagramPacket get() {
        return dataPacket;
    }

    public void set(byte[] blockNumber, byte[] data) {
        dataPacket = createPacket(DATA(blockNumber, data));
    }

    public void set(byte[] blockNumber, byte[] data, InetAddress address, int port) {
        dataPacket = createPacket(DATA(blockNumber, data), address, port);
    }

    private byte[] DATA(byte[] blockNumber, byte[] data) {
        byte[] request = new byte[2 + blockNumber.length + data.length];
        int counter = 2;

        request[1] = 3;
        System.arraycopy(blockNumber, 0, request, counter, blockNumber.length);

        counter += blockNumber.length;

        System.arraycopy(data, 0, request, counter, data.length);
        return request;
    }
}