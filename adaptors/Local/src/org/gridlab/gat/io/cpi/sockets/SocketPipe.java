/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.io.cpi.sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.io.cpi.PipeCpi;

/**
 * @author rob
 */
public class SocketPipe extends PipeCpi {
    Socket s;

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = PipeCpi.getSupportedCapabilities();
        capabilities.put("close", true);
        capabilities.put("getInputStream", true);
        capabilities.put("getOutputStream", true);
        return capabilities;
    }
       
    public SocketPipe(GATContext gatContext, Socket s) {
        super(gatContext);
        this.s = s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.Pipe#getInputStream()
     */
    public InputStream getInputStream() throws GATInvocationException {
        try {
            return s.getInputStream();
        } catch (IOException e) {
            throw new GATInvocationException("socketpipe", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.Pipe#getOutputStream()
     */
    public OutputStream getOutputStream() throws GATInvocationException {
        try {
            return s.getOutputStream();
        } catch (IOException e) {
            throw new GATInvocationException("socketpipe", e);
        }
    }

    public void close() throws GATInvocationException {
        try {
            s.close();
        } catch (Exception e) {
            throw new GATInvocationException("socketpipe", e);
        }
    }
}
