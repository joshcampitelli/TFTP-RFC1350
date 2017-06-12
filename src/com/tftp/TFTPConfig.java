package com.tftp;

/**
 * TFTPConfig encloses all the constants that may be used across the client, server,
 * and simulator.
 *
 * @author Ahmed Sakr
 * @since June the 12th, 2017
 */
public class TFTPConfig {

    // the nominal port that the main simulator port listens on
    public static final int SIMULATOR_PORT = 23;

    // the nominal port that the main server port listens on
    public static final int SERVER_PORT = 69;

    // the default verbose setting for the client, not final to allow for mutation
    public static boolean CLIENT_VERBOSE = false;

    // the default verbose setting for the server, not final to allow for mutation
    public static boolean SERVER_VERBOSE = false;
}
