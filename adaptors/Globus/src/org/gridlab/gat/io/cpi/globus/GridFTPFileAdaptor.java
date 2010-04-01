package org.gridlab.gat.io.cpi.globus;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.FTPClient;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class GridFTPFileAdaptor extends GlobusFileAdaptor {

    public static Preferences getSupportedPreferences() {
        Preferences p = GlobusFileAdaptor.getSupportedPreferences();
        p.put("ftp.connection.protection", "<default taken from globus>");
        p.put("gridftp.authentication.retry", "0");
        return p;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "globus", "gsiftp", "file", ""};
    }
    
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = GlobusFileAdaptor
                .getSupportedCapabilities();
        capabilities.put("exists", true);
        capabilities.put("move", true);
        return capabilities;
    }
    
    protected static Logger logger = LoggerFactory.getLogger(GridFTPFileAdaptor.class);

    static boolean USE_CLIENT_CACHING = true;
    
    private static int cacheSize = 0;

    private static Hashtable<String, ArrayList<GridFTPClient>> clienttable = new Hashtable<String, ArrayList<GridFTPClient>>();

    /**
     * Constructs a GridFTPFileAdaptor instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param location
     *                A URI which represents the URI corresponding to the
     *                physical file.
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this GridFTPFileAdaptor.
     */
    public GridFTPFileAdaptor(GATContext gatContext, URI location)
            throws GATObjectCreationException {
        super(gatContext, location);

        /*
         * Don't try to get the credential if we are dealing with a local file.
         * In that case, getting the credential here is too expensive.
         */
        if (location.isCompatible("file") && location.refersToLocalHost()) {
            return;
        }
        /*
         * try to get the credential to see whether we need to instantiate this
         * adaptor alltogether.
         */
        try {
            GlobusSecurityUtils.getGlobusCredential(gatContext, "gridftp",
                    location, DEFAULT_GRIDFTP_PORT);
        } catch (Exception e) {
            throw new GATObjectCreationException("gridftp", e);
        }
    }

    protected URI fixURI(URI in) {
        return fixURI(in, "gsiftp");
    }
    
    public void move(URI dest) throws GATInvocationException {
        URI uri = toURI();
        if (! uri.refersToLocalHost() && ! dest.refersToLocalHost()) {
            if (uri.getScheme().equals(dest.getScheme()) &&
                    uri.getAuthority().equals(dest.getAuthority())) {
                FTPClient client = null;
                try {
                    client = createClient(uri);
                    // setActiveOrPassive(client, gatContext.getPreferences());
                    if (client.exists(dest.getPath())) {
                        String dir = client.getCurrentDir();
                        try {
                            client.changeDir(dest.getPath());
                            client.changeDir(dir);
                            // Success, so dest was a directory.
                            dest = dest.setPath(dest.getPath() + "/" + getName());
                        } catch(Throwable e) {
                            // ignored
                        }
                    }
                    client.rename(getPath(), dest.getPath());
                    isDirCache.remove(uri);
                    return;
                } catch(Throwable e) {
                } finally {
                    if (client != null) {
                        destroyClient(client, uri, gatContext.getPreferences());
                    }
                }
            }
        }
        super.move(dest);
    }

    public boolean exists() throws GATInvocationException {
        if (cachedInfo != null) {
            return true;
        }

        FTPClient client = null;

        try {
            String remotePath = getPath();

            if (logger.isDebugEnabled()) {
                logger.debug("getINFO: remotePath = " + remotePath
                        + ", creating client to: " + toURI());
            }

            client = createClient(toURI());

            if (logger.isDebugEnabled()) {
                logger.debug("exists: client created");
            }
            return client.exists(remotePath);
        } catch (Exception e) {
            throw new GATInvocationException("globus", e);
        } finally {
            if (client != null) {
                destroyClient(client, toURI(), gatContext.getPreferences());
            }
        }
    }
    
    private static void setConnectionOptions(GridFTPClient c,
            Preferences preferences) throws Exception {
        c.setType(GridFTPSession.TYPE_IMAGE);
        // c.setMode(GridFTPSession.MODE_BLOCK);
    }
    

    private static int getProtectionMode(Preferences preferences) {
        String mode = (String) preferences.get("ftp.connection.protection");

        if (mode == null) {
            return -1;
        }

        if (mode.equalsIgnoreCase("clear")) {
            return GridFTPSession.PROTECTION_CLEAR;
        } else if (mode.equalsIgnoreCase("confidential")) {
            return GridFTPSession.PROTECTION_CONFIDENTIAL;
        } else if (mode.equalsIgnoreCase("private")) {
            return GridFTPSession.PROTECTION_PRIVATE;
        } else if (mode.equalsIgnoreCase("safe")) {
            return GridFTPSession.PROTECTION_SAFE;
        } else {
            throw new Error("Illegal channel protection mode: " + mode);
        }
    }

    /**
     * Set security parameters such as data channel authentication (defined by
     * the GridFTP protocol) and data channel protection (defined by RFC 2228).
     * If you do not specify these, data channels are authenticated by default.
     */
    private static void setSecurityOptions(GridFTPClient c,
            Preferences preferences) throws Exception {
        if (isOldServer(preferences)) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("setting localNoChannelAuthentication (for old servers)");
            }

            c.setLocalNoDataChannelAuthentication();
        }

        if (noAuthentication(preferences)) {
            if (logger.isDebugEnabled()) {
                logger.debug("setting data channelAuthentication to none");
            }

            c.setDataChannelAuthentication(DataChannelAuthentication.NONE);
        }

        int mode = getProtectionMode(preferences);

        if (mode > 0) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("setting data channel proptection to mode "
                                + mode);
            }

            c.setDataChannelProtection(mode);
        }

        // c.setProtectionBufferSize(16384);
        // c.setDataChannelAuthentication(DataChannelAuthentication.SELF);
        // c.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
        // c.setType(GridFTPSession.TYPE_IMAGE); //transfertype
        // c.setMode(GridFTPSession.MODE_EBLOCK); //transfermode
    }

    /**
     * Create an FTP Client.
     * 
     * @param hostURI
     *                the uri of the FTP host
     */
    protected FTPClient createClient(GATContext gatContext,
            Preferences additionalPreferences, URI hostURI)
            throws GATInvocationException, InvalidUsernameOrPasswordException {
        return doWorkCreateClient(gatContext, additionalPreferences, hostURI);
    }

    private static String getClientKey(URI hostURI, Preferences preferences) {
        return hostURI.resolveHost() + ":"
                + hostURI.getPort(DEFAULT_GRIDFTP_PORT) + preferences; // include
        // preferences
        // in
        // key
    }

    private static synchronized GridFTPClient getFromCache(String key) {
        GridFTPClient client = null;
        if (clienttable.containsKey(key)) {
            ArrayList<GridFTPClient> list = clienttable.get(key);
            client = list.remove(0);
            if (list.size() == 0) {
                clienttable.remove(key);
            }
            cacheSize--;
        }
        return client;
    }

    private static synchronized boolean putInCache(String key, FTPClient c) {
        if (cacheSize > 3) {
            return false;
        }
        cacheSize++;
        ArrayList<GridFTPClient> list;
        if (clienttable.containsKey(key)) {
            list = clienttable.get(key);
        } else {
            list = new ArrayList<GridFTPClient>();
            clienttable.put(key, list);
        }
        list.add((GridFTPClient) c);
        return true;
    }

    protected static GridFTPClient doWorkCreateClient(GATContext context,
            Preferences additionalPreferences, URI hostURI)
            throws GATInvocationException, InvalidUsernameOrPasswordException {
        GridFTPClient client = null;
        GATContext gatContext = context;
        
        try {
            if (additionalPreferences != null) {
                gatContext = (GATContext) context.clone();
                gatContext.addPreferences(additionalPreferences);
            }

            String host = hostURI.resolveHost();

            int port = hostURI.getPort(DEFAULT_GRIDFTP_PORT);

            if (logger.isDebugEnabled()) {
                logger.debug("open gridftp client to " + host + ":" + port);
            }
          
            String key = getClientKey(hostURI, gatContext.getPreferences());

            if (USE_CLIENT_CACHING) {
                client = getFromCache(key);
                if (client != null) {
                    try {
                        // test if the client is still alive
                        client.getCurrentDir();

                        if (logger.isDebugEnabled()) {
                            logger.debug("using cached client");
                        }
                    } catch (Exception except) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("could not reuse cached client: "
                                    + except);
                            except.printStackTrace();
                            try {
                                client.close();
                            } catch(Throwable e) {
                                // ignored
                            }
                        }

                        client = null;
                    }
                }
            }

            if (client == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("setting up a client to host: '" + host
                            + "' at port: '" + port + "'");
                }
                client = new GridFTPClient(host, port);

                if (logger.isDebugEnabled()) {
                    logger.debug("authenticating, preferences="
                            + gatContext.getPreferences());
                }

                setSecurityOptions(client, gatContext.getPreferences());

                if (logger.isDebugEnabled()) {
                    logger.debug("security options set");
                }

                // authenticate to the server
                int retry = 1;
                String tmp = (String) gatContext.getPreferences().get(
                        "gridftp.authenticate.retry");
                if (logger.isDebugEnabled()) {
                    logger.debug("gridftp.authenticate.retry=" + tmp);
                }
                if ((tmp != null)) {
                    try {
                        retry = Integer.parseInt(tmp);
                    } catch (NumberFormatException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("failed to parse value '" + tmp
                                    + "' for key 'gridftp.authenticate.retry'");
                        }
                    }
                }
                for (int i = 0; i < retry; i++) {
                    try {
                        
                        GSSCredential credential = GlobusSecurityUtils.getGlobusCredential(
                                gatContext, "gridftp", hostURI, DEFAULT_GRIDFTP_PORT);
                        
                        if (logger.isDebugEnabled()) {
                            logger.debug("createClient: got credential: \n" + (credential == null ? "NULL" : ((GlobusGSSCredentialImpl)credential).getGlobusCredential().toString()));
                        }            
                        
                        client.authenticate(credential);
                        if (logger.isDebugEnabled()) {
                            logger.debug("authenticating done using credential " + credential);
                        }
                    } catch (ServerException se) {
                        if (se.getMessage().contains(
                                "451 active connection to server failed")) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                            continue;
                        }
                    }
                    break;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("setting channel options");
                }

                setConnectionOptions(client, gatContext.getPreferences());
                tmp = (String) gatContext.getPreferences().get("ftp.clientwaitinterval");
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

                if (logger.isDebugEnabled()) {
                    logger.debug("done");
                }
            }

            return client;
        } catch (Exception e) {
            if (client != null) {
                doWorkDestroyClient(client, hostURI, gatContext.getPreferences());
            }
            throw new GATInvocationException("gridftp", e);
        }
    }

    protected void destroyClient(FTPClient c, URI hostURI,
            Preferences preferences) {
        doWorkDestroyClient(c, hostURI, preferences);
    }

    protected static void doWorkDestroyClient(FTPClient c, URI hostURI,
            Preferences preferences) {
        String key = getClientKey(hostURI, preferences);

        if (!USE_CLIENT_CACHING || !putInCache(key, c)) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("closing gridftp client");
                }

                c.close(false);
            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger
                            .info("doWorkDestroyClient, closing client, got exception (ignoring): "
                                    + e);
                }

                // ignore
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("closing gridftp client done");
        }
    }

    public static void end() {
        if (logger.isDebugEnabled()) {
            logger.debug("end of gridftp adaptor");
        }

        USE_CLIENT_CACHING = false;

        // destroy the cache
        if (clienttable == null) {
            return;
        }

        Enumeration<ArrayList<GridFTPClient>> e = clienttable.elements();

        while (e.hasMoreElements()) {
            ArrayList<GridFTPClient> list = e.nextElement();
            for (GridFTPClient c : list) {

                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("end of gridftp adaptor, closing client");
                    }

                    c.close(true);
                } catch (Exception x) {
                    if (logger.isDebugEnabled()) {
                        logger
                        .debug("end of gridftp adaptor, closing client, got exception (ignoring): "
                                + x);
                    }

                    // ignore
                }
            }
        }

        clienttable.clear();
        clienttable = null;
    }
}
