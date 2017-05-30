package com.tftp.simulation.modifications;

import com.tftp.simulation.Modification;

/**
 * PacketModification provides the template for selecting certain packets and deciding how to corrupt them
 * to produce certain errors.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 14th, 2017.
 */
public class PacketModification extends Modification {

    private byte errorId;

    /**
     * Sets the Error packet Id of the error to be produced. (i.e. ERROR_INVALID_TRANSFER_ID)
     */
    public void setErrorId(byte id) {
        this.errorId = id;
    }

    /**
     *
     * @return the error id of this instance
     */
    public int getErrorId() {
        return this.errorId;
    }


    @Override
    public String toString() {
        return "Packet " + super.toString();
    }
}
