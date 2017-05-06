import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;

import java.io.IOException;
import core.SRSocket;

/**
 *
 * @author Ahmed Sakr
 * @since May the 1st, 2017
 */

public class ErrorSimulator {

    private static int RECEIVE_PORT = 23;
    private static int SERVER_PORT = 69;

    private SRSocket receiveSocket;
    private HashMap<Integer, Integer> router;

    public ErrorSimulator() throws IOException {
        this.receiveSocket = new SRSocket("ErrorSimulator, Main Socket 'R'", RECEIVE_PORT);
        this.router = new HashMap<>();
    }

    public SRSocket getReceiveSocket() {
        return this.receiveSocket;
    }

    public int getDestinationTID(DatagramPacket packet) {
        int port = packet.getPort();
        return router.containsKey(port) ? router.get(port) : SERVER_PORT;
    }

    public void register(int key, int value) {
        if (router.get(key) == null) {
            router.put(key, value);
        }
    }

    public DatagramPacket produceFrom(DatagramPacket packet, int port, InetAddress address) {
        return new DatagramPacket(packet.getData(), packet.getLength(), address, port);
    }

    public void simulate() throws IOException {
        System.out.printf("ErrorSimulator has successfully launched its operations.\n\n");

        while (true) {
            System.out.printf("Listening...\n");
            DatagramPacket client = this.getReceiveSocket().receive();
            this.getReceiveSocket().notify(client, "Received Packet");

            // request received => produce a temporary send/receive socket to fulfill the networking requirements
            SRSocket temp = new SRSocket("IntermediateHost, Temp Socket 'S/R'");

            DatagramPacket server = this.produceFrom(client, getDestinationTID(client), InetAddress.getLocalHost());
            temp.notify(server, "Sending Packet");
            temp.send(server);

            DatagramPacket response = temp.receive();
            temp.notify(response, "Received Packet");
            register(client.getPort(), response.getPort());

            DatagramPacket result = this.produceFrom(response, client.getPort(), client.getAddress());
            temp.notify(result, "Sending Packet");
            temp.send(result);
            temp.close();
        }
    }

    public static void main(String[] args) {
        try {
            ErrorSimulator simulator = new ErrorSimulator();
            simulator.simulate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
