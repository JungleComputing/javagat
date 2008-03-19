package org.gridlab.gat.engine.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class OutputForwarder extends Thread {
    InputStream out;

    OutputStream destination;

    StringBuffer sb;

    boolean finished = false; // reached eof or got exception.

    public OutputForwarder(InputStream out) {
        this(out, false, null);
    }

    /**
     * forward the output to another stream. The destination stream is not
     * buffered, and not closed at the end.
     * 
     * @param out
     *                the input stream to forward
     * @param destination
     *                the destination output stream
     */
    public OutputForwarder(InputStream out, OutputStream destination) {
        this(out, false, destination);
    }

    /** All output is read from the stream, and appended to result. */
    public OutputForwarder(InputStream out, boolean logOutput) {
        this(out, logOutput, null);
    }

    /** All output is read from the stream, and appended to result. */
    private OutputForwarder(InputStream out, boolean logOutput,
            OutputStream destination) {
        super("OutputForwarderThread");

        if (logOutput) {
            sb = new StringBuffer();
        }

        this.destination = destination;

        InputStream buf;

        try {
            buf = new BufferedInputStream(out);
        } catch (Throwable e) {
            sb.append("error piping output: ");
            sb.append(e);

            return;
        }

        this.out = buf;
        setDaemon(true);

        start();
    }

    private void readInputStream() {
        if (out == null) {
            synchronized (this) {
                finished = true;
                notifyAll();

                return;
            }
        }

        int count = 0;

        try {
            while (true) {
                int res = out.read();

                if (res < 0) {
                    synchronized (this) {
                        destination.flush();
                        // roelof: don't close destination, should be done by
                        // the user of the OutputForwarder, because he might
                        // want to write other things to it (for instance if
                        // it's System.out)
                        // destination.close();
                        finished = true;

                        notifyAll();

                        return;
                    }
                }

                if (sb != null) {
                    sb.append((char) res);
                }

                if (destination != null) {
                    count++;
                    destination.write(res);
                }
            }
        } catch (Throwable e) {
            synchronized (this) {
                finished = true;
                notifyAll();

                return;
            }
        } finally {
            try {
                out.close();
            } catch (Throwable e) {
                // ignore
            }
        }
    }

    public void run() {
        // if (out != null) {
        readInputStream();

        // }
    }

    public synchronized StringBuffer getResult() {
        while (!finished) {
            try {
                wait();
            } catch (Throwable t) {
                // Ignore.
            }
        }

        return sb;
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized void waitUntilFinished() {
        while (!finished) {
            try {
                wait();
            } catch (Throwable t) {
                // Ignore.
            }
        }
    }

    protected void finalize() throws Throwable {
        try {
            if (out != null) {
                out.close();
            }
        } catch (Throwable t) {
            // ignore
        }
    }
}
