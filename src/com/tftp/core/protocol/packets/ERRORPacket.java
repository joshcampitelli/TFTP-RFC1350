package com.tftp.core.protocol.packets;

import java.net.DatagramPacket;
import com.tftp.core.protocol.Packet;


/**
 * ERRORPacket is one type of TFTP packet that is used for informing the other side of a detected error.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Brian Zhang, Ahmed Sakr, Josh Campitelli, Ahmed Khattab, Dario Luzuriaga
 * @since May the 18th, 2017.
 */
public class ERRORPacket extends Packet {

    public ERRORPacket(DatagramPacket packet) {
        super(packet);
    }

    public ERRORPacket(DatagramPacket packet, byte errorCode, byte[] errorMsg) {
        super(packet);
        setData(prepare(errorCode, errorMsg));
    }


    private byte[] prepare(byte errorCode, byte[] errorMsg) {
        byte[] error = new byte[5 + errorMsg.length];

        error[1] = 5;
        error[3] = errorCode;
        System.arraycopy(errorMsg, 0, error, 4, errorMsg.length);

        return error;
    }


    @Override
    public PacketTypes getType() {
        return PacketTypes.ERROR;
    }

    @Override
    public boolean isBlockNumbered() {
        return false;
    }

}
