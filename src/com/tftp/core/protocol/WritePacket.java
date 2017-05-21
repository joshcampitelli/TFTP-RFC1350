package com.tftp.core.protocol;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class WritePacket extends ReadWritePacket {

    private DatagramPacket packet; 
    private DatagramPacket writePacket;

    public WritePacket() {
        super();
    }

    public WritePacket(DatagramPacket packet) {
        super(packet);
    }

    public WritePacket(DatagramPacket packet, byte[] mode, byte[] filename) {
        super(packet);
        writePacket = createPacket(WRQ(mode, filename)); 
    }

    public WritePacket(byte[] mode, byte[] filename, InetAddress address, int port) {
        super();
        writePacket = createPacket(WRQ(mode, filename), address, port);
    }

    public DatagramPacket get() {
        return writePacket;
    }

    public DatagramPacket get(byte[] mode, byte[] filename) {
        return createPacket(WRQ(mode, filename));
    }
    
    public DatagramPacket get(byte[] mode, byte[] filename, InetAddress address, int port) {
        return createPacket(WRQ(mode, filename), address, port);
    }

    private byte[] WRQ(byte[] mode, byte[] filename) {
        return createRequest((byte) 2, mode, filename);
    }
}
