package com.tftp.simulation;

import java.lang.Runnable;
import java.lang.Math;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.net.SocketTimeoutException;

import com.tftp.core.SRSocket;
import com.tftp.core.protocol.Packet;
import com.tftp.core.protocol.Packet.PacketTypes;
import com.tftp.core.protocol.TFTPError;
import com.tftp.simulation.modifications.NetworkModification;
import com.tftp.simulation.modifications.PacketModification;

/**
 * MutableSession is similar to Connection for server sided requests. MutableSession allows the ErrorSimulator
 * to achieve concurrency while also being able to sniff the packets and modify them.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Ahmed Sakr, Brian Zhang, Josh Campitelli, Ahmed Khattab, Dario Luzuriaga
 * @since May the 14th, 2017.
 */
public class MutableSession extends SRSocket implements Runnable {

    private ErrorSimulator simulator;
    private InetAddress client, server;
    private int clientPort, serverPort;
    private boolean active = true;


    /**
     * Constructs the MutableSession by initializing the socket, client and destination, and dispatches the
     * original packet to begin the send/receive cycle.
     *
     * @throws IOException
     */
    public MutableSession(ErrorSimulator simulator, DatagramPacket request, int clientPort) throws IOException, InterruptedException {
        super(String.format("Mutable Session (client tid: %d)", clientPort));
        int SERVER_PORT = 69;

        this.client = request.getAddress();
        this.clientPort = clientPort;
        this.server = InetAddress.getLocalHost();
        this.serverPort = SERVER_PORT;
        this.destination = this.server;
        this.destinationPort = this.serverPort;

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
    private DatagramPacket calibrate(DatagramPacket client) throws IOException, InterruptedException {
        process(client);

        DatagramPacket server = receive();
        inform(server, "Received Packet");
        this.serverPort = server.getPort();

        return simulator.produceFrom(server, client.getPort(), client.getAddress());
    }

    private void intercept(DatagramPacket packet, Modification modification, InetAddress address, int destination) throws IOException, InterruptedException {
        if (modification instanceof PacketModification) {
            mutate(packet, (PacketModification) modification, address, destination);
        } else {
            network(packet, (NetworkModification) modification, destination);
        }
    }

    private void network(DatagramPacket packet, NetworkModification modification, int destination) throws IOException, InterruptedException {
        switch (modification.getErrorType()) {
            case ErrorSimulator.SIMULATE_DELAYED_PACKET:
                delay(packet, modification.getPacketType(), destination);
                break;
            case ErrorSimulator.SIMULATE_DUPLICATED_PACKET:

                // send the packet twice
                duplicate(packet, modification.getPacketType(), destination);
                break;
            case ErrorSimulator.SIMULATE_LOST_PACKET:
                if (modification.getPacketType() == PacketTypes.DATA) {

                    // we need to remain listening on this source as it sent the DATA packet, meaning
                    // it will resend the DATA packet. This statement effectively keeps this.destination
                    // to the same value after it returns to process()
                    this.destination = (this.destination == this.server) ? this.client : this.server;
                    this.destinationPort = (this.destinationPort == this.serverPort) ? this.clientPort : this.serverPort;
                }
                break;
        }
    }

    private void delay(DatagramPacket packet, PacketTypes type, int destination) throws InterruptedException, IOException {
        Thread.sleep(6000);
        DatagramPacket dispatch = simulator.produceFrom(packet, destination, InetAddress.getLocalHost());
        inform(dispatch, "Sending Packet");

        if (isDominantPacket(type)) {
            DatagramPacket retransmitted = receive();
            retransmitted = simulator.produceFrom(retransmitted, destination, InetAddress.getLocalHost());
            inform(retransmitted, "Sending retransmitted Packet");

            send(dispatch);
            send(retransmitted);

            this.destination = (this.destination == this.server) ? this.client : this.server;
            this.destinationPort = (this.destinationPort == this.serverPort) ? this.clientPort : this.serverPort;

            DatagramPacket response = receive();
            response = simulator.produceFrom(response, this.destinationPort, this.destination);
            inform(response, "Sending retransmitted Packet");

            DatagramPacket response2 = receive();
            response2 = simulator.produceFrom(response2, this.destinationPort, InetAddress.getLocalHost());
            inform(response2, "Sending retransmitted Packet");

            send(response);
            send(response2);
        } else {
            send(dispatch);
        }
    }

    private void duplicate(DatagramPacket packet, PacketTypes type, int destination) throws IOException {
        DatagramPacket dispatch = simulator.produceFrom(packet, destination, InetAddress.getLocalHost());
        inform(dispatch, "Sending Packet");
        inform(dispatch, "Sending Duplicate Packet");

        send(dispatch);
        send(dispatch);

        if (type == PacketTypes.DATA) {
            this.destination = (this.destination == this.server) ? this.client : this.server;
            this.destinationPort = (this.destinationPort == this.serverPort) ? this.clientPort : this.serverPort;

            DatagramPacket response = receive();
            response = simulator.produceFrom(response, this.destinationPort, this.destination);
            inform(response, "Sending retransmitted Packet");

            DatagramPacket response2 = receive();
            response2 = simulator.produceFrom(response2, this.destinationPort, this.destination);
            inform(response2, "Sending retransmitted Packet");

            send(response);
            send(response2);
        }
    }

    private boolean isDominantPacket(PacketTypes type) {
        return type == PacketTypes.RRQ || type == PacketTypes.WRQ || type == PacketTypes.DATA;
    }


    /**
     * Attempts to mutate the DatagramPacket depending on its error type.
     *
     * @return the response from the mutation cycle
     *         or the original packet if the error type does not match any of the tested ones.
     *
     * @throws IOException
     */
    private void mutate(DatagramPacket packet, PacketModification modification, InetAddress address, int port) throws IOException {
        if (modification.getErrorId() == TFTPError.UNKNOWN_TRANSFER_ID) {
            simulateInvalidTID(packet, address, port);
            active = false;
        } else if (modification.getErrorId() == TFTPError.ILLEGAL_TFTP_OPERATION) {
            simulateIllegalTftpOperation(packet, modification, address, port);
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
    private void simulateInvalidTID(DatagramPacket packet, InetAddress inet, int port) throws IOException {
        SRSocket temp = new SRSocket(String.format("InvalidTID Simulation (server id: %d)", port));

        DatagramPacket destination = simulator.produceFrom(packet, port, InetAddress.getLocalHost());
        temp.inform(destination, "Sending Packet");
        temp.send(destination);

        DatagramPacket response = temp.receive();
        temp.inform(response, "Received Packet");
        temp.close();

        port = port == this.clientPort ? this.serverPort : this.clientPort;
        inet = inet == this.client ? this.server : this.client;
        DatagramPacket result = simulator.produceFrom(response, port, inet);
        inform(result, "Sending Response");
        send(result);
    }


    /**
     * Simulates an Illegal TFTP Operation (ERROR type 4) case by changing the opcode to an unrecognized one.
     *
     * @return the response from the mutation cycle, or null if send-only
     *
     * @throws IOException
     */
    private void simulateIllegalTftpOperation(DatagramPacket packet, PacketModification modification, InetAddress address, int dest) throws IOException {
        switch (modification.getErrorType()) {
            case ErrorSimulator.SIMULATE_INVALID_OPCODE:

                // corrupt the opcode, random number above the last legal opcode (i.e. >5)
                packet.getData()[1] = (byte) (Math.random() * 100 + 6);
                break;

            case ErrorSimulator.SIMULATE_INVALID_PACKET_SIZE:

                // change the array size to more than 512
                byte[] arr = enlarge(packet.getData(), (int) (Math.random() * 512 + 512));
                packet.setData(arr);
                break;

            case ErrorSimulator.SIMULATE_INVALID_BLOCK_NUMBER:

                // corrupt the block number, choose a random byte
                packet.getData()[3]++;
                break;
            case ErrorSimulator.SIMULATE_INVALID_MODE:

                // corrupt the mode by just changing the last mode byte to a random one
                // effectively making it not "octet"
                packet.getData()[packet.getLength() - 2] = (byte) (Math.random() * 256);
                break;
        }

        packet = simulator.produceFrom(packet, dest, address);
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

    private void process(DatagramPacket packet) throws IOException, InterruptedException{
        if (simulator.isTargetPacket(packet)) {
            intercept(packet, simulator.dequeue(), destination, destinationPort);
        } else {
            DatagramPacket recipient = simulator.produceFrom(packet, destinationPort, destination);
            inform(recipient, "Sending Packet");
            send(recipient);
        }

        destination = (destination == this.server) ? this.client : this.server;
        destinationPort = (destinationPort == this.serverPort) ? this.clientPort : this.serverPort;
    }

    private InetAddress destination;
    private int destinationPort;

    @Override
    public void run() {
        try {
            destination = this.server;
            destinationPort = this.serverPort;
            while (active) {
                DatagramPacket packet = receive();
                inform(packet, "Received Packet");
                process(packet);
            }
        } catch (SocketTimeoutException ex) {
            // socket timed out, thread will die. No need to inform.
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
