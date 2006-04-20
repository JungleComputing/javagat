/*
 * Created on Dec 7, 2004
 */
package org.gridlab.gat.io.cpi.replicaService;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;

import org.globus.axis.gsi.GSIConstants;
import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.util.Util;

import org.gridforum.jgss.ExtendedGSSManager;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.io.LogicalFile;
import org.gridlab.gat.io.cpi.LogicalFileCpi;

import org.ietf.jgss.GSSCredential;

import zibdms_pkg.ZibdmsLocator;
import zibdms_pkg.ZibdmsPortType;

import zibdms_pkg.holders.StringArrayHolder;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Vector;

import javax.xml.rpc.holders.BooleanHolder;
import javax.xml.rpc.holders.StringHolder;

/**
 * @author rob
 */
public class ReplicaServiceAdaptor extends LogicalFileCpi {
    static {
        // we must tell the gat engine that we can unmarshal logical files.
        //    	GATEngine.registerAdvertisable(ReplicaServiceAdaptor.class);
    }

    static final String FILE_TYPE = "FILE";

    static final String OPERATION_OK = "200 OK";

    static final String NOT_FOUND = "404";

    static final String ALREADY_EXISTS = "400";

    SimpleProvider p;

    ZibdmsPortType replica;

    String absName; // replica service needs absolute paths.

    /**
     * @param gatContext
     * @param preferences
     * @param location
     * @throws GATObjectCreationException
     */
    public ReplicaServiceAdaptor(GATContext gatContext,
            Preferences preferences, String name, Integer mode)
            throws GATObjectCreationException, GATObjectCreationException {
        super(gatContext, preferences, name, mode);

        if (name.startsWith("/")) {
            absName = name;
        } else {
            String userDir = System.getProperty("user.home");

            if (userDir == null) {
                userDir = "/tmp";
            }

            absName = userDir + "/" + name;
        }

        // in the replica service, names must be absolute.
        try {
            // Prepare httpg handler.
            p = new SimpleProvider();
            p.deployTransport("httpg", new SimpleTargetedChain(
                new GSIHTTPSender()));
            Util.registerTransport();

            ZibdmsLocator s = new ZibdmsLocator();
            s.setEngineConfiguration(p);

            replica = s.getzibdms();

            // turn on credential delegation, it is turned off by default.
            Stub stub = (Stub) replica;

            ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager
                .getInstance();
            GSSCredential cred = manager
                .createCredential(GSSCredential.INITIATE_AND_ACCEPT);

            stub._setProperty(GSIConstants.GSI_CREDENTIALS, cred);
            stub._setProperty(GSIConstants.GSI_MODE,
                GSIConstants.GSI_MODE_FULL_DELEG);

            //			stub._setProperty(GSIConstants.GSI_AUTHORIZATION,
            //					new NoAuthorization());

            /*
             * System.err.println("Calling getServiceDesc:"); StringHolder
             * description = new StringHolder(); StringHolder ret = new
             * StringHolder();
             *
             * replica.getServiceDescription("aap", description, ret);
             *
             * System.err.println("descr = " + description.value + ", ret = " +
             * ret.value);
             */
            switch (this.mode) {
            case LogicalFile.OPEN: // open existing file

                if (!exists(absName)) {
                    throw new GATObjectCreationException(
                        "The logical file does not exist.");
                }

                create(absName);

                break;

            case LogicalFile.CREATE: // create if it does not exist.

                if (exists(absName)) {
                    throw new GATObjectCreationException(
                        "The logical file does already exist.");
                }

                create(absName);

                break;

            case LogicalFile.TRUNCATE: // create it, no matter what

                if (exists(absName)) { // if it exists, delete it

                    if (!isFile(absName)) {
                        throw new GATObjectCreationException(
                            "The logical file cannot be created, a directory with that name already exist.");
                    }

                    String ret = replica.rm(absName);

                    if (!ret.equals(OPERATION_OK)) {
                        throw new GATObjectCreationException(ret);
                    }
                }

                create(absName);

                break;

            default:
                throw new GATObjectCreationException(
                    "illegal mode in Logical file");
            }
        } catch (Exception e) {
            throw new GATObjectCreationException("replica service", e);
        }
    }

    private void create(String testName) throws GATInvocationException,
            GATObjectCreationException {
        String ret = null;

        try {
            // we have to create all directories in the path, if they don't exist yet            
            int pos = testName.lastIndexOf("/");

            if (pos == -1) {
                throw new Error("internal error");
            }

            String dir = testName.substring(0, pos);
            ret = replica.mkdirhier(dir);

            if (!ret.equals(OPERATION_OK) && !ret.startsWith(ALREADY_EXISTS)) {
                throw new GATObjectCreationException(ret);
            }

            ret = replica.create(testName, FILE_TYPE);
        } catch (Exception e) {
            throw new GATInvocationException("gridlab logical file", e);
        }

        if (!ret.equals(OPERATION_OK)) {
            throw new GATObjectCreationException(ret);
        }
    }

    private boolean exists(String testName) throws GATObjectCreationException,
            GATObjectCreationException {
        BooleanHolder result = new BooleanHolder();
        StringHolder ret = new StringHolder();

        try {
            replica.existsEntity(testName, result, ret);
        } catch (Exception e) {
            throw new GATObjectCreationException("gridlab logical file", e);
        }

        System.err.println("RET = " + ret.value);

        if (ret.value.startsWith(NOT_FOUND)) {
            return false;
        }

        if (!ret.value.equals(OPERATION_OK)) {
            throw new GATObjectCreationException(ret.value);
        }

        return result.value;
    }

    private boolean isFile(String testName) throws GATObjectCreationException {
        BooleanHolder result = new BooleanHolder();
        StringHolder ret = new StringHolder();

        try {
            replica.isFile(testName, result, ret);
        } catch (Exception e) {
            throw new GATObjectCreationException("gridlab logical file", e);
        }

        if (!ret.value.equals(OPERATION_OK)) {
            throw new GATObjectCreationException(ret.value);
        }

        return result.value;
    }

    /* method is not used --Rob
     private boolean isDirectory(String testName) throws GATObjectCreationException {
     BooleanHolder result = new BooleanHolder();
     StringHolder ret = new StringHolder();

     try {
     replica.isDirectory(testName, result, ret);
     } catch (Exception e) {
     throw new GATObjectCreationException("gridlab logical file", e);
     }
     if (!ret.value.equals(OPERATION_OK)) {
     throw new GATObjectCreationException(ret.value);
     }

     return result.value;
     }
     */
    public void addURI(URI location) throws GATInvocationException {
        String ret = null;

        try {
            System.err.println("adding URI: " + location.toString());
            ret = replica.addLocation(absName, location.toString());
        } catch (Exception e) {
            throw new GATInvocationException("gridlab logical file", e);
        }

        if (!ret.equals(OPERATION_OK)) {
            throw new GATInvocationException(ret);
        }

        System.err.println("URIs: " + getURIs());
    }

    public List getURIs() throws GATInvocationException {
        StringArrayHolder locations = new StringArrayHolder();
        StringHolder ret = new StringHolder();

        try {
            replica.getLocations(absName, locations, ret);
        } catch (Exception e) {
            throw new GATInvocationException("gridlab logical file", e);
        }

        if (!ret.value.equals(OPERATION_OK)) {
            throw new GATInvocationException(ret.value);
        }

        Vector res = new Vector();

        for (int i = 0; i < locations.value.getItem().length; i++) {
            String u = locations.value.getItem(i);

            try {
                URI uri = new URI(u);
                res.add(uri);
            } catch (URISyntaxException e) {
                // ignore
            }
        }

        return res;
    }

    public void removeURI(URI location) throws GATInvocationException {
        String ret = null;

        try {
            ret = replica.removeLocation(absName, location.toString());
        } catch (Exception e) {
            throw new GATInvocationException("gridlab logical file", e);
        }

        if (!ret.equals(OPERATION_OK)) {
            throw new GATInvocationException(ret);
        }
    }

    public void replicate(URI loc) throws IOException, GATInvocationException {
        String ret = null;

        try {
            if (isFile(absName)) {
                ret = replica.replicateFileTo(absName, loc.toString());
            } else {
                ret = replica.replicateFileTo(absName, loc.toString());
            }

            if (!ret.equals(OPERATION_OK)) {
                throw new GATInvocationException(ret);
            }
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("gridlab logical file", e);
        }
    }

    /*
     public String marshal() {
     return GATEngine.defaultMarshal(this);
     }

     public static Advertisable unmarshal(String s) {
     return GATEngine.defaultUnmarshal(ReplicaServiceAdaptor.class, s);
     }
     */
}
