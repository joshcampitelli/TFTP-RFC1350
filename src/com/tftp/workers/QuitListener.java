package com.tftp.workers;

import java.util.Scanner;
import com.tftp.Server;

/**
 * QuitListener is an independent thread that listens on the main server console for input, specifically "quit".
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 8th, 2017.
 */
public class QuitListener extends Thread {

    private Scanner scanner;
    private Server server;

    public QuitListener(Server server) {
        super("QuitListener");
        this.server = server;
        scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        String input;
        while (true) {
            input = scanner.nextLine();

            if (input.equals("quit")) {
                server.shutdown();
                break;
            }
        }
    }
}
