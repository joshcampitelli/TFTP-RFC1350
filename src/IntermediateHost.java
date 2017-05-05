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

public class IntermediateHost {

    private SRSocket receiveSocket;
    private static int RECEIVE_PORT = 23;
    private static int SERVER_PORT = 69;

    public IntermediateHost() throws IOException {
        this.receiveSocket = new SRSocket("IntermediateHost, Main Socket 'R'", RECEIVE_PORT);
    }

    public SRSocket getReceiveSocket() {
        return this.receiveSocket;
    }

    public DatagramPacket produceFrom(DatagramPacket packet, int port, InetAddress address) {
        return new DatagramPacket(packet.getData(), packet.getLength(), address, port);
    }

    public static void main(String[] args) {
        try {

            IntermediateHost host = new IntermediateHost();
            System.out.printf("IntermediateHost has successfully launched.\n\n");

            while (true) {
                System.out.printf("Listening...\n");
                DatagramPacket client = host.getReceiveSocket().receive();
                host.getReceiveSocket().notify(client, "Received Packet");

                // request received => produce a temporary send/receive socket to fulfill the networking requirements
                SRSocket temp = new SRSocket("IntermediateHost, Temp Socket 'S/R'");

                DatagramPacket server = host.produceFrom(client, SERVER_PORT, InetAddress.getLocalHost());
                temp.notify(server, "Sending Packet");
                temp.send(server);

                DatagramPacket response = temp.receive();
                temp.notify(response, "Received Packet");

                DatagramPacket result = host.produceFrom(response, client.getPort(), client.getAddress());
                temp.notify(result, "Sending Packet");
                temp.send(result);
                temp.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
