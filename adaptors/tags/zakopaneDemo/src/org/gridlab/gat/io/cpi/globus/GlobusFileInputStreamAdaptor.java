package org.gridlab.gat.io.cpi.globus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;

/**
 * A DefaultFileStreamAdaptor represents a connection to open file, the file may
 * be either remote or local.
 * <p>
 * A DefaultFileStreamAdaptor represents a seekable connection to a file and has
 * semantics similar to a standard Unix filedescriptor. It provides methods to
 * query the current position in the file and to seek to new positions.
 * <p>
 * To Write data to a DefaultFileStreamAdaptor it is necessary to construct a
 * Buffer and pack it with data. Similarly, to read data a buffer must be
 * created to store the read data. Writes and reads may either be blocking, or
 * asynchronous. Asynchronous writes or reads must be completed by appropriate
 * call.
 */
public abstract class GlobusFileInputStreamAdaptor extends FileInputStreamCpi {

	InputStream in;

	public GlobusFileInputStreamAdaptor(GATContext gatContext,
			Preferences preferences, URI location)
			throws AdaptorCreationException {
		super(gatContext, preferences, location);

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
	 * @see java.io.InputStream#available()
	 */
	public int available() throws GATInvocationException {
		try {
			return in.available();
		} catch (IOException e) {
			throw new GATInvocationException("DefaultFileInputStream", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	public void close() throws GATInvocationException {
		try {
			in.close();
		} catch (IOException e) {
			throw new GATInvocationException("DefaultFileInputStream", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#mark(int)
	 */
	public synchronized void mark(int arg0) {
		in.mark(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return in.markSupported();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws GATInvocationException {
		try {
			return in.read();
		} catch (IOException e) {
			throw new GATInvocationException("DefaultFileInputStream", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int offset, int len)
			throws GATInvocationException {
		try {
			return in.read(b, offset, len);
		} catch (IOException e) {
			throw new GATInvocationException("DefaultFileInputStream", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] arg0) throws GATInvocationException {
		try {
			return in.read(arg0);
		} catch (IOException e) {
			throw new GATInvocationException("DefaultFileInputStream", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#reset()
	 */
	public synchronized void reset() throws GATInvocationException {
		try {
			in.reset();
		} catch (IOException e) {
			throw new GATInvocationException("DefaultFileInputStream", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long arg0) throws GATInvocationException {
		try {
			return in.skip(arg0);
		} catch (IOException e) {
			throw new GATInvocationException("DefaultFileInputStream", e);
		}
	}
}