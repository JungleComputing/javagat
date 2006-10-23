package org.gridlab.gat.io.cpi.globus;

import java.util.Enumeration;
import java.util.Hashtable;

import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.FTPClient;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

public class GridFTPFileAdaptor extends GlobusFileAdaptor {
    static final boolean USE_CLIENT_CACHING = true;

    private static Hashtable clienttable = new Hashtable();

    /**
     * Constructs a LocalFileAdaptor instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     *
     * @param location   A URI which represents the URI corresponding to the physical
     *                   file.
     * @param gatContext A GATContext which is used to determine the access rights for
     *                   this LocalFileAdaptor.
     */
    public GridFTPFileAdaptor(GATContext gatContext, Preferences preferences,
        URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("gsiftp") && !location.isCompatible("file")) {
            throw new GATObjectCreationException("cannot handle this URI ("
                + location + ")");
        }

        /* try to get the credential to see whether we need to instantiate this adaptor alltogether */
        try {
            GlobusSecurityUtils.getGlobusCredential(gatContext, preferences,
                "gridftp", location, DEFAULT_GRIDFTP_PORT);
        } catch (Exception e) {
            throw new GATObjectCreationException("gridftp", e);
        }

        /* try to create a client to see if the remote site has a gridftp server */
        /*
         GridFTPClient c = null;

         try {
         c = doWorkCreateClient(gatContext, preferences, location);
         } catch (GATInvocationException e) {
         throw new GATObjectCreationException(
         "Could not create a gridftp connection to " + location, e);
         }

         doWorkDestroyClient(c, location, preferences);
         */
    }

    protected URI fixURI(URI in) {
        return fixURI(in, "gridftp");
    }

    private static void setConnectionOptions(GridFTPClient c,
        Preferences preferences) throws Exception {
        c.setType(GridFTPSession.TYPE_IMAGE);

        //        c.setMode(GridFTPSession.MODE_BLOCK);
    }

    /**
     * Set security parameters such as data channel authentication (defined
     * by the GridFTP protocol) and data channel protection (defined by RFC
     * 2228). If you do not specify these, data channels are authenticated
     * by default.
     */
    private static void setSecurityOptions(GridFTPClient c,
        Preferences preferences) throws Exception {
        if (isOldServer(preferences)) {
            if (GATEngine.DEBUG) {
                System.err
                    .println("setting localNoChannelAuthentication (for old servers)");
            }

            c.setLocalNoDataChannelAuthentication();
        }

        if (noAuthentication(preferences)) {
            if (GATEngine.DEBUG) {
                System.err
                    .println("setting data channelAuthentication to none");
            }

            c.setDataChannelAuthentication(DataChannelAuthentication.NONE);
        }

        int mode = getProtectionMode(preferences);

        if (mode > 0) {
            if (GATEngine.DEBUG) {
                System.err.println("setting data channel proptection to mode "
                    + mode);
            }

            c.setDataChannelProtection(mode);
        }

        // c.setProtectionBufferSize(16384);
        //		c.setDataChannelAuthentication(DataChannelAuthentication.SELF);
        //		c.setDataChannelProtection(GridFTPSession.PROTECTION_SAFE);
        //        c.setType(GridFTPSession.TYPE_IMAGE); //transfertype
        //        c.setMode(GridFTPSession.MODE_EBLOCK); //transfermode
    }

    /**
     * Create an FTP Client.
     *
     * @param hostURI the uri of the FTP host
     */
    protected FTPClient createClient(GATContext gatContext,
        Preferences preferences, URI hostURI) throws GATInvocationException {
        return doWorkCreateClient(gatContext, preferences, hostURI);
    }

    private static String getClientKey(URI hostURI, Preferences preferences) {
        return hostURI.resolveHost() + ":"
            + hostURI.getPort(DEFAULT_GRIDFTP_PORT) + preferences; // include preferences in key
    }

    private static synchronized GridFTPClient getFromCache(String key) {
        GridFTPClient client = null;
        if (clienttable.containsKey(key)) {
            client = (GridFTPClient) clienttable.remove(key);
        }
        return client;
    }

    private static synchronized void putInCache(String key, GridFTPClient c) {
        if (!clienttable.containsKey(key)) {
            clienttable.put(key, c);
        }
    }

    protected static GridFTPClient doWorkCreateClient(GATContext gatContext,
        Preferences preferences, URI hostURI) throws GATInvocationException {
        try {
            GSSCredential credential = GlobusSecurityUtils.getGlobusCredential(
                gatContext, preferences, "gridftp", hostURI,
                DEFAULT_GRIDFTP_PORT);
            String host = hostURI.resolveHost();

            int port = DEFAULT_GRIDFTP_PORT;

            // allow port override
            if (hostURI.getPort() != -1) {
                port = hostURI.getPort();
            }

            if (GATEngine.DEBUG) {
                System.err.println("open gridftp client to " + host + ":"
                    + port);
            }

            GridFTPClient client = null;
            String key = getClientKey(hostURI, preferences);

            if (USE_CLIENT_CACHING) {
                client = getFromCache(key);
                if (client != null) {
                    try {
                        // test if the client is still alive
                        client.getCurrentDir();

                        if (GATEngine.DEBUG) {
                            System.err.println("using cached client");
                        }
                    } catch (Exception except) {
                        if (GATEngine.DEBUG) {
                            System.err
                                .println("could not reuse cached client: "
                                    + except);
                            except.printStackTrace();
                        }

                        client = null;
                    }
                }
            }

            if (client == null) {
                client = new GridFTPClient(host, port);

                if (GATEngine.DEBUG) {
                    System.err.println("authenticating");
                }

                setSecurityOptions(client, preferences);

                // authenticate to the server
                client.authenticate(credential);

                setConnectionOptions(client, preferences);

                if (GATEngine.DEBUG) {
                    System.err.println("setting channel options");
                }

                if (GATEngine.DEBUG) {
                    System.err.println("done");
                }
            }

            return client;
        } catch (Exception e) {
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

        if (USE_CLIENT_CACHING && !clienttable.containsKey(key)) {
            clienttable.put(key, c);
        } else {
            try {
                if (GATEngine.DEBUG) {
                    System.err.println("closing gridftp client");
                }

                c.close();
            } catch (Exception e) {
                if (GATEngine.DEBUG) {
                    System.err
                        .println("doWorkDestroyClient, closing client, got exception (ignoring): "
                            + e);
                }

                // ignore
            }
        }
    }

    public static void end() {
        if (GATEngine.DEBUG) {
            System.err.println("end of gridftp adaptor");
        }

        // destroy the cache
        if (clienttable == null) {
            return;
        }

        Enumeration e = clienttable.elements();

        while (e.hasMoreElements()) {
            GridFTPClient c = (GridFTPClient) e.nextElement();

            try {
                if (GATEngine.DEBUG) {
                    System.err
                        .println("end of gridftp adaptor, closing client");
                }

                c.close(true);
            } catch (Exception x) {
                if (GATEngine.DEBUG) {
                    System.err
                        .println("end of gridftp adaptor, closing client, got exception (ignoring): "
                            + x);
                }

                // ignore
            }
        }

        clienttable.clear();
        clienttable = null;
    }
}
