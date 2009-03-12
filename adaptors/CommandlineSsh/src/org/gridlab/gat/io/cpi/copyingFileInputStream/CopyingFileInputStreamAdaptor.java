/*
 * Created on May 9, 2006
 */
package org.gridlab.gat.io.cpi.copyingFileInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;
import org.gridlab.gat.io.cpi.commandlineSsh.CommandlineSshFileAdaptor;

/**
 * This adaptor tries to copy the file locally with commandline ssh first. Next,
 * we create a local file inputstream to read the file. We do this, because the
 * commandline ssh adaptor is much faster than the Java-based adaptors.
 * 
 * @author rob
 */
public class CopyingFileInputStreamAdaptor extends FileInputStreamCpi {
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileInputStreamCpi
                .getSupportedCapabilities();
        capabilities.put("available", true);
        capabilities.put("close", true);
        capabilities.put("mark", true);
        capabilities.put("markSupported", true);
        capabilities.put("read", true);
        capabilities.put("reset", true);
        capabilities.put("skip", true);

        return capabilities;
    }

    // private static final int MAX_SIZE = 1 * 1024 * 1024;

    FileInputStream in;

    java.io.File localFile;

    public CopyingFileInputStreamAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);

        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (location.getHost() == null) {
            throw new GATObjectCreationException(
                    "this adaptor cannot read local files");
        }

        // See if we can read the file, and get the size.
        // size stuff commented out because the CommandlineSshFileAdaptor does not
        // support f.length! --Ceriel
        // long size = 0;
        CommandlineSshFileAdaptor f = null;
        try {
            f = new CommandlineSshFileAdaptor(gatContext, location);
            // size = f.length();
        } catch (Exception e) {
            throw new GATObjectCreationException("copying inputstream", e);
        }

//        if (size > MAX_SIZE) {
//            throw new GATObjectCreationException(
//                    "copying inputstream: file to large to copy");
//        }

        // now try to create a stream.
        try {
            java.io.File tmp = java.io.File.createTempFile(
                    "JavaGATCopyingStream", "tmp");
            String path = tmp.getPath();
            tmp.delete();

            URI dest = new URI("file:///" + path);
            f.copy(dest);

            localFile = new java.io.File(path);
            in = new FileInputStream(localFile);
        } catch (Exception e) {
            throw new GATObjectCreationException("copying inputstream", e);
        }
    }

    public int available() throws GATInvocationException {
        try {
            return in.available();
        } catch (IOException e) {
            throw new GATInvocationException("CopyingFileInputStream", e);
        }
    }

    public void close() throws GATInvocationException {
        try {
            in.close();
        } catch (IOException e) {
            // ignore
        }

        localFile.delete();
        localFile = null;
        in = null;
    }

    public int read() throws GATInvocationException {
        try {
            return in.read();
        } catch (IOException e) {
            throw new GATInvocationException("CopyingFileInputStream", e);
        }
    }

    public int read(byte[] arg0, int arg1, int arg2)
            throws GATInvocationException {
        try {
            return in.read(arg0, arg1, arg2);
        } catch (IOException e) {
            throw new GATInvocationException("CopyingFileInputStream", e);
        }
    }

    public int read(byte[] arg0) throws GATInvocationException {
        try {
            return in.read(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("CopyingFileInputStream", e);
        }
    }

    public void reset() throws GATInvocationException {
        try {
            in.reset();
        } catch (IOException e) {
            throw new GATInvocationException("CopyingFileInputStream", e);
        }
    }

    public long skip(long arg0) throws GATInvocationException {
        try {
            return in.skip(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("CopyingFileInputStream", e);
        }
    }

    public void mark(int readlimit) {
        in.mark(readlimit);
    }

    public boolean markSupported() {
        return in.markSupported();
    }
}
