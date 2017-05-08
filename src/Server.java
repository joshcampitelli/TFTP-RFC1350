import java.net.DatagramPacket;
import java.io.IOException;
import java.lang.Thread;

import core.SRSocket;
import core.Connection;
import exceptions.InvalidPacketException;

/**
 *
 * @author Ahmed Sakr
 * @since May the 1st, 2017
 */

public class Server extends SRSocket {

    public static int RECEIVE_PORT = 69;

    public Server() throws IOException {
        super("Server, Socket 'R'", RECEIVE_PORT);
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

            DatagramPacket packet = receive();
            notify(packet, "Received Packet");
            establish(packet);
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
