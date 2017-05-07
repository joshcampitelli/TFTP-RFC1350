import java.io.*;
 
/**
*
* @author Carleton University Summer 2017 SYSC3303A Group 2
* @since May the 5th, 2017
 * @class FileTransfer.java: It uses Java's byte stream I/O
 * classes to copy a file. It copies the contents of a text file in the root 
 * directory to out.dat (for example in.dat -> out.dat). 
 * Running use: FileTransfer in.dat
 *
 */


public class FileTransfer {
	
	// Define main function (where file[0] is the input file name
    public static void main(String[] file)
    throws FileNotFoundException, IOException
    {
     	
        /*
         * A FileInputStream object is created to read the file
         * as a byte stream. A BufferedInputStream object is wrapped
         * around the FileInputStream, which may increase the
         * efficiency of reading from the stream.
         */
        BufferedInputStream in = 
            new BufferedInputStream(new FileInputStream(file[0])); 
 
        /*
         * A FileOutputStream object is created to write the file
         * as a byte stream. A BufferedOutputStream object is wrapped
         * around the FileOutputStream, which may increase the
         * efficiency of writing to the stream.
         */
        BufferedOutputStream out =
            new BufferedOutputStream(new FileOutputStream("out.dat"));
 
        byte[] data = new byte[512];
        int n;
         
        /* Read the file in 512 byte chunks. */
        while ((n = in.read(data)) != -1) {
            /* 
             * We just read "n" bytes into array data. 
             * Now write them to the output file. 
             */
            out.write(data, 0, n); // write out.dat
        } // end while
        in.close();
        out.close();
    } // end main function
} // end class
