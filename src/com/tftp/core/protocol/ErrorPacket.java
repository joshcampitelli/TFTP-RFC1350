package com.tftp.core.protocol;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class ErrorPacket extends Packet {

    private DatagramPacket packet;
    private DatagramPacket errorPacket;

    public ErrorPacket() {
        super();
    }

    public ErrorPacket(DatagramPacket packet) {
        super(packet);
    }
    
    public ErrorPacket(DatagramPacket packet, byte errorCode, byte[] errorMsg) {
        super(packet);
        errorPacket = createPacket(ERROR(errorCode, errorMsg));
    }

    public ErrorPacket(byte errorCode, byte[] errorMsg, InetAddress address, int port) {
        super();
        errorPacket = createPacket(ERROR(errorCode, errorMsg), address, port);
    }

    public DatagramPacket get() {
        return errorPacket;
    }

    public void set(byte errorCode, byte[] errorMsg) {
        errorPacket = createPacket(ERROR(errorCode, errorMsg));
    }

    public void set(byte errorCode, byte[] errorMsg, InetAddress address, int port) {
        errorPacket = createPacket(ERROR(errorCode, errorMsg));
    }

    private byte[] ERROR(byte errorCode, byte[] errorMsg) {
        byte[] error = new byte[5 + errorMsg.length];

        error[1] = 5;
        error[3] = errorCode;
        System.arraycopy(errorMsg, 0, error, 4, errorMsg.length);

        return error;
    }
}
