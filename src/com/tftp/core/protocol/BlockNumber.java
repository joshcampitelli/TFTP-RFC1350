package com.tftp.core.protocol;


/**
 * BlockNumber encapsulates the logic between encoding blocknumbers as two's complement (i.e. default in java)
 * and reading them back correctly as unsigned numbers.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Ahmed Sakr, Josh Campitelli, Brian Zhang, Ahmed Khattab, Dario Luzuriaga
 * @since May the 16th, 2017.
 */
public class BlockNumber {


    /**
     * Converts the two byte block number into an unsigned integer.
     *
     * @return Unsigned block number
     */
    public static int getBlockNumber(byte[] block) {
        int blockNumber = Byte.toUnsignedInt(block[3]);
        blockNumber += Byte.toUnsignedInt(block[2]) << 8;

        return blockNumber;
    }


    /**
     * Converts the integer block number into a two-byte two's complement array.
     *
     * @param input The block number
     *
     * @return The block number, as a byte array
     */
    public static byte[] getBlockNumber(int input) {
        byte[] data = new byte[2]; // <- assuming "in" value in 0..65535 range and we can use 2 bytes only

        data[0] = (byte)((input >> 8) & 0xFF);
        data[1] = (byte)(input & 0xFF);

        return data;
    }
}
