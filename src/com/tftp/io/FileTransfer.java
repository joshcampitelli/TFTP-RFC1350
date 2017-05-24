package com.tftp.io;

import java.lang.AutoCloseable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

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
    public static String SERVER_DIRECTORY = "/data/server/";
    public static String CLIENT_DIRECTORY = "/data/client/";
    public static String parentDirectory;
    private File file;
    private AutoCloseable stream;
    private int lastBlockSize;

    /**
     * Constructs and preapres the class for all operations.
     *
     * @throws UnknownIOModeException a rogue mode value was provided, which is critical to the operations.
     */
    public FileTransfer(String file, int mode) throws FileNotFoundException, UnknownIOModeException {
        if (mode == WRITE && isFileExisting(file)) {
            file = getAdjustedFilename(file);
        }

        this.file = new File(parentDirectory + "/" + file);
        initialize(mode);
    }


    /**
     * Sets the parent directory of FileTransfer.
     *
     * @param path the path provided, may be absolute or relative.
     * @param relative specifies if the path is relative to the current parent directory or not.
     */
    public static void setStartingDirectory(String path, boolean relative) {
        if (relative) {
            parentDirectory = new File(new File("").getAbsolutePath() + path).getAbsolutePath();
        } else {
            parentDirectory = new File(path).getAbsolutePath();
        }
    }


    /**
     * Sets up the FileTransfer for this process to be working under a specific directory.
     * Prompts and informs the operator if they wish to change the directory.
     *
     * @param directory the default directory to be setup before informing the operator
     */
    public static void setup(String directory) {
        setStartingDirectory(directory, true);
        Scanner scanner = new Scanner(System.in);

        System.out.println("The starting directory for transfering files:");
        System.out.printf("\t%s\n\n", parentDirectory);
        System.out.printf("If you wish to keep it, hit enter. Otherwise, Please specify the absolute starting directory path: ");

        String parentDirectory = scanner.nextLine();
        if (!parentDirectory.isEmpty()) {
            setStartingDirectory(parentDirectory, false);
        }
    }


    /**
     * Checks if the parent directory is writable.
     *
     * @return the write privileges of the parent directory
     */
    public static boolean isWritable() {
        try {
            FileTransfer ft = new FileTransfer("test access.txt", FileTransfer.WRITE);
            ft.delete();
            return true;
        } catch (IOException | UnknownIOModeException ex) {
            return false;
        }
    }


    /**
     * Checks if the parent directory if readable.
     *
     * @return the read privileges of the parent directory
     */
    public static boolean isReadable() {
        try {
            FileTransfer ft = new FileTransfer("test access.txt", FileTransfer.READ);
            ft.delete();
            return true;
        } catch (IOException | UnknownIOModeException ex) {
            return false;
        }
    }

    /**
     * Checks if the specified file is already existing.
     *
     * @param file the relative path to the file
     *
     * @return  true    if the file exists
     *          false   otherwise
     */
    public static boolean isFileExisting(String file) {
        File f = new File(parentDirectory + "\\" + file);
        return f.exists() && !f.isDirectory();
    }


    /**
     * Retrieves the total allocatable bytes in the parent directory.
     *
     * @return the amount of allocatable bytes as a long primitive type
     */
    public static long getFreeSpace() {
        return new File(parentDirectory).getFreeSpace();
    }


    /**
    * Attempts to delete the file passed on construction of this instance.
    * WARNING: invoking delete() will discontinue you from accessing this instance normally as it invokes
    * the done() method.
    *
    * @return  if the deletion completed successfully.
    */
    public boolean delete() {
        done();
        return file.delete();
    }


    /**
    * Dynamically adjusts the filename to the next available name that does not exist.
    * e.g. if test.txt exists, this function returns test (1).txt.
    *      if test.txt and test (1).txt exist, this function returns test (2).txt.
    * @param filename the file path
    *
    * @return the adjusted, non-existing filename
    */
    private String getAdjustedFilename(String filename) {
        int i = 0;
        String adjusted = filename;
        while (isFileExisting(adjusted)) {
            int dot = filename.indexOf(".");
            i++;

            adjusted = String.format("%s (%d).%s", filename.substring(0, dot), i, filename.substring(dot + 1));
        }

        return adjusted;
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
