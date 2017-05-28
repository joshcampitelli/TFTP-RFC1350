package com.tftp;

import java.net.DatagramPacket;
import java.io.IOException;
import java.lang.Thread;
import java.util.Scanner;
import java.util.ArrayList;

import com.tftp.core.SRSocket;
import com.tftp.core.Connection;
import com.tftp.io.FileTransfer;
import com.tftp.io.TransferController;
import com.tftp.workers.QuitListener;
import com.tftp.exceptions.InvalidPacketException;
import java.net.SocketException;

/**
 * Server is the main worker and host of all the TFTP operations. Server is equipped with multi-threading to helpful
 * service multiple clients concurrently.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 1st, 2017.
 */
public class Server extends SRSocket {

    private int threadNumber;
    private TransferController controller;
    public static int RECEIVE_PORT = 69;
	public static boolean verbose;

    public Server() throws IOException {
        super("Server, Socket 'R'", RECEIVE_PORT);
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
     * Prints instructions on the console and retrieves input from the user.
     *
     * @return the new-line terminated input
     */
    public String getInput(String text) {
    	 Scanner scanner = new Scanner(System.in);
    	 System.out.printf(text);

    	 return scanner.nextLine();
    }

    /**
     * Attempts to establish a connection for the received packet. If the packet has been determined to be invalid,
     * an InvalidPacketException is thrown.
     *
     * @throws InvalidPacketException If the packet has been found to be illegal. Critical error.
     */
    public void establish(DatagramPacket packet) throws IOException {
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

    public static void main(String[] args) {
        Server server = null;

        try {

            server = new Server();
            FileTransfer.setup(FileTransfer.SERVER_DIRECTORY);

            String verbosity = server.getInput("The Server is set to quiet. Would you like to set it to verbose? (y/N) ");
            if (verbosity.toLowerCase().equals("y")) {
                verbose = true;
            }

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
