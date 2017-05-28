package com.tftp.simulation;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.IOException;
import com.tftp.core.SRSocket;
import com.tftp.core.protocol.Packet;
import com.tftp.core.protocol.Packet.PacketTypes;
import com.tftp.workers.SimulatorListener;

/**
 * ErrorSimulator aids in testing the rigidty and robustness of the transfer protocol implemented between
 * the client and server by intentionally tampering with packets to encode an illegal operation.
 *
 * ErrorSimulator spawns multiple MutableSessions, that each individually may manipulate the modifications queue.
 * Hence, many of the ErrorSimulator classes have mutual exclusion (i.e. external synchronization) to avoid
 * any faults.
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
    public static final byte SIMULATE_NO_SPECIAL_ERROR = 00;
    public static final byte SIMULATE_INVALID_OPCODE = 01;
    public static final byte SIMULATE_INVALID_PACKET_SIZE = 02;
    public static final byte SIMULATE_INVALID_BLOCK_NUMBER = 03;

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

    public String getInput(String text) {
      	 Scanner scanner = new Scanner(System.in);
      	 System.out.printf(text);
      	 return scanner.nextLine();
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
    public PacketModification queueModification(int blocknumber, PacketTypes type, byte errorId, byte errorType) {
        PacketModification modification = new PacketModification();
        modification.setPacketParameters(blocknumber, type);
        modification.setErrorId(errorId);
        modification.setErrorType(errorType);

        synchronized (modifications) {
            this.modifications.add(modification);
        }

        return modification;
    }


    /**
     * Checks if the provided packet matches the PacketModification template at the front of the queue.
     *
     * @param packet DatagramPacket received by the MutableSession
     *
     * @return if the packet matches the PacketModification template
     */
    public boolean isTargetPacket(DatagramPacket packet) {
        synchronized (modifications) {
            return !modifications.isEmpty() && modifications.peek().isMatchingPacket(packet);
        }
    }


    /**
     * Attempts to dequeue the front of the queue.
     *
     * @return The PacketModification at the front of the queue, if exists.
     */
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
            new SimulatorListener(simulator).start();

            simulator.simulate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
