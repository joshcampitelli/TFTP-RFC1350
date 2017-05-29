package com.tftp.workers;

import com.tftp.core.protocol.Packet.PacketTypes;
import com.tftp.core.protocol.TFTPError;
import com.tftp.simulation.ErrorSimulator;

/**
 * SimulatorListener is an independent thread that listens to the simulator console for modification additions.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 28th, 2017.
 */
public class SimulatorListener extends Thread {

    private ErrorSimulator simulator;

    public SimulatorListener(ErrorSimulator simulator) {
        super("SimulatorListener");
        this.simulator = simulator;
    }

    private PacketTypes getPacketType() {
        System.out.println("There are five types of modifiable packets:");
        System.out.println("\tRRQ (R)");
        System.out.println("\tWRQ (W)");
        System.out.println("\tDATA (D)");
        System.out.println("\tACK (A)");
        System.out.println("\tError (E)");

        String packetType = simulator.getInput("By typing the initial, which one would you like to modify? ");
        while (!packetType.toUpperCase().equals("D") &&
                    !packetType.toUpperCase().equals("A") &&
                    !packetType.toUpperCase().equals("R") &&
                    !packetType.toUpperCase().equals("W") &&
                    !packetType.toUpperCase().equals("E")) {
            packetType = simulator.getInput("By typing the initial, which one would you like to modify? ");
        }

        switch (packetType.toUpperCase()) {
            case "R":
                return PacketTypes.RRQ;
            case "W":
                return PacketTypes.WRQ;
            case "D":
                return PacketTypes.DATA;
            case "A":
                return PacketTypes.ACK;
            case "E":
                return PacketTypes.ERROR;
            default:
                return PacketTypes.UNKNOWN;
        }
    }

    private int getBlockNumber(PacketTypes type) {
        if (type == PacketTypes.DATA || type == PacketTypes.ACK) {
            int block = Integer.parseInt(simulator.getInput("Enter the block number of the packet to modify: "));
            while (block < 0 || block > 65000) {
                block = Integer.parseInt(simulator.getInput("Enter the block number of the packet to modify: "));
            }

            return block;
        }

        return 0;
    }

    private byte getErrorId(PacketTypes type) {
        System.out.println("The Error Simulator supports the following ERROR packets for your chosen packet type:");
        System.out.println("\tIllegal TFTP Operation (4)");
        if (!isInitiatingRequest(type)) {
            System.out.println("\tInvalid TID (5)");
        }

        int id = Integer.parseInt(simulator.getInput("By typing the numbered index, which one would you like to choose? "));
        while (id != 4 && id != 5) {
            id = Integer.parseInt(simulator.getInput("By typing the numbered index, which one would you like to choose? "));
        }

        if (id == 5) {
            return TFTPError.UNKNOWN_TRANSFER_ID;
        } else {
            return TFTPError.ILLEGAL_TFTP_OPERATION;
        }
    }


    private byte getErrorType(PacketTypes type, byte errorId) {
        if (errorId == TFTPError.UNKNOWN_TRANSFER_ID) {
            return ErrorSimulator.SIMULATE_NO_SPECIAL_ERROR;
        } else {
            System.out.println("You have chosen Error Packet 4. There are multiple ways to achieve it:");
            System.out.println("\tINVALID_OPCODE (1)");
            System.out.println("\tINVALID_PACKET_SIZE (2)");
            
            if (isBlockNumberRequest(type)) {
                System.out.println("\tINVALID_BLOCK_NUMBER (3)");
            }
            if (isInitiatingRequest(type)) {
                System.out.println("\tINVALID_MODE (4)");
            }

            String errsubStr = simulator.getInput("By providing the numbered key, which one would you like to choose? ");
            while (Integer.valueOf(errsubStr) < 1 && Integer.valueOf(errsubStr) > 4) {
                errsubStr = simulator.getInput("By providing the numbered key, which one would you like to choose? ");
            }

            return Byte.valueOf(errsubStr);
        }
    }


    private boolean isInitiatingRequest(PacketTypes type) {
        return type == PacketTypes.RRQ || type == PacketTypes.WRQ;
    }

    private boolean isBlockNumberRequest(PacketTypes type) {
        return type == PacketTypes.DATA || type == PacketTypes.ACK;
    }


    @Override
    public void run() {
        while (true) {
            PacketTypes packetType = getPacketType();
            int block = getBlockNumber(packetType);
            byte errorId = getErrorId(packetType);
            byte errorType = getErrorType(packetType, errorId);

            // Queue the modification
            System.out.printf("SUCCESS: Queued %s\n", simulator.queueModification(block, packetType, errorId, errorType));
        }
    }
}
