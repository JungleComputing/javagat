package org.gridlab.gat.io.cpi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.io.Pipe;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;

public abstract class PipeCpi extends MonitorableCpi implements Pipe {
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = MonitorableCpi.getSupportedCapabilities();
        capabilities.put("close", false);
        capabilities.put("getInputStream", false);
        capabilities.put("getOutputStream", false);
        return capabilities;
    }
    
    protected GATContext gatContext;

    public PipeCpi(GATContext gatContext) {
        this.gatContext = gatContext;
    }

    public void close() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public InputStream getInputStream() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public OutputStream getOutputStream() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
