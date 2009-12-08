package org.gridlab.gat.resources.cpi.globus;

import java.io.IOException;
import java.io.InputStream;

public class WritableInputStream extends InputStream {

    private byte[] queue = new byte[1024 * 1024];

    private int readPos = 0;

    private int writePos = 0;

    private boolean finished = false;

    public synchronized int read() throws IOException {
        while (readPos == writePos) {
            if (finished) {
                return -1;
            } else {
                // System.out.println("waiting: readPos" + readPos
                // + ", writePos: " + writePos);
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        int result = queue[readPos];
        readPos = (readPos + 1) % queue.length;
        notifyAll();
        return result;
    }

    public int read(byte[] buf) throws IOException {
        if (buf == null) {
            throw new NullPointerException("buffer is null");
        }
        int bytesWritten = 0;
        for (int i = 0; i < buf.length; i++) {
            int read = read();
            if (read == -1) {
                break;
            }
            bytesWritten++;
            buf[i] = (byte) read;
        }
        if (bytesWritten == 0) {
            return -1;
        }
        return bytesWritten;
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        if (buf == null) {
            throw new NullPointerException("buffer is null");
        }
        int bytesWritten = 0;
        for (int i = off; i < off + len; i++) {
            int read = read();
            if (read == -1) {
                break;
            }
            bytesWritten++;
            buf[i] = (byte) read;
        }
        if (bytesWritten == 0) {
            return -1;
        }
        return bytesWritten;
    }

    private int freeSpace() {
        return queue.length - available();
    }

    protected synchronized void write(byte[] data, int off, int len) {
        // System.out.println("writing: byte[" + data.length + "], offset: " +
        // off
        // + ", len: " + len);
        // System.out.println("writing: readPos" + readPos + ", writePos: "
        // + writePos);
        while (freeSpace() <= len + 1) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        for (int i = off; i < off + len; i++) {
            queue[writePos] = data[i];
            writePos = (writePos + 1) % queue.length;
        }
        notifyAll();
    }

    protected synchronized void finished() {
        this.finished = true;
        notifyAll();
    }

    public int available() {
        return (writePos - readPos) % queue.length;
    }

    public void close() {
        queue = null;
    }

}
