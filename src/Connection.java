import java.net.InetAddress;
import java.net.DatagramPacket;
import java.io.IOException;

/**
 *
 * @author Carleton Summer 2017 SYSC 3303A Group 2,000,000
 * @since May the 5th, 2017
 */
public class Connection extends SRSocket implements Runnable {

    private String name;
    private DatagramPacket client;

    public Connection(DatagramPacket packet, String name) throws IOException {
        super(name);
        this.client = packet;
    }

    public Connection(DatagramPacket packet, String name, int port) throws IOException {
        super(name, port, InetAddress.getLocalHost());
        this.client = packet;
    }

    public Connection(DatagramPacket packet, String name, int port, InetAddress address) throws IOException {
        super(name, port, address);
        this.client = packet;
    }

    /**
     * Produce a response from the given byte array.
     */
    public byte[] parse(byte[] data) throws InvalidPacketException {
        if (data[1] == 1) {
            return new byte[]{0, 3, 0, 1};
        } else if (data[1] == 2) {
            return new byte[]{0, 4, 0, 0};
        } else {
            throw new InvalidPacketException("Illegal data buffer!!!");
        }
    }

    private void acknowledge() throws IOException, InvalidPacketException {
        byte[] response = parse(client.getData());
        dispatch(response);
    }

    private void dispatch(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, client.getAddress(), client.getPort());
        notify(packet, "Sending Packet");
        send(packet);
    }

    @Override
    public void run() {
        try {
            acknowledge();

            while (true) {
                DatagramPacket packet = receive();
                notify(packet, "Received Packet");

                byte[] response = parse(packet.getData());
                dispatch(response);
            }
        } catch (IOException | InvalidPacketException e) {
            e.printStackTrace();
        }
    }
}
