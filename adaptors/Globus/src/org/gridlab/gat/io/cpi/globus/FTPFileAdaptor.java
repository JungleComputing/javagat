package org.gridlab.gat.io.cpi.globus;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.globus.ftp.FTPClient;
import org.globus.ftp.Session;
import org.globus.ftp.exception.ServerException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

@SuppressWarnings("serial")
public class FTPFileAdaptor extends GlobusFileAdaptor {

    public static String getDescription() {
        return "The FTP File Adaptor implements the File object using FTP.";
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = GlobusFileAdaptor
                .getSupportedCapabilities();
        capabilities.put("exists", true);
        return capabilities;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "ftp", "file", ""};
    }
    
    public static Preferences getSupportedPreferences() {
        Preferences p = GlobusFileAdaptor.getSupportedPreferences();
        return p;
    }
    
    String user;

    String password;

    /**
     * Constructs a LocalFileAdaptor instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param location
     *                A URI which represents the URI corresponding to the
     *                physical file.
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this LocalFileAdaptor.
     */
    public FTPFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);

        List<SecurityContext> l = SecurityContextUtils
                .getValidSecurityContextsByType(gatContext,
                        "org.gridlab.gat.security.PasswordSecurityContext",
                        "ftp", location.resolveHost(), location
                                .getPort(DEFAULT_FTP_PORT));

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
     * @param hostURI
     *                the uri of the FTP host
     */
    protected FTPClient createClient(GATContext context,
            Preferences additionalPreferences, URI hostURI)
            throws GATInvocationException {
        GATContext gatContext = context;
        
        if (additionalPreferences != null) {
            gatContext = (GATContext) context.clone();
            gatContext.addPreferences(additionalPreferences);
        }
        String host = hostURI.resolveHost();

        int port = hostURI.getPort(DEFAULT_FTP_PORT);

        try {
            FTPClient client = new FTPClient(host, port);
            client.authorize(user, password);
            
            if (isPassive(gatContext.getPreferences())) {
                setChannelOptions(client);
            }
            String tmp = (String) gatContext.getPreferences().get("ftp.clientwaitinterval");
            int waitinterval = DEFAULT_WAIT_INTERVAL;
            if (logger.isDebugEnabled()) {
                logger.debug("ftp.clientwaitinterval=" + tmp);
            }
            if ((tmp != null)) {
                try {
                    waitinterval = Integer.parseInt(tmp);
                } catch (NumberFormatException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("failed to parse value '" + tmp
                                + "' for key 'ftp.clientwaitinterval'");
                    }
                    waitinterval = DEFAULT_WAIT_INTERVAL;
                }
            }

            client.setClientWaitParams(30000, waitinterval);
            
            return client;
        } catch (Throwable e) {
            if (e instanceof ServerException) {
                if (((ServerException) e).getCode() == ServerException.SERVER_REFUSED) {
                    if (e
                            .getMessage()
                            .startsWith(
                                    "Server refused performing the request. Custom message: Bad password.")
                            || e
                                    .getMessage()
                                    .startsWith(
                                            "Server refused performing the request. Custom message: Bad user.")) {
                        throw new InvalidUsernameOrPasswordException(e);
                    }
                }
            }
            // ouch, both failed.
            throw new GATInvocationException("FTPFileAdaptor", e);
        }
    }
    
    protected void setImage(FTPClient client) throws GATInvocationException {
        try {
            client.setType(Session.TYPE_IMAGE);
        } catch (Throwable e) {
            throw new GATInvocationException("FTPFileAdaptor", e);
        }
    }

    protected void destroyClient(GATContext context, FTPClient c, URI hostURI) {
        try {
            c.close(true);
        } catch (Exception e) {
            // Ignore
        }
    }

    public boolean exists() throws GATInvocationException {
        FTPClient client = null;

        try {
            String remotePath = getPath();

            if (logger.isDebugEnabled()) {
                logger.debug("getINFO: remotePath = " + remotePath
                        + ", creating client to: " + toURI());
            }
            
            client = createClient(toURI());

            try {

                if (logger.isDebugEnabled()) {
                    logger.debug("getINFO: client created");
                }

                setActiveOrPassive(client, gatContext.getPreferences());

                Vector<?> v = client.list(remotePath);
                if (v.size() > 0) {
                    // Is OK, either for directory list or file list.
                    return true;
                }
                // Here, it could still be an empty directory.
                return isDirectory();
            } finally {
                destroyClient(client, toURI());
            }
        } catch (Exception e) {
            if (e instanceof GATInvocationException) {
                throw (GATInvocationException) e;
            }
            throw new GATInvocationException("FTPFileAdaptor", e);
        } finally {
            if (client != null)
                destroyClient(client, toURI());
        }

    }
}
