package org.gridlab.gat.io.cpi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.gliteMultiUser.srm.SrmConnector;
import org.gridlab.gat.io.cpi.globus.GridFTPFileOutputStreamAdaptor;
import org.gridlab.gat.resources.security.gliteMultiUser.GliteSecurityUtils;

/**
 * FileOutputStreamAdaptor for SRM.
 * 
 * The adaptor uses the {@link SrmConnector} to retrieve a transport url for the given {@link URI}. It depends on the
 * {@link GridFTPFileOutputStreamAdaptor} which writes the data to the srm resource.
 * 
 * @author Stefan Bozic
 */
public class SrmFileOutputStreamAdaptor extends FileOutputStreamCpi {

	/**
	 * The {@link InputStream} instance that is used for this adaptor.
	 */
	protected OutputStream out;

	/** The connector to the srm resource. */
	protected SrmConnector srmConnector;

	/**
	 * Constructs a new instance of SrmFileInputStreamAdaptor.
	 * 
	 * @param gatContext the gat context
	 * @param location the stream location
	 * @throws Exception An exception that might occurs.
	 */
	public SrmFileOutputStreamAdaptor(GATContext gatContext, URI location, Boolean append) throws Exception {
		super(gatContext, location, append);

		if (!location.isCompatible("srm")) {
			throw new AdaptorNotApplicableException("cannot handle this URI: " + location);
		}

		srmConnector = new SrmConnector(GliteSecurityUtils.getPathToUserVomsProxy(gatContext));

		// for streaming we need a gridftp uri
		String gridFtpUrl = srmConnector.getTURLForFileUpload(null, location);
		this.location = new URI(gridFtpUrl);

		// now try to create a stream.
		try {
			out = createStream();
		} catch (GATInvocationException e) {
			throw new GATObjectCreationException("SrmFileOutputStreamAdaptor", e);
		}
	}

	/**
	 * Creates an instance of an GridftpFileInputStream
	 * 
	 * @return an instance of an GridftpFileInputStream
	 * @throws GATInvocationException an exception that might occurs
	 */
	protected OutputStream createStream() throws GATInvocationException {
		try {
			Preferences additionalPreferences = new Preferences();
			additionalPreferences.put("ftp.connection.passive", "false");
			additionalPreferences.put("file.adaptor.name", "gridftp");
			additionalPreferences.put("ftp.server.noauthentication", "true");

			OutputStream output = GAT.createFileOutputStream(gatContext, additionalPreferences, location, false);

			return output;
		} catch (Exception e) {
			throw new GATInvocationException("SrmFileOutputStreamAdaptor", e);
		}
	}

	/**
	 * @see FileOutputStreamCpi#getSupportedCapabilities()
	 */
	public static Map<String, Boolean> getSupportedCapabilities() {
		Map<String, Boolean> capabilities = FileOutputStreamCpi.getSupportedCapabilities();
		capabilities.put("close", true);
		capabilities.put("flush", true);
		capabilities.put("write", true);

		return capabilities;
	}

	/**
	 * @see java.io.OutputStream#close()
	 */
	public void close() throws GATInvocationException {
		try {
			out.close();
		} catch (IOException e) {
			throw new GATInvocationException("globus output stream", e);
		} finally {
			try {
				srmConnector.finalizeFileUpload(this.location);
			} catch (IOException e) {
				logger.error("Cannot finalizeFileUpload for: " + this.location, e);
			}
		}
	}

	/**
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws GATInvocationException {
		try {
			out.flush();
		} catch (IOException e) {
			throw new GATInvocationException("globus output stream", e);
		}
	}

	/**
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int offset, int len) throws GATInvocationException {
		try {
			out.write(b, offset, len);
		} catch (IOException e) {
			throw new GATInvocationException("globus output stream", e);
		}
	}

	/**
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] arg0) throws GATInvocationException {
		try {
			out.write(arg0);
		} catch (IOException e) {
			throw new GATInvocationException("globus output stream", e);
		}
	}

	/**
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int arg0) throws GATInvocationException {
		try {
			out.write(arg0);
		} catch (IOException e) {
			throw new GATInvocationException("globus output stream", e);
		}
	}

}
