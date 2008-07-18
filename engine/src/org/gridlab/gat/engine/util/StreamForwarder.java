package org.gridlab.gat.engine.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class StreamForwarder implements Runnable {

    private static final Logger logger = Logger
            .getLogger(StreamForwarder.class);

    private final InputStream in;

    private final OutputStream out;

    private boolean finished = false; // reached eof or got exception.

    private String name;

    public StreamForwarder(InputStream in, OutputStream out) {
        this(in, out, "");
    }

    public StreamForwarder(InputStream in, OutputStream out, String name) {
        this.in = in;
        this.out = out;
        this.name = name;

        Thread thread = new Thread(this);
        thread.setName(name);
        thread.setDaemon(true);
        thread.start();
    }

    public void run() {
        byte[] buffer = new byte[1024];

        try {
            while (true) {
                int read;
                read = in.read(buffer);

                if (read == -1) {
                    synchronized (this) {
                        if (out != null) {
                            out.flush();
                        }

                        // roelof: don't close out, should be done by
                        // the user of the StreamForwarder, because he might
                        // want to write other things to it (for instance if
                        // it's System.out)
                        // out.close();
                        finished = true;

                        notifyAll();

                        return;
                    }
                }
                if (out != null) {
                    out.write(buffer, 0, read);
                    out.flush();
                }
                logger.info("Forwarder '" + name + "' forwarded: "
                        + new String(buffer, 0, read));

                // System.err.println("written: " + new String(buffer, 0,
                // read));

            }
        } catch (IOException e) {
            // IGNORE
        }
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

}
