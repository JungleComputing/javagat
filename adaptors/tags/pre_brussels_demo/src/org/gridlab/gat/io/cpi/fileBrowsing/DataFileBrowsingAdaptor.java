package org.gridlab.gat.io.cpi.fileBrowsing;

import java.io.IOException;
import java.net.URI;
import java.rmi.Remote;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.StringHolder;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.holders.UnsignedLongHolder;
import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.transport.GSIHTTPTransport;
import org.globus.axis.util.Util;
import org.globus.gsi.gssapi.auth.Authorization;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.cpi.FileCpi;
import org.ietf.jgss.GSSCredential;

import DATA_browsing_services.DATA_browsingLocator;
import DATA_browsing_services.DATA_browsingPortType;
import DATA_browsing_services.DirectoryEntry;
import DATA_browsing_services.holders.ArrayOfDirectoryEntryHolder;

public class DataFileBrowsingAdaptor extends FileCpi {

	static final String OPERATION_OK = "200 OK";

	SimpleProvider p;

	DATA_browsingPortType data;

	/**
	 * Constructs a DefaultFileAdaptor instance which corresponds to the
	 * physical file identified by the passed URI and whose access rights are
	 * determined by the passed GATContext.
	 * 
	 * @param location
	 *            A URI which represents the URI corresponding to the physical
	 *            file.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this DefaultFileAdaptor.
	 */
	public DataFileBrowsingAdaptor(GATContext gatContext,
			Preferences preferences, URI location)
			throws AdaptorCreationException {
		super(gatContext, preferences, location);

		if (location.getHost() == null) {
			throw new AdaptorCreationException(
					"The DataBrowsing adaptor can only copy remote files");
		}

		try {
			// Prepare httpg handler.
			p = new SimpleProvider();
			p.deployTransport("httpg", new SimpleTargetedChain(
					new GSIHTTPSender()));
			Util.registerTransport();
			DATA_browsingLocator s = new DATA_browsingLocator();
			s.setEngineConfiguration(p);
			data = s.getDATA_browsing();

			// turn on credential delegation, it is turned off by default.
			Stub stub = (Stub) data;
			stub._setProperty(GSIHTTPTransport.GSI_MODE,
					GSIHTTPTransport.GSI_MODE_FULL_DELEG);

		} catch (ServiceException e) {
			throw new AdaptorCreationException(e);
		}
		//        if (!alive()) throw new AdaptorCreationException("service is down");
	}

	protected boolean alive() {
		String res = null;
		try {
			res = data.getServiceDescription();
		} catch (Exception e) {
			return false;
		}
		return res != null;
	}

	/**
	 * Sets properties of Call when default are not enough.
	 */
	void preparePort(Remote ws, GSSCredential proxy, Authorization auth) {
		Stub stub = (Stub) ws;
		stub._setProperty(GSIHTTPTransport.GSI_CREDENTIALS, proxy);
		stub._setProperty(GSIHTTPTransport.GSI_AUTHORIZATION, auth);
		stub._setProperty(GSIHTTPTransport.GSI_MODE,
				GSIHTTPTransport.GSI_MODE_FULL_DELEG);
	}

	public long length() throws GATInvocationException, IOException {
		String res = null;
		UnsignedLongHolder size = null;
		StringHolder response = null;

		data.DATAConnectedSize(toURI().toString(), size, response);

		if (!response.toString().equals(OPERATION_OK)) {
			throw new IOException(response.toString());
		}

		return size.value.longValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#lastModified()
	 */
	public long lastModified() throws GATInvocationException, IOException {
		String res = null;
		LongHolder seconds = null;
		StringHolder response = null;

		data.DATAConnectedModTime(toURI().toString(), seconds, response);

		if (!response.toString().equals(OPERATION_OK)) {
			throw new IOException(response.toString());
		}

		return seconds.value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#list()
	 */
	public String[] list() throws IOException, GATInvocationException {
		DirectoryEntry[] entries = list(toURI().toString());
		String[] res = new String[entries.length];

		for (int i = 0; i < entries.length; i++) {
			res[i] = entries[i].getName();
		}

		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#mkdir()
	 */
	public boolean mkdir() throws GATInvocationException, IOException {
		String res = null;

		res = data.DATAConnectedMkdir(toURI().toString());

		if (!res.toString().equals(OPERATION_OK)) {
			throw new IOException(res);
		}

		return true;
	}

	protected void finalize() {
		String res = null;

		// result can be ignored
		try {
			res = data.DATAStopCache();
		} catch (Throwable t) {
		} // Ignore
	}

	protected DirectoryEntry[] list(String url) throws IOException {
		ArrayOfDirectoryEntryHolder entries = null;
		StringHolder response = null;

		data.DATAConnectedListStructured(url, entries, response);

		if (!response.toString().equals(OPERATION_OK)) {
			throw new IOException(response.toString());
		}

		return entries.value;
	}
}