import java.net.InetAddress;

import java.io.IOException;

/**
 *
 * @author Carleton Summer 2017 SYSC 3303A Group 2,000,000
 * @since May the 5th, 2017
 */
public class Connection extends SRSocket implements Runnable {

    private String name;

    public Connection(String name) throws IOException {
        super(name);
    }

    public Connection(String name, int port) throws IOException {
        super(name, port, InetAddress.getLocalHost());
    }

    public Connection(String name, int port, InetAddress address) throws IOException {
        super(name, port, address);
    }

    /**
     * @param byte[] DatagramPacket data
     *
     * Parses the DatagramPacket data to determine what and how to manipulate it.
     */
    public void parse(byte[] data) {
    }

    /**
     * @param byte[] DatagramPacket data
     *
     * Manipulates the data in the DatagramPacket before determining what to send though the DatagramSocket.
     */
    public byte[] manipulateData(byte[] data) {
        return data;
    }

    public void run() {
    }
}
