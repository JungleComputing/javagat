package org.gridlab.gat.io.cpi.fileBrowsing;

import DATA_browsing_services.DATA_browsingPortType;
import DATA_browsing_services.DirectoryEntry;

import DATA_browsing_services.holders.ArrayOfDirectoryEntryHolder;

import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.holders.UnsignedLongHolder;

import org.globus.axis.gsi.GSIConstants;

import org.globus.gsi.gssapi.auth.Authorization;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;

import org.ietf.jgss.GSSCredential;

import java.io.IOException;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.StringHolder;

public class DataFileBrowsingAdaptor extends FileCpi {
    static final String OPERATION_OK = "200 OK";

    SimpleProvider p;

    DATA_browsingPortType data;

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
    public DataFileBrowsingAdaptor(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("gsiftp")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        // @@@ broken at the moment.
        throw new GATObjectCreationException(
            "Data browsing is broken at this moment.");

        /*
         * checkName("fileBrowsing");
         *
         * if (location.getHost() == null) { throw new GATObjectCreationException(
         * "The DataBrowsing adaptor can only work with remote files"); }
         *
         * try { // Prepare httpg handler. p = new SimpleProvider();
         * p.deployTransport("httpg", new SimpleTargetedChain( new
         * GSIHTTPSender())); Util.registerTransport(); DATA_browsingLocator s =
         * new DATA_browsingLocator(); s.setEngineConfiguration(p); data =
         * s.getDATA_browsing();
         *  // turn on credential delegation, it is turned off by default. Stub
         * stub = (Stub) data; stub._setProperty(GSIConstants.GSI_MODE,
         * GSIConstants.GSI_MODE_FULL_DELEG);
         *  } catch (ServiceException e) { throw new
         * GATObjectCreationException(e); } // if (!alive()) throw new
         * GATObjectCreationException("service is down");
         */
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
        stub._setProperty(GSIConstants.GSI_CREDENTIALS, proxy);
        stub._setProperty(GSIConstants.GSI_AUTHORIZATION, auth);
        stub._setProperty(GSIConstants.GSI_MODE,
            GSIConstants.GSI_MODE_FULL_DELEG);
    }

    public long length() throws GATInvocationException {
        UnsignedLongHolder size = null;
        StringHolder response = null;

        try {
            data.DATAConnectedSize(toURI().toString(), size, response);

            if (!response.toString().equals(OPERATION_OK)) {
                throw new GATInvocationException(response.toString());
            }
        } catch (RemoteException e) {
            throw new GATInvocationException("data browsing", e);
        }

        return size.value.longValue();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.File#lastModified()
     */
    public long lastModified() throws GATInvocationException {
        LongHolder seconds = null;
        StringHolder response = null;

        try {
            data.DATAConnectedModTime(toURI().toString(), seconds, response);

            if (!response.toString().equals(OPERATION_OK)) {
                throw new GATInvocationException(response.toString());
            }
        } catch (RemoteException e) {
            throw new GATInvocationException("data browsing", e);
        }

        return seconds.value;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.File#list()
     */
    public String[] list() throws GATInvocationException {
        DirectoryEntry[] entries;

        try {
            entries = list(toURI().toString());
        } catch (IOException e) {
            throw new GATInvocationException("data browsing", e);
        }

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
    public boolean mkdir() throws GATInvocationException {
        String res = null;

        try {
            res = data.DATAConnectedMkdir(toURI().toString());

            if (!res.toString().equals(OPERATION_OK)) {
                throw new GATInvocationException(res);
            }
        } catch (RemoteException e) {
            throw new GATInvocationException("data browsing", e);
        }

        return true;
    }

    protected void finalize() {
        // result can be ignored
        try {
            data.DATAStopCache();
        } catch (Throwable t) {
            //			 Ignore
        }
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
