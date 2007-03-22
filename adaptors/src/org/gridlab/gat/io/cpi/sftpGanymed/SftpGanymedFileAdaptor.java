/*
 * Created on Mar 21, 2007 by rob
 */
package org.gridlab.gat.io.cpi.sftpGanymed;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.FileCpi;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import ch.ethz.ssh2.SFTPv3FileAttributes;

public class SftpGanymedFileAdaptor extends FileCpi {
    static final int SSH_PORT = 22;

    static final boolean USE_CLIENT_CACHING = true;

    private static Hashtable clienttable = new Hashtable();

    public SftpGanymedFileAdaptor(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("sftp") && !location.isCompatible("file")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }
    }

    private static String getClientKey(URI hostURI, Preferences preferences) {
        return hostURI.resolveHost() + ":" + hostURI.getPort(SSH_PORT)
                + preferences; // include preferences in key
    }

    private static synchronized SftpGanymedConnection getFromCache(String key) {
        SftpGanymedConnection client = null;
        if (clienttable.containsKey(key)) {
            client = (SftpGanymedConnection) clienttable.remove(key);
        }
        return client;
    }

    private static synchronized boolean putInCache(String key,
            SftpGanymedConnection c) {
        if (!clienttable.containsKey(key)) {
            clienttable.put(key, c);
            return true;
        }
        return false;
    }

    private SftpGanymedConnection openConnection(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATInvocationException {
        if (!USE_CLIENT_CACHING) {
            return doWorkcreateConnection(gatContext, preferences, location);
        }

        SftpGanymedConnection c = null;

        String key = getClientKey(location, preferences);
        c = getFromCache(key);

        if (c != null) {
            try {
                // test if the client is still alive
                c.sftpClient.stat(".");

                if (GATEngine.DEBUG) {
                    System.err.println("using cached client");
                }
            } catch (Exception except) {
                if (GATEngine.DEBUG) {
                    System.err.println("could not reuse cached client: "
                            + except);
                    except.printStackTrace();
                }

                c = null;
            }
        }

        if (c == null) {
            c = doWorkcreateConnection(gatContext, preferences, location);
        }

        return c;
    }

    private static SftpGanymedConnection doWorkcreateConnection(
            GATContext gatContext, Preferences preferences, URI location)
            throws GATInvocationException {
        SftpGanymedConnection res = new SftpGanymedConnection();
        res.remoteMachine = location;
        
        try {
            res.userInfo =
                    SftpGanymedSecurityUtils.getSftpCredential(gatContext,
                            preferences, "sftpGanymed", location, SSH_PORT);
        } catch (CouldNotInitializeCredentialException e) {
            throw new GATInvocationException("sftpGanymed", e);
        } catch (CredentialExpiredException e2) {
            throw new GATInvocationException("sftpGanymed", e2);
        }

        int port = location.getPort();
        if (port == -1) {
            port = SSH_PORT;
        }

        res.connection = new Connection(location.resolveHost(), port);

        try {
            res.connection.connect();
        } catch (IOException e) {
            throw new GATInvocationException("sftpGanymed", e);
        }

        boolean authenticated = false;
        try {
            if (res.userInfo.password != null
                    && res.connection.isAuthMethodAvailable(
                            res.userInfo.username, "password")) {
                authenticated =
                        res.connection.authenticateWithPassword(
                                res.userInfo.username, res.userInfo.password);
            }
        } catch (IOException e) {
            res.connection.close();
            throw new GATInvocationException("sftpGanymed", e);
        }

        if (!authenticated) {
            // try key-based authentication
            try {
                authenticated =
                        res.connection.authenticateWithPublicKey(
                                res.userInfo.username, res.userInfo.privateKey,
                                res.userInfo.password);
            } catch (IOException e) {
                res.connection.close();
                throw new GATInvocationException("sftpGanymed", e);
            }
        }

        if (!authenticated)
            throw new GATInvocationException("Unable to authenticate");

        try {
            res.sftpClient = new SFTPv3Client(res.connection);
        } catch (IOException e) {
            res.connection.close();
            throw new GATInvocationException("sftpGanymed", e);
        }

        return res;
    }

    public static void closeConnection(SftpGanymedConnection c)
            throws GATInvocationException {
        if (!USE_CLIENT_CACHING) {
            doWorkCloseConnection(c);
            return;
        }

        String key = getClientKey(c.remoteMachine, c.preferences);

        if (!putInCache(key, c)) {
            try {
                if (GATEngine.DEBUG) {
                    System.err.println("closing client");
                }

                doWorkCloseConnection(c);
            } catch (Exception e) {
                if (GATEngine.DEBUG) {
                    System.err
                            .println("end of sftpGanymed adaptor, closing client, got exception (ignoring): "
                                    + e);
                }

                // ignore
            }
        }
    }

    public static void doWorkCloseConnection(SftpGanymedConnection c) {
        c.sftpClient.close();
        c.sftpClient = null;
        c.connection.close();
        c.connection = null;
    }

    public boolean mkdir() throws GATInvocationException {
        SftpGanymedConnection c =
                openConnection(gatContext, preferences, location);
        try {
            c.sftpClient.mkdir(getPath(), 0700);
        } catch (IOException e) {
            return false;
        } finally {
            closeConnection(c);
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.FileCpi#isDirectory()
     */
    public boolean isDirectory() throws GATInvocationException {
        SftpGanymedConnection c =
                openConnection(gatContext, preferences, location);
        try {
            SFTPv3FileAttributes attr = c.sftpClient.stat(getPath());
            return attr.isDirectory();
        } catch (IOException e) {
            throw new GATInvocationException("sftpGanymed", e);
        } finally {
            closeConnection(c);
        }
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.FileCpi#delete()
     */
    public boolean delete() throws GATInvocationException {
        SftpGanymedConnection c =
                openConnection(gatContext, preferences, location);

        try {
            SFTPv3FileAttributes attr = c.sftpClient.stat(getPath());
            if (attr.isDirectory()) {
                c.sftpClient.rmdir(getPath());
            } else {
                c.sftpClient.rm(getPath());
            }
        } catch (IOException e) {
            return false;
        } finally {
            closeConnection(c);
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.FileCpi#exists()
     */
    public boolean exists() throws GATInvocationException {
        SftpGanymedConnection c =
                openConnection(gatContext, preferences, location);
        try {
            c.sftpClient.stat(getPath());
        } catch (IOException e) {
            System.err.println("exists: " + e);
            return false;
        } finally {
            closeConnection(c);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.FileCpi#list()
     */
    public String[] list() throws GATInvocationException {
        SftpGanymedConnection c =
                openConnection(gatContext, preferences, location);
        try {
            SFTPv3FileAttributes attr = c.sftpClient.stat(getPath());
            if (!attr.isDirectory()) {
                return null;
            }

            Vector result = c.sftpClient.ls(getPath());
            Vector newRes = new Vector();
            for (int i = 0; i < result.size(); i++) {
                SFTPv3DirectoryEntry entry =
                        (SFTPv3DirectoryEntry) result.get(i);
                if (!entry.filename.equals(".") && !entry.filename.equals("..")) {
                    newRes.add(entry.filename);
                }
            }

            String[] res = new String[newRes.size()];
            for (int i = 0; i < newRes.size(); i++) {
                res[i] = (String) newRes.get(i);
            }
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("sftpGanymed", e);
        } finally {
            closeConnection(c);
        }
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.FileCpi#isFile()
     */
    public boolean isFile() throws GATInvocationException {
        SftpGanymedConnection c =
                openConnection(gatContext, preferences, location);
        try {
            SFTPv3FileAttributes attr = c.sftpClient.stat(getPath());
            return attr.isRegularFile();
        } catch (IOException e) {
            throw new GATInvocationException("sftpGanymed", e);
        } finally {
            closeConnection(c);
        }
    }

    /* (non-Javadoc)
     * @see org.gridlab.gat.io.cpi.FileCpi#length()
     */
    public long length() throws GATInvocationException {
        SftpGanymedConnection c =
                openConnection(gatContext, preferences, location);
        try {
            SFTPv3FileAttributes attr = c.sftpClient.stat(getPath());
            return attr.size.longValue();
        } catch (IOException e) {
            throw new GATInvocationException("sftpGanymed", e);
        } finally {
            closeConnection(c);
        }
    }
}
