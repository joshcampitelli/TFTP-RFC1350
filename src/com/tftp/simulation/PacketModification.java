package com.tftp.simulation;

import java.util.ArrayList;
import java.net.DatagramPacket;
import com.tftp.core.Packet;
import com.tftp.core.Packet.PacketTypes;

/**
 * ErrorSimulator aids in testing the rigidty and robustness of the transfer protocol implemented between
 * the client and server by intentionally tampering with packets to encode an illegal operation.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 14th, 2017.
 */
public class PacketModification {

    private int blockNumber = -1;
    private PacketTypes packetType = PacketTypes.UNKNOWN;
    private int errorType = Packet.ERROR_ILLEGAL_TFTP_OPERATION;

    private boolean matchAny = false;

    /*public byte[] apply(DatagramPacket packet) {
        if (errorType == Packet.ERROR_UNKNOWN_TRANSFER_ID) {

        }
    }*/

    /**
     * Sets the lookout attributes (i.e. block number and packet type) to the specific parameters.
     * Once set, this packet modification requirements will be met when the lookout attributes match.

     * IMPORTANT: boolean matchAny, if true, will result in a packet match if any of the lookout variables
     * are equal to the compared ones. If you wish to have the packet in question match all of the lookout
     * attributes, assert matchAny to false.
     */
    public void setPacketParameters(int blockNumber, PacketTypes type, boolean matchAny) {
        this.blockNumber = blockNumber;
        this.packetType = type;
        this.matchAny = matchAny;
    }


    /**
     * Sets the type of error the modified packet should produce. When providing the value, please only use
     * the static constants in Packet. (i.e. ERROR_UNKNOWN_TRANSFER_ID and ERROR_ILLEGAL_TFTP_OPERATION)
     */
    public void setErrorType(int error) {
        this.errorType = error;
    }


    public int getErrorType() {
        return this.errorType;
    }

    /**
     * Checks if the DatagramPacket target matches the lookout variables.
     *
     * @return  true    if this is the packet in question
     *          false   otherwise
     */
    public boolean isMatchingPacket(DatagramPacket target) {
        Packet packet = new Packet();
        int blocknum = getBlockUnsigned(target);

        if (matchAny) {
            return packet.checkPacketType(target) == packetType || blocknum == blockNumber;
        } else {
            return packet.checkPacketType(target) == packetType && blocknum == blockNumber;
        }
    }

    /**
     * Converts the two byte block number into an unsigned integer.
     *
     * @return Unsigned block number
     */
    private int getBlockUnsigned(DatagramPacket target) {
        int blocknum = 0;
        blocknum += Byte.toUnsignedInt(target.getData()[3]);
        blocknum += Byte.toUnsignedInt(target.getData()[2]) << 8;

        return blocknum;
    }
}
