package com.tftp.core.protocol;


/**
 * BlockNumber encapsulates the logic between encoding blocknumbers as two's complement (i.e. default in java)
 * and reading them back correctly as unsigned numbers.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 16th, 2017.
 */
public class BlockNumber {

    /**
     * Converts the two byte block number into an unsigned integer.
     *
     * @return Unsigned block number
     */
    public static int getBlockNumber(byte[] block) {
        int blocknum = 0;
        blocknum += Byte.toUnsignedInt(block[3]);
        blocknum += Byte.toUnsignedInt(block[2]) << 8;

        return blocknum;
    }

    public static byte[] getBlockNumber(int input) {
        byte[] data = new byte[2]; // <- assuming "in" value in 0..65535 range and we can use 2 bytes only

        data[0] = (byte)((input >> 8) & 0xFF);
        data[1] = (byte)(input & 0xFF);

        return data;
    }
}
