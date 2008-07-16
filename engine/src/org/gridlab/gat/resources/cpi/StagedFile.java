/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileOutputStream;

public abstract class StagedFile {

    protected static Logger logger = Logger.getLogger(StagedFile.class);

    protected GATContext gatContext;

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

    public StagedFile(GATContext context, File origSrc, File origDest,
            String host, String sandbox) {
        super();
        this.gatContext = context;
        this.origSrc = origSrc;
        this.origDest = origDest;
        this.host = host;
        this.sandbox = sandbox;
    }

    protected boolean inSandbox(String path) {
        // if it isn't (1) an absolute path or (2) a path that jumps out of the
        // sandbox, this path is regarded as IN the sandbox. Note that there are
        // a false negatives in the case (1) an absolute path that refers to
        // the sandbox, (2) a relative path like "../sandbox/path/to/file".
        try {
            return !((path.startsWith("/")) || (new java.net.URI(path)
                    .normalize().toString().startsWith("..")));
        } catch (URISyntaxException e) {
            // won't happen!
            return true;
        }
    }

    protected File resolve(File f, boolean useNameOnly)
            throws GATInvocationException {
        URI uri = f.toGATURI();
        logger.info("resolving uri: " + uri);
        if (uri.getHost() == null || useNameOnly) {
            try {
                uri = uri.setHost(host);
            } catch (URISyntaxException e) {
                throw new GATInvocationException("StageFile", e);
            }
        }
        logger.info("host done: " + uri);
        if (uri.getScheme() == null) {
            try {
                uri = uri.setScheme("any");
            } catch (URISyntaxException e) {
                throw new GATInvocationException("StageFile", e);
            }
        }
        logger.info("scheme done: " + uri);
        if (useNameOnly && !uri.hasAbsolutePath()) {
            if (f.isDirectory()) {
                try {
                    uri = uri.setPath("");
                } catch (URISyntaxException e) {
                    throw new GATInvocationException("StageFile", e);
                }
            } else {
                try {
                    uri = uri.setPath(f.getName());
                } catch (URISyntaxException e) {
                    throw new GATInvocationException("StageFile", e);
                }
            }
        }
        logger.info("path done: " + uri + "\npath: " + uri.getUnresolvedPath());
        if (!uri.hasAbsolutePath()) {
            try {
                logger.info("setting path to :" + sandbox + "/"
                        + uri.getUnresolvedPath());
                uri = uri.setPath(sandbox + "/" + uri.getUnresolvedPath());
            } catch (URISyntaxException e) {
                throw new GATInvocationException("StageFile", e);
            }
        }
        if (f.isDirectory()) {
            try {
                relativeURI = new URI(f.getPath());
            } catch (URISyntaxException e) {
                throw new GATInvocationException("StageFile", e);
            }
        } else {
            try {
                relativeURI = new URI(f.getName());
            } catch (URISyntaxException e) {
                throw new GATInvocationException("StageFile", e);
            }
        }
        logger.info("sandbox done: " + uri);
        try {
            return GAT.createFile(f.getFileInterface().getGATContext(), uri);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("StageFile", e);
        }
    }

    // /** Creates a file object that points to the sandbox. */
    // protected File resolve(File f, boolean useNameOnly)
    // throws GATInvocationException {
    // // this doesn't work...
    // URI uri = f.toGATURI(); // VU.sleep.stderr
    //
    // String dest = "any://"; // any://
    // dest += (uri.getUserInfo() == null) ? "" : uri.getUserInfo(); // any://
    //
    // String origHost = f.toGATURI().getHost();
    // if (useNameOnly || origHost == null) {
    // dest += host; // any://fs0.das3.cs.vu.nl
    // } else {
    // dest += origHost;
    // }
    //
    // dest += (uri.getPort() == -1) ? "" : (":" + uri.getPort());
    // dest += "/"; // any://fs0.das3.cs.vu.nl/
    //
    // if (inSandbox) {
    // dest += sandbox == null ? "" : sandbox + "/"; //
    // any://fs0.das3.cs.vu.nl/.JavaGAT769/
    // }
    //
    // if (useNameOnly) {
    // // if f is a directory we dont have to put its name to the dest,
    // // because copy dir a to dir b already ends up in b/a so adding the
    // // name would make it b/a/a which is wrong!
    // if (!f.isDirectory()) {
    // java.io.File tmp = new java.io.File(uri.getPath());
    // dest += tmp.getName();
    // try {
    // relativeURI = new URI(tmp.getName());
    // } catch (URISyntaxException e) {
    // // ignore
    // }
    // }
    // } else {
    // dest += uri.getPath();
    // try {
    // relativeURI = new URI(uri.getPath());
    // } catch (URISyntaxException e) {
    // // ignore
    // }
    // }
    //
    // try {
    // URI destURI = new URI(dest);
    // return GAT
    // .createFile(f.getFileInterface().getGATContext(), destURI);
    // } catch (Exception e) {
    // throw new GATInvocationException("StageFile", e);
    // }
    // }

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
            out = GAT.createFileOutputStream(gatContext, f);
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
                resolvedSrc = GAT.createFile(gatContext, resolvedSrcURIString);
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
                resolvedDest = GAT
                        .createFile(gatContext, resolvedDestURIString);
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
