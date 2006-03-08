/*
 * Created on Apr 20, 2004
 */
package org.gridlab.gat.io;

import java.io.Serializable;

import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * @author rob
 */
public interface EndPoint extends Monitorable, Advertisable, Serializable {
	public Pipe connect() throws GATInvocationException;

	public Pipe listen() throws GATInvocationException;

	public void listen(PipeListener pipeListener) throws GATInvocationException;
}