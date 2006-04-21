package org.gridlab.gat.io.cpi.dataMovement;

import DATA_movement_services.DATA_movementLocator;
import DATA_movement_services.DATA_movementPortType;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;

import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.util.Util;

import org.globus.gsi.gssapi.auth.NoAuthorization;

import org.gridforum.jgss.ExtendedGSSManager;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.FileCpi;

import org.ietf.jgss.GSSCredential;

import java.rmi.RemoteException;

public class DataMovementFileAdaptor extends FileCpi {
    static final String OPERATION_OK = "200 OK";

    SimpleProvider p;

    DATA_movementPortType data;

    /**
     * Constructs a LocalFileAdaptor instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     *
     * @param location
     *            A URI which represents the URI corresponding to the physical
     *            file.
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this LocalFileAdaptor.
     */
    public DataMovementFileAdaptor(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("gsiftp")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        //        throw new GATObjectCreationException("Data movement is broken");
        try { // Prepare httpg handler. 
            p = new SimpleProvider();
            p.deployTransport("httpg", new SimpleTargetedChain(
                new GSIHTTPSender()));
            Util.registerTransport();

            DATA_movementLocator s = new DATA_movementLocator();
            s.setEngineConfiguration(p);
            data = s.getDATA_movement();

            // turn on credential delegation, it is turned off by default.
            Stub stub = (Stub) data;

            ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
                .getInstance();
            GSSCredential cred = manager
                .createCredential(GSSCredential.INITIATE_AND_ACCEPT);

            stub._setProperty(GSIConstants.GSI_CREDENTIALS, cred);
            stub._setProperty(GSIConstants.GSI_MODE,
                GSIConstants.GSI_MODE_NO_DELEG); // GSI_MODE_FULL_DELEG);

            stub._setProperty(GSIConstants.GSI_AUTHORIZATION,
                new NoAuthorization());
        } catch (Exception e) {
            throw new GATObjectCreationException("datamovement", e);
        }

        if (!alive()) {
            throw new GATObjectCreationException("service is down");
        }
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

    /*
     void preparePort(Remote ws, GSSCredential proxy, Authorization auth) {
     Stub stub = (Stub) ws;
     stub._setProperty(GSIConstants.GSI_CREDENTIALS, proxy);
     stub._setProperty(GSIConstants.GSI_AUTHORIZATION, auth);
     stub._setProperty(GSIConstants.GSI_MODE,
     GSIConstants.GSI_MODE_FULL_DELEG);
     }
     */
    protected static URI fixURI(URI in) {
        return fixURI(in, "gsiftp");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.io.File#copy(java.net.URI)
     */
    public void copy(URI dest) throws GATInvocationException {
        //       if (!alive()) {
        //          throw new GATInvocationException("Gridlab DATA service is down");
        //     }
        //        if (isDirectory()) {
        //           copyDirectory(gatContext, preferences, toURI(), dest);
        //           return;
        //       }
        String sourceURL = fixURI(toURI()).toString();
        String destURL = fixURI(dest).toString();

        if (GATEngine.DEBUG) {
            System.err.println("Gridlab DATA: copying from " + sourceURL
                + " to " + destURL);
        }

        try {
            String res = data.DATACopyFileDefaults(sourceURL, destURL);

            if (!res.equals(OPERATION_OK)) {
                throw new GATInvocationException(res);
            }
        } catch (RemoteException e) {
            throw new GATInvocationException("data movement", e);
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
            res = data.DATADeleteFileDefaults(fixURI(toURI()).toString());
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
    public void move(URI dest) throws GATInvocationException {
        String res = null;

        try {
            res = data.DATAMoveFileDefaults(fixURI(toURI()).toString(), fixURI(
                dest).toString());

            if (!res.equals(OPERATION_OK)) {
                throw new GATInvocationException(res);
            }
        } catch (RemoteException e) {
            throw new GATInvocationException("data movement", e);
        }
    }
}
