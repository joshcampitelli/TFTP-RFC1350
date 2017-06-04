package com.tftp.exceptions;

import java.lang.Exception;

/**
 * InvalidPacketException is a simple Exception that will be used in abnormal cases when a packet being handled
 * is not expected or corrupted.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Ahmed Sakr, Josh Campitelli, Brian Zhang, Ahmed Khattab, Dario Luzuriaga
 * @since May the 2nd, 2017.
 */
@SuppressWarnings("serial")
public class InvalidPacketException extends Exception {

    public InvalidPacketException(String error) {
        super(error);
    }
}
