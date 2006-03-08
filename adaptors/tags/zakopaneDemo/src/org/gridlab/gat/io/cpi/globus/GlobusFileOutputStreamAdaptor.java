package org.gridlab.gat.io.cpi.globus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;

public abstract class GlobusFileOutputStreamAdaptor extends FileOutputStreamCpi {

	OutputStream out;

	public GlobusFileOutputStreamAdaptor(GATContext gatContext,
			Preferences preferences, URI location, Boolean append)
			throws AdaptorCreationException {
		super(gatContext, preferences, location, append);

		// We don't have to handle the local case, the GAT engine will select
		// the local adaptor.
		if (location.getHost() == null) {
			throw new AdaptorCreationException(
					"this adaptor cannot copy local files");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws IOException {
		out.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int offset, int len) throws IOException {
		out.write(b, offset, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] arg0) throws IOException {
		out.write(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int arg0) throws IOException {
		out.write(arg0);
	}
}