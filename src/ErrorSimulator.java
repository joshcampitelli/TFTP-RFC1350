import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

import java.io.IOException;

/**
 *
 * @author Ahmed Sakr
 * @since May the 1st, 2017
 */

public class ErrorSimulator {

    private SRSocket receiveSocket;
    private static int RECEIVE_PORT = 23;
    private static int SERVER_PORT = 69;

    public ErrorSimulator() throws IOException {
        this.receiveSocket = new SRSocket("ErrorSimulator, Main Socket 'R'", RECEIVE_PORT);
    }

    public SRSocket getReceiveSocket() {
        return this.receiveSocket;
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

            DatagramPacket server = this.produceFrom(client, SERVER_PORT, InetAddress.getLocalHost());
            temp.notify(server, "Sending Packet");
            temp.send(server);

            DatagramPacket response = temp.receive();
            temp.notify(response, "Received Packet");

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
