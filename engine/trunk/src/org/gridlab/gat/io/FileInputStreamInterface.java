package org.gridlab.gat.io;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * This interface is for internal GAT use only. It has to be public for
 * techinical reasons.
 *
 * All GAT users should use org.gridlab.gat.io.FileInputStream
 *
 * @author rob
 */
public interface FileInputStreamInterface extends Monitorable {
    int available() throws GATInvocationException;

    void close() throws GATInvocationException;

    void mark(int readlimit);

    boolean markSupported();

    abstract int read() throws GATInvocationException;

    int read(byte[] b) throws GATInvocationException;

    int read(byte[] b, int off, int len) throws GATInvocationException;

    void reset() throws GATInvocationException;

    long skip(long n) throws GATInvocationException;
}
