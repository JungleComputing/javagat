package org.gridlab.gat.io.cpi.dataMovement;

import java.io.IOException;
import java.net.URI;
import java.rmi.Remote;

import javax.xml.rpc.ServiceException;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.transport.GSIHTTPTransport;
import org.globus.axis.util.Util;
import org.globus.gsi.gssapi.auth.Authorization;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.net.RemoteException;
import org.ietf.jgss.GSSCredential;

import DATA_movement_services.DATA_movementLocator;
import DATA_movement_services.DATA_movementPortType;

public class DataMovementFileAdaptor extends FileCpi {

	static final String OPERATION_OK = "200 OK";

	SimpleProvider p;

	DATA_movementPortType data;

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
	public DataMovementFileAdaptor(GATContext gatContext,
			Preferences preferences, URI location)
			throws AdaptorCreationException {
		super(gatContext, preferences, location);

		if (location.getHost() == null) {
			throw new AdaptorCreationException(
					"The DataMovement adaptor can only copy remote files");
		}

		try {
			// Prepare httpg handler.
			p = new SimpleProvider();
			p.deployTransport("httpg", new SimpleTargetedChain(
					new GSIHTTPSender()));
			Util.registerTransport();
			DATA_movementLocator s = new DATA_movementLocator();
			s.setEngineConfiguration(p);
			data = s.getDATA_movement();

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#copy(java.net.URI)
	 */
	public void copy(URI dest) throws GATInvocationException, IOException,
			RemoteException {
		String sourceURL = toURI().toString();
		String destURL = dest.toString();
		String res = data.DATACopyFileDefaults(sourceURL, destURL);

		if (!res.equals(OPERATION_OK)) {
			throw new IOException(res);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.File#delete()
	 */
	public boolean delete() {
		String res = null;

		try {
			res = data.DATADeleteFileDefaults(toURI().toString());
		} catch (Exception e) {
			return false;
		}

		if (!res.equals(OPERATION_OK)) {
			return false;
		}

		return true;
	}

	// Override inefficient move of super class. Data movement supports a more
	// efficient move.
	public void move(URI location) throws IOException, GATInvocationException {
		String res = null;

		res = data
				.DATAMoveFileDefaults(toURI().toString(), location.toString());

		if (!res.equals(OPERATION_OK)) {
			throw new IOException(res);
		}
	}
}