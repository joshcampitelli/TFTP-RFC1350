package com.tftp;

import java.net.DatagramPacket;
import java.io.IOException;
import java.lang.Thread;
import java.util.ArrayList;

import com.tftp.core.SRSocket;
import com.tftp.core.Connection;
import com.tftp.exceptions.InvalidPacketException;
import java.net.SocketException;

/**
 *
 * @author Ahmed Sakr
 * @since May the 1st, 2017
 */

public class Server extends SRSocket {

    private QuitListener quitListener;
    private int threadNumber;
    public static int RECEIVE_PORT = 69;

    public Server() throws IOException {
        super("Server, Socket 'R'", RECEIVE_PORT);
    }


    /**
     * Attempts to establish a connection for the received packet. If the packet has been determined to be invalid,
     * an InvalidPacketException is thrown.
     *
     * @throws InvalidPacketException If the packet has been found to be illegal. Critical error.
     */
    public void establish(DatagramPacket packet) throws IOException {
        Connection connection = new Connection(packet);
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

    public static void main(String[] args) {
        Server server = null;
        try {
            server = new Server();
            server.launch();
        } catch (SocketException e) {
            System.out.printf("Shutdown successful: no more incoming connections to be serviced...\n");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidPacketException e) {
            server.shutdown();
            System.out.printf("Invalid packet encountered. Server is attempting to shutdown...\n");
        }
    }
}
