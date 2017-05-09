package com.tftp.core;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.io.IOException;

import com.tftp.Client;

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

    protected String getName() {
        return this.name;
    }

    public DatagramPacket receive() throws IOException {
        byte[] data = new byte[514];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        this.receive(packet);

        // reduce the buffer to the size of the data received, if possible
        System.out.println(packet.getLength());
        data = shrink(packet.getData(), packet.getLength());
        packet.setData(data);

        return packet;
    }

    public void send(DatagramPacket packet) throws IOException {
        super.send(packet);
    }
    public void send(InetAddress host, int port, byte[] data) throws IOException {
        this.send(new DatagramPacket(data, data.length, host, port));
    }

    public void inform(DatagramPacket packet, String event, boolean extra) {
        if (!Client.verbose) {
            return;
        }

        int len = packet.getLength();
        System.out.printf("%s: %s:\n", this.name, event);
        System.out.printf("%s Host Address: %s, Host port: %d, Length: %d\n",
                        event.contains("Send") ? "To" : "From", packet.getAddress(), packet.getPort(), len);
        System.out.printf("Data (as string): %s\n", new String(packet.getData(), 0, packet.getData().length));

        if (extra) {
            System.out.printf("Data (as bytes): %s\n\n", Arrays.toString(packet.getData()));
        }
    }

    public void inform(DatagramPacket packet, String event) {
        inform(packet, event, false);
    }


    /**
     * Shrinks the provided array to a new, specified length. If the new length provided is bigger
     * than the array's existing size, then the method returns without doing anything.
     *
     * @return the shrunk array
     */
    protected byte[] shrink(byte[] arr, int newLength) {
        if (newLength > arr.length) {
            return arr;
        }

        byte[] arr1 = new byte[newLength];
        for (int i = 0; i < arr1.length; i++) {
            arr1[i] = arr[i];
        }

        return arr1;
    }

    //converts an integer into a 2 byte array
    protected byte[] getBlockNumber(int input) {
        byte[] data = new byte[2]; // <- assuming "in" value in 0..65535 range and we can use 2 bytes only

        data[1] = (byte)(input & 0xFF);
        data[0] = (byte)((input >> 8) & 0xFF);

        return data;
    }
}
