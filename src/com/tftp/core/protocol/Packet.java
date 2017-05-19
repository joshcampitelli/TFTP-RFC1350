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
public class Packet {

    private DatagramPacket packet;
    public enum PacketTypes { ACK, DATA, RRQ, WRQ, ERROR, UNKNOWN };
    public static int DATA_SIZE = 516;
    public static int ACK_SIZE = 4;

    public static final byte ERROR_ILLEGAL_TFTP_OPERATION = 04;
    public static final byte ERROR_UNKNOWN_TRANSFER_ID = 05;

    public static final byte NO_SPECIAL_ERROR = 00;
    public static final byte INVALID_OPCODE = 01;
    public static final byte INVALID_PACKET_SIZE = 02;
    public static final byte INVALID_BLOCK_NUMBER = 03;

    public Packet() {
    }

    public Packet(DatagramPacket packet) {
        this.packet = packet;
    }

    /**
     *
     * Recursively matches a byte array pattern with the provided form as a string where the following letters in the string are important:
     *
     * - c: stands for control and checks for the given byte with the control byte the array provided
     * - x: stands for don't care, used for skipping a dynamic input that terminates once the next pattern in line is found.
     */
    protected boolean matches(byte[] data, int size, String form, byte opcode) {
        return matches(data, 0, size, form, opcode, false);
    }

    protected boolean matches(byte[] data, int index, int size, String form, byte opcode, boolean inText) {
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
    public PacketTypes checkPacketType(DatagramPacket packet) {
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
     * @param byte   the read/write byte
     * @param byte[] the mode byte array
     * @param byte[] the filename of the file being transferred
     *
     * @return newly constructed read or write byte array
     *
     * Builds the master byte array composed from the mode, filename and read/write bytes.
     */
    public byte[] createRequest(byte rw, byte[] mode, byte[] filename) {
        byte[] request = new byte[2 + filename.length + 1 + mode.length + 1];
        int counter = 2; // filename starts at index 2

        request[0] = 0;
        request[1] = rw;

        System.arraycopy(filename, 0, request, counter, filename.length);

        // +1 -> there is a 0 between filename and mode
        counter += (filename.length + 1);

        System.arraycopy(mode, 0, request, counter, mode.length);

        return request;
    }

    /**
     * @param byte[] the mode byte array
     * @param byte[] the filename of the file being transferred
     *
     * @return a read byte array
     */
    public byte[] RRQ(byte[] mode, byte[] filename) {
        // Safe typecast
        return createRequest((byte) 1, mode, filename);
    }

    /**
     *
     * @return a write byte array
     */
    public byte[] WRQ(byte[] mode, byte[] filename) {
        return createRequest((byte) 2, mode, filename);
    }

    /**
     * @param byte[] the matching blockNumber for the acknowledgement and data packets
     * @param byte[] the data being sent
     *
     * @return a data byte array
     */
    public byte[] DATA(byte[] blockNumber, byte[] data) {
        byte[] request = new byte[2 + blockNumber.length + data.length];
        int counter = 2;

        request[1] = 3;
        System.arraycopy(blockNumber, 0, request, counter, blockNumber.length);

        counter += blockNumber.length;

        System.arraycopy(data, 0, request, counter, data.length);
        return request;
    }

    /**
     * @param byte[] the matching blockNumber for the acknowledgement and data packets
     *
     * @return an acknowledgement byte array
     */
    public byte[] ACK(byte[] blockNumber) {
        byte[] ack = new byte[ACK_SIZE];
        ack[1] = 4;
        System.arraycopy(blockNumber, 0, ack, 2, 2);
        return ack;
    }

    /**
     *
     * @return a error byte array
     */
    public byte[] ERROR(byte errorCode, byte[] errorMsg) {
        // 5 because last byte is 0
        byte[] error = new byte[5 + errorMsg.length];

        error[1] = 5;
        error[3] = errorCode;
        System.arraycopy(errorMsg, 0, error, 4, errorMsg.length);

        return error;
    }

    /**
     * @param byte[] data for the datagram packet
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

    /**
     * @param byte[] mode byte array
     * @param byte[] filename of the file being transferred
     *
     * @return read datagram packet
     */
    public DatagramPacket RRQPacket(byte[] mode, byte[] filename) {
        return createPacket(RRQ(mode, filename));
    }

    public DatagramPacket RRQPacket(byte[] mode, byte[] filename, InetAddress address, int port) {
        return createPacket(RRQ(mode, filename), address, port);
    }

    /**
     *
     * @return write datagram packet
     */
    public DatagramPacket WRQPacket(byte[] mode, byte[] filename) {
        return createPacket(WRQ(mode, filename));
    }

    public DatagramPacket WRQPacket(byte[] mode, byte[] filename, InetAddress address, int port) {
        return createPacket(WRQ(mode, filename), address, port);
    }

    /**
     *
     * @return data datagram packet
     */
    public DatagramPacket DATAPacket(byte[] blockNumber, byte[] data) {
        return createPacket(DATA(blockNumber, data));
    }

    public DatagramPacket DATAPacket(byte[] blockNumber, byte[] data, InetAddress address, int port) {
        return createPacket(DATA(blockNumber, data), address, port);
    }

    /**
     *
     * @return ack datagram packet
     */
    public DatagramPacket ACKPacket(byte[] blockNumber) {
        return createPacket(ACK(blockNumber));
    }

    public DatagramPacket ACKPacket(byte[] blockNumber, InetAddress address, int port) {
        return createPacket(ACK(blockNumber), address, port);
    }

    /**
     *
     * @return error datagram packet
     */
    public DatagramPacket ERRORPacket(byte errorCode, byte[] errorMsg) {
        return createPacket(ERROR(errorCode, errorMsg));
    }

    public DatagramPacket ERRORPacket(byte errorCode, byte[] errorMsg, InetAddress address, int port) {
        return createPacket(ERROR(errorCode, errorMsg), address, port);
    }
}
