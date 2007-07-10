package org.gridlab.gat.io.cpi.globus;

import java.util.List;

import org.globus.ftp.FTPClient;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

public class FTPFileAdaptor extends GlobusFileAdaptor {
    String user;

    String password;

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
    public FTPFileAdaptor(GATContext gatContext, Preferences preferences,
            URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("ftp") && !location.isCompatible("file")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        List l = SecurityContextUtils.getValidSecurityContextsByType(
            gatContext, preferences,
            "org.gridlab.gat.security.PasswordSecurityContext", "ftp", location
                .resolveHost(), location.getPort(DEFAULT_FTP_PORT));

        if ((l == null) || (l.size() == 0)) {
            throw new GATObjectCreationException(
                "Could not find a valid security context for this " + ""
                    + "adaptor to use for the specified host/port");
        }

        // for now, just take the first one from the list that matches
        PasswordSecurityContext c = (PasswordSecurityContext) l.get(0);
        user = c.getUsername();
        password = c.getPassword();
    }

    protected URI fixURI(URI in) {
        return fixURI(in, "ftp");
    }

    protected static void setChannelOptions(FTPClient client) throws Exception {
        // no options needed for normal ftp case
    }

    /**
     * Create an FTP Client
     *
     * @param hostURI the uri of the FTP host
     */
    protected FTPClient createClient(GATContext gatContext,
            Preferences preferences, URI hostURI) throws GATInvocationException {
        String host = hostURI.resolveHost();

        int port = hostURI.getPort(DEFAULT_FTP_PORT);

        try {
            FTPClient client = new FTPClient(host, port);
            client.authorize(user, password);

            if (isPassive(preferences)) {
                setChannelOptions(client);
            }

            return client;
        } catch (Exception e) {
            // ouch, both failed.
            throw new GATInvocationException("ftp", e);
        }
    }

    protected void destroyClient(FTPClient c, URI hostURI,
            Preferences preferences) {
        try {
            c.close(true);
        } catch (Exception e) {
            // Ignore
        }
    }
}
