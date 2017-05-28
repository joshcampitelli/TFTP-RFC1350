package com.tftp.core.protocol;

import java.net.DatagramPacket;
import java.net.InetAddress;

import java.util.Arrays;

/**
 * Packet allows for creation and detection of all possible packets encountered in the TFTP protocol (i.e. RRQ,
 * WRQ, DATA, ACK, ERROR, and UNKNOWN).
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 8th, 2017.
 */
public abstract class Packet {

    private DatagramPacket packet;
    public enum PacketTypes { ACK, DATA, RRQ, WRQ, ERROR, UNKNOWN }
    public static int ACK_SIZE = 4;

    public Packet() {
    }

    public Packet(DatagramPacket packet) {
        this.packet = packet;
    }


    public abstract DatagramPacket getDatagram();

    /**
     *
     * Recursively matches a byte array pattern with the provided form as a string where the following letters in the string are important:
     *
     * - c: stands for control and checks for the given byte with the control byte the array provided
     * - x: stands for don't care, used for skipping a dynamic input that terminates once the next pattern in line is found.
     */
    protected static boolean matches(byte[] data, int size, String form, byte opcode) {
        return matches(data, 0, size, form, opcode, false);
    }

    protected static boolean matches(byte[] data, int index, int size, String form, byte opcode, boolean inText) {
        // base case
        if (form.isEmpty() && index == size) {
            return true;
        }

        if (index == size && form.length() == 1 && form.charAt(0) == 'x') {
            return true;
        }

        char letter = 0;
        if (!form.isEmpty()) {
            letter = form.charAt(0);
        }

        if (letter == 'c' && data[index] == opcode) {
            return matches(data, ++index, size, form.substring(1), opcode, false);
        } else if (letter == '0' && data[index] == 0) {
            return matches(data, ++index, size, form.substring(1), opcode, false);
        } else if (letter == 'x') {
            return matches(data, ++index, size, form.substring(1), opcode, true);
        } else if (letter == 'n') {
            return matches(data, ++index, size, form.substring(1), opcode, false);
        } else if (inText) {
            return matches(data, ++index, size, form, opcode, true);
        } else {
            return false;
        }
    }

    /**
     *
     * PacketTypes method uses the matches method to determine the type of packet sent to the server
     * then returns the type as an enum temporarily, could have a class with setPacketType() etc.
     */
    public static PacketTypes getPacketType(DatagramPacket packet) {
        byte read = 1, write = 2, data = 3, ack = 4, error = 5;

        if (matches(packet.getData(), packet.getLength(), "0cx0x0", read)) {
            return PacketTypes.RRQ;
        } else if (matches(packet.getData(), packet.getLength(), "0cx0x0", write)) {
            return PacketTypes.WRQ;
        } else if (matches(packet.getData(), packet.getLength(), "0cnnx", data)) {
            return PacketTypes.DATA;
        } else if (matches(packet.getData(), packet.getLength(), "0cnn", ack)) {
            return PacketTypes.ACK;
        } else if (matches(packet.getData(), packet.getLength(), "0cx0", error)) {
            return PacketTypes.ERROR;
        } else {
            return PacketTypes.UNKNOWN;
        }
    }

    /**
     * @param data byte[] for the datagram packet
     *
     * @return datagram packet
     *
     * Creates a DatagramPacket from received DatagramPacket.
     */
    public DatagramPacket createPacket(byte[] data) {
        return  new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
    }

    /**
     *
     * Creates a DatagramPacket from nothing.
     */
    public DatagramPacket createPacket(byte[] data, InetAddress address, int port) {
        return new DatagramPacket(data, data.length, address, port);
    }
}
