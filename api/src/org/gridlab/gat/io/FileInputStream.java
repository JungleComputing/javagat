/*
 * Created on Sep 23, 2004
 */
package org.gridlab.gat.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.gridlab.gat.GATIOException;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * @author rob
 */
/**
 * A FileInputStream represents a connection to open file, the file may be
 * either remote or local. See {@link java.io.FileInputStream}.
 */
public class FileInputStream extends InputStream implements Monitorable {
    FileInputStreamInterface in;

    /**
     * Do not use this constructor, it is for internal GAT use. Use
     * GAT.create...
     */
    public FileInputStream(FileInputStreamInterface in) {
        this.in = in;
    }

    /**
     * Returns the FileInputStreamInterface object associated with this
     * {@link FileInputStream}. The FileInputStreamInterface offers the same
     * functionality as the {@link FileInputStream} object. The difference
     * between the FileInputStreamInterface object and the FileInputStream
     * object is that the FileInputStreamInterface is an internal GAT object and
     * it will throw {@link GATInvocationException}s upon failures, whereas the
     * FileInputStream object would have default values.
     * <p>
     * 
     * @return the FileInputStreamInterface object
     */
    public org.gridlab.gat.io.FileInputStreamInterface getFileInputStreamInterface() {
        return in;
    }

    /** {@inheritDoc} */
    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException {
        return in.getMeasurement(metric);
    }

    /** {@inheritDoc} */
    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        return in.getMetricDefinitionByName(name);
    }

    /** {@inheritDoc} */
    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        in.addMetricListener(metricListener, metric);
    }

    /** See {@link java.io.FileInputStream#available}. */
    public int available() throws IOException {
        try {
            return in.available();
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** See {@link java.io.FileInputStream#close}. */
    public void close() throws IOException {
        try {
            in.close();
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** See {@link java.lang.Object#equals}. */
    public boolean equals(Object arg0) {
        return in.equals(arg0);
    }

    /** {@inheritDoc} */
    public List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException {
        return in.getMetricDefinitions();
    }

    /** See {@link java.lang.Object#hashCode}. */
    public int hashCode() {
        return in.hashCode();
    }

    /** See {@link java.io.InputStream#mark}. */
    public void mark(int arg0) {
        in.mark(arg0);
    }

    /** See {@link java.io.InputStream#markSupported}. */
    public boolean markSupported() {
        return in.markSupported();
    }

    /** See {@link java.io.FileInputStream#read()}. */
    public int read() throws IOException {
        try {
            return in.read();
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** See {@link java.io.FileInputStream#read(byte[])}. */
    public int read(byte[] arg0) throws IOException {
        try {
            return in.read(arg0);
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** See {@link java.io.FileInputStream#read(byte[], int, int)}. */
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        try {
            return in.read(arg0, arg1, arg2);
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** {@inheritDoc} */
    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        in.removeMetricListener(metricListener, metric);
    }

    /** See {@link java.io.InputStream#reset}. */
    public void reset() throws IOException {
        try {
            in.reset();
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** See {@link java.io.FileInputStream#skip}. */
    public long skip(long arg0) throws IOException {
        try {
            return in.skip(arg0);
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** See {@link java.lang.Object#toString}. */
    public String toString() {
        return in.toString();
    }
}
