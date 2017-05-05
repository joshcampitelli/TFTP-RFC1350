import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

import java.util.Arrays;

import java.io.IOException;

/**
 *
 * @author Ahmed Sakr
 * @since May the 2nd, 2017
 */

public class SRSocket extends DatagramSocket {

    private String name;

    public SRSocket(String name) throws IOException {
        super();
        this.name = name;
    }

    public SRSocket(String name, int port) throws IOException {
        this(name, port, InetAddress.getLocalHost());
    }

    public SRSocket(String name, int port, InetAddress address) throws IOException {
        super(port, address);
        this.name = name;
    }

    public DatagramPacket receive() throws IOException {
        byte[] data = new byte[100];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        this.receive(packet);

        return packet;
    }

    public void send(DatagramPacket packet) throws IOException {
        super.send(packet);
    }
    public void send(InetAddress host, int port, byte[] data) throws IOException {
        this.send(new DatagramPacket(data, data.length, host, port));
    }

    public void notify(DatagramPacket packet, String event) {
        int len = packet.getLength();
        System.out.printf("%s: %s:\n", this.name, event);
        System.out.printf("%s Host Address: %s, Host port: %d, Length: %d\n",
                        event.contains("Send") ? "To" : "From", packet.getAddress(), packet.getPort(), len);
        System.out.printf("Data (as string): %s\n", new String(packet.getData(), 0, packet.getData().length));
    }

    public void notifyXtra(DatagramPacket packet, String event) {
        this.notify(packet, event);
        System.out.printf("Data (as bytes): %s\n\n", Arrays.toString(packet.getData()));
    }
}
