package com.tftp.exceptions;

/**
 *
 * @author Carleton Summer 2017 SYSC 3303A Group 2
 * @since May the 5th, 2017
 */
@SuppressWarnings("serial")
public class UnknownIOModeException extends Exception {

    public UnknownIOModeException(String error) {
        super(error);
    }
}
