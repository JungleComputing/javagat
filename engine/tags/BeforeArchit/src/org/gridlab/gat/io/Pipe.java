/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.io;

import java.io.InputStream;
import java.io.OutputStream;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * @author rob
 */

public interface Pipe extends Monitorable {
	OutputStream getOutputStream() throws GATInvocationException;

	InputStream getInputStream() throws GATInvocationException;
}