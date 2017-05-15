package com.tftp.simulation;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

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
    private static int SERVER_PORT = 69;

    private ArrayList<PacketModification> modifications;

    public ErrorSimulator() throws IOException {
        super("ErrorSimulator, Main Socket 'R'", RECEIVE_PORT);
        this.modifications = new ArrayList<>();
    }

    public DatagramPacket produceFrom(DatagramPacket packet, int port, InetAddress address) {
        return new DatagramPacket(packet.getData(), packet.getLength(), address, port);
    }

    /**
     * Acquires the modification for the packet provided.
     *
     * @return the PacketModification object corresponding to the packet.
     */
    public PacketModification getModification(DatagramPacket packet) {
        PacketModification rule = null;
        synchronized (modifications) {
            for (PacketModification modification : modifications) {

                // a PacketModification rule exists for this specific packet
                if (modification.isMatchingPacket(packet)) {

                    // exiting synchronized block as critical section is over
                    rule = modification;
                    break;
                }
            }
        }

        return rule;
    }


    public void addModification(int blocknumber, PacketTypes type, boolean matchAny, int errorType) {
        PacketModification modification = new PacketModification();
        modification.setPacketParameters(blocknumber, type, matchAny);
        modification.setErrorType(errorType);

        this.modifications.add(modification);
    }

    public void simulate() throws IOException {
        System.out.printf("ErrorSimulator has successfully launched its operations.\n\n");
        int sessions = 0;

        while (true) {
            DatagramPacket client = receive();
            inform(client, "Received Packet");

            // request received => produce a temporary send/receive socket to fulfill the networking requirements
            SRSocket temp = new SRSocket("ErrorSimulator, Temp Socket 'S/R'");

            DatagramPacket server = this.produceFrom(client, SERVER_PORT, InetAddress.getLocalHost());
            temp.inform(server, "Sending Packet");
            temp.send(server);

            DatagramPacket response = temp.receive();
            temp.inform(response, "Received Packet");

            DatagramPacket result = this.produceFrom(response, client.getPort(), client.getAddress());

            MutableSession session = new MutableSession(this, result, client.getPort(), response.getPort());
            new Thread(session, "Session" + sessions++).start();
        }
    }

    public static void main(String[] args) {
        try {
            ErrorSimulator simulator = new ErrorSimulator();
            simulator.addModification(13, PacketTypes.DATA, false, Packet.ERROR_UNKNOWN_TRANSFER_ID);
            simulator.simulate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
