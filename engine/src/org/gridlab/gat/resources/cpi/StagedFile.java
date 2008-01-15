/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileOutputStream;

public abstract class StagedFile {

    protected static Logger logger = Logger.getLogger(StagedFile.class);

    protected GATContext gatContext;

    protected Preferences preferences;

    protected File origSrc;

    protected File origDest;

    private File resolvedSrc;

    private File resolvedDest;

    private String resolvedSrcURIString;
    private String resolvedDestURIString;

    protected String host;

    protected String sandbox;

    protected boolean inSandbox;

    protected URI relativeURI;

    public StagedFile() {
        // constructor needed for castor marshalling, do *not* use
    }

    public StagedFile(GATContext context, Preferences preferences,
            File origSrc, File origDest, String host, String sandbox) {
        super();
        this.gatContext = context;
        this.preferences = preferences;
        this.origSrc = origSrc;
        this.origDest = origDest;
        this.host = host;
        this.sandbox = sandbox;
    }

    /** Creates a file object that points to the sandbox. */
    protected File resolve(File f, boolean useNameOnly)
            throws GATInvocationException {
        URI uri = f.toGATURI();

        String relativeDest = null;
        String dest = "any://";
        dest += (uri.getUserInfo() == null) ? "" : uri.getUserInfo();

        String origHost = f.toGATURI().getHost();
        if (useNameOnly || origHost == null) {
            dest += host;
        } else {
            dest += origHost;
        }

        dest += (uri.getPort() == -1) ? "" : (":" + uri.getPort());
        dest += "/";

        if (inSandbox) {
            dest += sandbox == null ? "" : sandbox + "/";
        }

        if (useNameOnly) {
            java.io.File tmp = new java.io.File(uri.getPath());
            dest += tmp.getName();
            relativeDest = tmp.getName();
        } else {
            dest += uri.getPath();
            relativeDest = uri.getPath();
        }

        try {
            URI destURI = new URI(dest);
            relativeURI = new URI(relativeDest);

            return GAT.createFile(gatContext, preferences, destURI);
        } catch (Exception e) {
            throw new GATInvocationException("StageFile", e);
        }
    }

    protected void wipe(File f) throws GATInvocationException {
        if (!f.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("file to wipe does not exists, skipping.");
            }
            return;
        }

        if (!f.isFile()) {
            if (logger.isDebugEnabled()) {
                logger.debug("file to wipe is not a normal file, skipping.");
            }
            return;
        }

        long size = f.length();

        FileOutputStream out = null;

        try {
            out = GAT.createFileOutputStream(gatContext, preferences, f);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("resource broker", e);
        }

        try {
            int bufSize = 64 * 1024;
            byte[] buf = new byte[bufSize];
            long wiped = 0;
            while (wiped != size) {
                int toWipe;
                if (size - wiped < bufSize) {
                    toWipe = (int) (size - wiped);
                } else {
                    toWipe = bufSize;
                }

                out.write(buf, 0, toWipe);
                wiped += toWipe;
            }
        } catch (Exception e) {
            throw new GATInvocationException("resource broker", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    /**
     * @return the inSandbox
     */
    public boolean isInSandbox() {
        return inSandbox;
    }

    /**
     * @param inSandbox
     *                the inSandbox to set
     */
    public void setInSandbox(boolean inSandbox) {
        this.inSandbox = inSandbox;
    }

    protected void setResolvedSrc(File resolvedSrc) {
        this.resolvedSrc = resolvedSrc;
        resolvedSrcURIString = resolvedSrc.toGATURI().toString();
    }

    public File getResolvedSrc() {
        // if this sandbox object was retrieved from the advert service, we have
        // to recreate the file object
        if (resolvedSrc == null && resolvedSrcURIString != null) {
            try {
                resolvedSrc = GAT.createFile(gatContext, preferences,
                        resolvedSrcURIString);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
        return resolvedSrc;
    }

    /**
     * @return the resolvedDestURIString
     */
    public String getResolvedDestURIString() {
        return resolvedDestURIString;
    }

    /**
     * @param resolvedDestURIString
     *                the resolvedDestURIString to set
     */
    public void setResolvedDestURIString(String resolvedDestURIString) {
        this.resolvedDestURIString = resolvedDestURIString;
    }

    protected void setResolvedDest(File resolvedDest) {
        this.resolvedDest = resolvedDest;
        resolvedDestURIString = resolvedDest.toGATURI().toString();
    }

    public File getResolvedDest() {
        // if this sandbox object was retrieved from the advert service, we have
        // to recreate the file object
        if (resolvedDest == null && resolvedDestURIString != null) {
            try {
                resolvedDest = GAT.createFile(gatContext, preferences,
                        resolvedDestURIString);
            } catch (Exception e) {
                throw new Error(e);
            }
        }
        return resolvedDest;
    }

    /**
     * @return the resolvedSrcURIString
     */
    public String getResolvedSrcURIString() {
        return resolvedSrcURIString;
    }

    /**
     * @param resolvedSrcURIString
     *                the resolvedSrcURIString to set
     */
    public void setResolvedSrcURIString(String resolvedSrcURIString) {
        this.resolvedSrcURIString = resolvedSrcURIString;
    }
}
