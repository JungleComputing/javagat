package org.gridlab.gat.engine.util;

import ibis.util.ThreadPool;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamForwarder implements Runnable {

    private static final Logger logger = LoggerFactory
            .getLogger(StreamForwarder.class);

    private final InputStream in;

    private final OutputStream out;

    private boolean finished = false; // reached eof or got exception.

    private String name;

    public StreamForwarder(InputStream in, OutputStream out) {
        this(in, out, "");
    }

    public StreamForwarder(InputStream in, OutputStream out, String name) {
        this.in = new BufferedInputStream(in);
        this.out = out;
        this.name = name;
        if (logger.isDebugEnabled()) {
            logger.debug(name + ": in = " + in);
        }
        ThreadPool.createNew(this, name);
    }

    public void run() {
        byte[] buffer = new byte[4096];
        
        try {        
            for (;;) {
        	int read;
        	if (logger.isDebugEnabled()) {
        	    logger.debug(name + ": reading from " + in);
        	}
        	read = in.read(buffer);

        	if (read == -1) {
        	    if (logger.isDebugEnabled()) {
        		logger.debug(name + ": StreamForwarder got EOF");
        	    }
        	    if (out != null) {
        		out.flush();
        	    }

        	    // roelof: don't close out, should be done by
        	    // the user of the StreamForwarder, because he might
        	    // want to write other things to it (for instance if
        	    // it's System.out)
        	    // out.close();
        	    return;
        	}
        	if (logger.isTraceEnabled()) {
        	    for (int i = 0; i < read; i++) {
        		logger.trace(name + ": read byte: " + buffer[i] + " ("
        			+ ((char) buffer[i]) + ")");
        	    }
        	}

        	if (out != null) {
        	    out.write(buffer, 0, read);
        	    out.flush();
        	    if (logger.isTraceEnabled()) {
        		logger.trace(name + " forwarded: "
        			+ new String(buffer, 0, read));
        	    }
        	} else {
        	    if (logger.isInfoEnabled()) {
        		logger.info(name + ": forwarding impossible, outputstream closed");
        	    }
        	}
            }

        } catch (IOException e) {
            if (logger.isInfoEnabled()) {
        	logger.info(name + ": caught exception: " + e);
            }
            if (logger.isDebugEnabled()) {
        	StringWriter writer = new StringWriter();
        	e.printStackTrace(new PrintWriter(writer));
        	logger.debug(name + ": stacktrace: \n" + writer.toString());
            }
        } finally {
            synchronized (this) {
                finished = true;
                notifyAll();
            }
            
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
    
    public void close() {
	if (out != null) {
	    try {
		out.close();
	    } catch (Throwable e) {
		// ignored
	    }
	}
    }
}
