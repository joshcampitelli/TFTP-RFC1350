package com.tftp.simulation;

import java.lang.Runnable;
import java.lang.Math;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketTimeoutException;

import com.tftp.core.protocol.Packet;
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


    /**
     * Calibrates the newly birthed MutableSession by acquiring the TID of the destination.
     *
     * @param client The packet that has been received by the ErrorSimulator
     *
     * @return The packet received from the unknown party.
     *
     * @throws IOException
     */
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
    private void mutate(DatagramPacket packet, PacketModification modification, int destination, boolean sendOnly) throws IOException {
        System.out.printf("[IMPORTANT] %s: %s\n", getName(), modification);

        if (modification.getErrorId() == Packet.ERROR_UNKNOWN_TRANSFER_ID) {
            simulateInvalidTID(packet, destination);
        } else if (modification.getErrorId() == Packet.ERROR_ILLEGAL_TFTP_OPERATION) {
            simulateIllegalTftpOperation(packet, modification, destination, sendOnly);
        }
    }


    /**
     * Simulates an Invalid Transfer Id (ERROR type 5) case by creating a new socket and fulfilling the cycle through it.
     *
     *
     * @return the response from the mutation cycle, or null if send-only
     *
     * @throws IOException
     */
    private void simulateInvalidTID(DatagramPacket packet, int dest) throws IOException {
        SRSocket temp = new SRSocket(String.format("InvalidTID Simulation (dest id: %d)", dest));

        DatagramPacket destination = simulator.produceFrom(packet, dest, InetAddress.getLocalHost());
        temp.inform(destination, "Sending Packet");
        temp.send(destination);

        DatagramPacket response = temp.receive();
        temp.inform(response, "Received Packet");
        temp.close();
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
    private void simulateIllegalTftpOperation(DatagramPacket packet, PacketModification modification, int dest, boolean sendOnly) throws IOException {
        switch (modification.getErrorType()) {
            case Packet.INVALID_OPCODE:

                // corrupt the opcode, random number above the last legal opcode (i.e. >5)
                packet.getData()[1] = (byte) (Math.random() * 100 + 6);
                break;

            case Packet.INVALID_PACKET_SIZE:

                // change the array size to more than 512
                byte[] arr = enlarge(packet.getData(), (int) (Math.random() * 512 + 512));
                packet.setData(arr);
                break;

            case Packet.INVALID_BLOCK_NUMBER:

                // corrupt the block number, choose a random byte
                packet.getData()[2] = (byte) (Math.random() * 256);
                packet.getData()[3] = (byte) (Math.random() * 256);
                break;
        }

        packet = simulator.produceFrom(packet, dest, InetAddress.getLocalHost());
        inform(packet, "Sending Packet", true);
        send(packet);
    }


    /**
     * Increases the capacity of the array provided to a specified new length while maintaing its contents.
     *
     * @param array the array in question
     * @param newLength The new capacity of the array
     *
     * @return  the original array if the new length has been found to be less than the current array length,
     *          otherwise the newly constructed array.
     */
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
            int dest = this.dest;

            while (true) {
                DatagramPacket packet = receive(1000);
                inform(packet, "Received Packet");

                if (simulator.isTargetPacket(packet)) {
                    mutate(packet, simulator.dequeue(), dest, false);
                } else {
                    DatagramPacket recipient = simulator.produceFrom(packet, dest, InetAddress.getLocalHost());
                    inform(recipient, "Sending Packet");
                    send(recipient);
                }

                dest = (dest == this.dest) ? this.source : this.dest;
            }
        } catch (SocketTimeoutException e) {
            System.out.println("[IMPORTANT] No packet received in 1000 ms. Terminating!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
