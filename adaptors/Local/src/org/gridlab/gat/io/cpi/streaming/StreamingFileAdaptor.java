/*
 * Created on Sep 12, 2007 by roelof
 */
package org.gridlab.gat.io.cpi.streaming;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.io.cpi.FileCpi;

@SuppressWarnings("serial")
public class StreamingFileAdaptor extends FileCpi {

    public static String getDescription() {
        return "The Streaming File Adaptor only implements the File copy operation, by creating a JavaGAT FileInputStream to read the source, and a JavaGAT FileOutputStream to write the destination.";
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        capabilities.put("exists", true);
        return capabilities;
    }

    public StreamingFileAdaptor(GATContext gatContext, URI location) {
        super(gatContext, location);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.cpi.FileCpi#copy(org.gridlab.gat.URI)
     */
    public void copy(URI dest) throws GATInvocationException {
	DataInputStream dataIn = null;
	DataOutputStream dataOut = null;
        try {
            FileInputStream in = GAT
                    .createFileInputStream(gatContext, location);
            dataIn = new DataInputStream(in);
            FileOutputStream out = GAT.createFileOutputStream(gatContext, dest);
            dataOut = new DataOutputStream(out);
            byte[] buffer = new byte[1024];
            while (true) {
                int len = dataIn.read(buffer);
                if (len == -1) {
                    break;
                }
                dataOut.write(buffer, 0, len);
            }
        } catch (Exception e) {
            if (e instanceof GATInvocationException) {
                throw (GATInvocationException) e;
            }
            throw new GATInvocationException("StreamingFileAdaptor", e);
        } finally {
            if (dataIn != null) {
        	try {
        	    dataIn.close();
        	} catch(Throwable e) {
        	    // ignored
        	}
            }
            if (dataOut != null) {
        	try {
        	    dataOut.flush();
        	} catch(Throwable e) {
        	    // ignored
        	}
        	try {
        	    dataOut.close();
        	} catch(Throwable e) {
        	    // ignored
        	}
            }
        }
    }

    public boolean exists() throws GATInvocationException {
	
	FileInputStream in = null;
	
        if (!(location.isCompatible("http") || location.isCompatible("https") || location
                .isCompatible("ftp"))) {
            throw new UnsupportedOperationException("Not implemented");
        } else {
            try {
                in = GAT.createFileInputStream(gatContext,
                        location);
                in.read();
                return true;
            } catch (GATObjectCreationException e) {
                throw new GATInvocationException("StreamingFileAdaptor"
                        + e.toString("    "));
            } catch (IOException e) {
                return false;
            } finally {
        	if (in != null) {
        	    try {
        		in.close();
        	    } catch (Throwable e) {
        		// ignored
        	    }
        	}
            }
        }

    }
}
