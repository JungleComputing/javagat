/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.io;

import java.io.InputStream;
import java.io.OutputStream;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * A Pipe represents a connection to another process.
 * 
 * The real communication is done through the streams that this pipe contains.
 * 
 * @author rob
 */
public interface Pipe extends Monitorable {
    /**
     * Get the output stream connected to this pipe.
     * 
     * @return the output stream connected to this pipe.
     * @throws GATInvocationException
     *                 an IO error occurred
     */
    OutputStream getOutputStream() throws GATInvocationException;

    /**
     * Get the input stream connected to this pipe.
     * 
     * @return the input stream connected to this pipe.
     * @throws GATInvocationException
     *                 an IO error occurred
     */
    InputStream getInputStream() throws GATInvocationException;

    /**
     * Close the connection to the other process.
     * 
     * @throws GATInvocationException
     *                 an IO error occurred
     */
    void close() throws GATInvocationException;
}
