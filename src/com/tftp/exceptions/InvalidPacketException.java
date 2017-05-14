package com.tftp.exceptions;

import java.lang.Exception;

/**
 * InvalidPacketException is a simple Exception that will be used in abnormal cases when a packet being handled
 * is not expected or corrupted.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 2nd, 2017.
 */
@SuppressWarnings("serial")
public class InvalidPacketException extends Exception {

    public InvalidPacketException(String error) {
        super(error);
    }
}
