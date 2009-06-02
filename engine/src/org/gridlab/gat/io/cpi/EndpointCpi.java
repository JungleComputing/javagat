package org.gridlab.gat.io.cpi;

import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.io.Endpoint;
import org.gridlab.gat.io.Pipe;
import org.gridlab.gat.io.PipeListener;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;

/**
 * Capability provider interface to the Endpoint class.
 * <p>
 * Capability provider wishing to provide the functionality of the Endpoint class
 * must extend this class and implement all of the abstract methods in this
 * class. Each abstract method in this class mirrors the corresponding method in
 * this EndPoint class and will be used to implement the corresponding method in
 * the EndPoint class at runtime.
 */
public abstract class EndpointCpi extends MonitorableCpi implements Endpoint {
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = MonitorableCpi.getSupportedCapabilities();
        capabilities.put("connect", false);
        capabilities.put("listen", false);
        return capabilities;
    }
    
    protected GATContext gatContext;

    /**
     * Constructs a EndPointCpi instance which corresponds to the physical
     * EndPoint identified by the passed Location and whose access rights are
     * determined by the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this EndPointCpi.
     * @param preferences
     *                the preferences to be associated with this adaptor
     */
    protected EndpointCpi(GATContext gatContext) {
        this.gatContext = gatContext;
    }

    public boolean equals(Object object) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int hashCode() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Pipe connect() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Pipe listen() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Pipe listen(int timeout) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void listen(PipeListener pipeListener) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void listen(PipeListener pipeListener, int timeout)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
