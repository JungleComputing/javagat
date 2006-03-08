package org.gridlab.gat.io.cpi.local;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.IPUtils;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;

public class LocalFileOutputStreamAdaptor extends FileOutputStreamCpi {

	FileOutputStream out;

	public LocalFileOutputStreamAdaptor(GATContext gatContext,
			Preferences preferences, URI location, Boolean append)
			throws IOException, AdaptorCreationException {
		super(gatContext, preferences, location, append);

		String path = null;

		if (location.getScheme() == null || location.getScheme().equals("file")) {
			path = IPUtils.getPath(location);
		} else {
			throw new AdaptorCreationException("not a local file, scheme is: "
					+ location.getScheme());
		}

		java.io.File f = new java.io.File(path);

		out = new FileOutputStream(f, append.booleanValue());
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