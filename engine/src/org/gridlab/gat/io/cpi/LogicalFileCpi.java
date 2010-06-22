package org.gridlab.gat.io.cpi;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;

/**
 * Capability provider interface to the LogicalFile class.
 * <p>
 * Capability provider wishing to provide the functionality of the LogicalFile
 * class must extend this class and implement all of the abstract methods in
 * this class. Each abstract method in this class mirrors the corresponding
 * method in this LogicalFile class and will be used to implement the
 * corresponding method in the LogicalFile class at runtime.
 */
public abstract class LogicalFileCpi extends MonitorableCpi implements LogicalFile {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = MonitorableCpi.getSupportedCapabilities();
        capabilities.put("addFile", true);
        capabilities.put("addURI", true);
        capabilities.put("removeFile", true);
        capabilities.put("removeURI", true);
        capabilities.put("replicate", true);
        capabilities.put("getURIs", true);
        capabilities.put("getFiles", true);
        return capabilities;
    }
    
    public static Preferences getSupportedPreferences() {
        Preferences preferences = MonitorableCpi.getSupportedPreferences();
        preferences.put("LogicalFile.adaptor.name", "<no default>");
        preferences.put("adaptors.local", "false");

        return preferences;
    }

    protected static Logger logger = LoggerFactory.getLogger(LogicalFileCpi.class);
    
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
    protected LogicalFileCpi(GATContext gatContext, String name, Integer mode)
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
            String s) {
        SerializedLogicalFile f = (SerializedLogicalFile) GATEngine
                .defaultUnmarshal(SerializedLogicalFile.class, s);

        try {
            LogicalFile lf = GAT.createLogicalFile(context, f.getName(),
                    f.getMode());

            for (int i = 0; i < f.getFiles().size(); i++) {
                lf.addURI(new URI(f.getFiles().get(i)));
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
