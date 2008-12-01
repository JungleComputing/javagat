package org.gridlab.gat.io.cpi;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.gridlab.gat.engine.util.NoInfoLogging;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * Capability provider interface to the File class.
 * <p>
 * Capability provider wishing to provide the functionality of the File class
 * must extend this class and implement all of the abstract methods in this
 * class. Each abstract method in this class mirrors the corresponding method in
 * this File class and will be used to implement the corresponding method in the
 * File class at runtime.
 */
public abstract class FileCpi implements FileInterface, java.io.Serializable {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
        capabilities.put("getGATContext", true);
        capabilities.put("equals", true);
        capabilities.put("toURI", true);
        capabilities.put("toURL", true);
        capabilities.put("toString", true);
        capabilities.put("recursivelyDeleteDirectory", true);
        capabilities.put("addMetricListener", false);
        capabilities.put("getMetricDefinitions", false);
        capabilities.put("getMeasurement", false);
        capabilities.put("compareTo", true);
        capabilities.put("deleteOneExit", true);
        capabilities.put("getAbsolutePath", false);
        capabilities.put("getCanonicalPath", false);
        capabilities.put("getPath", true);
        capabilities.put("hashcode", true);
        capabilities.put("copy", false);
        capabilities.put("canRead", false);
        capabilities.put("canWrite", false);
        capabilities.put("createNewFile", false);
        capabilities.put("delete", false);
        capabilities.put("exists", false);
        capabilities.put("getAbsoluteFile", false);
        capabilities.put("getCanonicalFile", false);
        capabilities.put("getName", true);
        capabilities.put("getParent", true);
        capabilities.put("getParentFile", true);
        capabilities.put("isDirectory", false);
        capabilities.put("isAbsolute", true);
        capabilities.put("isFile", false);
        capabilities.put("isHidden", false);
        capabilities.put("lastModified", false);
        capabilities.put("length", false);
        capabilities.put("list", false);
        capabilities.put("listFiles", true);
        capabilities.put("mkdir", false);
        capabilities.put("mkdirs", true);
        capabilities.put("move", true);
        capabilities.put("renameTo", false);
        capabilities.put("setLastModified", false);
        capabilities.put("setReadOnly", false);

        return capabilities;
    }

    public static Preferences getSupportedPreferences() {
        Preferences preferences = new Preferences();
        preferences.put("file.adaptor.name", "<no default>");
        preferences.put("adaptors.local", "false");
        preferences.put("file.create", "false");

        return preferences;
    }

    protected static Logger logger = Logger.getLogger(FileCpi.class);

    protected GATContext gatContext;

    protected URI location;

    protected boolean ignoreHiddenFiles = false;

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
     */
    protected FileCpi(GATContext gatContext, URI location) {
        this.gatContext = gatContext;
        this.location = location;

        String res = (String) gatContext.getPreferences().get(
                "file.hiddenfiles.ignore");
        if (res != null && res.equalsIgnoreCase("true")) {
            if (logger.isDebugEnabled()) {
                logger.debug("ignoring hidden files");
            }
            ignoreHiddenFiles = true;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("FileCpi: created file with URI " + location);
        }
    }

    @NoInfoLogging
    public final GATContext getGATContext() {
        return gatContext;
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
    @NoInfoLogging
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
            FileInterface f = GAT.createFile(gatContext, location)
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
        recursiveDeleteDirectory(gatContext, location);
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

    public MetricEvent getMeasurement(Metric metric)
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

    @NoInfoLogging
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
            String dest = location.toString().replace(getPath(), getParent());
            if (logger.isDebugEnabled()) {
                logger.debug("GET PARENTFILE: orig = " + location + " new* = "
                        + dest);
            }
            return GAT.createFile(gatContext, new URI(dest));
        } catch (Exception e) {
            throw new GATInvocationException("file cpi", e);
        }
    }

    @NoInfoLogging
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

    public static final boolean isAbsolute(URI location) {
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

    public String[] list() throws GATInvocationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public File[] listFiles() throws GATInvocationException {
        if (!isDirectory()) {
            throw new GATInvocationException("this is not a directory: "
                    + location);
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
                res[i] = GAT.createFile(gatContext, new URI(uri));
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
                if (filter.accept(GAT.createFile(gatContext, new URI(location
                        .getPath())), l[i])) {
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
                if (filter.accept(GAT.createFile(gatContext, new URI(l[i]
                        .getPath())))) {
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
                if (filter.accept(GAT.createFile(gatContext, new URI(location
                        .getPath())), l[i].getPath())) {
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

        FileInterface parent = getParentFile().getFileInterface();

        if (parent == null) {
            return false;
        }

        return parent.mkdirs() && mkdir();
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

    @NoInfoLogging
    public final String toString() {
        return "GAT file: " + toURI().toString();
    }

    @NoInfoLogging
    public final URL toURL() throws MalformedURLException {
        return location.toURL();
    }

    protected static URI fixURI(URI in, String destScheme) {
        // if destscheme != null replaces or adds destscheme to in
        // if local relative file, add "user.dir" in front of it
        String uriString = in.toString();
        if (destScheme != null) {
            if (in.getScheme() != null) {
                uriString = uriString.replaceFirst(in.getScheme(), destScheme);
            } else {
                if (in.getAuthority() == null) {
                    uriString = destScheme + ":///" + uriString;
                } else {
                    uriString = destScheme + "://" + uriString;
                }
            }
        }
        if (in.refersToLocalHost() && !isAbsolute(in)) {
            if (destScheme == null && in.getScheme() == null) {
                uriString = "file:" + uriString;
            }
            uriString = uriString.replace(in.getPath(), new java.io.File(System
                    .getProperty("user.dir")).toURI().getPath()
                    + in.getPath());
        }
        URI fixedURI = null;
        try {
            fixedURI = new URI(uriString);
        } catch (URISyntaxException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("internal UnsupportedOperationException: " + e);
            }
        }
        return fixedURI;
    }

    protected void copyDir(URI destination) throws Exception {
        copyDirectory(gatContext, null, location, destination);
    }

    protected void copyDirContents(URI destination)
            throws GATInvocationException {
        // list all the files and copy recursively.
        if (logger.isDebugEnabled()) {
            logger.debug("copyDirectory contents '" + location + "' to '"
                    + destination + "'");
        }
        File source = null;
        try {
            source = GAT.createFile(gatContext, location);
        } catch (GATObjectCreationException e1) {
            // should not happen
        }
        File[] files = (File[]) source.listFiles();
        if (files == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("copyDirectory: no files in src directory: "
                        + location);
            }
            return;
        }
        for (File file : files) {
            FileInterface f = file.getFileInterface();
            if (logger.isDebugEnabled()) {
                logger.debug("copyDirectory: file to copy = " + f);
            }
            try {
                f.copy(new URI(destination + "/" + f.getName()));
            } catch (URISyntaxException e) {
                // would not happen
            }
        }
    }

    protected static void copyDirectory(GATContext gatContext,
            Preferences additionalPreferences, URI dirURI, URI dest)
            throws GATInvocationException {
        if (logger.isDebugEnabled()) {
            logger.debug("copyDirectory '" + dirURI + "' to '" + dest + "'");
        }
        FileInterface destFile = null;

        // check whether the target is an existing file
        boolean existingFile = false;
        boolean existingDir = false;
        try {
            destFile = GAT.createFile(gatContext, additionalPreferences, dest)
                    .getFileInterface();
            if (destFile.exists()) {
                if ( !destFile.isDirectory()) {            
                    existingFile = true;
                } else {
                    existingDir = true;
                }
            }
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("file cpi", e);
        } catch (GATInvocationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("copyDirectory not able to check if '"
                        + dest.toString() + "' is existing file");
            }
        }
        // if so, it isn't possible to copy the directory to that target
        if (existingFile) {
            throw new GATInvocationException("cannot overwrite non-directory '"
                    + dest.toString() + "' with directory '"
                    + dirURI.toString() + "'!");
        }
        
        if (! existingDir) {
            // copy dir a to b will result in a new directory b with contents
            // that is a copy of the contents of a.
        } else if (gatContext.getPreferences().containsKey("file.directory.copy")
                && ((String) gatContext.getPreferences().get(
                        "file.directory.copy")).equalsIgnoreCase("contents")) {
            // don't modify the dest dir, so copy dir a to dir b ends up as
            // copying a/* to b/*, note that a/dir/* also ends up in b/*
        } else {
            // because copy dir a to dir b ends up as b/a we've to add /a to the
            // dest.
            String sourcePath = dirURI.getPath();
            if (sourcePath.endsWith("/")) {
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
        }
        if (logger.isDebugEnabled()) {
            logger.debug("dest=" + dest);
        }
        // create destination dir
        try {
            destFile = GAT.createFile(gatContext, additionalPreferences, dest)
                    .getFileInterface();
            if (gatContext.getPreferences().containsKey("file.create")
                    && ((String) gatContext.getPreferences().get("file.create"))
                            .equalsIgnoreCase("true")) {
                destFile.mkdirs();
            } else {
                // if source is a dir 'dir1' and dest is a dir 'dir2' then the
                // result of dir1.copy(dir2) will be dir2/dir1/.. so even if the
                // 'file.create' flag isn't set, create the dir1 in dir2 before
                // copying the files.
                boolean mkdir = destFile.mkdir();
                if (logger.isDebugEnabled()) {
                    logger.debug("mkdir: " + mkdir);
                }
            }
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("file cpi", e);
        }
        FileInterface dir = null;

        try {
            dir = GAT.createFile(gatContext, additionalPreferences, dirURI)
                    .getFileInterface();
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("file cpi", e);
        }
        // list all the files and copy recursively.
        File[] files = (File[]) dir.listFiles();
        if (files == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("copyDirectory: no files in src directory: "
                        + dirURI.toString());
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
            logger.debug("src is file: " + f.isFile());
            logger.debug("src is dir: " + f.isDirectory());

            if (f.isFile()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("copyDirectory: copying " + f);
                }

                f.copy(newDest);
            } else if (f.isDirectory()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("copyDirectory: copying dir " + f);
                }
                if (gatContext.getPreferences().containsKey(
                        "file.directory.copy")
                        && ((String) gatContext.getPreferences().get(
                                "file.directory.copy"))
                                .equalsIgnoreCase("contents")) {
                    Preferences newPrefs = new Preferences();
                    newPrefs.remove("file.directory.copy");
                    copyDirectory(gatContext, newPrefs, f.toURI(), dest);
                } else {
                    copyDirectory(gatContext, null, f.toURI(), dest);
                }
            } else {
                throw new GATInvocationException(
                        "file cpi, don't know how to handle file: " + f
                                + " (links are not supported).");
            }
        }
    }

    public static void recursiveDeleteDirectory(GATContext gatContext,
            URI dirUri) throws GATInvocationException {
        FileInterface dir;
        try {
            dir = GAT.createFile(gatContext, dirUri).getFileInterface();
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("generic file cpi", e);
        }

        recursiveDeleteDirectory(gatContext, dir);
    }

    public static void recursiveDeleteDirectory(GATContext gatContext,
            FileInterface dir) throws GATInvocationException {

        if (logger.isInfoEnabled()) {
            logger.info("recursive delete dir: " + dir);
        }

        // just try to delete the directory directly, some adaptors might be
        // able to do this!
        try {
            if (dir.delete()) {
                return;
            }
        } catch (Throwable t) {
            // ignore
        }

        GATInvocationException exception = new GATInvocationException();
        File[] files = (File[]) dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                try {
                    if (files[i].getFileInterface().isDirectory()) {
                        recursiveDeleteDirectory(gatContext, files[i]
                                .toGATURI());
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

    public static Advertisable unmarshal(GATContext context, String s) {
        SerializedFile f = (SerializedFile) GATEngine.defaultUnmarshal(
                SerializedFile.class, s);

        try {
            return GAT.createFile(context, new URI(f.getLocation()));
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
                File f = GAT.createFile(gatContext, toURI());
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
