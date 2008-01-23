package org.gridlab.gat.io.cpi;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
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
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;

/**
 * Capability provider interface to the File class.
 * <p>
 * Capability provider wishing to provide the functionality of the File class
 * must extend this class and implement all of the abstract methods in this
 * class. Each abstract method in this class mirrors the corresponding method in
 * this File class and will be used to implement the corresponding method in the
 * File class at runtime.
 */
public abstract class FileCpi implements FileInterface {
    protected static Logger logger = Logger.getLogger(FileCpi.class);

    protected GATContext gatContext;

    protected Preferences preferences;

    protected URI location;

    /**
     * Constructs a FileCpi instance which corresponds to the physical file
     * identified by the passed Location and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param location
     *                A Location which represents the URI corresponding to the
     *                physical file.
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this FileCpi.
     * @param preferences
     *                the preferences to be associated with this adaptor
     */
    protected FileCpi(GATContext gatContext, Preferences preferences,
            URI location) {
        this.gatContext = gatContext;
        this.preferences = preferences;
        this.location = location;

        if (logger.isDebugEnabled()) {
            logger.debug("FileCpi: created file with URI " + location);
        }
    }

    /**
     * Tests this File for equality with the passed Object.
     * <p>
     * If the given object is not a File, then this method immediately returns
     * false.
     * <p>
     * If the given object is a File, then it is deemed equal to this instance
     * if a URI object constructed from this File's location and a URI object
     * constructed from the passed File's URI are equal as determined by the
     * Equals method of URI.
     * 
     * @param object
     *                The Object to test for equality
     * @return A boolean indicating equality
     */
    public final boolean equals(Object object) {
        if (!(object instanceof org.gridlab.gat.io.File)) {
            return false;
        }

        org.gridlab.gat.io.File file = (org.gridlab.gat.io.File) object;

        return location.equals(file.toGATURI());
    }

    /**
     * This method returns the URI of this File
     * 
     * @return The URI of this File
     */
    public final URI toURI() {
        return location;
    }

    /**
     * This method moves the physical file represented by this File instance to
     * a physical file identified by the passed URI.
     * 
     * @param destination
     *                The URI to which to move the physical file corresponding
     *                to this File instance
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws java.io.IOException
     *                 Upon non-remote IO problem
     */
    public void move(URI destination) throws GATInvocationException {
        // Step 1: Copy the original file
        copy(destination);

        // Step 2: Delete the original file
        // We create a new File object for this, as it might need a different
        // adaptor.
        try {
            FileInterface f = GAT.createFile(gatContext, preferences, location)
                    .getFileInterface();
            f.delete();
        } catch (Exception e) {
            throw new GATInvocationException("delete failed", e);
        }

        // This is not correct: files are immutable. --Rob
        // Step 3: Update location
        // this.location = destination;
    }

    /**
     * This method deletes a directory and everything that is in it. This method
     * can only be called on a directory, not on a file.
     * 
     * @throws GATInvocationException
     */
    public void recursivelyDeleteDirectory() throws GATInvocationException {
        recursiveDeleteDirectory(gatContext, preferences, location);
    }

    public void copy(URI loc) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public MetricValue getMeasurement(Metric metric)
            throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean canRead() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean canWrite() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int compareTo(org.gridlab.gat.io.File other) {
        return location.compareTo(other.toGATURI());
    }

    public int compareTo(Object other) {
        if (other instanceof FileCpi) {
            return location.compareTo(((FileCpi) other).location);
        } else {
            throw new Error("illegal compareTo operation");
        }
    }

    public boolean createNewFile() throws GATInvocationException {
        // try {
        // OutputStream o = GAT.createFileOutputStream(gatContext, preferences,
        // location);
        // o.close();
        // return true;
        // } catch (Exception e) {
        // throw new GATInvocationException("file cpi", e);
        // }
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean delete() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void deleteOnExit() {
        Runtime.getRuntime().addShutdownHook(new DeleteHook(this));
    }

    public boolean exists() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public File getAbsoluteFile() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getAbsolutePath() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public File getCanonicalFile() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getCanonicalPath() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public final String getName() {
        String path = getPath();

        return new java.io.File(path).getName();
    }

    public String getParent() throws GATInvocationException {
        String path = getPath();

        int pos = path.lastIndexOf("/");
        if (pos == -1) {
            // no slash
            return null;
        }

        String res = path.substring(0, pos);

        if (logger.isDebugEnabled()) {
            logger.debug("GET PARENT: orig = " + path + " parent = " + res);
        }

        return res;
    }

    public File getParentFile() throws GATInvocationException {
        if (getParent() == null) {
            return null;
        }
        try {
            String dest = location.getScheme() + "://";
            dest += (location.getUserInfo() == null) ? "" : location
                    .getUserInfo();
            dest += location.getHost();
            dest += (location.getPort() == -1) ? ""
                    : (":" + location.getPort());
            dest += "/";
            dest += getParent();

            if (logger.isDebugEnabled()) {
                logger.debug("GET PARENTFILE: orig = " + location + " new = "
                        + dest);
            }

            return GAT.createFile(gatContext, preferences, new URI(dest));
        } catch (Exception e) {
            throw new GATInvocationException("file cpi", e);
        }
    }

    public final String getPath() {
        String res = location.getPath();
        if (res == null) {
            throw new Error("path not specified correctly in URI: " + location);
        }
        return res;
    }

    public final int hashCode() {
        return location.hashCode();
    }

    public final boolean isAbsolute() {
        return new java.io.File(location.getPath()).isAbsolute();
    }

    public boolean isDirectory() throws GATInvocationException {
        if (location.getPath().endsWith(File.separator)) {
            return true;
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isFile() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isHidden() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public long lastModified() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public long length() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * The Adaptor writer MUST implement either list() or listFiles(). The rest
     * is built on top of that.
     */
    public String[] list() throws GATInvocationException {
        if (!isDirectory()) {
            return null;
        }

        File[] f = listFiles();
        String[] res = new String[f.length];

        for (int i = 0; i < f.length; i++) {
            res[i] = f[i].getPath();
        }

        return res;
    }

    /**
     * The Adaptor writer MUST implement either list() or listFiles(). The rest
     * is built on top of that.
     */
    public File[] listFiles() throws GATInvocationException {
        if (!isDirectory()) {
            return null;
        }

        try {
            String[] f = list();
            File[] res = new File[f.length];

            for (int i = 0; i < f.length; i++) {
                String uri = location.toString();

                if (!uri.endsWith("/")) {
                    uri += "/";
                }

                uri += f[i];
                res[i] = GAT.createFile(gatContext, preferences, new URI(uri));
            }

            return res;
        } catch (Exception e) {
            throw new GATInvocationException("file cpi", e);
        }
    }

    public String[] list(FilenameFilter filter) throws GATInvocationException {
        if (!isDirectory()) {
            return null;
        }

        try {
            String[] l = list();

            if (filter == null) {
                return l;
            }

            Vector<String> v = new Vector<String>();

            for (int i = 0; i < l.length; i++) {
                if (filter.accept(GAT.createFile(gatContext, preferences,
                        new URI(location.getPath())), l[i])) {
                    v.add(l[i]);
                }
            }

            String[] res = new String[v.size()];

            for (int i = 0; i < res.length; i++) {
                res[i] = (String) v.get(i);
            }

            return res;
        } catch (Throwable t) {
            throw new GATInvocationException("file cpi", t);
        }
    }

    public File[] listFiles(FileFilter filter) throws GATInvocationException {
        if (!isDirectory()) {
            return null;
        }

        try {
            File[] l = listFiles();

            if (filter == null) {
                return l;
            }

            Vector<File> v = new Vector<File>();

            for (int i = 0; i < l.length; i++) {
                if (filter.accept(GAT.createFile(gatContext, preferences,
                        new URI(l[i].getPath())))) {
                    v.add(l[i]);
                }
            }

            File[] res = new File[v.size()];

            for (int i = 0; i < res.length; i++) {
                res[i] = (File) v.get(i);
            }

            return res;
        } catch (Throwable t) {
            throw new GATInvocationException("file cpi", t);
        }
    }

    public File[] listFiles(FilenameFilter filter)
            throws GATInvocationException {
        if (!isDirectory()) {
            return null;
        }

        try {
            File[] l = listFiles();

            if (filter == null) {
                return l;
            }

            Vector<File> v = new Vector<File>();

            for (int i = 0; i < l.length; i++) {
                if (filter.accept(GAT.createFile(gatContext, preferences,
                        new URI(location.getPath())), l[i].getPath())) {
                    v.add(l[i]);
                }
            }

            File[] res = new File[v.size()];

            for (int i = 0; i < res.length; i++) {
                res[i] = (File) v.get(i);
            }

            return res;
        } catch (Throwable t) {
            throw new GATInvocationException("file cpi", t);
        }
    }

    public boolean mkdir() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean mkdirs() throws GATInvocationException {
        if (exists()) {
            return false;
        }
        if (mkdir()) {
            return true;
        }

        File canonFile = null;
        try {
            canonFile = getCanonicalFile();
        } catch (Exception e) {
            return false;
        }

        File parent = (File) canonFile.getParentFile();

        if (parent == null) {
            return false;
        }

        return parent.mkdirs() && canonFile.mkdir();
    }

    public boolean renameTo(File arg0) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean setLastModified(long arg0) throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean setReadOnly() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public final String toString() {
        return "GAT file: " + toURI().toString();
    }

    public final URL toURL() throws MalformedURLException {
        return location.toURL();
    }

    protected static URI fixURI(URI in, String destScheme) {
        try {
            String scheme = in.getScheme();
            String s = in.toString();

            if (scheme == null) {
                // three slashes because of empty hostname
                return new URI(destScheme + ":///" + s);
            } else if (scheme.equals(destScheme)) {
                return in;
            } else {
                int index = s.indexOf(':');

                return new URI(destScheme + ":"
                        + s.substring(index + 1, s.length()));
            }
        } catch (URISyntaxException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("internal UnsupportedOperationException: " + e);
            }
            return in;
        }
    }

    protected static void copyDirectory(GATContext gatContext,
            Preferences preferences, URI dirURI, URI dest)
            throws GATInvocationException {
        if (logger.isDebugEnabled()) {
            logger.debug("copyDirectory");
        }
        FileInterface destFile = null;
        boolean existingFile = false;
        try {
            destFile = GAT.createFile(gatContext, preferences, dest)
                    .getFileInterface();
            if (destFile.exists() && !destFile.isDirectory()) {
                existingFile = true;
            }
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("file cpi", e);
        } catch (GATInvocationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("copyDirectory not able to check if '"
                        + dest.toString() + "' is existing file");
            }
        }
        if (existingFile) {
            throw new GATInvocationException("cannot overwrite non-directory '"
                    + dest.toString() + "' with directory '"
                    + dirURI.toString() + "'!");
        }

        String sourcePath = dirURI.getPath();
        if (sourcePath.endsWith(File.separator)) {
            sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
        }
        if (sourcePath.length() > 0) {
            int start = sourcePath.lastIndexOf(File.separator) + 1;
            String separator = "";
            if (!dest.toString().endsWith(File.separator)) {
                separator = File.separator;
            }
            try {
                dest = new URI(dest.toString() + separator
                        + sourcePath.substring(start));
            } catch (URISyntaxException e) {
                // should not happen
            }
        }
        // create destination dir
        try {
            if (preferences.containsKey("file.create")) {
                if (((String) preferences.get("file.create"))
                        .equalsIgnoreCase("true")) {
                    destFile = GAT.createFile(gatContext, preferences, dest)
                            .getFileInterface();
                    destFile.mkdirs();
                }
            }
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("file cpi", e);
        }
        FileInterface dir = null;

        try {
            dir = GAT.createFile(gatContext, preferences, dirURI)
                    .getFileInterface();
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("file cpi", e);
        }
        // list all the files and copy recursively.
        File[] files = (File[]) dir.listFiles();
        if (files == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("copyDirectory: no files in src directory");
            }
            return;
        }
        for (int i = 0; i < files.length; i++) {
            FileInterface f = files[i].getFileInterface();

            if (logger.isDebugEnabled()) {
                logger.debug("copyDirectory: file to copy = " + f);
            }

            URI newDest = null;

            try {
                String newDestString = dest.toString();
                newDestString += ("/" + f.getName());
                newDest = new URI(newDestString);
            } catch (URISyntaxException e) {
                throw new GATInvocationException("file cpi", e);
            }
            logger.debug("new dest: " + newDest.toString());

            if (f.isFile()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("copyDirectory: copying " + f);
                }

                f.copy(newDest);
            } else if (f.isDirectory()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("copyDirectory: copying dir " + f);
                }

                copyDirectory(gatContext, preferences, f.toURI(), newDest);
            } else {
                throw new GATInvocationException(
                        "file cpi, don't know how to handle file: " + f
                                + " (links are not supported).");
            }
        }
    }

    public static void recursiveDeleteDirectory(GATContext gatContext,
            Preferences preferences, URI dirUri) throws GATInvocationException {
        FileInterface dir;
        try {
            dir = GAT.createFile(gatContext, preferences, dirUri)
                    .getFileInterface();
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("generic file cpi", e);
        }

        recursiveDeleteDirectory(gatContext, preferences, dir);
    }

    public static void recursiveDeleteDirectory(GATContext gatContext,
            Preferences preferences, FileInterface dir)
            throws GATInvocationException {

        if (logger.isInfoEnabled()) {
            logger.info("recursive delete dir: " + dir);
        }

        GATInvocationException exception = new GATInvocationException();
        File[] files = (File[]) dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                try {
                    if (files[i].getFileInterface().isDirectory()) {
                        recursiveDeleteDirectory(gatContext, preferences,
                                files[i].toGATURI());
                    } else {
                        if (logger.isInfoEnabled()) {
                            logger.info("delete: " + files[i]);
                        }
                        files[i].getFileInterface().delete();
                    }
                } catch (GATInvocationException e) {
                    exception.add("file cpi", e);
                }
            }
        }

        try {
            dir.delete();
        } catch (Exception e) {
            exception.add("file cpi", e);
        }

        if (exception.getNrChildren() != 0) {
            throw exception;
        }
    }

    public String marshal() {
        SerializedFile f = new SerializedFile();
        f.setLocation(location.toString());

        return GATEngine.defaultMarshal(f);
    }

    public static Advertisable unmarshal(GATContext context,
            Preferences preferences, String s) {
        SerializedFile f = (SerializedFile) GATEngine.defaultUnmarshal(
                SerializedFile.class, s);

        try {
            return GAT.createFile(context, preferences,
                    new URI(f.getLocation()));
        } catch (Exception e) {
            throw new Error("could not create new GAT object");
        }
    }

    protected boolean determineIsDirectory() throws GATInvocationException {
        // create a seperate file object to determine whether this file
        // is a directory. This is needed, because the source might be a local
        // file, and some adaptors might not work locally (like gridftp).
        // This goes wrong for local -> remote copies.
        if (toURI().refersToLocalHost()) {
            try {
                java.io.File f = new java.io.File(getPath());
                return f.isDirectory();
            } catch (Exception e) {
                throw new GATInvocationException("fileCPI", e);
            }
        } else {
            try {
                return isDirectory();
            } catch (Exception e) {
                // ignore
            }

            try {
                File f = GAT.createFile(gatContext, preferences, toURI());
                return f.getFileInterface().isDirectory();
            } catch (Exception e2) {
                throw new GATInvocationException("fileCPI", e2);
            }
        }
    }

    private static class DeleteHook extends Thread {
        FileCpi f;

        DeleteHook(FileCpi f) {
            this.f = f;
        }

        public void run() {
            try {
                f.delete();
            } catch (Throwable t) {
                // Ignore
            }
        }
    }
}
