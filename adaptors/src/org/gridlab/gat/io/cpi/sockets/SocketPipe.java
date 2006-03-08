/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.io.cpi.sockets;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.Pipe;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.Socket;

import java.util.List;

/**
 * @author rob
 */
public class SocketPipe implements Pipe {
    Socket s;

    GATContext gatContext;

    Preferences preferences;

    public SocketPipe(GATContext gatContext, Preferences preferences, Socket s) {
        this.gatContext = gatContext;
        this.preferences = preferences;
        this.s = s;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.Pipe#getInputStream()
     */
    public InputStream getInputStream() throws GATInvocationException {
        try {
            System.err.println("returning: " + s.getInputStream());

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

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.monitoring.Monitorable#addMetricListener(org.gridlab.gat.monitoring.MetricListener,
     *      org.gridlab.gat.monitoring.Metric)
     */
    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        throw new Error("Not implemented");
    }

    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        throw new Error("Not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.monitoring.Monitorable#getMetrics()
     */
    public List getMetricDefinitions() throws GATInvocationException {
        throw new Error("Not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.monitoring.Monitorable#removeMetricListener(org.gridlab.gat.monitoring.MetricListener,
     *      org.gridlab.gat.monitoring.Metric)
     */
    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        throw new Error("Not implemented");
    }

    public MetricValue getMeasurement(Metric metric)
            throws GATInvocationException {
        throw new Error("Not implemented");
    }
}
