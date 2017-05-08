import java.net.DatagramPacket;
import java.io.IOException;
import java.lang.Thread;
import java.util.ArrayList;

import core.SRSocket;
import core.Connection;
import exceptions.InvalidPacketException;

/**
 *
 * @author Ahmed Sakr
 * @since May the 1st, 2017
 */

public class Server extends SRSocket {

    private QuitListener quitListener;
    private ArrayList<Thread> connections;
    private int threadNumber;
    public static int RECEIVE_PORT = 69;

    public Server() throws IOException {
        super("Server, Socket 'R'", RECEIVE_PORT);
        this.connections = new ArrayList<>();
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
        Thread thread = new Thread(connection, "Connection" + threadNumber++);
        thread.start();

        connections.add(thread);
    }

    public void launch() throws IOException, InvalidPacketException {
        System.out.printf("Server has successfully launched.\n\n");
        QuitListener listener = new QuitListener(this, "QuitListener");
        listener.start();

        while (!isClosed()) {
            System.out.printf("Listening...\n");

            DatagramPacket packet = receive();
            inform(packet, "Received Packet");
            establish(packet);
        }
    }

    public void shutdown() {
        close();
    }



    public static void main(String[] args) {
        Server server = null;
        try {
            server = new Server();
            server.launch();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidPacketException e) {
            System.out.printf("Invalid packet encountered. Server shutting down: no more connections accepted.\n");
        }
    }
}
