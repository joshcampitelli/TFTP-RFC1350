import java.net.InetAddress;
import java.net.DatagramPacket;
import java.io.IOException;

/**
 *
 * @author Carleton Summer 2017 SYSC 3303A Group 2
 * @since May the 5th, 2017
 */
public class Connection extends SRSocket implements Runnable {

    private String name;
    private DatagramPacket client;
    private int TID, clientTID;

    public Connection(DatagramPacket packet) throws IOException {
        super(String.format("Connection (Client TID: %d)", packet.getPort()));
        this.client = packet;
        this.TID = getPort();
        this.clientTID = packet.getPort();
    }

    public int getTID() {
        return this.TID;
    }

    public int getClientTID() {
        return this.clientTID;
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

    private void process(DatagramPacket request) throws IOException, InvalidPacketException {
        byte[] response = parse(request.getData());
        DatagramPacket packet = new DatagramPacket(response, response.length, client.getAddress(), client.getPort());

        notify(packet, "Sending Packet");
        send(packet);
    }

    @Override
    public void run() {
        try {
            process(client); // acknowledge the first packet to begin the send-receive cycle

            while (true) {
                DatagramPacket packet = receive();
                notify(packet, "Received Packet");
                process(packet);
            }
        } catch (IOException | InvalidPacketException e) {
            e.printStackTrace();
        }
    }
}
