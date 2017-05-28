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

    @Override
    public void run() {
        PacketTypes pcktyp = PacketTypes.UNKNOWN;
        byte errorSubtype = 0;
        byte errorId = 0;
        String errsubStr;
        int block = -1;

        while (true) {
            // Request the user to enter the type of packet to modify
            System.out.println("There are four types of modifiable packets:");
            System.out.println("\tRRQ (R)");
            System.out.println("\tWRQ (W)");
            System.out.println("\tDATA (D)");
            System.out.println("\tACK (A)");

            String packetType = simulator.getInput("By typing the initial, which one would you like to modify? ");
            while (!packetType.toUpperCase().equals("D") &&
                        !packetType.toUpperCase().equals("A") &&
                        !packetType.toUpperCase().equals("R") &&
                        !packetType.toUpperCase().equals("W")) {
                packetType = simulator.getInput("By typing the initial, which one would you like to modify? ");
            }

            switch (packetType.toUpperCase()) {
                case "R":
                    pcktyp = PacketTypes.RRQ;
                    break;
                case "W":
                    pcktyp = PacketTypes.WRQ;
                    break;
                case "D":
                    pcktyp = PacketTypes.DATA;
                    break;
                case "A":
                    pcktyp = PacketTypes.ACK;
                    break;
            }

            if (pcktyp == PacketTypes.DATA || pcktyp == PacketTypes.ACK) {
                String blockNumber = simulator.getInput("Enter the block number of the packet to modify: ");
                block = Integer.parseInt(blockNumber);
                while (block < 0 || block > 65000) {
                    blockNumber = simulator.getInput("Enter the block number of the packet to modify: ");
                    block = Integer.parseInt(blockNumber);
                }
            }

            // Request the user to enter the error 4 to 5 to produce in the packet
            System.out.println("The Error Simulator supports the following ERROR packets:");
            System.out.println("\tIllegal TFTP Operation (4)");
            System.out.println("\tInvalid TID (5)");
            String errid = simulator.getInput("By typing the numbered index, which one would you like to choose? ");
            int interrid = Integer.parseInt(errid);
            while (interrid != 4 && interrid != 5) {
                errid = simulator.getInput("By typing the numbered index, which one would you like to choose? ");
                interrid = Integer.parseInt(errid);
            }

            // Request the user to enter a sub-type of error
            if (interrid == 5) {
                errorId = TFTPError.UNKNOWN_TRANSFER_ID;
                errorSubtype = ErrorSimulator.SIMULATE_NO_SPECIAL_ERROR;
            } else {
                errorId = TFTPError.ILLEGAL_TFTP_OPERATION;
                // Request a sub-error to enter
                System.out.println("You have chosen Error Packet 4. There are multiple ways to achieve it:");
                System.out.println("\tINVALID_OPCODE (1)");
                System.out.println("\tINVALID_PACKET_SIZE (2)");
                System.out.println("\tINVALID_BLOCK_NUMBER (3)");
                System.out.println("\tINVALID_MODE (4)");

                errsubStr = simulator.getInput("By providing the numbered key, which one would you like to choose? ");
                while (Integer.valueOf(errsubStr) < 1 && Integer.valueOf(errsubStr) > 4) {
                    errsubStr = simulator.getInput("By providing the numbered key, which one would you like to choose? ");
                }

                errorSubtype = Byte.valueOf(errsubStr);
            }

            // Execute the simulator
            System.out.printf("SUCCESS: Queued %s\n", simulator.queueModification(block, pcktyp, errorId, errorSubtype));
        }
    }
}
