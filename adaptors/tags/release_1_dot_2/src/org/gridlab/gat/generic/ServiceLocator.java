/*
 * Created on Sep 15, 2004
 */
package org.gridlab.gat.generic;

import igrid_pkg.IgridLocator;
import igrid_pkg.IgridPortType;

import java.net.URI;

import javax.xml.rpc.ServiceException;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.util.Util;
import org.globus.gsi.gssapi.auth.NoAuthorization;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * @author rob
 */
public class ServiceLocator {

    static ServiceLocator locator;

    SimpleProvider p;

    IgridPortType igrid;

    private ServiceLocator() throws ServiceException, GSSException {
        // Prepare httpg handler.
        p = new SimpleProvider();
        p
            .deployTransport("httpg", new SimpleTargetedChain(
                new GSIHTTPSender()));
        Util.registerTransport();
        IgridLocator s = new IgridLocator();
        s.setEngineConfiguration(p);
        igrid = s.getigrid();

        // turn on credential delegation, it is turned off by default.
        Stub stub = (Stub) igrid;

        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
            .getInstance();
        GSSCredential cred = manager
            .createCredential(GSSCredential.INITIATE_AND_ACCEPT);

        stub._setProperty(GSIConstants.GSI_CREDENTIALS, cred);
        stub._setProperty(GSIConstants.GSI_MODE,
            GSIConstants.GSI_MODE_FULL_DELEG);
        stub
            ._setProperty(GSIConstants.GSI_AUTHORIZATION, new NoAuthorization());

        //		stub._setProperty(GSIConstants.GSI_MODE,
        //				GSIConstants.GSI_MODE_FULL_DELEG);
    }

    public static ServiceLocator getServiceLocator() {
        if (locator == null) {
            try {
                locator = new ServiceLocator();
            } catch (Exception e) {
                return null;
            }
        }

        return locator;
    }

    public URI getServiceLocation(String name) {
        try {
            System.err.println("SERVICE lookup:" + name);
            String s = igrid.lookupWebservice(name, null);
            System.err.println("SERVICE " + name + " is at " + s);
        } catch (Exception e) {
            System.err.println("error: " + e);
            return null;
        }

        return null;
    }
}
