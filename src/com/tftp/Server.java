package com.tftp;

import java.net.DatagramPacket;
import java.io.IOException;
import java.lang.Thread;
import java.net.InetAddress;

import com.tftp.core.Socket;
import com.tftp.core.Connection;
import com.tftp.io.TransferController;
import com.tftp.workers.QuitListener;
import com.tftp.exceptions.InvalidPacketException;

/**
 * Server is the main worker and host of all the TFTP operations. Server is equipped with multi-threading to helpful
 * service multiple clients concurrently.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Ahmed Sakr, Josh Campitelli, Brian Zhang, Ahmed Khattab, Dario Luzuriaga
 * @since May the 1st, 2017.
 */
public class Server extends Socket {

    private int threadNumber;
    private TransferController controller;

    public Server() throws IOException {
        super("Server, Socket 'R'", TFTPConfig.SERVER_PORT, InetAddress.getLocalHost());
        System.out.printf("Listening on: %s\n", InetAddress.getLocalHost());
        controller = new TransferController();
    }


    /**
     *
     * @return the server's TransferController instance.
     */
    public TransferController getTransferController() {
        return controller;
    }


    /**
     * Attempts to establish a connection for the received packet. If the packet has been determined to be invalid,
     * an InvalidPacketException is thrown.
     *
     * @throws InvalidPacketException If the packet has been found to be illegal. Critical error.
     */
    private void establish(DatagramPacket packet) throws IOException {
        Connection connection = new Connection(this, packet);
        Thread thread = new Thread(connection, "Connection" + threadNumber++);
        thread.start();

        System.out.printf("[IMPORTANT]: Request received. Established new connection!\n");
    }

    /**
     * Launches the operations of the server: starts listening for packets and establishes connections.
     *
     * @throws IOException
     * @throws InvalidPacketException
     */
    public void launch() throws IOException, InvalidPacketException {
        System.out.printf("Server has successfully launched.\n");

        new QuitListener(this).start();
        System.out.printf("If you would like to shutdown the server, type \"quit\".\n\n");

        while (!isClosed()) {
            DatagramPacket packet = receive();
            inform(packet, "Received Packet");
            establish(packet);
        }
    }


    /**
     * Closes the receive socket of the server as a measure of not accepting anymore connections.
     */
    public void shutdown() {
        close();
    }
}
