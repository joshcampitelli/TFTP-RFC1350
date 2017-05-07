package io;

import java.lang.AutoCloseable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import exceptions.UnknownIOModeException;

/**
 * FileTransfer acts as a medium to write/read bytes of data while abstracting the I/O jargon.
 * Please note: FileTransfer is intended to be use for one READ or WRITE operation. The class stream
 * will automatically close and terminate once the last block has been reached.
 *
 * FileTransfer determines the last block by reading its length. If its length is less than BLOCK_SIZE,
 * then the last block has been received and the class shall terminate after processing it.
 *
 * @author Ahmed Sakr (SYSC3303 Group 2)
 */
public class FileTransfer {

    public static int READ = 1;
    public static int WRITE = 2;
    public static int BLOCK_SIZE = 512; // 512 bytes

    private String filepath;
    private AutoCloseable stream;

    /**
     * Constructs and preapres the class for all operations.
     *
     * @throws UnknownIOModeException a rogue mode value was provided, which is critical to the operations.
     */
    public FileTransfer(String filepath, int mode) throws FileNotFoundException, UnknownIOModeException {
        this.filepath = filepath;
        initialize(mode);
    }

    /**
     * Reads up to BLOCK_SIZE worth of data from the FileInputStream.
     * Checks if last block was reached and takes appropriate measures if necessary.
     *
     * @return the buffer byte array with the data read.
     */
    public byte[] read() throws IOException {
        byte[] block;
        if (this.stream instanceof FileInputStream) {

            // safe typecast, no need to worry about runtime errors
            FileInputStream reader = (FileInputStream) stream;
            block = new byte[BLOCK_SIZE];

            if (reader.read(block) < BLOCK_SIZE) {
                done();
            }
        }

        return null;
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

    /**
     * Initializes the class by constructing the instance variable ("stream") with the correct subclass.
     * The subclasses are FileInputStream and FileOutputStream. Both of these subclasses implement AutoCloseable.
     *
     * @throws UnknownIOModeException a rogue mode value was provided, which is critical to the operations.
     */
    private void initialize(int mode) throws FileNotFoundException, UnknownIOModeException { 
        if (mode == READ) {
            stream = new FileInputStream(this.filepath);
        } else if (mode == WRITE) {
            stream = new FileOutputStream(this.filepath);
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
