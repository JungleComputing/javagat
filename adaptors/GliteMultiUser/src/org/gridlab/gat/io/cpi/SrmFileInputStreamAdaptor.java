package org.gridlab.gat.io.cpi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.gliteMultiUser.srm.SrmConnector;
import org.gridlab.gat.io.cpi.globus.GridFTPFileInputStreamAdaptor;
import org.gridlab.gat.resources.security.gliteMultiUser.GliteSecurityUtils;

/**
 * FileInputStreamAdaptor for SRM.
 * 
 * The adaptor uses the {@link SrmConnector} to retrieve a transport url for the given {@link URI}. It depends on the
 * {@link GridFTPFileInputStreamAdaptor} which reads the data from the srm resource.
 * 
 * @author Stefan Bozic
 */
public class SrmFileInputStreamAdaptor extends FileInputStreamCpi {

	/**
	 * The {@link InputStream} instance that is used for this adaptor.
	 */
	protected InputStream in;

	/** The connector to the srm resource. */
	protected SrmConnector srmConnector;

	/**
	 * Constructs a new instance of SrmFileInputStreamAdaptor.
	 * 
	 * @param gatContext the gat context
	 * @param location the stream location
	 * @throws Exception An exception that might occurs.
	 */
	public SrmFileInputStreamAdaptor(GATContext gatContext, URI location) throws Exception {
		super(gatContext, location);

		if (!location.isCompatible("srm")) {
			throw new AdaptorNotApplicableException("cannot handle this URI: " + location);
		}

		srmConnector = new SrmConnector(GliteSecurityUtils.getPathToUserVomsProxy(gatContext));

		// for streaming we need a gridftp uri
		String gridFtpUrl = srmConnector.getTURLForFileDownload(location);
		this.location = new URI(gridFtpUrl);

		// now try to create a stream.
		try {
			in = createStream();
		} catch (GATInvocationException e) {
			throw new GATObjectCreationException("grid ftp inputstream", e);
		}
	}

	/**
	 * Creates an instance of an GridftpFileInputStream
	 * 
	 * @return an instance of an GridftpFileInputStream
	 * @throws GATInvocationException an exception that might occurs
	 */
	protected InputStream createStream() throws GATInvocationException {
		try {
			Preferences additionalPreferences = new Preferences();
			additionalPreferences.put("ftp.connection.passive", "false");
			additionalPreferences.put("file.adaptor.name", "gridftp");

			InputStream input = GAT.createFileInputStream(gatContext, additionalPreferences, location);

			return input;
		} catch (Exception e) {
			throw new GATInvocationException("gridftp", e);
		}
	}

	/**
	 * @see FileInputStreamCpi#getSupportedCapabilities()
	 */
	public static Map<String, Boolean> getSupportedCapabilities() {
		Map<String, Boolean> capabilities = FileInputStreamCpi.getSupportedCapabilities();
		capabilities.put("available", true);
		capabilities.put("close", true);
		capabilities.put("mark", true);
		capabilities.put("markSupported", true);
		capabilities.put("read", true);
		capabilities.put("reset", true);
		capabilities.put("skip", true);

		return capabilities;
	}

	/**
	 * @see FileInputStreamCpi#getSupportedPreferences()
	 */
	public static Preferences getSupportedPreferences() {
		Preferences preferences = FileInputStreamCpi.getSupportedPreferences();
		preferences.put("ftp.connection.passive", "false");
		return preferences;
	}

	/**
	 * @see java.io.InputStream#available()
	 */
	public int available() throws GATInvocationException {
		try {
			return in.available();
		} catch (IOException e) {
			throw new GATInvocationException("GlobusFileInputStream", e);
		}
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileInputStreamCpi#close()
	 */
	public void close() throws GATInvocationException {
		try {
			in.close();
		} catch (IOException e) {
			throw new GATInvocationException("GlobusFileInputStream", e);
		}
	}

	/**
	 * @see java.io.InputStream#mark(int)
	 */
	public synchronized void mark(int arg0) {
		in.mark(arg0);
	}

	/**
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return in.markSupported();
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws GATInvocationException {
		try {
			return in.read();
		} catch (IOException e) {
			throw new GATInvocationException("GlobusFileInputStream", e);
		}
	}

	/**
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int offset, int len) throws GATInvocationException {
		try {
			return in.read(b, offset, len);
		} catch (IOException e) {
			throw new GATInvocationException("GlobusFileInputStream", e);
		}
	}

	/**
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] arg0) throws GATInvocationException {
		try {
			return in.read(arg0);
		} catch (IOException e) {
			throw new GATInvocationException("GlobusFileInputStream", e);
		}
	}

	/**
	 * @see java.io.InputStream#reset()
	 */
	public synchronized void reset() throws GATInvocationException {
		try {
			in.reset();
		} catch (IOException e) {
			throw new GATInvocationException("GlobusFileInputStream", e);
		}
	}

	/**
	 * @see java.io.InputStream#skip(long)
	 */
	public long skip(long arg0) throws GATInvocationException {
		try {
			return in.skip(arg0);
		} catch (IOException e) {
			throw new GATInvocationException("GlobusFileInputStream", e);
		}
	}
}
