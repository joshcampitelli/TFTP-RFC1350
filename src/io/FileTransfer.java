package io;

import java.lang.AutoCloseable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import exceptions.UnknownIOModeException;

public class FileTransfer {

    public static int READ = 1;
    public static int WRITE = 2;
    public static int BLOCK_SIZE = 512; // 512 bytes

    private String filepath;
    private AutoCloseable stream;

    public FileTransfer(String filepath, int mode) throws UnknownIOModeException {
        this.filepath = filepath;
        initialize(mode);
    }

    public byte[] read() {
        byte[] block;
        if (this.stream instanceof FileInputStream) {
            FileInputStream reader = (FileInputStream) stream;
            block = new byte[BLOCK_SIZE];

            try {
                if (reader.read(block) < BLOCK_SIZE) {
                    done();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void write(byte[] b) {
        if (this.stream instanceof FileOutputStream) {
            FileOutputStream writer = (FileOutputStream) stream;
            try {
                writer.write(b);
            } catch(IOException e) {
                e.printStackTrace();
            }

            if (b.length < BLOCK_SIZE) {
                done();
            }
        }
    }

    public boolean isComplete() {
        return stream == null;
    }

    private void initialize(int mode) throws UnknownIOModeException { 
        if (mode == READ) {
            try {
                stream = new FileInputStream(this.filepath);
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (mode == WRITE) {
            try {
                stream = new FileOutputStream(this.filepath);
            } catch(FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            throw new UnknownIOModeException("I/O Mode provided is not recognized!");
        }
    }

    private void done() {
        try {
            stream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        stream = null;
    }

}
