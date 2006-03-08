/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.io.cpi.sockets;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.engine.IPUtils;
import org.gridlab.gat.io.Pipe;
import org.gridlab.gat.io.PipeListener;
import org.gridlab.gat.io.cpi.EndpointCpi;

/**
 * @author rob
 */
public class SocketEndpointAdaptor extends EndpointCpi implements Serializable {

	static {
		// we must tell the gat engine that we can unmarshal endpoints.
		GATEngine.registerAdvertisable(SocketEndpointAdaptor.class);
	}

	int localPort; // filled in locally

	InetAddress localAddress; // filled in locally

	String localIP;

	int remotePort; // we get this from the advert service

	InetAddress remoteAddress; // we get this from the advert service

	PipeListener listener;

	boolean localEndpoint = true;

	ServerSocket serverSocket;

	public SocketEndpointAdaptor() {
		super(null, null);
	}

	/**
	 * @param gatContext
	 * @param preferences
	 * @throws AdaptorCreationException
	 */
	public SocketEndpointAdaptor(GATContext gatContext, Preferences preferences)
			throws AdaptorCreationException {
		super(gatContext, preferences);
		try {
			localAddress = IPUtils.getLocalHostAddress();
			serverSocket = new ServerSocket(0, 0, localAddress); // bind to any
			// free port
			localPort = serverSocket.getLocalPort();
			localIP = localAddress.getHostAddress();
		} catch (IOException e) {
			throw new AdaptorCreationException(e);
		}
	}

	public boolean equals(Object object) {
		if (!(object instanceof SocketEndpointAdaptor))
			return false;

		SocketEndpointAdaptor other = (SocketEndpointAdaptor) object;

		if (localEndpoint != other.localEndpoint) {
			return false;
		}

		if (localEndpoint) {
			return localAddress.equals(other.localAddress)
					&& localPort == other.localPort;
		}
		return remoteAddress.equals(other.remoteAddress)
				&& remotePort == other.remotePort;

	}

	public int hashCode() {
		if (localEndpoint) {
			return localPort;
		}
		return remotePort;

	}

	/**
	 * This call can only work when this EndPoint was retrieved through the
	 * advert service.
	 */
	public Pipe connect() throws GATInvocationException {
		if (localEndpoint) {
			throw new GATInvocationException(
					"Trying to connect an endpoint that was not obtained through the advert service");
		}

		try {
			Socket s = new Socket(remoteAddress, remotePort);
			return new SocketPipe(gatContext, preferences, s);
		} catch (IOException e) {
			throw new GATInvocationException("socketendpoint", e);
		}
	}

	public Pipe listen() throws GATInvocationException {
		if (!localEndpoint) {
			throw new GATInvocationException("cannot listen to local endpoint");
		}

		try {
			Socket s = serverSocket.accept();
			return new SocketPipe(gatContext, preferences, s);
		} catch (IOException e) {
			throw new GATInvocationException("socketPipe", e);
		}
	}

	public void listen(PipeListener pipeListener) throws GATInvocationException {
		if (!localEndpoint) {
			throw new GATInvocationException("cannot listen to local endpoint");
		}
		throw new Error("Not implemented"); //@@@
	}

	/**
	 * @return Returns the localIP.
	 */
	public String getLocalIP() {
		return localIP;
	}

	/**
	 * @param localIP
	 *            The localIP to set.
	 */
	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}

	/**
	 * @return Returns the localPort.
	 */
	public int getLocalPort() {
		return localPort;
	}

	/**
	 * @param localPort
	 *            The localPort to set.
	 */
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.Advertisable#marshal()
	 */
	public String marshal() {
		StringWriter sw = new StringWriter();

		try {
			Marshaller.marshal(this, sw);
		} catch (Exception e) {
			throw new Error("could not marshal object: " + e);
		}

		return sw.toString();
	}

	public static Advertisable unmarshal(String s) {
		StringReader sr = new StringReader(s);
		try {
			SocketEndpointAdaptor res = (SocketEndpointAdaptor) Unmarshaller
					.unmarshal(SocketEndpointAdaptor.class, sr);

			res.remotePort = res.localPort;
			res.localPort = -1;

			res.remoteAddress = InetAddress.getByName(res.localIP);
			res.localIP = null;
			res.localAddress = null;
			res.localIP = null;
			res.localEndpoint = false;
			return res;
		} catch (Exception e) {
			throw new Error("could not unmarshal object: " + e);
		}
	}

	public String toString() {
		return "endpoint: localPort = " + localPort + ", localAddr = "
				+ localAddress + ", remotePort = " + remotePort
				+ ", remoteAddr = " + remoteAddress;
	}
}