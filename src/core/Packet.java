package core;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 *
 * @author SYSC3303 Team 2
 */
public class Packet {
    private DatagramPacket packet;
    private enum PacketTypes { ACK, DATA, RRQ, WRQ, ERROR, UNKNOWN };

    public Packet() {
    }

    public Packet(DatagramPacket packet) {
        this.packet = packet;
    } 

    public DatagramPacket parse(byte[] blocknumber) {
        return parse(blocknumber, packet.getData());
    }

    public DatagramPacket parse(byte[] blocknumber, byte[] data) {
        if (data[1] == 1) {
            return DATAPacket(blocknumber, data);
        } else if (data[1] == 2) {
            return ACKPacket(blocknumber);
        } else if (data[1] == 3) {
            return ACKPacket(blocknumber);
        } else if (data[1] == 4) {
            return DATAPacket(blocknumber, data);
        } else {
            return null;
        }
    }

    /**
     *
     * @return true     if the val is present
     *         false    otherwise
     *
     * Simple helper method used to check if a value is present in the array.
     */
    private boolean contains(byte[] arr, byte val) {
        for (byte b : arr) {
            if (b == val) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * Recursively matches a byte array pattern with the provided form as a string where the following letters in the string are important:
     *
     * - c: stands for control and checks for the given byte with the control byte the array provided
     * - x: stands for don't care, used for skipping a dynamic input that terminates once the next pattern in line is found.
     */
    protected boolean matches(byte[] data, int size, String form, byte[] control) {
        return matches(data, 0, size, form, control, false);
    }

    protected boolean matches(byte[] data, int index, int size, String form, byte[] control, boolean inText) {
        // base case
        if (form.isEmpty() && index == size) {
            return true;
        }

        char letter = form.charAt(0);
        if (letter == 'c' && contains(control, data[index])) {
            return matches(data, ++index, size, form.substring(1), control, false);
        } else if (letter == '0' && data[index] == 0) {
            return matches(data, ++index, size, form.substring(1), control, false);
        } else if (letter == 'x' && data[index] != 0) {
            return matches(data, ++index, size, form.substring(1), control, true);
        } else if (inText) {
            return matches(data, ++index, size, form, control, true);
        } else {
            return false;
        }
    }

    //PacketTypes method uses the matches method to determine the type of packet sent to the server
    //then returns the type as an enum temporarily, could have a class with setPacketType() etc.
    private PacketTypes checkPacketType(DatagramPacket packet) {
        byte[] readValues = {1};
        byte[] writeValues = {2};
        byte[] dataValues = {3};
        byte[] ackValues = {4};
        byte[] errorValues = {5};

        if (matches(packet.getData(), packet.getLength(), "0cx0x0", readValues)) {
            return PacketTypes.RRQ;
        } else if (matches(packet.getData(), packet.getLength(), "0cx0x0", writeValues)) {
            return PacketTypes.WRQ;
        } else if (matches(packet.getData(), packet.getLength(), "0cx", dataValues)) {
            return PacketTypes.DATA;
        } else if (matches(packet.getData(), packet.getLength(), "0cx", ackValues)) {
            return PacketTypes.ACK;
        } else if (matches(packet.getData(), packet.getLength(), "0cx0", errorValues)) {
            return PacketTypes.ERROR;
        } else {
            return PacketTypes.UNKNOWN;
        }
    }

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

    public byte[] RRQ(byte[] mode, byte[] filename) {
        // Safe typecast
        return createRequest((byte) 1, mode, filename);
    }

    public byte[] WRQ(byte[] mode, byte[] filename) {
        return createRequest((byte) 2, mode, filename);
    } 

    public byte[] DATA(byte[] blocknumber, byte[] data) {
        byte[] aarthi = new byte[2 + blocknumber.length];
        int counter = 2;

        data[1] = 3;
        System.arraycopy(blocknumber, 0, aarthi, counter, blocknumber.length);

        counter += blocknumber.length;

        System.arraycopy(data, 0, aarthi, counter, data.length);
        return  aarthi;
    }

    public byte[] ACK(byte[] blocknumber) {
        byte[] ack = new byte[2 + blocknumber.length];
        ack[1] = 4;
        System.arraycopy(blocknumber, 0, ack, 2, blocknumber.length);
        return ack;
    }

    /**
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

    public DatagramPacket RRQPacket(byte[] mode, byte[] filename) {
        return createPacket(RRQ(mode, filename));
    }

    public DatagramPacket RRQPacket(byte[] mode, byte[] filename, InetAddress address, int port) {
        return createPacket(RRQ(mode, filename), address, port);
    }

    public DatagramPacket WRQPacket(byte[] mode, byte[] filename) {
        return createPacket(WRQ(mode, filename));
    }

    public DatagramPacket WRQPacket(byte[] mode, byte[] filename, InetAddress address, int port) {
        return createPacket(WRQ(mode, filename), address, port);
    }

    public DatagramPacket DATAPacket(byte[] blocknumber, byte[] data) {
        return createPacket(DATA(blocknumber, data));
    }

    public DatagramPacket DATAPacket(byte[] blocknumber, byte[] data, InetAddress address, int port) {
        return createPacket(DATA(blocknumber, data), address, port);
    }

    public DatagramPacket ACKPacket(byte[] blocknumber) {
        return createPacket(ACK(blocknumber));
    }

    public DatagramPacket ACKPacket(byte[] blocknumber, InetAddress address, int port) {
        return createPacket(ACK(blocknumber), address, port);
    }

    public void run() {
    }
}
