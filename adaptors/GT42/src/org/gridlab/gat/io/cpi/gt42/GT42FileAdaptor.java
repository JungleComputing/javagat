package org.gridlab.gat.io.cpi.gt42;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.GeneralException;
import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Permissions;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.security.gt42.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class implementes the
 * {@link org.gridlab.gat.io.cpi.FileCpi FileCpi} class. Represents an Globus
 * file. That implementation uses the JavaCog abstraction layer. The subclasses
 * represent different File adaptors, using different JavaCog abstraction layer
 * providers.
 * 
 * @author Balazs Bokodi
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("serial")
abstract public class GT42FileAdaptor extends FileCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("canRead", true);
        capabilities.put("canWrite", true);
        capabilities.put("delete", true);
        capabilities.put("exists", true);
        capabilities.put("getAbsoluteFile", true);
        capabilities.put("getAbsolutePath", true);
        capabilities.put("isDirectory", true);
        capabilities.put("isFile", true);
        capabilities.put("lastModified", true);
        capabilities.put("length", true);
        capabilities.put("list", true);
        capabilities.put("mkdir", true);
        capabilities.put("setLastModified", true);
        capabilities.put("setReadOnly", true);
        return capabilities;
    }

    FileResource resource = null;

    String srcProvider;

    private boolean localFile = false;

    static final int DEFAULT_GRIDFTP_PORT = 2811;

    String[] providers = { "gsiftp", "local", "gt2ft", "condor", "ssh",
            "gt4ft", "gt4", "gsiftp-old", "gt3.2.1", "gt2", "ftp", "webdav" };

    protected static Logger logger = LoggerFactory.getLogger(GT42FileAdaptor.class);

    /**
     * Creates new GAT GT4 file object. The constructor is called by the
     * subclasses.
     * 
     * @param gatContext
     *                GAT context
     * @param preferences
     *                GAT preferences
     * @param location
     *                FILE location URI
     * @param prov
     *                marks the JavaCog provider, possible values are: gt2ft,
     *                gsiftp, condor, ssh, gt4ft, local, gt4, gsiftp-old,
     *                gt3.2.1, gt2, ftp, webdav. Aliases: webdav <-> http; local
     *                <-> file; gsiftp-old <-> gridftp-old; gsiftp <-> gridftp;
     *                gt4 <-> gt3.9.5, gt4.0.2, gt4.0.1, gt4.0.0
     */
    public GT42FileAdaptor(GATContext gatContext, URI location, String prov)
            throws GATObjectCreationException {
        super(gatContext, location);
        if (prov.equals("local")) {
            if (!location.isCompatible("file")) {
                throw new AdaptorNotApplicableException(
                        "cannot handle this URI: " + location);
            }
        } else if (prov.equals("gsiftp")) {
            if (location.isCompatible("file") && location.refersToLocalHost()) {
                localFile = true;
            } else if (!location.isCompatible("gridftp")) {
                throw new AdaptorNotApplicableException(
                        "cannot handle this URI: " + location);
            }
        }

        srcProvider = prov;
        if (!localFile) {
            try {
                resource = AbstractionFactory.newFileResource(srcProvider);
            } catch (Exception e) {
                throw new AdaptorNotApplicableException(
                        "GT42FileAdaptor: cannot create FileResource, " + e);
            }
            resource.setName("gt42file: " + Math.random());
            SecurityContext securityContext = null;
            try {
                securityContext = AbstractionFactory
                        .newSecurityContext(srcProvider);
                securityContext.setCredentials(getCredential(srcProvider,
                        location));
            } catch (Exception e) {
                throw new AdaptorNotApplicableException(
                        "GT42FileAdaptor: getSecurityContext failed, " + e);
            }
            resource.setSecurityContext(securityContext);
            ServiceContact serviceContact = new ServiceContactImpl(location
                    .getHost(), location.getPort());
            resource.setServiceContact(serviceContact);
            try {
                resource.start();
            } catch (Exception e) {
                throw new AdaptorNotApplicableException(
                        "GT42FileAdaptor: resource.start failed, " + e);
            }
        }
    }

    /**
     * Abstract method implemented by subclasses. Returns a proper
     * <code>SecurityContext</code> object. This object is used by the
     * FileResource (in our case) or by services.
     */
    protected GSSCredential getCredential(String provider, URI loc)
            throws GATInvocationException {
        GSSCredential cred = null;
        if (provider.equalsIgnoreCase("local")
                || provider.equalsIgnoreCase("condor")
                || provider.equalsIgnoreCase("ssh")
                || provider.equalsIgnoreCase("ftp")
                || provider.equalsIgnoreCase("webdav")) {
            return cred;
        }
        try {
            cred = GlobusSecurityUtils.getGlobusCredential(gatContext,
                    "gt42gridftp", loc, DEFAULT_GRIDFTP_PORT);
        } catch (Exception e) {
            throw new GATInvocationException(
                    "GT42GridFTPFileAdaptor: could not initialize credentials, "
                            + e);
        }
        return cred;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#canRead()
     */
    public boolean canRead() throws GATInvocationException {
    	 if (!exists()) {
         	return false;
         }    	
    	if (!localFile) {
            GridFile gf = null;
            try {
                gf = resource.getGridFile(location.getPath());
                return gf.userCanRead();
            } catch (FileNotFoundException e) {
                throw new GATInvocationException(e.getMessage());
            } catch (GeneralException e) {
                throw new GATInvocationException(e.getMessage());
            }
        } else {
            return super.canRead();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#canWrite()
     */
    public boolean canWrite() throws GATInvocationException {
        if (!exists()) {
        	return false;
        }
    	if (!localFile) {
            GridFile gf = null;
            try {
                gf = resource.getGridFile(location.getPath());
            } catch (FileNotFoundException e) {
                throw new GATInvocationException();
            } catch (GeneralException e) {
                throw new GATInvocationException(e.getMessage());
            }
            return gf.userCanWrite();
        } else {
            return super.canWrite();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#delete()
     */
    public boolean delete() throws GATInvocationException {
        if (!localFile) {
            if (isDirectory()) {
                try {
                    resource.deleteDirectory(location.getPath(), true);
                } catch (DirectoryNotFoundException e) {
                    return false;
                } catch (GeneralException e) {
                    throw new GATInvocationException(e.getMessage());
                }
            } else {
                try {
                       resource.deleteFile(location.getPath());
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        } else {
            return super.delete();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#exists()
     */
    public boolean exists() throws GATInvocationException {
        if (!localFile) {
            try {
                return resource.exists(location.getPath());
            } catch (GeneralException e) {
                throw new GATInvocationException("gt42file", e);
            } catch (FileNotFoundException e) {
                throw new GATInvocationException("gt42file", e);
            }
        } else {
            return super.exists();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#getAbsoluteFile()
     */
    public org.gridlab.gat.io.File getAbsoluteFile()
            throws GATInvocationException {
        try {
            return GAT.createFile(gatContext, getAbsolutePath());
        } catch (Exception e) {
            throw new GATInvocationException("cannot create GAT.createFile: "
                    + e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#getAbsolutePath()
     */
    public String getAbsolutePath() throws GATInvocationException {
        if (location.hasAbsolutePath()) {
            return location.getPath();
        }
        if (!localFile) {

            GridFile gf = null;
            try {
                gf = resource.getGridFile(location.getPath());
                return gf.getAbsolutePathName();
            } catch (FileNotFoundException e) {
                throw new GATInvocationException();
            } catch (GeneralException e) {
                throw new GATInvocationException(e.getMessage());
            }
        } else {
            return super.getAbsolutePath();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#isDirectory()
     */
    public boolean isDirectory() throws GATInvocationException {
        // How should be handled the / in the in the end of the location?
        // Probably a bug in the Cog Toolkit?
        if (!exists()) {
            return false;
        }
        if (!localFile) {

            GridFile gf = null;
            try {
                String path = location.getPath();
                gf = resource.getGridFile(path);
                if (gf == null && path.endsWith("/")) {
                    gf = resource.getGridFile(path + '.');
                }
                if (gf == null) {
                    throw new GATInvocationException("GridFile is null");
                }
                return gf.isDirectory();
            } catch (FileNotFoundException e) {
                return false;
                // throw new GATInvocationException();
            } catch (GeneralException e) {
                throw new GATInvocationException(e.getMessage());
            }
        } else {
            return super.isDirectory();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#isFile()
     */
    public boolean isFile() throws GATInvocationException {
        if (!exists()) {
            return false;
        }
        if (!localFile) {

            GridFile gf = null;
            try {
                gf = resource.getGridFile(location.getPath());
                return gf.isFile();
            } catch (FileNotFoundException e) {
                return false;
            } catch (GeneralException e) {
                throw new GATInvocationException(e.getMessage());
            }
        } else {
            return super.isFile();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#lastModified()
     * 
     * Please note there's an issue with the gt42 file adaptor (gt42gridftp). It
     * takes time zones into account where as the
     * {@link java.io.File#lastModified()} doesn't. For instance a file that's
     * last modified on 10 July 1984 at 00:00 GMT +2:00 will have a last
     * modified time of 9 July 1984 at 22:00 using the gt42 file adaptor. The
     * {@link java.io.File#lastModified()} will return 10 July 1984, 00:00.
     */
    public long lastModified() throws GATInvocationException {
    	if (!exists())return 0L;
    	if (!localFile) {
            GridFile gf = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Date d;
            try {
                gf = resource.getGridFile(location.getPath());
                d = sdf.parse(gf.getLastModified());
                if (logger.isInfoEnabled()) {
                    logger.info("Last modified: " + gf.getLastModified());
                }
                return d.getTime();
            } catch (FileNotFoundException e) {
                throw new GATInvocationException(e.getMessage());
            } catch (ParseException e) {
                throw new GATInvocationException(e.getMessage());
            } catch (GeneralException e) {
                throw new GATInvocationException(e.getMessage());
            }
        } else {
            return super.lastModified();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#length()
     */
    public long length() throws GATInvocationException {
        if(!exists())return 0L;
    	if (!localFile) {
            GridFile gf = null;
            try {
                gf = resource.getGridFile(location.getPath());
                return gf.getSize();
            } catch (FileNotFoundException e) {
                throw new GATInvocationException(e.getMessage());
            } catch (GeneralException e) {
                throw new GATInvocationException(e.getMessage());
            }
        } else {
            return super.length();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#list()
     */
    public String[] list() throws GATInvocationException {
        if (!localFile) {
            Collection<?> c;
            if (!isDirectory()) {
                return null;
            }
            try {
                c = resource.list(location.getPath());
            } catch (DirectoryNotFoundException e) {
                throw new GATInvocationException(e.getMessage());
            } catch (GeneralException e) {
                throw new GATInvocationException(e.getMessage());
            }
            String[] res = new String[c.size() - 2];
            Iterator<?> iterator = c.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                GridFile element = (GridFile) iterator.next();
                if (!element.getName().equalsIgnoreCase(".")
                        && !element.getName().equalsIgnoreCase("..")) {
                    res[i] = element.getName();
                    i++;
                }
            }
            return res;
        } else {
            return super.list();
        }
    }

    public boolean createNewFile(){
    	System.out.println("");
    	System.out.println("------createNewFile GT42FileAdaptor does not exist-----");
    	System.out.println("");
    	return false;
    }
    
    
    
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#mkdir()
     */
    public boolean mkdir() throws GATInvocationException {
        if (!localFile) {
            try {
                resource.createDirectory(location.getPath());
            } catch (GeneralException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("", e);
                }
                return false;
            }
            return true;
        } else {
            return super.mkdir();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#renameTo(java.io.File)
     */
    // public boolean renameTo(java.io.File arg0) throws GATInvocationException
    // {
    // resource.rename(location.getPath(), arg0.get
    // }
    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#setLastModified(long)
     */
    public boolean setLastModified(long arg0) throws GATInvocationException {
        if (!localFile) {
            GridFile gf = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Date d = new Date(arg0);
            try {
                gf = resource.getGridFile(location.getPath());
            } catch (GeneralException e) {
                throw new GATInvocationException(e.getMessage());
            } catch (FileNotFoundException e) {
                throw new GATInvocationException(e.getMessage());
            }
            gf.setLastModified(sdf.format(d));
            return true;
        } else {
            return super.setLastModified(arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#setReadOnly()
     */
    public boolean setReadOnly() throws GATInvocationException {
        if (!localFile) {
            GridFile gf = null;
            try {
                gf = resource.getGridFile(location.getPath());
            } catch (FileNotFoundException e) {
                throw new GATInvocationException(e.getMessage());
            } catch (GeneralException e) {
                throw new GATInvocationException(e.getMessage());
            }
            Permissions perm = gf.getUserPermissions();
            perm.setWrite(false);
            gf.setUserPermissions(perm);
            return true;
        } else {
            return super.setReadOnly();
        }
    }
}
