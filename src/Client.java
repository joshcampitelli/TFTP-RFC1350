import java.util.Scanner;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

import core.SRSocket;
import core.Packet;

/**
 *
 * @author Ahmed Sakr
 * @since May the 1st, 2017
 */
public class Client extends SRSocket {

    public static byte REQUEST_READ = 1, REQUEST_WRITE = 2;
	public static int ERRORSIMULATOR_PORT = 23;
    public static int MODE_NORMAL = 1, MODE_TEST = 2;

    private int TID;
    private int serverPort = 69;
    private int connectionMode;

    public Client() throws IOException {
        super("Client");
        this.TID = getPort();
    }

    /**
     * Scans the console for user input.
     *
     * @param text the text to display when prompting the user for input
     *
     * @return the user input as a string
     */
    public String getInput(String text) {
        Scanner scanner = new Scanner(System.in);
        System.out.printf(text);

        return scanner.nextLine();
    }

    public int getTID() {
        return this.TID;
    }

    public void setMode(int mode) {
        this.connectionMode = mode;
    }

    public int getMode() {
        return this.connectionMode;
    }

	private void sendReceive(byte[] filename, byte[] mode) throws IOException {
        byte rw;

        for (int i = 0; i <= 10; i++) {

            int port = serverPort;
            if (this.getMode() == MODE_TEST) {
                port = ERRORSIMULATOR_PORT;
            }

            DatagramPacket packet;

            if (i % 2 == 0)
                packet = new Packet().RRQPacket(mode, filename, InetAddress.getLocalHost(), port);
            else
                packet = new Packet().WRQPacket(mode, filename, InetAddress.getLocalHost(), port);

            inform(packet, "Sending packet", true);
            send(packet);

            System.out.printf("Waiting for response from server...\n");

            DatagramPacket response = receive();
			      serverPort = response.getPort();
            inform(response, "Packet Received", true);
        }
	}

    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.setMode(MODE_NORMAL);

            byte[] filename = client.getInput("Enter file name: ").getBytes();
            byte[] mode = "octet".getBytes();
			client.sendReceive(filename, mode);
		} catch (IOException e) {
            e.printStackTrace();
        }
    }
}
