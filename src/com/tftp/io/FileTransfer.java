package com.tftp.io;

import java.lang.AutoCloseable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.tftp.exceptions.UnknownIOModeException;

/**
 * FileTransfer acts as a medium to write/read bytes of data while abstracting the I/O jargon.
 * Please note: FileTransfer is intended to be use for one READ or WRITE operation. The class stream
 * will automatically close and terminate once the last block has been reached.
 *
 * FileTransfer determines the last block by reading its length. If its length is less than BLOCK_SIZE,
 * then the last block has been received and the class shall terminate after processing it.
 *
 * @author Josh Campitelli, Ahmed Khattab, Dario Luzuriaga, Ahmed Sakr, and Brian Zhang
 * @since May the 6th, 2017.
 */
public class FileTransfer {

    public static int READ = 1;
    public static int WRITE = 2;
    public static int BLOCK_SIZE = 512; // 512 bytes
    public static String SERVER_DIRECTORY = "./data/server/";
    public static String CLIENT_DIRECTORY = "./data/client/";

    private File file;
    private AutoCloseable stream;
    private int lastBlockSize;

    /**
     * Constructs and preapres the class for all operations.
     *
     * @throws UnknownIOModeException a rogue mode value was provided, which is critical to the operations.
     */
    public FileTransfer(String filepath, int mode) throws FileNotFoundException, UnknownIOModeException {
        this.file = new File(filepath);
        initialize(mode);
    }

    /**
     * Reads up to BLOCK_SIZE worth of data from the FileInputStream.
     * Checks if last block was reached and takes appropriate measures if necessary.
     *
     * @return the buffer byte array with the data read.
     */
    public byte[] read() throws IOException {
        byte[] block = null;
        if (this.stream instanceof FileInputStream) {

            // safe typecast, no need to worry about runtime errors
            FileInputStream reader = (FileInputStream) stream;
            block = new byte[BLOCK_SIZE];
            lastBlockSize = reader.read(block);

            // if nothing was read, lastBlockSize has to be set to 0
            if (lastBlockSize == -1) {
                lastBlockSize = 0;
            }

            if (lastBlockSize < BLOCK_SIZE) {
                done();
            }
        }

        return block;
    }


    /**
     * Writes up to 512 bytes of data to the FileOutputStream.
     * Checks if last block was reached and takes appropriate measures if necessary.
     */
    public void write(byte[] b) throws IOException {
        // TODO: abnormal request: writing more than 512 bytes at a time: should it be critical?
        if (b.length > BLOCK_SIZE) {
            return;
        }

        if (this.stream instanceof FileOutputStream) {

            // safe typecast, no need to worry about runtime errors
            FileOutputStream writer = (FileOutputStream) stream;
            writer.write(b);
            if (b.length < BLOCK_SIZE) {
                done();
            }
        }
    }


    /**
     * Checks if the stream is still open. A stream will only remain open as long as the last block
     * is yet to be detected.
     */
    public boolean isComplete() {
        return stream == null;
    }

    public int lastBlockSize() {
        return lastBlockSize;
    }

    /**
     * Initializes the class by constructing the instance variable ("stream") with the correct subclass.
     * The subclasses are FileInputStream and FileOutputStream. Both of these subclasses implement AutoCloseable.
     *
     * @throws UnknownIOModeException a rogue mode value was provided, which is critical to the operations.
     */
    private void initialize(int mode) throws FileNotFoundException, UnknownIOModeException {
        if (mode == READ) {
            stream = new FileInputStream(this.file.getAbsolutePath());
        } else if (mode == WRITE) {
            stream = new FileOutputStream(this.file.getAbsolutePath());
        } else {
            throw new UnknownIOModeException("I/O Mode provided is not recognized!");
        }
    }

    /**
     * Internally called when the last data block has been detected. All operations shall cease.
     */
    private void done(){
        try {
            stream.close();
            stream = null;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
