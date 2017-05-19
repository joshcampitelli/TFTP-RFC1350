package com.tftp.simulation;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.LinkedList;

import java.io.IOException;
import com.tftp.core.SRSocket;
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
 * @since May the 1st, 2017.
 */
public class ErrorSimulator extends SRSocket {

    private static int RECEIVE_PORT = 23;
    private final LinkedList<PacketModification> modifications;

    /**
     * Constructs the ErrorSimulator by initializing the main receive socket (listening on port 23) and the
     * send/receive socket that will be used for establishing a connection with the server.
     *
     * @throws IOException
     */
    public ErrorSimulator() throws IOException {
        super("ErrorSimulator, Main Socket 'R'", RECEIVE_PORT);
        this.modifications = new LinkedList<>();
    }


    /**
     * Clones the given packet, changing only the port and address it is being dispatched to.
     *
     * @return The Updated DatagramPacket
     */
    public DatagramPacket produceFrom(DatagramPacket packet, int port, InetAddress address) {
        return new DatagramPacket(packet.getData(), packet.getLength(), address, port);
    }




    /**
     * Appends a PacketModification rule to the list being harbored by the simulator.
     * In the odd chance that you will be needing to call this method after simulation has begun, external
     * synchronization has encapsulated the critical section and it should be safe to do so.
     *
     * @param blocknumber the block number the packet must match to be considered for modification
     * @param errorId the error packet id to which it should belong to
     * @param type The type of packet it must be to be considered for modification
     * @param errorType The type of error packet to produce from the modification
     */
    public void queueModification(int blocknumber, PacketTypes type, byte errorId, byte errorType) {
        PacketModification modification = new PacketModification();
        modification.setPacketParameters(blocknumber, type);
        modification.setErrorId(errorId);
        modification.setErrorType(errorType);

        synchronized (modifications) {
            this.modifications.add(modification);
        }
    }


    public boolean isTargetPacket(DatagramPacket packet) {
        synchronized (modifications) {
            return modifications.peek().isMatchingPacket(packet);
        }
    }

    public PacketModification dequeue() {
        synchronized (modifications) {
            if (!modifications.isEmpty()) {
                return modifications.remove();
            }

            return null;
        }
    }


    /**
     * Runs the simulation cycle for the ErrorSimulator. Once a packet is received, it is processed and a
     * MutableSession is created for that specific client.
     *
     * @throws IOException
     */
    public void simulate() throws IOException {
        System.out.printf("ErrorSimulator has successfully launched its operations.\n\n");
        int sessions = 0;

        while (true) {
            DatagramPacket client = receive();
            inform(client, "Received Packet");

            MutableSession session = new MutableSession(this, client, client.getPort());
            new Thread(session, "Session" + sessions++).start();
        }
    }

    public static void main(String[] args) {
        try {
            ErrorSimulator simulator = new ErrorSimulator();
            simulator.presetModifications();
            simulator.simulate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A preset for modifications to be added to the Packet Modifications list.
     * IMPORTANT: add and remove as many as you want while testing.
     */
    public void presetModifications() {
        queueModification(3, PacketTypes.ACK, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_OPCODE);
        queueModification(7, PacketTypes.DATA, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_BLOCK_NUMBER);
        queueModification(15, PacketTypes.DATA, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_PACKET_SIZE);
        queueModification(23, PacketTypes.ACK, Packet.ERROR_UNKNOWN_TRANSFER_ID, Packet.NO_SPECIAL_ERROR);
        queueModification(29, PacketTypes.ACK, Packet.ERROR_ILLEGAL_TFTP_OPERATION, Packet.INVALID_BLOCK_NUMBER);
        queueModification(222, PacketTypes.DATA, Packet.ERROR_UNKNOWN_TRANSFER_ID, Packet.NO_SPECIAL_ERROR);
    }
}
