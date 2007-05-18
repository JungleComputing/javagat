package org.gridlab.gat.io.cpi.gt4;

import java.util.List;
import java.io.IOException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.FileInputStreamInterface;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;

public class GT4FileInputStreamAdaptor extends FileInputStreamCpi {
    /**
     * Constructs a FileInputStream Cpi instance which corresponds to the
     * physical file identified by the passed Location and whose access rights
     * are determined by the passed GATContext.
     *
     * @param location
     *            A Location which represents the URI corresponding to the
     *            physical file.
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this FileCpi.
     * @param preferences
     *            the preferences to be associated with this adaptor
     */
    protected GT4FileInputStreamAdaptor(GATContext gatContext,
            Preferences preferences, URI location) throws IOException, AdaptorNotApplicableException  {
        super(gatContext, preferences, location);
	throw new AdaptorNotApplicableException("to have not been implemented yet");
    }

    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.monitoring.Monitorable#getMetrics()
     */
    public List getMetricDefinitions() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MetricValue getMeasurement(Metric metric)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int available() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void close() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void mark(int readlimit) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean markSupported() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int read() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int read(byte[] b, int off, int len) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int read(byte[] b) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void reset() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public long skip(long n) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
