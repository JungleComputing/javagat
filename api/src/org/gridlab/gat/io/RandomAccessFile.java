/*
 * Created on May 16, 2007 by rob
 */
package org.gridlab.gat.io;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.gridlab.gat.GATIOException;
import org.gridlab.gat.URI;

/**
 * This class is used for random access to local and remote files.
 * <p>
 * An instance of this class presents an abstract, system-independent view of a
 * physical file.
 */
public class RandomAccessFile extends java.io.RandomAccessFile {

    private RandomAccessFileInterface f;

    public RandomAccessFile(RandomAccessFileInterface f)
            throws FileNotFoundException {
        super(getDummyFile(), "rw");
        this.f = f;
    }

    private static java.io.File getDummyFile() {
        try {
            java.io.File tmp = java.io.File.createTempFile("JavaGATDummy",
                    "tmp");
            return tmp;
        } catch (IOException e) {
            throw new Error("could not create dummy file: " + e);
        }
    }

    /**
     * @see java.io.File#toURI()
     */
    public URI toURI() {
        return f.toURI();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#close()
     */
    public void close() throws IOException {
        try {
            f.close();
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#getFilePointer()
     */
    public long getFilePointer() throws IOException {
        try {
            return f.getFilePointer();
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#length()
     */
    public long length() throws IOException {
        try {
            return f.length();
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#read()
     */
    public int read() throws IOException {
        try {
            return f.read();
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            return f.read(b, off, len);
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        try {
            return f.read(b);
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#seek(long)
     */
    public void seek(long pos) throws IOException {
        try {
            f.seek(pos);
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#setLength(long)
     */
    public void setLength(long newLength) throws IOException {
        try {
            f.setLength(newLength);
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#skipBytes(int)
     */
    public int skipBytes(int n) throws IOException {
        try {
            return f.skipBytes(n);
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            f.write(b, off, len);
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#write(byte[])
     */
    public void write(byte[] b) throws IOException {
        try {
            f.write(b);
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.RandomAccessFile#write(int)
     */
    public void write(int b) throws IOException {
        try {
            f.write(b);
        } catch (Exception e) {
            throw new GATIOException(e);
        }
    }
}
