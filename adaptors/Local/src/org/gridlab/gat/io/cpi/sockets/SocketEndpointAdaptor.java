/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.io.cpi.sockets;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.exolab.castor.xml.Marshaller;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.Pipe;
import org.gridlab.gat.io.PipeListener;
import org.gridlab.gat.io.cpi.EndpointCpi;

/**
 * @author rob
 */
@SuppressWarnings("serial")
public class SocketEndpointAdaptor extends EndpointCpi implements Serializable {

    public static String getDescription() {
        return "The Socket Endpoint Adaptor implements the EndPoint object on top of Java sockets.";
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = EndpointCpi
                .getSupportedCapabilities();
        capabilities.put("connect", true);
        capabilities.put("listen", true);
        return capabilities;
    }

    int localPort; // filled in locally

    String localHost;

    int remotePort; // we get this from the advert service

    String remoteHost;

    PipeListener listener;

    boolean localEndpoint = true;

    ServerSocket serverSocket;

    public SocketEndpointAdaptor() {
        super(null);
    }

    /**
     * @param gatContext
     * @throws GATObjectCreationException
     */
    public SocketEndpointAdaptor(GATContext gatContext)
            throws GATObjectCreationException {
        super(gatContext);

        try {
            localHost = GATEngine.getLocalHostName();
            serverSocket = new ServerSocket(0, 0, GATEngine
                    .getLocalHostAddress());
            // bind to
            // any
            // free port
            localPort = serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new GATObjectCreationException("socket endpoint", e);
        }
    }

    public boolean equals(Object object) {
        if (!(object instanceof SocketEndpointAdaptor)) {
            return false;
        }

        SocketEndpointAdaptor other = (SocketEndpointAdaptor) object;

        if (localEndpoint != other.localEndpoint) {
            return false;
        }

        if (localEndpoint) {
            return localHost.equals(other.localHost)
                    && (localPort == other.localPort);
        }

        return remoteHost.equals(other.remoteHost)
                && (remotePort == other.remotePort);
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
            Socket s = new Socket(remoteHost, remotePort);

            return new SocketPipe(gatContext, s);
        } catch (IOException e) {
            throw new GATInvocationException("socketendpoint", e);
        }
    }

    public Pipe listen() throws GATInvocationException {
        return listen(0);
    }

    public Pipe listen(int timeout) throws GATInvocationException {
        if (!localEndpoint) {
            throw new GATInvocationException("cannot listen to local endpoint");
        }

        try {
            serverSocket.setSoTimeout(timeout);
            Socket s = serverSocket.accept();

            return new SocketPipe(gatContext, s);
        } catch (IOException e) {
            throw new GATInvocationException("socketPipe", e);
        }
    }

    public void listen(PipeListener pipeListener) throws GATInvocationException {
        if (!localEndpoint) {
            throw new GATInvocationException("cannot listen to local endpoint");
        }

        throw new UnsupportedOperationException("Not implemented"); // TODO
        // implement
        // listen
    }

    /*
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

    public static Advertisable unmarshal(GATContext context, String s) {
        try {
            SocketEndpointAdaptor res = (SocketEndpointAdaptor) GATEngine
                    .defaultUnmarshal(SocketEndpointAdaptor.class, s);

            res.remotePort = res.localPort;
            res.localPort = -1;

            res.remoteHost = res.localHost;
            res.localEndpoint = false;

            return res;
        } catch (Exception e) {
            throw new Error("could not unmarshal object: " + e);
        }
    }

    public String toString() {
        return "endpoint: localPort = " + localPort + ", localHost = "
                + localHost + ", remotePort = " + remotePort
                + ", remoteHost = " + remoteHost;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

}
