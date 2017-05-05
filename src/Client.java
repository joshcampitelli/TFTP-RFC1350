import java.util.Scanner;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Ahmed Sakr
 * @since May the 1st, 2017
 */
public class Client {

    public static byte REQUEST_READ = 1, REQUEST_WRITE = 2;
    public static int SERVER_PORT = 23;
    private SRSocket sendReceive;

    private int connectionMode;
    public static int MODE_NORMAL = 1, MODE_TEST = 2;

    public Client() throws IOException {
        this.sendReceive = new SRSocket("Client");
    }

    public SRSocket getSocket() {
        return this.sendReceive;
    }

    /**
     * Scans the console for user input.
     *
     * @param text the text to display when prompting the user for input
     *
     * @return the user input as a string
     */
    public String getInput(String text) {
        Scanner scanner = new Scanner(System.in);
        System.out.printf(text);

        return scanner.nextLine();
    }

    /**
     * Builds the master byte array, composed from the mode, filename, and read/write bytes.
     *
     * @param rw the read/write byte
     * @param mode the mode byte array
     * @param filename the filename byte array
     */
    public byte[] buildRequest(byte rw, byte[] mode, byte[] filename) {
        byte[] request = new byte[2 + filename.length + 1 + mode.length  + 1];
        int counter = 0;

        request[0] = 0;            // first '0' byte
        request[1] = rw;           // read or write request
        counter += 2;

        System.arraycopy(filename, 0, request, counter, filename.length);

        // must add 1 as there is a '0' byte between filename and mode
        counter += (filename.length + 1);

        System.arraycopy(mode, 0, request, counter, mode.length);
        return request;
    }

    public void setMode(int mode) {
        this.connectionMode = mode;
    }

    public int getMode() {
        return this.connectionMode;
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.setMode(MODE_TEST);

            byte[] filename = client.getInput("Enter file name: ").getBytes();
            byte[] mode = client.getInput("Enter mode: ").getBytes();
            byte[] request;
            byte rw;

            for (int i = 0; i <= 10; i++) {
                rw = i % 2 == 0 ? REQUEST_READ : REQUEST_WRITE;
                request = client.buildRequest(i == 10 ? (byte) 4 : rw, mode, filename);

                int port = 69;
                if (client.getMode() == MODE_TEST) {
                    port = 23;
                }

                DatagramPacket packet = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), port);
                client.getSocket().notifyXtra(packet, "Sending packet");
                client.getSocket().send(InetAddress.getLocalHost(), port, request);

                System.out.printf("Waiting for response from server...\n");
                DatagramPacket response = client.getSocket().receive();
                client.getSocket().notifyXtra(response, "Packet Received");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
