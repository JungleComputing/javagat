package org.gridlab.gat.io.cpi;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.LogicalFile;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * Capability provider interface to the LogicalFile class.
 * <p>
 * Capability provider wishing to provide the functionality of the LogicalFile
 * class must extend this class and implement all of the abstract methods in
 * this class. Each abstract method in this class mirrors the corresponding
 * method in this LogicalFile class and will be used to implement the
 * corresponding method in the LogicalFile class at runtime.
 */
public abstract class LogicalFileCpi implements LogicalFile, Monitorable {
    protected GATContext gatContext;

    protected Preferences preferences;

    protected String name;

    protected int mode;

    /**
     * Files in the LogicalFile. elements are URIs.
     */
    protected Vector<URI> files;

    /*
     static {
     // we must tell the gat engine that we can unmarshal logical files.
     GATEngine.registerAdvertisable(LogicalFileCpi.class);
     }
     */

    /**
     * This constructor creates a LogicalFileCpi corresponding to the passed URI
     * instance and uses the passed GATContext to broker resources.
     *
     * @param gatContext
     *            The GATContext used to broker resources
     * @param preferences
     *            the preferences to be associated with this adaptor
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public LogicalFileCpi(GATContext gatContext, Preferences preferences,
            String name, Integer mode) throws GATObjectCreationException {
        this.gatContext = gatContext;
        this.preferences = preferences;
        files = new Vector<URI>();
        this.mode = mode.intValue();

        switch (this.mode) {
        case LogicalFile.CREATE:
            break;

        case LogicalFile.OPEN:
            break;

        case LogicalFile.TRUNCATE:
            break;

        default:
            throw new GATObjectCreationException("illegal mode in Logical file");
        }
    }

    /**
     * Adds the passed File instance to the set of physical files represented by
     * this LogicalFile instance.
     *
     * @param file
     *            A File instance to add to the set of physical files
     *            represented by this LogicalFile instance.
     */
    public void addFile(File file) throws GATInvocationException {
        addURI(file.toGATURI());
    }

    /**
     * Adds the physical file at the passed URI to the set of physical files
     * represented by this LogicalFile instance.
     *
     * @param location
     *            The URI of a physical file to add to the set of physical files
     *            represented by this LogicalFile instance.
     * @throws GATInvocationException
     *             Thrown upon problems accessing the remote instance
     */
    public void addURI(URI location) throws GATInvocationException {
        files.add(location);
    }

    public MetricDefinition getMetricDefinitionByName(String myName)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Removes the passed File instance from the set of physical files
     * represented by this LogicalFile instance.
     *
     * @param file
     *            A File instance to remove from the set of physical files
     *            represented by this LogicalFile instance.
     */
    public void removeFile(File file) throws GATInvocationException {
        removeURI(file.toGATURI());
    }

    /**
     * Removes the physical file at the passed URI from the set of physical
     * files represented by this LogicalFile instance.
     *
     * @param location
     *            The URI of a physical file to remove from the set of physical
     *            files represented by this LogicalFile instance.
     * @throws GATInvocationException
     *             Thrown upon problems accessing the remote instance
     */
    public void removeURI(URI location) throws GATInvocationException {
        files.remove(location);
    }

    /**
     * Replicates the logical file represented by this instance to the physical
     * file specified by the passed URI.
     *
     * @param loc
     *            The URI of the new physical file
     * @throws java.io.IOException
     *             Upon non-remote IO problem
     * @throws GATInvocationException
     *             Thrown upon problems accessing the remote instance
     */
    public void replicate(URI loc) throws IOException, GATInvocationException {
        if (files.size() == 0) {
            throw new IOException("Must have at least one source file");
        }

        URI u = (URI) files.get(0);
        File f = null;

        try {
            f = GAT.createFile(gatContext, preferences, u);
        } catch (Exception e) {
            throw new GATInvocationException("default logical file", e);
        }

        f.copy(loc);
        files.add(loc);
    }

    /**
     * Returns a java.util.List of URI instances each of which is the URI of a
     * physical file represented by this instance.
     *
     * @return The java.util.List of URIs
     */
    public List<URI> getURIs() throws GATInvocationException {
        return new Vector<URI>(files);
    }

    /**
     * Returns a java.util.List of File instances each of which is a File
     * corresponding to a physical file represented by this instance.
     *
     * @return The java.util.List of Files
     */
    public List<File> getFiles() throws GATInvocationException {
        Vector<File> res = new Vector<File>();

        List<URI> uris = getURIs();

        for (int i = 0; i < uris.size(); i++) {
            try {
                File f = GAT.createFile(gatContext, preferences, (URI) uris
                    .get(i));
                res.add(f);
            } catch (Exception e) {
                // Ignore
            }
        }

        return res;
    }

    /**
     * This method adds the passed instance of a MetricListener to the
     * java.util.List of MetricListeners which are notified of MetricEvents by
     * an instance of this class. The passed MetricListener is only notified of
     * MetricEvents which correspond to Metric instance passed to this method.
     *
     * @param metricListener
     *            The MetricListener to notify of MetricEvents
     * @param metric
     *            The Metric corresponding to the MetricEvents for which the
     *            passed MetricListener will be notified
     */
    public void addMetricListener(MetricListener metricListener, Metric metric) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Removes the passed MetricListener from the java.util.List of
     * MetricListeners which are notified of MetricEvents corresponding to the
     * passed Metric instance.
     *
     * @param metricListener
     *            The MetricListener to notify of MetricEvents
     * @param metric
     *            The Metric corresponding to the MetricEvents for which the
     *            passed MetricListener will be notified
     */
    public void removeMetricListener(MetricListener metricListener,
            Metric metric) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method returns a java.util.List of Metric instances. Each Metric
     * instance in this java.util.List is a Metric which can be monitored on
     * this instance.
     *
     * @return An java.util.List of Metric instances. Each Metric instance in
     *         this java.util.List is a Metric which can be monitored on this
     *         instance.
     */
    public List<MetricDefinition> getMetricDefinitions() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MetricValue getMeasurement(Metric metric)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String marshal() {
        SerializedLogicalFile f = new SerializedLogicalFile();
        f.setMode(mode);
        f.setName(name);

        Vector<String> fileStrings = new Vector<String>();

        for (int i = 0; i < files.size(); i++) {
            fileStrings.add(files.get(i).toString());
        }

        f.setFiles(fileStrings);

        return GATEngine.defaultMarshal(f);
    }

    public static Advertisable unmarshal(GATContext context,
            Preferences preferences, String s) {
        SerializedLogicalFile f = (SerializedLogicalFile) GATEngine
            .defaultUnmarshal(SerializedFile.class, s);

        try {
            LogicalFile lf = GAT.createLogicalFile(context, preferences, f
                .getName(), f.getMode());

            for (int i = 0; i < f.getFiles().size(); i++) {
                lf.addURI(new URI((String) f.getFiles().get(i)));
            }

            return lf;
        } catch (Exception e) {
            throw new Error("could not create new GAT object");
        }
    }
}
