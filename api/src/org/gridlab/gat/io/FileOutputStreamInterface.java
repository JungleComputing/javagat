/*
 * Created on Sep 24, 2004
 */
package org.gridlab.gat.io;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.SingleAdaptorPerProxy;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * This interface is for internal GAT use only. It has to be public for
 * techinical reasons.
 * 
 * All GAT users should use org.gridlab.gat.io.FileInputStream
 * 
 * @author rob
 */
public interface FileOutputStreamInterface extends Monitorable,
        SingleAdaptorPerProxy {
    public void close() throws GATInvocationException;

    public void flush() throws GATInvocationException;

    public void write(byte[] arg0, int arg1, int arg2)
            throws GATInvocationException;

    public void write(byte[] arg0) throws GATInvocationException;

    public void write(int arg0) throws GATInvocationException;
}
