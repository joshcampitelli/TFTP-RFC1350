package com.tftp.simulation;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
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

    private HashMap<Integer, Integer> router;
    private ArrayList<PacketModification> modifications;

    public ErrorSimulator() throws IOException {
        super("ErrorSimulator, Main Socket 'R'", RECEIVE_PORT);
        this.router = new HashMap<>();
        this.modifications = new ArrayList<>();
    }

    public DatagramPacket produceFrom(DatagramPacket packet, int port, InetAddress address) {
        return new DatagramPacket(packet.getData(), packet.getLength(), address, port);
    }

    /**
     * Mutate, if applicable, the packet to the specified Modification in the list of modifications.
     * External Synchronization on the arraylist as it is not thread-safe to loop and access the elements
     * unless the lock is obtained.
     *
     * @return the new mutated byte array.
     */
    public byte[] mutate(DatagramPacket packet) {
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

        // no PacketModification rule was found for this packet
        if (rule == null) {
            return packet.getData();
        }



        return packet.getData();
    }


    public void addModification(int blocknumber, PacketTypes type, boolean matchAny, int errorType) {
        PacketModification modification = new PacketModification();
        modification.setPacketParameters(blocknumber, type, matchAny);
        modification.setErrorType(errorType);

        this.modifications.add(modification);
    }

    public void simulate() throws IOException {
        System.out.printf("ErrorSimulator has successfully launched its operations.\n\n");

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
            new Thread(session, "Session" + router.size()).start();
        }
    }

    public static void main(String[] args) {
        try {
            ErrorSimulator simulator = new ErrorSimulator();
            simulator.addModification(10, PacketTypes.DATA, false, Packet.ERROR_ILLEGAL_TFTP_OPERATION);
            simulator.simulate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MutableSession extends SRSocket implements Runnable {

    ErrorSimulator simulator;
    int source, dest;

    MutableSession(ErrorSimulator simulator, DatagramPacket result, int source, int dest) throws IOException {
        super(String.format("Mutable Session (client tid: %d)", source));
        this.source = source;
        this.dest = dest;
        this.simulator = simulator;

        inform(result, "Sending Packet");
        send(result);
    }


    @Override
    public void run() {
        try {
            while (true) {
                DatagramPacket client = receive();
                inform(client, "Received Packet");

                client.setData(simulator.mutate(client));

                DatagramPacket server = simulator.produceFrom(client, dest, InetAddress.getLocalHost());
                inform(server, "Sending Packet");
                send(server);

                DatagramPacket response = receive();
                inform(response, "Received Packet");

                DatagramPacket result = simulator.produceFrom(response, source, client.getAddress());
                inform(result, "Sending Packet");
                send(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
