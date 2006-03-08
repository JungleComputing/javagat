/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.io.cpi;

import java.net.InetAddress;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.Pipe;
import org.gridlab.gat.io.PipeListener;

/**
 * @author rob
 */
public class SocketEndPointAdaptor extends EndpointCpi {

	// these are filled in locally
	int localPort;

	InetAddress localIP;

	// these two we get from the advert service
	int remotePort;

	InetAddress remoteIP;

	PipeListener listener;

	/**
	 * @param gatContext
	 * @param preferences
	 * @throws AdaptorCreationException
	 */
	public SocketEndPointAdaptor(GATContext gatContext, Preferences preferences)
			throws AdaptorCreationException {
		super(gatContext, preferences);
	}

	public boolean equals(Object object) {
		throw new Error("Not implemented");
	}

	/**
	 * This call can only work when this EndPoint was retrieved through the
	 * advert service
	 */
	public Pipe connect() throws GATInvocationException {
		if (remoteIP == null) {
			throw new GATInvocationException(
					"Trying to connect an endpoint that was not obtained through the advert service");
		}
		throw new Error("Not implemented");
	}

	public Pipe listen() throws GATInvocationException {
		throw new Error("Not implemented");
	}

	public void listen(PipeListener pipeListener) throws GATInvocationException {
		throw new Error("Not implemented");
	}
}