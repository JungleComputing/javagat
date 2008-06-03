/*
 * Created on Nov 3, 2006
 */
package org.gridlab.gat.io;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATIOException;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricEvent;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * @author rob
 */
/**
 * An abstract representation of a physical file. See {@link java.io.File}.
 * <p>
 * An instance of this class presents an abstract, system-independent view of a
 * physical file. User interfaces and operating systems use system-dependent
 * pathname strings to identify physical files. GAT, however, uses an operating
 * system independent pathname string to identify a physical file. A physical
 * file in GAT is identified by a URI.
 * <p>
 * An instance of this File class allows for various high-level operations to be
 * performed on a physical file. For example, one can, with a single API call,
 * copy a physical file from one location to a second location, move a physical
 * file from one location to a second location, delete a physical file, and
 * perform various other operations on a physical file. The utility of this
 * high-level view of a physical file is multi-fold. The client of an instance
 * of this class does not have to concern themselves with the details of reading
 * every single byte of a physical file when all they wish to do is copy the
 * physical file to a new location. Similarly, a client does not have to deal
 * with all the various error states that can occur when moving a physical file.
 * Have all the various bytes been read correctly? Have all the various bytes
 * been saved correctly? Did the deletion of the original file proceed
 * correctly? The client simply has to call a single API call and the physical
 * file is moved.
 */
@SuppressWarnings("serial")
public class File extends java.io.File implements Monitorable, Advertisable,
        java.io.Serializable {
    org.gridlab.gat.io.FileInterface f;

    /**
     * Do not use this constructor, it is for internal GAT use.
     */
    public File(org.gridlab.gat.io.FileInterface f) {
        super("dummy");
        this.f = f;
    }

    /**
     * This method copies the physical file represented by this File instance to
     * a physical file identified by the passed URI.
     * 
     * @param loc
     *                The new location
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws java.io.IOException
     *                 Upon non-remote IO problem
     */
    public void copy(URI loc) throws GATInvocationException {
        f.copy(loc);
    }

    /**
     * Returns the FileInterface object associated with this {@link File}. Use
     * The FileInterface offers the same functionality as the File object. The
     * difference between the FileInterface object and the File object is that
     * the FileInterface is an internal GAT object and it will throw
     * {@link GATInvocationException}s upon failures, whereas the File object
     * would have default values.
     * <p>
     * This might be handy in certain circumstances. Suppose that one checks
     * whether a file exists and if not one creates a file at that location.
     * Using the File object the code will look like this:
     * <p>
     * <code>
     * if (!file.exists()) {
     *     file.createNewFile();
     * }
     * </code>
     * <p>
     * Now suppose that the {@link #exists()} call fails because the network is
     * down for a moment. The File object will return false for this call,
     * because the network was down, even though the file might exist. To
     * overcome this one can change the code to code which uses the
     * FileInterface instead of the File object:
     * <p>
     * <code>
     * if (!file.getFileInterface().exists()) {
     *      file.getFileInterface().createNewFile();
     * }
     * </code>
     * <p>
     * Now an exception will be thrown by the FileInterface if the network is
     * down. It is possible to retry the code until the network is up again.
     * 
     * @return the FileInterface object
     */
    public org.gridlab.gat.io.FileInterface getFileInterface() {
        return f;
    }

    /**
     * This method moves the physical file represented by this File instance to
     * a physical file identified by the passed URI.
     * 
     * @param location
     *                The URI to which to move the physical file corresponding
     *                to this File instance
     * @throws GATInvocationException
     *                 Thrown upon problems accessing the remote instance
     * @throws IOException
     *                 Upon non-remote IO problem
     */
    public void move(URI location) throws GATInvocationException {
        f.move(location);
    }

    /**
     * This method deletes a directory and everything that is in it. This method
     * can only be called on a directory, not on a file.
     * 
     * @throws GATInvocationException
     */
    public void recursivelyDeleteDirectory() throws GATInvocationException {
        f.recursivelyDeleteDirectory();
    }

    /**
     * @see java.io.File#canRead()
     */
    public boolean canRead() {
        try {
            return f.canRead();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#canWrite()
     */
    public boolean canWrite() {
        try {
            return f.canWrite();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#compareTo(java.io.File)
     */
    public int compareTo(File pathname) {
        return f.compareTo(pathname);
    }

    /**
     * @see java.io.File#createNewFile()
     */
    public boolean createNewFile() throws IOException {
        try {
            return f.createNewFile();
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /**
     * @see java.io.File#delete()
     */
    public boolean delete() {
        try {
            return f.delete();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 
     * @see java.io.File#deleteOnExit()
     */
    public void deleteOnExit() {
        f.deleteOnExit();
    }

    /**
     * @see java.io.File#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return f.equals(obj);
    }

    /**
     * @see java.io.File#exists()
     */
    public boolean exists() {
        try {
            return f.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#getAbsoluteFile()
     */
    public java.io.File getAbsoluteFile() {
        try {
            return f.getAbsoluteFile();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see java.io.File#getAbsolutePath()
     */
    public String getAbsolutePath() {
        try {
            return f.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see java.io.File#getCanonicalFile()
     */
    public java.io.File getCanonicalFile() throws IOException {
        try {
            return f.getCanonicalFile();
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /**
     * @see java.io.File#getCanonicalPath()
     */
    public String getCanonicalPath() throws IOException {
        try {
            return f.getCanonicalPath();
        } catch (GATInvocationException e) {
            throw new GATIOException(e);
        }
    }

    /**
     * @see java.io.File#getName()
     */
    public String getName() {
        try {
            return f.getName();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see java.io.File#getParent()
     */
    public String getParent() {
        try {
            return f.getParent();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the parent file
     * @see java.io.File#getParentFile()
     */
    public java.io.File getParentFile() {
        try {
            return f.getParentFile();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see java.io.File#getPath()
     */
    public String getPath() {
        try {
            return f.getPath();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see java.io.File#hashCode()
     */
    public int hashCode() {
        try {
            return f.hashCode();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * @see java.io.File#isAbsolute()
     */
    public boolean isAbsolute() {
        return f.isAbsolute();
    }

    /**
     * @see java.io.File#isDirectory()
     */
    public boolean isDirectory() {
        try {
            return f.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#isFile()
     */
    public boolean isFile() {
        try {
            return f.isFile();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#isHidden()
     */
    public boolean isHidden() {
        try {
            return f.isHidden();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#lastModified()
     * 
     * Please note there's an issue with the gt4 file adaptor (gt4gridftp). It
     * takes time zones into account where as the
     * {@link java.io.File#lastModified()} doesn't. For instance a file that's
     * last modified on 10 July 1984 at 00:00 GMT +2:00 will have a last
     * modified time of 9 July 1984 at 22:00 using the gt4 file adaptor. The
     * {@link java.io.File#lastModified()} will return 10 July 1984, 00:00.
     */
    public long lastModified() {
        try {
            return f.lastModified();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * @see java.io.File#length()
     */
    public long length() {
        try {
            return f.length();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * @see java.io.File#list()
     */
    public String[] list() {
        try {
            return f.list();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see java.io.File#list(java.io.FilenameFilter)
     */
    public String[] list(FilenameFilter filter) {
        try {
            return f.list(filter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see java.io.File#listFiles()
     */
    public java.io.File[] listFiles() {
        try {
            return f.listFiles();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see java.io.File#listFiles(java.io.FileFilter)
     */
    public java.io.File[] listFiles(FileFilter filter) {
        try {
            return f.listFiles(filter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see java.io.File#listFiles(java.io.FilenameFilter)
     */
    public java.io.File[] listFiles(FilenameFilter filter) {
        try {

            return f.listFiles(filter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @see java.io.File#mkdir()
     */
    public boolean mkdir() {
        try {
            return f.mkdir();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#mkdirs()
     */
    public boolean mkdirs() {
        try {
            return f.mkdirs();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#renameTo(java.io.File)
     */
    public boolean renameTo(java.io.File dest) {
        try {
            org.gridlab.gat.io.File a = (org.gridlab.gat.io.File) dest;
            return f.renameTo(a);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#setLastModified(long)
     */
    public boolean setLastModified(long time) {
        try {
            return f.setLastModified(time);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#setReadOnly()
     */
    public boolean setReadOnly() {
        try {
            return f.setReadOnly();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see java.io.File#toString()
     */
    public String toString() {
        return f.toString();
    }

    /**
     * @see java.io.File#toURI()
     */
    public java.net.URI toURI() {
        throw new Error("please use toGATURI to retreive a file's URI.");
    }

    /**
     * @see java.io.File#toURI()
     */
    public org.gridlab.gat.URI toGATURI() {
        return f.toURI();
    }

    /**
     * @see java.io.File#toURL()
     */
    public URL toURL() throws MalformedURLException {
        return f.toURL();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.Advertisable#marshal()
     */
    public String marshal() {
        return f.marshal();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.monitoring.Monitorable#addMetricListener(org.gridlab.gat.monitoring.MetricListener,
     *      org.gridlab.gat.monitoring.Metric)
     */
    public void addMetricListener(MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        f.addMetricListener(metricListener, metric);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.monitoring.Monitorable#getMeasurement(org.gridlab.gat.monitoring.Metric)
     */
    public MetricEvent getMeasurement(Metric metric)
            throws GATInvocationException {
        return f.getMeasurement(metric);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.monitoring.Monitorable#getMetricDefinitionByName(java.lang.String)
     */
    public MetricDefinition getMetricDefinitionByName(String name)
            throws GATInvocationException {
        return f.getMetricDefinitionByName(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.monitoring.Monitorable#getMetricDefinitions()
     */
    public List<MetricDefinition> getMetricDefinitions()
            throws GATInvocationException {
        return f.getMetricDefinitions();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.monitoring.Monitorable#removeMetricListener(org.gridlab.gat.monitoring.MetricListener,
     *      org.gridlab.gat.monitoring.Metric)
     */
    public void removeMetricListener(MetricListener metricListener,
            Metric metric) throws GATInvocationException {
        f.removeMetricListener(metricListener, metric);
    }

    /**
     * Read a file object from a stream.
     * 
     * @param stream
     *                the stream to write to
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        URI u = (URI) stream.readObject();
        GATContext c = (GATContext) stream.readObject();

        try {
            File newFile = GAT.createFile(c, u);
            f = newFile.f;
        } catch (Exception e) {
            throw new Error("Could not create File object: " + u);
        }
    }

    /**
     * Serialize this file, by just writing the URI, the Preferences and the
     * GATContext.
     * 
     * @param stream
     *                the stream to write to
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(toGATURI());
        stream.writeObject(getFileInterface().getGATContext());
    }
}
