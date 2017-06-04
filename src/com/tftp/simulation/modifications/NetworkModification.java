package com.tftp.simulation.modifications;

import com.tftp.simulation.Modification;


/**
 * NetworkModification is a subclass of Modification that allows for network errors (i.e. delay, loss, and duplicate)
 * in the transmission of packets.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Ahmed Sakr, Josh Campitelli, Brian Zhang, Ahmed Khattab, Dario Luzuriaga
 * @since May the 30th, 2017.
 */
public class NetworkModification extends Modification {


    @Override
    public String toString() {
        return "Network " + super.toString();
    }
}
