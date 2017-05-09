package com.tftp.exceptions;

import java.lang.Exception;

/**
 *
 * @author Ahmed Sakr
 * @since May the 2nd, 2017
 */
@SuppressWarnings("serial")
public class InvalidPacketException extends Exception {

    public InvalidPacketException(String error) {
        super(error);
    }
}
