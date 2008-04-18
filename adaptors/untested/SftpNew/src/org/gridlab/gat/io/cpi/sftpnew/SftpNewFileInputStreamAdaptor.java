/*
 * Created on Jun 28, 2005
 */
package org.gridlab.gat.io.cpi.sftpnew;

import java.io.IOException;
import java.io.InputStream;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;

import com.jcraft.jsch.SftpATTRS;

/**
 * @author rob
 */
public class SftpNewFileInputStreamAdaptor extends FileInputStreamCpi {
	InputStream in;

	long available = -1;

	private SftpNewConnection connection;

	public SftpNewFileInputStreamAdaptor(GATContext gatContext, URI location)
			throws GATObjectCreationException {
		super(gatContext, location);

		if (!location.isCompatible("sftp")) {
			throw new GATObjectCreationException("cannot handle this URI");
		}

		// We don't have to handle the local case, the GAT engine will select
		// the local adaptor.
		if (location.getHost() == null) {
			throw new GATObjectCreationException(
					"this adaptor cannot read local files");
		}

		// now try to create a stream.
		try {
			in = createStream();
		} catch (GATInvocationException e) {
			throw new GATObjectCreationException("SftpNewFileInputStream", e);
		}
	}

	protected InputStream createStream() throws GATInvocationException {
		String path = location.getPath();

		try {
			connection = SftpNewFileAdaptor.createChannel(gatContext, location);

			SftpATTRS attr = connection.channel.lstat(location.getPath());
			available = attr.getSize();

			return connection.channel.get(path);
		} catch (Exception e) {
			throw new GATInvocationException("SftpNewFileInputStream", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	public int available() throws GATInvocationException {
		return (int) available;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	public void close() throws GATInvocationException {
		if (in != null) {
			try {
				in.close();
			} catch (Throwable t) { // ignore
			}
		}

		SftpNewFileAdaptor.closeChannel(connection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws GATInvocationException {
		try {
			int res = in.read();
			if (res >= 0)
				available--;
			return res;
		} catch (IOException e) {
			throw new GATInvocationException("SftpNewFileInputStream", e);
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
			int res = in.read(b, offset, len);
			if (res >= 0)
				available -= res;
			return res;
		} catch (IOException e) {
			throw new GATInvocationException("SftpNewFileInputStream", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] arg0) throws GATInvocationException {
		try {
			int res = in.read(arg0);
			if (res >= 0)
				available -= res;
			return res;
		} catch (IOException e) {
			throw new GATInvocationException("SftpNewFileInputStream", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long arg0) throws GATInvocationException {
		try {
			long res = in.skip(arg0);
			if (res >= 0)
				available -= res;
			return res;
		} catch (IOException e) {
			throw new GATInvocationException("SftpNewFileInputStream", e);
		}
	}
}
