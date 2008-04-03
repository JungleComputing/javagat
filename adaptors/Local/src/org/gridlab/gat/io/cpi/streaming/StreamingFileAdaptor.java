/*
 * Created on Sep 12, 2007 by roelof
 */
package org.gridlab.gat.io.cpi.streaming;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.io.cpi.FileCpi;

@SuppressWarnings("serial")
public class StreamingFileAdaptor extends FileCpi {

	public StreamingFileAdaptor(GATContext gatContext, URI location) {
		super(gatContext, location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.cpi.FileCpi#copy(org.gridlab.gat.URI)
	 */
	public void copy(URI dest) throws GATInvocationException {
		try {
			FileInputStream in = GAT
					.createFileInputStream(gatContext, location);
			DataInputStream dataIn = new DataInputStream(in);
			File dstFile = GAT.createFile(gatContext, dest);
			FileOutputStream out = GAT.createFileOutputStream(gatContext,
					dstFile);
			DataOutputStream dataOut = new DataOutputStream(out);
			byte[] buffer = new byte[1024];
			while (true) {
				int len = dataIn.read(buffer);
				if (len == -1) {
					break;
				}
				dataOut.write(buffer, 0, len);
			}
			dataIn.close();
			dataOut.flush();
			dataOut.close();
		} catch (Exception e) {
			if (e instanceof GATInvocationException) {
				throw (GATInvocationException) e;
			}
			throw new GATInvocationException("StreamingFileAdaptor", e);
		}
	}

	// this method does *not* work for empty files
	public boolean exists() throws GATInvocationException {
		if (!(location.isCompatible("http") || location.isCompatible("https") || location
				.isCompatible("ftp"))) {
			throw new UnsupportedOperationException("Not implemented");
		} else {
			try {
				FileInputStream in = GAT.createFileInputStream(gatContext,
						location);
				int res = in.read();
				try {
					in.close();
				} catch (IOException e) {
					logger.info("closing failed: " + e);
				}
				return res != -1;
			} catch (GATObjectCreationException e) {
				throw new GATInvocationException("StreamingFileAdaptor"
						+ e.toString("    "));
			} catch (IOException e) {
				throw new GATInvocationException("StreamingFileAdaptor" + e);
			}
		}

	}
}
