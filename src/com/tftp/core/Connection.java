package com.tftp.core;

import com.tftp.core.protocol.*;
import com.tftp.core.protocol.packets.ACKPacket;
import com.tftp.core.protocol.packets.DATAPacket;
import com.tftp.core.protocol.packets.ERRORPacket;
import com.tftp.exceptions.InvalidPacketException;
import com.tftp.exceptions.UnknownIOModeException;
import com.tftp.io.FileTransfer;

import java.net.DatagramPacket;
import java.io.IOException;

/**
 * Connection allows for the dedicated thread birth for a new transfer request to be serviced. Connection containsKey
 * all the logic and power to fulfill all the needs of the transfer.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 1st, 2017.
 */
public class Connection extends SRSocket implements Runnable {

    private DatagramPacket request;
    private int TID, clientTID;
    private FileTransfer fileTransfer;
    private int ackBlock = 0;
    private int dataBlock = 1;
    private boolean active = true;

    public Connection(DatagramPacket packet) throws IOException {
        super(String.format("Connection (Client TID: %d)", packet.getPort()));
        this.request = packet;
        this.TID = getPort();
        this.clientTID = packet.getPort();
    }

    public int getTID() {
        return this.TID;
    }

    public int getClientTID() {
        return this.clientTID;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    /**
     * Extracts the RRQ/WRQ parameters. (i.e. filename, mode)
     *
     * @param packet The DatagramPacket holding the RRQ/WRQ request
     *
     * @return a String array of length 2, where:
     *         index 0 is always filename, index 1 is always mode.
     */
    private String[] extractTransferParameters(DatagramPacket packet) {
        String[] parameters = new String[2];
        String data = new String(packet.getData()).substring(2);
        int delimiter = data.indexOf((char) ((byte) 0));

        // filename and mode, respectively
        parameters[0] = data.substring(0, delimiter);
        parameters[1] = data.substring(delimiter + 1, data.length() - 1);
        return parameters;
    }


    //Removes the opcode & block number (first 4 bytes) from the data
    //used only for DATA Packets
    private byte[] extractData(byte[] msg) {
        byte[] data = new byte[msg.length - 4];
        System.arraycopy(msg, 4, data, 0, msg.length - 4);
        return data;
    }

    //Handles the different types of packets sent to the server, returns the returnPacket to go back to client (ACK/DATA)
    private DatagramPacket handlePacket(DatagramPacket receivedPacket) throws UnknownIOModeException, IOException, InvalidPacketException {
        int blockNumber = -1;
        if (Packet.getPacketType(receivedPacket) == Packet.PacketTypes.ACK) {
            blockNumber = dataBlock - 1;
        } else if (Packet.getPacketType(receivedPacket) == Packet.PacketTypes.DATA) {
            blockNumber = ackBlock;
        }

        DatagramPacket errorPacket;
        if (Packet.getPacketType(receivedPacket) == Packet.PacketTypes.ERROR) {
            return errorReceived(receivedPacket);
        } else {
            errorPacket = parseUnknownPacket(receivedPacket, clientTID, blockNumber);
        }

        if (errorPacket != null && errorPacket.getData()[3] == 4) {
            setActive(false);
            return errorPacket; //Sends the error packet
        } else if (errorPacket != null && errorPacket.getData()[3] == 5) {
            return new ERRORPacket(receivedPacket, TFTPError.UNKNOWN_TRANSFER_ID, "Unknown transfer ID".getBytes()).getDatagram();
        }

        if (Packet.getPacketType(receivedPacket) == Packet.PacketTypes.RRQ) {
            return rrqReceived(receivedPacket);
        } else if (Packet.getPacketType(receivedPacket) == Packet.PacketTypes.WRQ) {
            return wrqReceived(receivedPacket);
        } else if (Packet.getPacketType(receivedPacket) == Packet.PacketTypes.ACK) {
            return ackReceived(receivedPacket);
        } else if (Packet.getPacketType(receivedPacket) == Packet.PacketTypes.DATA) {
            return dataReceived(receivedPacket);
        } else {
            throw new InvalidPacketException("Illegal packet parsed!!!");
        }
    }

    //Read Request Received initializes the FileTransfer for mode READ, and sends DATA1 Packet
    private DatagramPacket rrqReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        String[] parameters = extractTransferParameters(packet);
        String filename = parameters[0];
        String mode = parameters[1];

        if (!mode.equalsIgnoreCase("octet")) {
            return new ERRORPacket(packet, TFTPError.ILLEGAL_TFTP_OPERATION, "Illegal mode for RRQ".getBytes()).getDatagram();
        }

        if (!FileTransfer.isFileExisting(filename)) {//File Does Not Exist
            System.out.println("Invalid Request Received, File Does Not Exist.");
            return new ERRORPacket(packet, TFTPError.FILE_NOT_FOUND, ("File Not Found: " + filename).getBytes()).getDatagram();
        } else if (!FileTransfer.isReadable()) {
            return new ERRORPacket(packet, TFTPError.ACCESS_VIOLATION, ("Access violation").getBytes()).getDatagram();
        }

        fileTransfer = new FileTransfer(filename, FileTransfer.READ);
        byte[] data = fileTransfer.read();
        data = shrink(data, fileTransfer.lastBlockSize());

        DatagramPacket temp = new DATAPacket(packet, BlockNumber.getBlockNumber(dataBlock), data).getDatagram();
        dataBlock++;

        return temp;
    }

    //Write Request Received initializes the FileTransfer for mode WRITE, then sends ACK0 Packet
    private DatagramPacket wrqReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        String[] parameters = extractTransferParameters(packet);
        String filename = parameters[0];
        String mode = parameters[1];

        if (!mode.equalsIgnoreCase("octet")) {
            return new ERRORPacket(packet, TFTPError.ILLEGAL_TFTP_OPERATION, "Illegal mode for WRQ".getBytes()).getDatagram();
        }

        if (!FileTransfer.isWritable()) {
            return new ERRORPacket(packet, TFTPError.ACCESS_VIOLATION, ("Access violation").getBytes()).getDatagram();
        }

        fileTransfer = new FileTransfer(filename, FileTransfer.WRITE);

        DatagramPacket temp =  new ACKPacket(packet, BlockNumber.getBlockNumber(ackBlock)).getDatagram();
        ackBlock++;

        return temp;
    }

    //Ack Received gets the bytes from the FileTransfer Object then sends DATA1 Packet
    private DatagramPacket ackReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        //Send Data from the file
        byte[] data = fileTransfer.read();

        DatagramPacket temp = new DATAPacket(packet, BlockNumber.getBlockNumber(dataBlock), data).getDatagram();

        // shrink data array to amount of read bytes
        temp.setData(shrink(temp.getData(), fileTransfer.lastBlockSize() + 4));
        dataBlock++;
        return temp;

    }

    //Data Received extracts the data (removed opcode/block#) then uses FileTransfer Object to Write the data
    private DatagramPacket dataReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        byte[] msg = extractData(packet.getData());

        if (FileTransfer.getFreeSpace() < msg.length) {
            fileTransfer.delete(); //Deletes the Incomplete file from the server.
            return new ERRORPacket(packet, TFTPError.DISK_FULL, ("Disk Full or Allocation Exceeded").getBytes()).getDatagram();
        }

        fileTransfer.write(msg);

        DatagramPacket temp = new ACKPacket(packet, BlockNumber.getBlockNumber(ackBlock)).getDatagram();
        ackBlock++;
        return temp;
    }

    //Error Received handles the error packets which are sent to the server, different from detecting errors
    //No matter which error the Connection RECEIVES it shuts down. This is because if it receives an error
    //Packet that means the client has sent it an error, if its TID then the Connection is communicating with
    //The incorrect client.
    private DatagramPacket errorReceived(DatagramPacket packet) throws UnknownIOModeException, IOException {
        //If the Server receives an invalid TID it must terminate, this means the Server is communicating with an incorrect Client
        byte[] errorMsg = new byte[packet.getLength() - 4];
        System.arraycopy(packet.getData(), 4, errorMsg, 0, packet.getData().length - 4);
        System.out.println("Error Packet Received: Error Code: 0" + packet.getData()[3] + ", Error Message: " + new String(errorMsg));
        return null;
    }

    private void process(DatagramPacket request) throws IOException, InvalidPacketException, UnknownIOModeException {
        DatagramPacket packet = handlePacket(request);

        if (packet == null){
            setActive(false);
        } else {
            inform(packet, "Sending Packet");
            send(packet);
        }

        // transfer complete
        if (fileTransfer != null && fileTransfer.isComplete()) {
            setActive(false);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                process(request);

                if (!isActive()) {
                    break;
                }

                request = receive();
                inform(request, "Received Packet", true);
            }

            System.out.printf("%s terminated and is closing...\n", getName());
        } catch (IOException | InvalidPacketException | UnknownIOModeException e) {
            System.out.printf("%s sent an invalid request. Terminating thread...\n", getName());
        }
    }
}
