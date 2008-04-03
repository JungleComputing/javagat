package org.gridlab.gat.io.cpi.gt4;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

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
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

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
abstract public class GT4FileAdaptor extends FileCpi {
	FileResource resource = null;

	String srcProvider;

	static final int DEFAULT_GRIDFTP_PORT = 2811;

	String[] providers = { "gsiftp", "local", "gt2ft", "condor", "ssh",
			"gt4ft", "gt4", "gsiftp-old", "gt3.2.1", "gt2", "ftp", "webdav" };

	/**
	 * Creates new GAT GT4 file object. The constructor is called by the
	 * subclasses.
	 * 
	 * @param gatContext
	 *            GAT context
	 * @param preferences
	 *            GAT preferences
	 * @param location
	 *            FILE location URI
	 * @param prov
	 *            marks the JavaCog provider, possible values are: gt2ft,
	 *            gsiftp, condor, ssh, gt4ft, local, gt4, gsiftp-old, gt3.2.1,
	 *            gt2, ftp, webdav. Aliases: webdav <-> http; local <-> file;
	 *            gsiftp-old <-> gridftp-old; gsiftp <-> gridftp; gt4 <->
	 *            gt3.9.5, gt4.0.2, gt4.0.1, gt4.0.0
	 */
	public GT4FileAdaptor(GATContext gatContext, URI location, String prov)
			throws GATObjectCreationException {
		super(gatContext, location);
		if (prov.equals("local")) {
			if (!location.isCompatible("file")) {
				throw new AdaptorNotApplicableException(
						"cannot handle this URI");
			}
		} else if (prov.equals("gsiftp")) {
			if (!location.isCompatible("gridftp")) {
				throw new AdaptorNotApplicableException(
						"cannot handle this URI");
			}
		}
		srcProvider = prov;
		try {
			resource = AbstractionFactory.newFileResource(srcProvider);
		} catch (Exception e) {
			throw new AdaptorNotApplicableException(
					"GT4FileAdaptor: cannot create FileResource, " + e);
		}
		resource.setName("gt4file: " + Math.random());
		SecurityContext securityContext = null;
		try {
			securityContext = AbstractionFactory
					.newSecurityContext(srcProvider);
			securityContext
					.setCredentials(getCredential(srcProvider, location));
		} catch (Exception e) {
			throw new AdaptorNotApplicableException(
					"GT4FileAdaptor: getSecurityContext failed, " + e);
		}
		resource.setSecurityContext(securityContext);
		ServiceContact serviceContact = new ServiceContactImpl(location
				.getHost(), location.getPort());
		resource.setServiceContact(serviceContact);
		try {
			resource.start();
		} catch (Exception e) {
			throw new AdaptorNotApplicableException(
					"GT4FileAdaptor: resource.start failed, " + e);
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
					"gt4gridftp", loc, DEFAULT_GRIDFTP_PORT);
		} catch (Exception e) {
			throw new GATInvocationException(
					"GT4GridFTPFileAdaptor: could not initialize credentials, "
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
		GridFile gf = null;
		try {
			gf = resource.getGridFile(location.getPath());
			return gf.userCanRead();
		} catch (FileNotFoundException e) {
			throw new GATInvocationException(e.getMessage());
		} catch (GeneralException e) {
			throw new GATInvocationException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#canWrite()
	 */
	public boolean canWrite() throws GATInvocationException {
		GridFile gf = null;
		try {
			gf = resource.getGridFile(location.getPath());
		} catch (FileNotFoundException e) {
			throw new GATInvocationException();
		} catch (GeneralException e) {
			throw new GATInvocationException(e.getMessage());
		}
		return gf.userCanWrite();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#createNewFile()
	 */
	public boolean createNewFile() {
		throw new UnsupportedOperationException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#delete()
	 */
	public boolean delete() throws GATInvocationException {
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#exists()
	 */
	public boolean exists() {
		try {
			return resource.exists(location.getPath());
		} catch (GeneralException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("unable to perform exists: " + e.getMessage());
			}
		} catch (FileNotFoundException e) {
			return false;
		}
		return false;

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
		GridFile gf = null;
		try {
			gf = resource.getGridFile(location.getPath());
			return gf.getAbsolutePathName();
		} catch (FileNotFoundException e) {
			throw new GATInvocationException();
		} catch (GeneralException e) {
			throw new GATInvocationException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getCanonicalFile()
	 */
	public org.gridlab.gat.io.File getCanonicalFile() {
		throw new UnsupportedOperationException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#getCanonicalPath()
	 */
	public String getCanonicalPath() throws GATInvocationException {
		throw new UnsupportedOperationException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isDirectory()
	 */
	public boolean isDirectory() throws GATInvocationException {
		// How should be handled the / in the in the end of the location?
		// Probably a bug in the Cog Toolkit?
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isFile()
	 */
	public boolean isFile() throws GATInvocationException {
		GridFile gf = null;
		try {
			gf = resource.getGridFile(location.getPath());
			return gf.isFile();
		} catch (FileNotFoundException e) {
			throw new GATInvocationException();
		} catch (GeneralException e) {
			throw new GATInvocationException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#isHidden()
	 */
	public boolean isHidden() {
		throw new UnsupportedOperationException("Not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#lastModified()
	 */
	public long lastModified() throws GATInvocationException {
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#length()
	 */
	public long length() throws GATInvocationException {
		GridFile gf = null;
		try {
			gf = resource.getGridFile(location.getPath());
			return gf.getSize();
		} catch (FileNotFoundException e) {
			throw new GATInvocationException(e.getMessage());
		} catch (GeneralException e) {
			throw new GATInvocationException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#list()
	 */
	public String[] list() throws GATInvocationException {
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#mkdir()
	 */
	public boolean mkdir() throws GATInvocationException {
		try {
			resource.createDirectory(location.getPath());
		} catch (GeneralException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
			return false;
		}
		return true;
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#setReadOnly()
	 */
	public boolean setReadOnly() throws GATInvocationException {
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
	}
}
