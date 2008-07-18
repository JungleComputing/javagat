package org.gridlab.gat.advert.cpi.globus;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.xml.rpc.ServiceException;

import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.globus.axis.util.Util;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.engine.GATEngine;

import stubs.IndexServiceProxyService.Entries;
import stubs.IndexServiceProxyService.GetEntries;
import stubs.IndexServiceProxyService.IndexServiceProxyServicePortType;
import stubs.IndexServiceProxyService.service.IndexServiceProxyServiceAddressingLocator;

public class GlobusAdvertServiceAdaptor extends AdvertServiceCpi {
    private static final String SEPERATOR = "/";

    private String pwd = SEPERATOR;

    private IndexServiceProxyServicePortType service;

    static {
        Util.registerTransport();
    }

    public GlobusAdvertServiceAdaptor(GATContext gatContext)
            throws GATObjectCreationException {
        super(gatContext);

        // Create EPR
        String serviceURI = (String) gatContext.getPreferences().get(
                "AdvertService.globus.uri");
        EndpointReferenceType epr = new EndpointReferenceType();
        try {
            epr.setAddress(new Address(serviceURI));
        } catch (Exception e) {
            // System.err.println("ERROR: Malformed URI '" + serviceURI + "'");
            // e.printStackTrace();
            throw new GATObjectCreationException("ERROR: Malformed URI '"
                    + serviceURI + "'", e);
        }

        // Get portType
        IndexServiceProxyServiceAddressingLocator locator = new IndexServiceProxyServiceAddressingLocator();

        try {
            this.service = locator.getIndexServiceProxyServicePortTypePort(epr);
        } catch (ServiceException e) {
            // System.err.println("ERROR: Unable to obtain portType");
            // e.printStackTrace();
            throw new GATObjectCreationException(
                    "ERROR: Unable to obtain portType", e);
        }

        // Setup security options
        // ((Stub) factory)._setProperty(Constants.GSI_TRANSPORT,
        // Constants.SIGNATURE);
        // ((Stub) factory)._setProperty(Constants.AUTHORIZATION,
        // NoAuthorization.getInstance());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#getAdvertisable(java.lang.String).
     */
    public Advertisable getAdvertisable(String path)
            throws GATInvocationException, NoSuchElementException {
        path = normalizePath(path);
        stubs.IndexServiceProxyService.Entry e = null;
        try {
            e = this.service.get(path);
        } catch (Exception exc) {
            throw new GATInvocationException("Error in indexService.get()", exc);
        }

        if (e == null)
            return null;
        else {
            Advertisable advert = GATEngine.getGATEngine()
                    .unmarshalAdvertisable(gatContext,
                            e.getSerializedAdvertisable());
            return advert;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#add(org.gridlab.gat.advert.Advertisable,
     *      java.util.Map, java.lang.String)
     */
    public void add(Advertisable advert, MetaData metaData, String path)
            throws GATInvocationException {
        path = normalizePath(path);
        try {
            stubs.IndexServiceProxyService.Entry entry = new stubs.IndexServiceProxyService.Entry();
            entry.setPath(path);
            entry.setMetaData(MetaDataUtils.toSoapMetaData(metaData));
            entry.setSerializedAdvertisable(advert.marshal());
            this.service.put(entry);
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace();

            if (e.getCause() != null) {
                System.err.println("CAUSE: " + e.getCause());
                e.getCause().printStackTrace();
            }

            throw new GATInvocationException("globus advert", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#delete(java.lang.String)
     */
    public void delete(String path) throws NoSuchElementException,
            GATInvocationException {
        path = normalizePath(path);
        try {
            this.service.remove(path);
        } catch (Exception e) {
            throw new GATInvocationException("Error in indexService.remove()",
                    e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#find(java.util.Map)
     */
    public String[] find(MetaData query) throws GATInvocationException {
        Vector<String> res = new Vector<String>();
        Entries entries = null;
        try {
            entries = this.service.getEntries(new GetEntries());
        } catch (Exception e) {
            throw new GATInvocationException(
                    "Error in indexService.getEntries()", e);
        }

        stubs.IndexServiceProxyService.Entry[] entries2 = entries.getEntry();
        for (int i = 0; i < entries2.length; i++) {
            stubs.IndexServiceProxyService.Entry entry = entries2[i];
            String path = entry.getPath();
            MetaData metaData = MetaDataUtils.toMetaData(entry.getMetaData());

            if (metaData.match(query))
                res.add(path);
        }

        String[] s = new String[res.size()];
        for (int i = 0; i < s.length; i++)
            s[i] = (String) res.get(i);

        return s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#getMetaData(java.lang.String)
     */
    public MetaData getMetaData(String path) throws NoSuchElementException,
            GATInvocationException {
        path = normalizePath(path);
        stubs.IndexServiceProxyService.Entry e = null;
        try {
            e = this.service.get(path);
        } catch (Exception exc) {
            throw new GATInvocationException("Error in indexService.get()", exc);
        }

        if (e == null)
            return null;
        else
            return MetaDataUtils.toMetaData(e.getMetaData());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#getPWD()
     */
    public String getPWD() {
        return this.pwd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#setPWD(java.lang.String)
     */
    public void setPWD(String path) {
        this.pwd = path;
    }

    private String normalizePath(String path) throws GATInvocationException {
        try {
            if (!path.startsWith(SEPERATOR))
                path = this.pwd + SEPERATOR + path;

            URI u = new URI(path);
            return u.normalize().getPath();
        } catch (Exception e) {
            throw new GATInvocationException("globus advert", e);
        }
    }
}
