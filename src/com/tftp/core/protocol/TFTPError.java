package com.tftp.core.protocol;

/**
 * TFTPError provides final constant values for all different types of errors present in the TFTP.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 28th, 2017.
 */
public class TFTPError {

    public static final byte NOT_DEFINED = 0;
    public static final byte FILE_NOT_FOUND = 1;
    public static final byte ACCESS_VIOLATION = 2;
    public static final byte DISK_FULL = 3;
    public static final byte ILLEGAL_TFTP_OPERATION = 4;
    public static final byte UNKNOWN_TRANSFER_ID = 5;
    public static final byte FILE_EXISTS = 6;
    public static final byte NO_SUCH_USER = 7;
}
