package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import com.tftp.core.protocol.Packet;

public class ERRORPacket extends Packet {

    private DatagramPacket errorPacket;

    public ERRORPacket(DatagramPacket packet) {
        super(packet);
    }

    public ERRORPacket(DatagramPacket packet, byte errorCode, byte[] errorMsg) {
        super(packet);
        errorPacket = createPacket(prepare(errorCode, errorMsg));
    }

    @Override
    public DatagramPacket getDatagram() {
        return errorPacket;
    }

    public void setDatagram(byte errorCode, byte[] errorMsg) {
        errorPacket = createPacket(prepare(errorCode, errorMsg));
    }

    private byte[] prepare(byte errorCode, byte[] errorMsg) {
        byte[] error = new byte[5 + errorMsg.length];

        error[1] = 5;
        error[3] = errorCode;
        System.arraycopy(errorMsg, 0, error, 4, errorMsg.length);

        return error;
    }
}
