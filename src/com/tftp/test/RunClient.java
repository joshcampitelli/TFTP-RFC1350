package com.tftp.test;


import com.tftp.Client;
import com.tftp.TFTPConfig;
import com.tftp.core.protocol.IPAddress;
import com.tftp.exceptions.UnknownIOModeException;
import com.tftp.io.FileTransfer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * RunClient is a runnable class for Client.java
 *
 * @author Ahmed Sakr
 * @since June the 12th, 2017
 */
public class RunClient {


    /**
     * Scans the console for user input.
     *
     * @param text the text to display when prompting the user for input
     * @return the user input as a string
     */
    private static String getInput(String text) {
        Scanner scanner = new Scanner(System.in);
        System.out.printf(text);

        return scanner.nextLine();
    }


    public static void main(String[] args) {
        try {
            Client client = new Client();
            FileTransfer.setup(TFTPConfig.CLIENT_DIRECTORY);
            System.out.println("Input 'q' during any instruction to quit.");

            while (true) {
                String dataMode = getInput("The Client is set to normal. Would you like to set it to test? (y/N) ");
                if (dataMode.toLowerCase().equals("y")) {
                    client.setNormal(false);
                } else if (dataMode.toLowerCase().equals("q")) {
                    break;
                }

                String verbosity = getInput("The Client is set to quiet. Would you like to set it to verbose? (y/N) ");
                if (verbosity.toLowerCase().equals("y")) {
                    TFTPConfig.CLIENT_VERBOSE = true;
                } else if (verbosity.toLowerCase().equals("q")) {
                    break;
                }

                System.out.printf("Your IP address is: %s\n", InetAddress.getLocalHost().getHostAddress());
                System.out.println("If you would like to have your destination IP as yours, type \"localhost\".\n");
                String ip = getInput("Please enter the IP of the destination: ").toLowerCase();
                while (!ip.equals("localhost") && !IPAddress.isValidIP(ip))  {
                    ip = getInput("Please enter the IP of the destination: ").toLowerCase();
                }

                if (ip.equals("localhost")) {
                    ip = InetAddress.getLocalHost().getHostAddress();
                }

                String newTransfer = "y";
                String requestType = "";
                while (newTransfer.equalsIgnoreCase("y")) {
                    requestType = "";
                    while (!(requestType.toLowerCase().equals("r") || requestType.toLowerCase().equals("w") || requestType.toLowerCase().equals("q"))) {
                        requestType = getInput("Would you like to Write or Read? (W/R) ");
                    }
                    if (requestType.toLowerCase().equals("q")) {
                        break;
                    }

                    byte[] filename = getInput("Enter file name: ").getBytes();
                    byte[] mode = "octet".getBytes();
                    client.transfer(filename, mode, ip, requestType);
                    client.close();

                    client = new Client();
                    client.setNormal(!dataMode.toLowerCase().equals("y"));

                    newTransfer = getInput("Would you like to start a new transfer? (y/N) ");
                }
                if (requestType.toLowerCase().equals("q") || !newTransfer.equalsIgnoreCase("y")) {
                    break;
                }
            }
            client.close();
            System.out.println("Client closed");
        } catch (IOException | UnknownIOModeException e) {
            e.printStackTrace();
        }
    }
}
