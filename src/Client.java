import java.util.Scanner;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

import core.SRSocket;
import core.Packet;
import exceptions.UnknownIOModeException;
import io.FileTransfer;

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
    private int dataBlock = 1;
    private int ackBlock = 0;
    private FileTransfer fileTransfer;

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

    private void sendRequest(byte[] filename, byte[] mode, String requestType) throws IOException, UnknownIOModeException {
        int port = serverPort;
        if (this.getMode() == MODE_TEST) {
            port = ERRORSIMULATOR_PORT;
        }

        DatagramPacket packet;

        if (requestType.toLowerCase().equals("r")){
            packet = new Packet().RRQPacket(mode, filename, InetAddress.getLocalHost(), port);
            inform(packet, "Sending RRQ packet", true);
            send(packet);
            System.out.printf("Waiting for response from server...\n");
            rrq();
        } else {
            packet = new Packet().WRQPacket(mode, filename, InetAddress.getLocalHost(), port);
            inform(packet, "Sending WRQ packet", true);
            send(packet);
            System.out.printf("Waiting for response from server...\n");
            fileTransfer = new FileTransfer(new String(filename), FileTransfer.READ);
            wrq();
        }
    }

    private void rrq() throws IOException {
        DatagramPacket response = receive();

        serverPort = response.getPort();
        inform(response, "Packet Received", true);

        if (response.getData().length < 514) {
            System.out.println("======READ COMPLETE=======");
            System.out.println("Closing the Clients Stream");
        } else {
            DatagramPacket ackPacket = new Packet(response).ACKPacket(getBlockNumber(ackBlock));
            inform(ackPacket, "Sending ACK Packet", true);
            send(ackPacket);
            ackBlock++;
            rrq();
        }
    }

    private void wrq() throws IOException {
        DatagramPacket response = receive();

        serverPort = response.getPort();
        inform(response, "Packet Received", true);

        Packet packet = new Packet();
        byte[] data = fileTransfer.read();

        if (packet.checkPacketType(response) == Packet.PacketTypes.ACK) {
            DatagramPacket dataPacket = new Packet(response).DATAPacket(getBlockNumber(dataBlock), data);
            inform(dataPacket, "Sending DATA Packet", true);
            send(dataPacket);
            dataBlock++;

            if (dataPacket.getData().length < 514) {
                System.out.println("======WRITE COMPLETE=======");
                System.out.println("Closing the Clients Stream");
            } else {
                wrq();
            }
        }
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();

            // TODO: implement the normal or test mode prompt to the client
            String dataMode = client.getInput("Choose Mode: Normal Mode (N), Test Mode (T)");
            if (dataMode.toLowerCase().equals("n")) {
                client.setMode(MODE_NORMAL);
            } else {
                client.setMode(MODE_TEST);
            }

            /*
            String verbosity = client.getInput("Choose Verbosity: Verbose (V), Quiet(Q)");
            if (mode.toLowerCase().equals("v")) {

            } else {

            }       */

            String requestType = "";
            while (!(requestType.toLowerCase().equals("r") || requestType.toLowerCase().equals("w"))) {
                requestType = client.getInput("Would you like to Write or Read? (W/R)");
            }

            byte[] filename = client.getInput("Enter file name: ").getBytes();
            byte[] mode = "octet".getBytes();
            client.sendRequest(filename, mode, requestType);
        } catch (IOException | UnknownIOModeException e) {
            e.printStackTrace();
        }
    }
}
