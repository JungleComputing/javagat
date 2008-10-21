package org.gridlab.gat.io.cpi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.LogicalFile;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;
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

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
        capabilities.put("addMetricListener", false);
        capabilities.put("getMetricDefinitionsByName", false);
        capabilities.put("getMetricDefinitions", false);
        capabilities.put("removeMetricListener", false);
        capabilities.put("getMeasurement", false);
        capabilities.put("addFile", true);
        capabilities.put("addURI", true);
        capabilities.put("removeFile", true);
        capabilities.put("removeURI", true);
        capabilities.put("replicate", true);
        capabilities.put("getURIs", true);
        capabilities.put("getFiles", true);
        return capabilities;
    }

    protected static Logger logger = Logger.getLogger(LogicalFileCpi.class);

    protected GATContext gatContext;

    protected String name;

    protected int mode;

    /**
     * Files in the LogicalFile. elements are URIs.
     */
    protected List<URI> files;

    /*
     * static { // we must tell the gat engine that we can unmarshal logical
     * files. GATEngine.registerAdvertisable(LogicalFileCpi.class); }
     */

    /**
     * This constructor creates a LogicalFileCpi corresponding to the passed URI
     * instance and uses the passed GATContext to broker resources.
     * 
     * @param gatContext
     *                The GATContext used to broker resources
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public LogicalFileCpi(GATContext gatContext, String name, Integer mode)
            throws GATObjectCreationException {
        this.gatContext = gatContext;
        files = new Vector<URI>();
        this.mode = mode.intValue();
        this.name = name;

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
     *                A File instance to add to the set of physical files
     *                represented by this LogicalFile instance.
     */
    public void addFile(File file) throws GATInvocationException {
        addURI(file.toGATURI());
    }

    /**
     * Adds the physical file at the passed URI to the set of physical files
     * represented by this LogicalFile instance.
     * 
     * @param location
     *                The URI of a physical file to add to the set of physical
     *                files represented by this LogicalFile instance.
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     */
    public void addURI(URI location) throws GATInvocationException {
        // if (files.contains(location)) {
        // if (logger.isInfoEnabled()) {
        // logger.info("logical file '" + name
        // + "' already contains URI '" + location + "'.");
        // }
        // return;
        // }
        // try {
        // if (GAT.createFile(location).getFileInterface().exists()) {
        files.add(location);
        // } else {
        // throw new GATInvocationException(
        // "The file at URI '"
        // + location
        // + "' doesn't exist. Use replicate to add a new copy to the
        // LogicalFile");
        // }
        // } catch (GATObjectCreationException e) {
        // throw new GATInvocationException("Unable to test file at URI '"
        // + location + "' for existence.", e);
        // }

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
     *                A File instance to remove from the set of physical files
     *                represented by this LogicalFile instance.
     */
    public void removeFile(File file) throws GATInvocationException {
        removeURI(file.toGATURI());
    }

    /**
     * Removes the physical file at the passed URI from the set of physical
     * files represented by this LogicalFile instance.
     * 
     * @param location
     *                The URI of a physical file to remove from the set of
     *                physical files represented by this LogicalFile instance.
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     */
    public void removeURI(URI location) throws GATInvocationException {
        files.remove(location);
    }

    /**
     * Replicates the logical file represented by this instance to the physical
     * file specified by the passed URI.
     * 
     * @param loc
     *                The URI of the new physical file
     * @throws java.io.IOException
     *                 Upon non-remote IO problem
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     */
    public void replicate(URI loc) throws IOException, GATInvocationException {
        if (files.size() == 0) {
            throw new IOException("Must have at least one source file");
        }
        if (files.contains(loc)) {
            if (logger.isInfoEnabled()) {
                logger.info("logical file '" + name
                        + "' already contains URI '" + loc + "'.");
            }
            return;
        }
        // try {
        // if (GAT.createFile(loc).getFileInterface().exists()) {
        // throw new GATInvocationException(
        // "Unable to replicate logical file '" + name
        // + "' to location '" + loc
        // + "': target already exists.");
        // }
        // } catch (GATObjectCreationException e) {
        // throw new GATInvocationException(
        // "Unable to replicate logical file '" + name
        // + "' to location '" + loc
        // + "': target cannot be checked for existence.", e);
        // }
        GATInvocationException exception = new GATInvocationException(
                "default logical file");
        for (URI u : getOrderedURIs(loc)) {
            FileInterface f = null;

            try {
                f = GAT.createFile(gatContext, u).getFileInterface();
                f.copy(loc);
            } catch (Exception e) {
                exception.add("default logical file", e);
                continue;
            }

            files.add(loc);
            return;
        }
        throw exception;
    }

    public URI getClosestURI(URI loc) throws GATInvocationException {
        if (files == null || files.size() == 0) {
            throw new GATInvocationException("No files in logical file '"
                    + name + "' to compare with");
        }
        return files.get(0);
    }

    public List<URI> getOrderedURIs(URI location) throws GATInvocationException {
        if (files == null || files.size() == 0) {
            throw new GATInvocationException("No files in logical file '"
                    + name + "' to order");
        }
        List<URI> result = new Vector<URI>();
        synchronized (files) {
            for (URI uri : files) {
                result.add(uri);
            }
        }
        return result;
    }

    /**
     * Returns a java.util.List of URI instances each of which is the URI of a
     * physical file represented by this instance.
     * 
     * @return The java.util.List of URIs
     */
    public List<URI> getURIs() throws GATInvocationException {
        return files;
    }

    /**
     * Returns a java.util.List of File instances each of which is a File
     * corresponding to a physical file represented by this instance.
     * 
     * @return The java.util.List of Files
     */
    public List<File> getFiles() throws GATInvocationException {
        List<File> res = new Vector<File>();

        synchronized (files) {
            for (URI uri : files) {
                try {
                    File f = GAT.createFile(gatContext, uri);
                    res.add(f);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        return res;
    }

    public String getName() throws GATInvocationException {
        return name;
    }

    /**
     * This method adds the passed instance of a MetricListener to the
     * java.util.List of MetricListeners which are notified of MetricEvents by
     * an instance of this class. The passed MetricListener is only notified of
     * MetricEvents which correspond to Metric instance passed to this method.
     * 
     * @param metricListener
     *                The MetricListener to notify of MetricEvents
     * @param metric
     *                The Metric corresponding to the MetricEvents for which the
     *                passed MetricListener will be notified
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
     *                The MetricListener to notify of MetricEvents
     * @param metric
     *                The Metric corresponding to the MetricEvents for which the
     *                passed MetricListener will be notified
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

    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String marshal() {
        SerializedLogicalFile f = new SerializedLogicalFile();
        f.setMode(mode);
        f.setName(name);

        Vector<String> fileStrings = new Vector<String>();

        synchronized (files) {
            for (int i = 0; i < files.size(); i++) {
                fileStrings.add(files.get(i).toString());
            }
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

    public String toString() {
        return name;
    }
}
