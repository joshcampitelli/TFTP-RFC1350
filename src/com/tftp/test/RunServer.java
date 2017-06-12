package com.tftp.test;


import com.tftp.Server;
import com.tftp.TFTPConfig;
import com.tftp.exceptions.InvalidPacketException;
import com.tftp.io.FileTransfer;

import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;

/**
 * RunServer is a runnable class for Server.java
 *
 * @author Ahmed Sakr
 * @since June the 12th, 2017
 */
public class RunServer {


    /**
     * Prints instructions on the console and retrieves input from the user.
     *
     * @return the new-line terminated input
     */
    public static String getInput(String text) {
        Scanner scanner = new Scanner(System.in);
        System.out.printf(text);

        return scanner.nextLine();
    }


    public static void main(String[] args) {
        Server server = null;

        try {
            server = new Server();
            FileTransfer.setup(TFTPConfig.SERVER_DIRECTORY);

            String verbosity = getInput("The Server is set to quiet. Would you like to set it to verbose? (y/N) ");
            if (verbosity.toLowerCase().equals("y")) {
                TFTPConfig.SERVER_VERBOSE = true;
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
