import java.util.Scanner;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

import core.SRSocket;

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

    /**
     * Builds the master byte array, composed from the mode, filename, and read/write bytes.
     *
     * @param rw the read/write byte
     * @param mode the mode byte array
     * @param filename the filename byte array
     */
    public byte[] buildRequest(byte rw, byte[] mode, byte[] filename) {
        byte[] request = new byte[2 + filename.length + 1 + mode.length  + 1];
        int counter = 0;

        request[0] = 0;            // first '0' byte
        request[1] = rw;           // read or write request
        counter += 2;

        System.arraycopy(filename, 0, request, counter, filename.length);

        // must add 1 as there is a '0' byte between filename and mode
        counter += (filename.length + 1);

        System.arraycopy(mode, 0, request, counter, mode.length);
        return request;
    }

    public void setMode(int mode) {
        this.connectionMode = mode;
    }

    public int getMode() {
        return this.connectionMode;
    }

	private void sendReceive(byte[] filename, byte[] mode) throws IOException {
        byte[] request;
        byte rw;

        for (int i = 0; i <= 10; i++) {
            rw = i % 2 == 0 ? REQUEST_READ : REQUEST_WRITE;
            request = this.buildRequest(i == 10 ? (byte) 4 : rw, mode, filename);

            int port = serverPort;
            if (this.getMode() == MODE_TEST) {
                port = ERRORSIMULATOR_PORT;
            }

            DatagramPacket packet = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), port);
            notifyXtra(packet, "Sending packet");
            send(packet);

            System.out.printf("Waiting for response from server...\n");

            DatagramPacket response = receive();
			serverPort = response.getPort();
            notifyXtra(response, "Packet Received");
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
