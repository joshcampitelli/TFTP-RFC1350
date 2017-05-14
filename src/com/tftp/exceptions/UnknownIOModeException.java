package com.tftp.exceptions;

/**
 * UnknownIOModeException is needed for FileTransfer to inform the outside world that a critical error
 * has been encountered.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 5th, 2017.
 */
@SuppressWarnings("serial")
public class UnknownIOModeException extends Exception {

    public UnknownIOModeException(String error) {
        super(error);
    }
}
