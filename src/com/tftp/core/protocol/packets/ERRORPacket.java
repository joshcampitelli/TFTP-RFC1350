package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import com.tftp.core.protocol.Packet;

public class ERRORPacket extends Packet {

    private DatagramPacket packet;
    private DatagramPacket errorPacket;

    public ERRORPacket() {
        super();
    }

    public ERRORPacket(DatagramPacket packet) {
        super(packet);
    }

    public ERRORPacket(DatagramPacket packet, byte errorCode, byte[] errorMsg) {
        super(packet);
        errorPacket = createPacket(ERROR(errorCode, errorMsg));
    }

    public ERRORPacket(byte errorCode, byte[] errorMsg, InetAddress address, int port) {
        super();
        errorPacket = createPacket(ERROR(errorCode, errorMsg), address, port);
    }

    @Override
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
