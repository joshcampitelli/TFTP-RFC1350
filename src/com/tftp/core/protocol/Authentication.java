package com.tftp.core.protocol;


import com.tftp.core.protocol.Packet.PacketTypes;
import com.tftp.core.protocol.packets.*;
import com.tftp.exceptions.AccessViolationException;
import com.tftp.io.FileTransfer;

import java.net.DatagramPacket;

/**
 * Authentication handles the verification and validation of incoming packets to ensure that they meet
 * the standards to be consumed.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Ahmed Sakr, Josh Campitelli, Brian Zhang, Ahmed Khattab, Dario Luzuriaga
 * @since June the 1st, 2017.
 */
public class Authentication {

    private int tid;
    private byte error;
    private boolean duplicate;
    private String message, mode, filename;
    private Packet packet;


    /**
     * Constructs the authenticator with the target TID.
     *
     * @param tid The Target TID
     */
    public Authentication(int tid) {
        this.tid = tid;
        this.mode = "";
    }


    /**
     * Sets the file name for this authentication instance.
     * This is used for checking I/O permissions.
     *
     * @param filename The qualified name of the file.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }


    /**
     * Sets the mode (i.e. writing, reading) for this authentication instance.
     * This is used for detecting if the packet received is the expected one.
     *
     * @param mode The authentication mode
     */
    public void setMode(String mode) {
        this.mode = mode.toLowerCase();
    }


    /**
     * Sets the TFTPError type for the most recent verify() invocation.
     *
     * @param error The TFTPError code as a byte
     */
    private void setError(byte error) {
        this.error = error;
    }


    /**
     * Sets the error message for the most recent verify() invocation.
     *
     * @param message the error message
     */
    private void setErrorMessage(String message) {
        this.message = message;
    }


    /**
     * Asserts if the most recent verify() invocation resulted in a duplicate packet outcome.
     *
     * @param duplicate the status of the most recent verification
     */
    private void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }


    /**
     *
     * @return the error message
     */
    private String getMessage() {
        return this.message;
    }


    /**
     *
     * @return the resulting packet of the most recent verify() invocation.
     */
    public Packet getResult() {
        return this.packet;
    }


    /**
     *
     * @return the most recent TFTPError code.
     */
    public byte getError() {
        return this.error;
    }


    /**
     *
     * @return if the most recent verify() invocation resulted in a duplicate packet to be identified.
     */
    public boolean isDuplicate() {
        return this.duplicate;
    }


    /**
     * Extracts the RRQ/WRQ parameters. (i.e. filename, mode)
     *
     * @param packet The DatagramPacket holding the RRQ/WRQ request
     *
     * @return a String array of length 2, where:
     *         index 0 is always filename, index 1 is always mode.
     */
    public String[] extractTransferParameters(DatagramPacket packet) {
        String[] parameters = new String[2];
        String data = new String(packet.getData()).substring(2);
        int delimiter = data.indexOf((char) ((byte) 0));

        // filename and mode, respectively
        parameters[0] = data.substring(0, delimiter);
        parameters[1] = data.substring(delimiter + 1, data.length() - 1);
        return parameters;
    }


    /**
     * Verifies if the provided target packet is legal and may be consumed.
     * Checks for both errors in the packet itself and the I/O environment.
     *
     * @param target The DatagramPacket in question
     * @param block the actual block number
     *
     * @return  true    if the packet is legal, expected, and may be consumed
     *          false   otherwise
     */
    public boolean verify(DatagramPacket target, int block) {
        reset();
        Packet packet = toPacket(target);
        byte[] data = target.getData();

        if (target.getPort() != this.tid) {
            setError(TFTPError.UNKNOWN_TRANSFER_ID);
            setErrorMessage("Incorrect TID");
        } else {
            try {
                if (verifyPacketContents(packet, data, block) && verifyFileEnvironment(packet)) {
                    return true;
                }
            } catch (AccessViolationException ex) {
                setError(TFTPError.ACCESS_VIOLATION);
                setErrorMessage("Access Violation: File not accessible.");
                storeResult(target);
                return false;
            }
        }

        storeResult(target);
        return false;
    }


    /**
     * Verifies if the provided Packet contains a legal data buffer.
     *
     * @param packet The Packet instance
     * @param data The data buffer of the packet
     * @param block the actual block number
     *
     * @return  true    if the data buffer is legal
     *          false   otherwise
     */
    private boolean verifyPacketContents(Packet packet, byte[] data, int block) {
        if (data.length > 516) {
            setError(TFTPError.ILLEGAL_TFTP_OPERATION);
            setErrorMessage("Block greater than 516 bytes");
        } else if (packet == null) {
            setError(TFTPError.ILLEGAL_TFTP_OPERATION);
            setErrorMessage("Undefined OpCode or Packet");
        } else if (mode.equals("reading") && packet.getType() == PacketTypes.DATA) {
            setError(TFTPError.ILLEGAL_TFTP_OPERATION);
            setErrorMessage("Received DATA, Expected ACK");
        } else if (mode.equals("writing") && packet.getType() == PacketTypes.ACK) {
            setError(TFTPError.ILLEGAL_TFTP_OPERATION);
            setErrorMessage("Received ACK, Expected DATA");
        } else if (packet.isBlockNumbered() && BlockNumber.getBlockNumber(packet.getDatagram().getData()) > block) {
            setError(TFTPError.ILLEGAL_TFTP_OPERATION);
            setErrorMessage("Incorrect Block Number");
        } else if (packet.isBlockNumbered() && BlockNumber.getBlockNumber(packet.getDatagram().getData()) < block) {
            setDuplicate(true);
        } else {
            return true;
        }

        return false;
    }


    /**
     * Verifies if the environment set up for the packet allows for consumption (i.e. readable/writable).
     *
     * @param packet The Packet instance, used to divert logic flow accordingly
     *
     * @return  true    if the I/O environment is correctly set up
     *          false   otherwise
     */
    private boolean verifyFileEnvironment(Packet packet) throws AccessViolationException {
        if (packet.getType() == PacketTypes.RRQ || packet.getType() == PacketTypes.WRQ) {
            String[] parameters = extractTransferParameters(packet.getDatagram());
            this.filename = parameters[0];
            String mode = parameters[1];

            if (!mode.equalsIgnoreCase("octet")) {
                setError(TFTPError.ILLEGAL_TFTP_OPERATION);
                setErrorMessage(String.format("Illegal mode for %s", packet.getType()));
                return false;
            }
        }

        if (packet.getType() == PacketTypes.RRQ && !FileTransfer.isFileExisting(filename)) {
            setError(TFTPError.FILE_NOT_FOUND);
            setErrorMessage(String.format("File Not Found: %s", filename));
        } else if (packet.getType() == PacketTypes.DATA && !FileTransfer.isWritable(filename)) {
            setError(TFTPError.ACCESS_VIOLATION);
            setErrorMessage("Access Violation: File not writable.");
        } else if (packet.getType() == PacketTypes.DATA && FileTransfer.getFreeSpace() < packet.getDatagram().getData().length - 4) {
            setError(TFTPError.DISK_FULL);
            setErrorMessage("Disk Full: Not enough memory to write contents.");
        } else if (packet.getType() == PacketTypes.ACK && !FileTransfer.isReadable(filename)) {
            setError(TFTPError.ACCESS_VIOLATION);
            setErrorMessage("Access Violation: File not readable.");
        } else {
            return true;
        }

        return false;
    }


    /**
     * Stores the resulting DatagramPacket as a Packet instance.
     *
     * @param packet The emerging DatagramPacket
     */
    private void storeResult(DatagramPacket packet) {
        if (!isDuplicate()) {
            this.packet = new ERRORPacket(packet, getError(), getMessage().getBytes());
        } else {
            this.packet = toPacket(packet);
        }
    }


    /**
     * Converts the DatagramPacket to the correct subclass of Packet.
     *
     * @param packet The DatagramPacket to be converted to Packet
     *
     * @return The Packet instance. May be null if packet was found to be unknown.
     */
    private Packet toPacket(DatagramPacket packet) {
        switch (Packet.getPacketType(packet)) {
            case RRQ:
                return new RRQPacket(packet);
            case WRQ:
                return new WRQPacket(packet);
            case DATA:
                return new DATAPacket(packet);
            case ACK:
                return new ACKPacket(packet);
            case ERROR:
                return new ERRORPacket(packet);
            default:
                return null;
        }
    }


    /**
     * Resets the instance variables. Invoked for every verify() invocation.
     */
    private void reset() {
        setDuplicate(false);
        setError(TFTPError.NOT_DEFINED);
        setErrorMessage("");
    }
}
