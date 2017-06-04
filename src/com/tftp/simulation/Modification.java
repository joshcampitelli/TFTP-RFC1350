package com.tftp.simulation;

import com.tftp.core.protocol.Packet.PacketTypes;
import java.net.DatagramPacket;
import com.tftp.core.protocol.BlockNumber;
import com.tftp.core.protocol.Packet;


/**
 * Modification is an abstract class that defines and provides the basic logic for any subclass that wishes
 * to manipulate and create a fault in the packet transmission.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Ahmed Sakr, Brian Zhang, Josh Campitelli, Ahmed Khattab, Dario Luzuriaga
 * @since May the 16th, 2017.
 */
public abstract class Modification {
    private int blockNumber = -1;
    private byte errorType = -1;
    private PacketTypes packetType = PacketTypes.UNKNOWN;

    /**
     * Sets the lookout attributes (i.e. block number and packet type) to the specific parameters.
     * Once set, this packet modification requirements will be met when the lookout attributes match.

     * IMPORTANT: boolean matchAny, if true, will result in a packet match if any of the lookout variables
     * are equal to the compared ones. If you wish to have the packet in question match all of the lookout
     * attributes, assert matchAny to false.
     */
    public void setPacketParameters(int blockNumber, PacketTypes type) {
        this.blockNumber = blockNumber;
        this.packetType = type;
    }


    /**
     * Sets the type of error the modified packet should produce. When providing the value, please only use
     * the static constants in Packet. (i.e. SIMULATOR_INVALID_OPCODE)
     */
    public void setErrorType(byte error) {
        this.errorType = error;
    }


    /**
     *
     * @return the error type of this instance
     */
    public int getErrorType() {
        return this.errorType;
    }


    public PacketTypes getPacketType() {
        return this.packetType;
    }


    /**
     * Checks if the DatagramPacket target matches the lookout variables.
     *
     * @return  true    if this is the packet in question
     *          false   otherwise
     */
    public boolean isMatchingPacket(DatagramPacket target) {
        if (packetType == PacketTypes.RRQ || packetType == PacketTypes.WRQ || packetType == PacketTypes.ERROR) {
            return Packet.getPacketType(target) == packetType;
        } else {
            int blocknum = BlockNumber.getBlockNumber(target.getData());
            return Packet.getPacketType(target) == packetType && blocknum == blockNumber;
        }
    }

    @Override
    public String toString() {
        return String.format("Modification for blocknumber #%d, packet type: %s", blockNumber, packetType);
    }
}
