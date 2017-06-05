package com.tftp.core.protocol;

import java.net.InetSocketAddress;

/**
 * Client defines and builds the logic for a user capable of transferring files to a pre-defined server port
 * through the agreed upon TFTP protocol.
 *
 * Course: Real Time Concurrent Systems
 * Term: Summer 2017
 *
 * @author Josh Campitelli, Ahmed Sakr, Brian Zhang, Ahmed Khattab, Dario Luzuriaga
 * @since June the 5th, 2017.
 */
public class IPAddress {


    public static InetSocketAddress toInetSocketAddress(String ip, int port) {
        return new InetSocketAddress(ip, port);
    }

    public static boolean isValidIP(String ip) {
        return isValidIP(ip, 0);
    }

    private static boolean isValidIP(String ip, int dots) {
        if (ip.isEmpty()) {
            return false;
        } else if (!ip.contains(".")) {
            return dots == 3;
        } else {
            return isValidIP(ip.substring(ip.indexOf(".") + 1), ++dots);
        }
    }
}
