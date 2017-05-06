import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.io.IOException;
import java.lang.Thread;

/**
 *
 * @author Ahmed Sakr
 * @since May the 1st, 2017
 */

public class Server {

    private SRSocket receiveSocket;
    public static int RECEIVE_PORT = 69;

    public Server() throws IOException {
        this.receiveSocket = new SRSocket("Server, Socket 'R'", RECEIVE_PORT);
    }

    public SRSocket getReceiveSocket() {
        return this.receiveSocket;
    }

    /**
     * Tests the packet's data buffer if it contains a valid client request.
     *
     * @return  true    if the packet contains a valid data buffer that may be processed
     *          false   otherwise
     */
    public boolean validatePacket(DatagramPacket packet) {
        byte[] readWriteValues = {1, 2};
        return matches(packet.getData(), packet.getLength(), "0cx0x0", readWriteValues);
    }

    private boolean matches(byte[] data, int size, String form, byte[] control) {
        return matches(data, 0, size, form, control, false);
    }

    /**
     * Recursively Matches a byte array pattern with the provided form as a string, where the following letters in the string are
     * important:
     *
     *   - c: stands for control, and checks for the given byte with the control byte array provided
     *   - x: stands for dont care, used for skipping a dynamic input that terminates once the next pattern in line
     *   is found.
     *
     */
    private boolean matches(byte[] data, int index, int size, String form, byte[] control, boolean inText) {
        // base case
        if (form.isEmpty() && index == size) {
            return true;
        }

        char letter = form.charAt(0);
        if (letter == 'c' && contains(control, data[index])) {
            return matches(data, ++index, size, form.substring(1), control, false);
        } else if (letter == '0' && data[index] == 0) {
            return matches(data, ++index, size, form.substring(1), control, false);
        } else if (letter == 'x' && data[index] != 0) {
            return matches(data, ++index, size, form.substring(1), control, true);
        } else if (inText){
            return matches(data, ++index, size, form, control, true);
        } else {
            return false;
        }
    }


    /**
     * Simple helper method use to check if a value is present in the array.
     *
     * @return  true    if the val is present
     *          false   otherwise
     */
    private boolean contains(byte[] arr, byte val) {
        for (byte b : arr) {
            if (b == val) {
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to establish a connection for the received packet. If the packet has been determined to be invalid,
     * an InvalidPacketException is thrown.
     *
     * @throws InvalidPacketException If the packet has been found to be illegal. Critical error.
     */
    public void establish(DatagramPacket packet) throws IOException, InvalidPacketException {
        if (!validatePacket(packet)) {
            throw new InvalidPacketException("Invalid packet parsed. Aborting...");
        }

        Connection connection = new Connection(packet);
        new Thread(connection).start();
    }

    public void launch() throws IOException, InvalidPacketException {
        System.out.printf("Server has successfully launched.\n\n");

        while (true) {
            System.out.printf("Listening...\n");

            DatagramPacket packet = this.getReceiveSocket().receive();
            this.getReceiveSocket().notify(packet, "Received Packet");
            this.establish(packet);
        }
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.launch();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidPacketException e) {
            // TODO: implement a nice way to terminate server
            e.printStackTrace();
        }
    }
}
