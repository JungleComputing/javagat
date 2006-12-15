/*
 * Created on Oct 20, 2006 by rob
 */
package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileOutputStream;

public abstract class StagedFile {
    GATContext gatContext;

    Preferences preferences;

    File origSrc;

    File origDest;

    File resolvedSrc;

    File resolvedDest;

    String host;

    String sandbox;

    boolean inSandbox;

    URI relativeURI;

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
        
        String origHost = f.toURI().getHost();
        if(useNameOnly || origHost == null) {
            dest += host;
        } else {
            dest += origHost;
        }
        
        dest += (uri.getPort() == -1) ? "" : (":" + uri.getPort());
        dest += "/";
        
        if(inSandbox) {
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
        if(!f.exists()) {
            if(GATEngine.DEBUG) {
                System.err.println("file to wipe does not exists.");
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
            if(out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
