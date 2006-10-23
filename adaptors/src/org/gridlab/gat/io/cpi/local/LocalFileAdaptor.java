package org.gridlab.gat.io.cpi.local;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.AdaptorNotSelectedException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.FileCpi;

public class LocalFileAdaptor extends FileCpi {
    File f;

    /**
     * @param gatContext
     * @param preferences
     * @param location
     */
    public LocalFileAdaptor(GATContext gatContext, Preferences preferences,
        URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.refersToLocalHost()) {
            throw new AdaptorNotSelectedException(
                "Cannot use remote files with the local file adaptor");
        }

        if (!location.isCompatible("file")) {
            throw new AdaptorNotSelectedException("cannot handle this URI");
        }

        location = correctURI(location);

        // we have a host name, which means that this file is relative to $HOME
        if (location.getHost() != null) {
            String home = System.getProperty("user.home");
            if (home == null) {
                throw new GATObjectCreationException(
                    "local file adaptor could not get user home dir");
            }

            String dest = location.getScheme() + "://";
            dest += (location.getUserInfo() == null) ? "" : location
                .getUserInfo();
            dest += location.getHost();
            dest += (location.getPort() == -1) ? ""
                : (":" + location.getPort());
            dest += "/";
            dest += location.getPath();

            try {
                location = new URI(dest);
            } catch (URISyntaxException e) {
                throw new Error("internal error in LocalFile: " + e);
            }
        }

        if (GATEngine.DEBUG) {
            System.err.println("LocalFileAdaptor: LOCATION = " + location);
        }

        if ((location.getPath() == null) || (location.getPath().equals(""))) {
            f = new File(".");
        } else {
            f = new File(location.getPath());
        }
    }

    // Make life a bit easier for the programmer:
    // If the URI does not have a scheme part, just consider it a local
    // file.
    // The ctor of java.io.file does not accept this.
    protected URI correctURI(URI in) {
        if (in.getScheme() == null) {
            try {
                return new URI("file:///" + in.getPath());
            } catch (URISyntaxException e) {
                throw new Error("internal error in LocalFile: " + e);
            }
        }

        return in;
    }

    /**
     * This method copies the physical file represented by this File instance to
     * a physical file identified by the passed URI.
     *
     * @param destination
     *            The new location
     * @throws java.io.IOException
     *             Upon non-remote IO problem
     */
    public void copy(URI destination) throws GATInvocationException {
        if (!destination.refersToLocalHost()) {
            throw new AdaptorNotApplicableException(
                "default file: cannot copy to remote destination");
        }

        if (GATEngine.VERBOSE) {
            System.err.println("local copy of " + getPath() + " to "
                + destination.getPath());
        }

        if (destination.getPath().equals(toURI().getPath())) {
            if (GATEngine.VERBOSE) {
                System.err
                    .println("local copy, source is the same file as dest.");
            }

            return;
        }

        if (!exists()) {
            throw new GATInvocationException(
                "the local source file does not exist, path = " + getPath());
        }

        if (isDirectory()) {
            if (GATEngine.DEBUG) {
                System.err.println("local copy, it is a dir");
            }

            copyDirectory(gatContext, preferences, toURI(), destination);

            return;
        }

        if (GATEngine.DEBUG) {
            System.err.println("local copy, it is a file");
        }

        File tmp = new File(correctURI(destination).getPath());

        // if the destination URI is a dir, append the file name.
        if (tmp.isDirectory()) {
            String u = destination.toString() + "/" + getName();

            try {
                destination = new URI(u);
            } catch (URISyntaxException e) {
                throw new GATInvocationException("default file", e);
            }
        }

        // Create destination file
        File destinationFile = new File(correctURI(destination).getPath());

        if (GATEngine.DEBUG) {
            System.err.println("creating local file " + destinationFile);
        }

        BufferedInputStream inBuf;
        BufferedOutputStream outBuf;

        try {
            destinationFile.createNewFile();

            // Copy source to destination
            FileInputStream in = new FileInputStream(f);
            inBuf = new BufferedInputStream(in);

            FileOutputStream out = new FileOutputStream(destinationFile);
            outBuf = new BufferedOutputStream(out);
        } catch (IOException e) {
            throw new GATInvocationException("local file", e);
        }

        try {
            long bytesWritten = 0;
            byte[] buf = new byte[1024];

            while (bytesWritten != f.length()) {
                int len = inBuf.read(buf);
                outBuf.write(buf, 0, len);
                bytesWritten += len;
            }
        } catch (IOException e) {
            throw new GATInvocationException("default file", e);
        } finally {
            try {
                outBuf.close();
            } catch (IOException e) {
                throw new GATInvocationException("default file", e);
            }

            try {
                inBuf.close();
            } catch (IOException e) {
                throw new GATInvocationException("default file", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#canRead()
     */
    public boolean canRead() {
        return f.canRead();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#canWrite()
     */
    public boolean canWrite() {
        return f.canWrite();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#compareTo(gat.io.File)
     */
    public int compareTo(org.gridlab.gat.io.File other) {
        return toURI().compareTo(other.toURI());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#compareTo(java.lang.Object)
     */
    public int compareTo(Object other) {
        if(other instanceof org.gridlab.gat.io.File) {
            org.gridlab.gat.io.File o = (org.gridlab.gat.io.File) other;
            return toURI().compareTo(o.toURI());
        } else if(other instanceof URI) {
            URI u = (URI) other;
            return toURI().compareTo(u);
        } else {
            throw new RuntimeException("files can only be compared to files and URIs");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#createNewFile()
     */
    public boolean createNewFile() throws GATInvocationException {
        try {
            return f.createNewFile();
        } catch (IOException e) {
            throw new GATInvocationException("local file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#delete()
     */
    public boolean delete() {
        return f.delete();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#exists()
     */
    public boolean exists() {
        return f.exists();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#getAbsoluteFile()
     */
    public org.gridlab.gat.io.File getAbsoluteFile()
        throws GATInvocationException {
        try {
            return GAT.createFile(gatContext, preferences, localToURI(f
                .getAbsoluteFile().getPath()));
        } catch (Exception e) {
            throw new GATInvocationException("default file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#getAbsolutePath()
     */
    public String getAbsolutePath() {
        return f.getAbsolutePath();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#getCanonicalFile()
     */
    public org.gridlab.gat.io.File getCanonicalFile()
        throws GATInvocationException {
        try {
            return GAT.createFile(gatContext, preferences, localToURI(f
                .getCanonicalFile().getPath()));
        } catch (Exception e) {
            throw new GATInvocationException("default file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#getCanonicalPath()
     */
    public String getCanonicalPath() throws GATInvocationException {
        try {
            return f.getCanonicalPath();
        } catch (IOException e) {
            throw new GATInvocationException("local file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#getParent()
     */
    public String getParent() {
        return f.getParent();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#getParentFile()
     */
    public org.gridlab.gat.io.File getParentFile()
        throws GATInvocationException {
        try {
            return GAT.createFile(gatContext, preferences, localToURI(f
                .getParentFile().getPath()));
        } catch (Exception e) {
            throw new GATInvocationException("default file", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#isDirectory()
     */
    public boolean isDirectory() {
        return f.isDirectory();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#isFile()
     */
    public boolean isFile() {
        return f.isFile();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#isHidden()
     */
    public boolean isHidden() {
        return f.isHidden();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#lastModified()
     */
    public long lastModified() throws GATInvocationException {
        return f.lastModified();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#length()
     */
    public long length() throws GATInvocationException {
        return f.length();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#list()
     */
    public String[] list() throws GATInvocationException,
        GATInvocationException {
        return f.list();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#list(java.io.FilenameFilter)
     */
    public String[] list(FilenameFilter arg0) {
        return f.list(arg0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#listFiles()
     */
    public org.gridlab.gat.io.File[] listFiles() throws GATInvocationException {
        File[] r = f.listFiles();
        org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];

        for (int i = 0; i < r.length; i++) {
            try {
                res[i] = GAT.createFile(gatContext, preferences,
                    localToURI(r[i].getPath()));
            } catch (Exception e) {
                throw new GATInvocationException("default file", e);
            }
        }

        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#listFiles(java.io.FileFilter)
     */
    public org.gridlab.gat.io.File[] listFiles(FileFilter arg0)
        throws GATInvocationException {
        File[] r = f.listFiles(arg0);
        org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];

        for (int i = 0; i < r.length; i++) {
            try {
                res[i] = GAT.createFile(gatContext, preferences,
                    localToURI(r[i].getPath()));
            } catch (Exception e) {
                throw new GATInvocationException("default file", e);
            }
        }

        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#listFiles(java.io.FilenameFilter)
     */
    public org.gridlab.gat.io.File[] listFiles(FilenameFilter arg0)
        throws GATInvocationException {
        File[] r = f.listFiles(arg0);
        org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];

        for (int i = 0; i < r.length; i++) {
            try {
                res[i] = GAT.createFile(gatContext, preferences,
                    localToURI(r[i].getPath()));
            } catch (Exception e) {
                throw new GATInvocationException("default file", e);
            }
        }

        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#mkdir()
     */
    public boolean mkdir() throws GATInvocationException {
        return f.mkdir();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#mkdirs()
     */
    public boolean mkdirs() {
        return f.mkdirs();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#move(java.net.URI)
     */
    public void move(URI destination) throws GATInvocationException {
        if (!destination.refersToLocalHost()) {
            throw new AdaptorNotApplicableException(
                "default file: cannot move to remote destination");
        }

        if (GATEngine.VERBOSE) {
            System.err.println("local move of " + getPath() + " to "
                + destination.getPath());
        }

        if (destination.getPath().equals(toURI().getPath())) {
            if (GATEngine.VERBOSE) {
                System.err
                    .println("local move, source is the same file as dest.");
            }

            return;
        }

        if (!exists()) {
            throw new GATInvocationException(
                "the local source file does not exist, path = " + getPath());
        }

        File tmp = new File(destination.getPath());
        boolean res = f.renameTo(tmp);

        if (!res) {
            throw new GATInvocationException("Could not move file");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#renameTo(java.io.File)
     */
    public boolean renameTo(java.io.File arg0) throws GATInvocationException {
        File tmp = new File(arg0.toURI());

        return f.renameTo(tmp);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#setLastModified(long)
     */
    public boolean setLastModified(long arg0) {
        return f.setLastModified(arg0);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#setReadOnly()
     */
    public boolean setReadOnly() {
        return f.setReadOnly();
    }

    /**
     * Converts a local path into a URI
     *
     * @return a URI representing the path
     */
    public URI localToURI(String path) throws URISyntaxException {
        return new URI(path.replace(File.separatorChar, '/'));
    }
}
