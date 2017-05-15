package com.tftp;

import java.util.Scanner;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;

import com.tftp.core.SRSocket;
import com.tftp.core.Packet;
import com.tftp.exceptions.UnknownIOModeException;
import com.tftp.io.FileTransfer;

/**
 * Client defines and builds the logic for a user capable of transferring files to a pre-defined server port
 * through the agreed upon TFTP protocol.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 1st, 2017.
 */
public class Client extends SRSocket {

    public static final byte REQUEST_READ = 1, REQUEST_WRITE = 2;
    public static final int ERRORSIMULATOR_PORT = 23;
    public static boolean verbose;

    private int TID;
    private int serverPort = 69;
    private int dataBlock = 1;
    private int ackBlock = 0;
    private boolean isNormal = true;
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

    public void setNormal(boolean normal) {
        this.isNormal = normal;
    }

    private void sendRequest(byte[] filename, byte[] mode, String requestType) throws IOException, UnknownIOModeException {
        int port = serverPort;
        if (!this.isNormal) {
            port = ERRORSIMULATOR_PORT;
        }

        DatagramPacket packet;

        if (requestType.toLowerCase().equals("r")){
            packet = new Packet().RRQPacket(mode, filename, InetAddress.getLocalHost(), port);
            inform(packet, "Sending RRQ packet", true);
            send(packet);
            fileTransfer = new FileTransfer(FileTransfer.CLIENT_DIRECTORY + new String(filename), FileTransfer.WRITE);
            rrq();
        } else {
            packet = new Packet().WRQPacket(mode, filename, InetAddress.getLocalHost(), port);
            inform(packet, "Sending WRQ packet", true);
            send(packet);

            fileTransfer = new FileTransfer(FileTransfer.CLIENT_DIRECTORY + new String(filename), FileTransfer.READ);
            wrq();
        }
    }

    private void rrq() throws IOException {
        DatagramPacket response;
        do {
            response = receive();

            serverPort = response.getPort();
            inform(response, "Packet Received", true);

            // unpack the data portion and write it to the file
            int length = response.getData().length;
            byte[] data = new byte[length - 4];
            System.arraycopy(response.getData(), 4, data, 0, length - 4);
            fileTransfer.write(data);

            DatagramPacket ackPacket = new Packet(response).ACKPacket(getBlockNumber(ackBlock));
            ackPacket.setPort(serverPort);

            inform(ackPacket, "Sending ACK Packet", true);
            send(ackPacket);
            ackBlock++;
        } while (response.getData().length == Packet.DATA_SIZE);
    }

    private void wrq() throws IOException {
        DatagramPacket response;
        while(true) {
            response = receive();

            serverPort = response.getPort();
            inform(response, "Packet Received", true);

            Packet packet = new Packet();
            byte[] data = fileTransfer.read();

            if (packet.checkPacketType(response) == Packet.PacketTypes.ACK) {
                DatagramPacket dataPacket = new Packet(response).DATAPacket(getBlockNumber(dataBlock), data);
                dataPacket.setData(shrink(dataPacket.getData(), fileTransfer.lastBlockSize() + 4));
                dataPacket.setPort(serverPort);

                inform(dataPacket, "Sending DATA Packet", true);
                send(dataPacket);
                dataBlock++;

                if (fileTransfer.isComplete()) {
                    System.out.println("[IMPORTANT] Transfer complete!");
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();

            String dataMode = client.getInput("The Client is set to normal. Would you like to set it to test? (y/N) ");
            if (dataMode.toLowerCase().equals("y")) {
                client.setNormal(false);
            }


            String verbosity = client.getInput("The Client is set to quiet. Would you like to set it to verbose? (y/N) ");
            if (verbosity.toLowerCase().equals("y")) {
                verbose = true;
            }

            String requestType = "";
            while (!(requestType.toLowerCase().equals("r") || requestType.toLowerCase().equals("w"))) {
                requestType = client.getInput("Would you like to Write or Read? (W/R) ");
            }

            byte[] filename = client.getInput("Enter file name: ").getBytes();
            byte[] mode = "octet".getBytes();
            
            System.out.printf("[IMPORTANT] Beginning file transfer of \"%s\" with server!\n", new String(filename));
            client.sendRequest(filename, mode, requestType);
        } catch (IOException | UnknownIOModeException e) {
            e.printStackTrace();
        }
    }
}
