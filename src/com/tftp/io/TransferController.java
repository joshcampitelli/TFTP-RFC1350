package com.tftp.io;

import java.util.ArrayList;

/**
 * TransferController controls and limits the operations on all transfers happening on the server.
 * Very critical in applying locks to files.
 *
 * Methods operating on the data structure must be internally synchronized to guarantee thread-safety.
 *
 * @author Ahmed Sakr, Brian Zhang, Josh Campitelli, Ahmed Khattab, Dario Luzuriaga
 * @since May the 28th, 2017.
 */
public class TransferController {

    private ArrayList<FileTransfer> transfers;

    public TransferController() {
        transfers = new ArrayList<>();
    }


    /**
     * Determines if the file provided is currently locked by polling the transfers collection.
     *
     * This method is THREAD-SAFE.
     *
     * @param file the filename
     *
     * @return the file's lock status
     */
    public synchronized boolean isFileLocked(String file) {
        for (FileTransfer ft : transfers) {
            if (ft.getFileName().equalsIgnoreCase(file) && ft.hasFileLock()) {
                return true;
            }
        }

        return false;
    }


    /**
     * Registers a new FileTransfer with the controller.
     *
     * This method is THREAD-SAFE.
     *
     * @param transfer The FileTransfer object to be registered
     */
    public synchronized void registerTransfer(FileTransfer transfer) {
        transfers.add(transfer);
    }


    /**
     * Deregisters a FileTransfer object from the collection, if it exists.
     *
     * This method is THREAD-SAFE.
     *
     * @param transfer The FileTransfer object to be deregistered
     */
    public synchronized void deregisterTransfer(FileTransfer transfer) {
        transfers.remove(transfer);
    }
}
