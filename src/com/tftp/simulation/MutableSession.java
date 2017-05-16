package com.tftp.simulation;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.lang.Runnable;
import com.tftp.core.Packet;
import com.tftp.core.SRSocket;

/**
 * MutableSession is similar to Connection for server sided requests. MutableSession allows the ErrorSimulator
 * to achieve concurrency while also being able to sniff the packets and modify them.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 14th, 2017.
 */
public class MutableSession extends SRSocket implements Runnable {

    private static int SERVER_PORT = 69;
    private ErrorSimulator simulator;
    private int source, dest;


    /**
     * Constructs the MutableSession by initializing the socket, source and destination, and dispatches the
     * original packet to begin the send/receive cycle.
     *
     * @throws IOException
     */
    public MutableSession(ErrorSimulator simulator, DatagramPacket request, int source) throws IOException {
        super(String.format("Mutable Session (client tid: %d)", source));
        this.source = source;
        this.simulator = simulator;

        DatagramPacket response = calibrate(request);
        inform(response, "Sending Packet");
        send(response);
    }

    private DatagramPacket calibrate(DatagramPacket client) throws IOException {
        DatagramPacket server = simulator.produceFrom(client, SERVER_PORT, InetAddress.getLocalHost());
        inform(server, "Sending Packet");
        send(server);

        DatagramPacket response = receive();
        inform(response, "Received Packet");
        this.dest = response.getPort();

        return simulator.produceFrom(response, client.getPort(), client.getAddress());
    }

    /**
     * Attempts to mutate the DatagramPacket depending on its error type.
     *
     * @return the response from the mutation cycle
     *         or the original packet if the error type does not match any of the tested ones.
     *
     * @throws IOException
     */
    private DatagramPacket mutate(DatagramPacket packet, PacketModification modification, int destination, boolean sendOnly) throws IOException {
        System.out.printf("[IMPORTANT] %s: %s\n", getName(), modification);
        if (modification.getErrorId() == Packet.ERROR_UNKNOWN_TRANSFER_ID) {
            return simulateInvalidTID(packet, destination, sendOnly);
        } else if (modification.getErrorId() == Packet.ERROR_ILLEGAL_TFTP_OPERATION) {
            return simulateIllegalTftpOperation(packet, modification, sendOnly);
        }

        return packet;
    }


    /**
     * Simulates an Invalid Transfer Id (ERROR type 5) case by creating a new socket and fulfilling the cycle through it.
     *
     * @param sendOnly true - only performs the send part of the cycle, otherwise performs the full send/receive
     *
     * @return the response from the mutation cycle, or null if send-only
     *
     * @throws IOException
     */
    private DatagramPacket simulateInvalidTID(DatagramPacket packet, int dest, boolean sendOnly) throws IOException {
        System.out.println("YAS");
        SRSocket temp = new SRSocket(String.format("InvalidTID Simulation (dest id: %d)", dest));

        DatagramPacket destination = simulator.produceFrom(packet, dest, InetAddress.getLocalHost());
        temp.inform(destination, "Sending Packet");
        temp.send(destination);

        DatagramPacket response = temp.receive();
        temp.inform(response, "Received Packet");
        temp.close();

        System.out.println("[IMPORTANT] Fulfilled Invalid TID simulation. Killing Thread.");

        try {
            Thread.currentThread().interrupt();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        return null;
    }


    /**
     * Simulates an Illegal TFTP Operation (ERROR type 4) case by changing the opcode to an unrecognized one.
     *
     * @param sendOnly true - only performs the send part of the cycle, otherwise performs the full send/receive
     *
     * @return the response from the mutation cycle, or null if send-only
     *
     * @throws IOException
     */
    private DatagramPacket simulateIllegalTftpOperation(DatagramPacket packet, PacketModification modification, boolean sendOnly) throws IOException {
        int errorType = modification.getErrorType();

        if (errorType == Packet.INVALID_OPCODE) {
            packet.getData()[1] = 13;
        } else if (errorType == Packet.INVALID_PACKET_SIZE) {
            byte[] arr = enlarge(packet.getData(), 1024);
            packet.setData(arr);
        }

        inform(packet, "Sending Packet");
        send(packet);

        if (!sendOnly) {
            DatagramPacket response = receive();
            return response;
        } else {
            return null;
        }
    }

    private byte[] enlarge(byte[] array, int newLength) {
        if (array.length > newLength) {
            return array;
        }

        byte[] newArr = new byte[newLength];
        System.arraycopy(array, 0, newArr, 0, array.length);
        return newArr;
    }

    @Override
    public void run() {
        try {
            while (true) {
                DatagramPacket client = receive();
                inform(client, "Received Packet");

                PacketModification modification = simulator.getModification(client);
                DatagramPacket response = null;

                if (modification != null) {
                    response = mutate(client, modification, dest, false);
                } else {
                    DatagramPacket server = simulator.produceFrom(client, dest, InetAddress.getLocalHost());
                    inform(server, "Sending Packet");
                    send(server);

                    response = receive();
                    inform(response, "Received Packet");
                }

                modification = simulator.getModification(response);
                if (modification != null) {
                    mutate(response, modification, source, true);
                } else {
                    DatagramPacket result = simulator.produceFrom(response, source, client.getAddress());
                    inform(result, "Sending Packet");
                    send(result);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
