package org.gridlab.gat.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.gridlab.gat.GATIOException;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * A FileOutputStream represents a connection to open file, the file may be
 * either remote or local. See {@link java.io.FileOutputStream}.
 */
public class FileOutputStream extends OutputStream implements Monitorable {
    FileOutputStreamInterface out;

    /**
     * Do not use this constructur, it is for internal GAT use.
     */
    public FileOutputStream(FileOutputStreamInterface out) {
        this.out = out;
    }

    /**
     * Returns the FileOutputStreamInterface object associated with this
     * {@link FileOutputStream}. The FileOutputStreamInterface offers the same
     * functionality as the {@link FileOutputStream} object. The difference
     * between the FileInputStreamInterface object and the FileOutputStream
     * object is that the FileOutputStreamInterface is an internal GAT object
     * and it will throw {@link GATInvocationException}s upon failures, whereas
     * the FileOutputStream object would have default values.
     * <p>
     * 
     * @return the FileOutputStreamInterface object
     */
    public org.gridlab.gat.io.FileOutputStreamInterface getFileOutputStreamInterface() {
        return out;
    }

    /** {@inheritDoc} */
    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        out.addMetricListener(metricListener, metric);
    }

    /** {@inheritDoc} */
    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException {
        return out.getMeasurement(metric);
    }

    /** {@inheritDoc} */
    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        return out.getMetricDefinitionByName(name);
    }

    /** See {@link java.io.OutputStream#close}. */
    public void close() throws IOException {
        try {
            out.close();
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** See {@link java.lang.Object#equals}. */
    public boolean equals(Object arg0) {
        return out.equals(arg0);
    }

    /** See {@link java.io.OutputStream#flush}. */
    public void flush() throws IOException {
        try {
            out.flush();
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** {@inheritDoc} */
    public List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException {
        return out.getMetricDefinitions();
    }

    /** See {@link java.lang.Object#hashCode}. */
    public int hashCode() {
        return out.hashCode();
    }

    /** {@inheritDoc} */
    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        out.removeMetricListener(metricListener, metric);
    }

    /** See {@link java.lang.Object#toString}. */
    public String toString() {
        return out.toString();
    }

    /** See {@link java.io.OutputStream#write(byte[])}. */
    public void write(byte[] arg0) throws IOException {
        try {
            out.write(arg0);
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** See {@link java.io.OutputStream#write(byte[], int, int)}. */
    public void write(byte[] arg0, int arg1, int arg2) throws IOException {
        try {
            out.write(arg0, arg1, arg2);
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /** See {@link java.io.OutputStream#write(int)}. */
    public void write(int arg0) throws IOException {
        try {
            out.write(arg0);
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }
}
