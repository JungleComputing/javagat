package org.gridlab.gat.io.cpi.gt4;

import java.io.EOFException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.impl.common.ProviderMethodException;
import org.globus.cog.abstraction.impl.common.task.InvalidProviderException;
import org.globus.cog.abstraction.impl.common.task.InvalidSecurityContextException;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.GeneralException;
import org.globus.cog.abstraction.impl.file.IllegalHostException;
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
import org.ietf.jgss.GSSException;

/**
 * This abstract class implements the {@link org.gridlab.gat.io.cpi.FileCpi
 * FileCpi} class. Represents an Globus file. That implementation uses the
 * JavaCog abstraction layer. The subclasses represent different File adaptors,
 * using different JavaCog abstraction layer providers.
 * 
 * @author Balazs Bokodi
 * @author Bastian Boegel
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("serial")
abstract public class GT4FileAdaptor extends FileCpi {

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

	private GATContext gatContextCopy = null;

	static final int DEFAULT_GRIDFTP_PORT = 2811;

	String[] providers = { "gsiftp", "local", "gt2ft", "condor", "ssh", "gt4ft", "gt4", "gsiftp-old", "gt3.2.1", "gt2",
			"ftp", "webdav" };

	protected static Logger logger = LoggerFactory.getLogger(GT4FileAdaptor.class);

	/**
	 * Creates new GAT GT4 file object. The constructor is called by the
	 * subclasses.
	 * 
	 * @param gatContext GAT context
	 * @param preferences GAT preferences
	 * @param location FILE location URI
	 * @param prov marks the JavaCog provider, possible values are: gt2ft,
	 *            gsiftp, condor, ssh, gt4ft, local, gt4, gsiftp-old, gt3.2.1,
	 *            gt2, ftp, webdav. Aliases: webdav <-> http; local <-> file;
	 *            gsiftp-old <-> gridftp-old; gsiftp <-> gridftp; gt4 <->
	 *            gt3.9.5, gt4.0.2, gt4.0.1, gt4.0.0
	 */
	public GT4FileAdaptor(GATContext gatContext, URI location, String prov) throws GATObjectCreationException {
		super((GATContext) gatContext.clone(), location);
		gatContextCopy = (GATContext) gatContext.clone();
		if (prov.equals("local")) {
			if (!location.isCompatible("file")) {
				throw new AdaptorNotApplicableException("cannot handle this URI: " + location);
			}
		} else if (prov.equals("gsiftp")) {
			if (location.isCompatible("file") && location.refersToLocalHost()) {
				localFile = true;
			} else if (!location.isCompatible("gridftp") && !location.isCompatible("gsiftp")) {
				throw new AdaptorNotApplicableException("cannot handle this URI: " + location);
			}
		}

		srcProvider = prov;
		if (!localFile) {
			try {
				logger.info("XXXXXX: create resource for file");
				resource = AbstractionFactory.newFileResource(srcProvider);
				logger.info("XXXXXX: resource.class: " + resource.getClass().getName());
			} catch (Exception e) {
				throw new AdaptorNotApplicableException("GT4FileAdaptor: cannot create FileResource, " + e);
			}
			resource.setName("gt4file: " + Math.random());
			SecurityContext securityContext = null;
			try {
				securityContext = AbstractionFactory.newSecurityContext(srcProvider);
				securityContext.setCredentials(getCredential(srcProvider, location));
			} catch (Exception e) {
				throw new AdaptorNotApplicableException("GT4FileAdaptor: getSecurityContext failed, " + e);
			}
			resource.setSecurityContext(securityContext);
			ServiceContact serviceContact = new ServiceContactImpl(location.getHost(), location.getPort());
			resource.setServiceContact(serviceContact);
			try {
				resourceStart();
				// resource.start();
				// if (resource.isStarted())
				resourceStop();
			} catch (Exception e) {
				throw new AdaptorNotApplicableException("GT4FileAdaptor: resource.start failed, " + e);
			} finally {

			}
		}
	}

	/**
	 * Abstract method implemented by subclasses. Returns a proper
	 * <code>SecurityContext</code> object. This object is used by the
	 * FileResource (in our case) or by services.
	 */
	protected GSSCredential getCredential(String provider, URI loc) throws GATInvocationException {
		GSSCredential cred = null;
		if (provider.equalsIgnoreCase("local") || provider.equalsIgnoreCase("condor")
				|| provider.equalsIgnoreCase("ssh") || provider.equalsIgnoreCase("ftp")
				|| provider.equalsIgnoreCase("webdav")) {
			return cred;
		}
		try {
			cred = GlobusSecurityUtils.getGlobusCredential(gatContext, "gt4gridftp", loc, DEFAULT_GRIDFTP_PORT);
		} catch (Exception e) {
			throw new GATInvocationException("GT4GridFTPFileAdaptor: could not initialize credentials, " + e);
		}
		return cred;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#canRead()
	 */
	public boolean canRead() throws GATInvocationException {
		if (!localFile) {
			GridFile gf = null;
			try {
				resourceStart();
				gf = resource.getGridFile(location.getPath());
				if (gf == null) {
					// Apparently, this happens for non-existing files.
					return false;
				}
				boolean userCanRead = gf.userCanRead();
				return userCanRead;
			} catch (FileNotFoundException e) {
				throw new GATInvocationException(e.getMessage());
			} catch (GeneralException e) {
				throw new GATInvocationException(e.getMessage());
			} finally {
				resourceStop();
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
		if (!localFile) {
			try {
				resourceStart();
				GridFile gf = null;
				try {
					gf = resource.getGridFile(location.getPath());
				} catch (FileNotFoundException e) {
					throw new GATInvocationException();
				} catch (GeneralException e) {
					throw new GATInvocationException(e.getMessage());
				}
				if (gf == null) {
					// Apparently happens for non-existing files.
					return false;
				}
				boolean canWrite = gf.userCanWrite();
				return canWrite;
			} finally {
				resourceStop();
			}
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
					resourceStart();
					resource.deleteDirectory(location.getPath(), true);
				} catch (DirectoryNotFoundException e) {
					return false;
				} catch (GeneralException e) {
					throw new GATInvocationException(e.getMessage());
				} finally {
					resourceStop();
				}
			} else {
				try {
					resourceStart();
					resource.deleteFile(location.getPath());
				} catch (Exception e) {
					return false;
				} finally {
					resourceStop();
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
				resourceStart();
				boolean exists = resource.exists(location.getPath());
				return exists;
			} catch (GeneralException e) {
				throw new GATInvocationException("gt4file", e);
			} catch (FileNotFoundException e) {
				throw new GATInvocationException("gt4file", e);
			} finally {
				resourceStop();
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
	public org.gridlab.gat.io.File getAbsoluteFile() throws GATInvocationException {
		try {
			return GAT.createFile(gatContext, getAbsolutePath());
		} catch (Exception e) {
			throw new GATInvocationException("cannot create GAT.createFile: " + e);
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
				resourceStart();
				gf = resource.getGridFile(location.getPath());
				String absolutePathName = gf.getAbsolutePathName();
				return absolutePathName;
			} catch (FileNotFoundException e) {
				throw new GATInvocationException();
			} catch (GeneralException e) {
				throw new GATInvocationException(e.getMessage());
			} finally {
				resourceStop();
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
				resourceStart();
				String path = location.getPath();
				gf = resource.getGridFile(path);
				if (gf == null && path.endsWith("/")) {
					gf = resource.getGridFile(path + '.');
				}
				if (gf == null) {
					throw new GATInvocationException("GridFile is null");
				}
				boolean isDirectory = gf.isDirectory();
				return isDirectory;
			} catch (FileNotFoundException e) {
				return false;
				// throw new GATInvocationException();
			} catch (GeneralException e) {
				throw new GATInvocationException(e.getMessage());
			} finally {
				resourceStop();
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
				resourceStart();
				gf = resource.getGridFile(location.getPath());
				boolean isFile = gf.isFile();
				return isFile;
			} catch (FileNotFoundException e) {
				return false;
			} catch (GeneralException e) {
				throw new GATInvocationException(e.getMessage());
			} finally {
				resourceStop();
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
	 * Please note there's an issue with the gt4 file adaptor (gt4gridftp). It
	 * takes time zones into account where as the {@link
	 * java.io.File#lastModified()} doesn't. For instance a file that's last
	 * modified on 10 July 1984 at 00:00 GMT +2:00 will have a last modified
	 * time of 9 July 1984 at 22:00 using the gt4 file adaptor. The {@link
	 * java.io.File#lastModified()} will return 10 July 1984, 00:00.
	 */
	public long lastModified() throws GATInvocationException {
		if (!localFile) {
			GridFile gf = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Date d;
			try {
				resourceStart();
				gf = resource.getGridFile(location.getPath());
				if (gf == null) {
					return 0;
				}
				d = sdf.parse(gf.getLastModified());
				if (logger.isInfoEnabled()) {
					logger.info("Last modified: " + gf.getLastModified());
				}
				long time = d.getTime();
				return time;
			} catch (FileNotFoundException e) {
				throw new GATInvocationException(e.getMessage());
			} catch (ParseException e) {
				throw new GATInvocationException(e.getMessage());
			} catch (GeneralException e) {
				throw new GATInvocationException(e.getMessage());
			} finally {
				resourceStop();
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
		if (!localFile) {
			GridFile gf = null;
			try {
				resourceStart();
				gf = resource.getGridFile(location.getPath());
				if (gf == null) {
					// Apparently can happen for non-existing files.
					return 0;
				}
				long length = gf.getSize();
				return length;
			} catch (FileNotFoundException e) {
				throw new GATInvocationException(e.getMessage());
			} catch (GeneralException e) {
				throw new GATInvocationException(e.getMessage());
			} finally {
				resourceStop();
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
				resourceStart();
				c = resource.list(location.getPath());
			} catch (DirectoryNotFoundException e) {
				throw new GATInvocationException(e.getMessage());
			} catch (GeneralException e) {
				throw new GATInvocationException(e.getMessage());
			} finally {
				resourceStop();
			}
			String[] res = new String[c.size() - 2];
			Iterator<?> iterator = c.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				GridFile element = (GridFile) iterator.next();
				if (!element.getName().equalsIgnoreCase(".") && !element.getName().equalsIgnoreCase("..")) {
					res[i] = element.getName();
					i++;
				}
			}
			return res;
		} else {
			return super.list();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.io.File#mkdir()
	 */
	public boolean mkdir() throws GATInvocationException {
		if (!localFile) {
			try {
				logger.info("XXXXXX: mkdir(): before resourceStart");
				resourceStart();
				logger.info("XXXXXX: mkdir(): createdir");
				resource.createDirectory(location.getPath());
			} catch (GeneralException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("", e);
				}
				return false;
			} finally {
				logger.info("XXXXXX: mkdir(): resource.stop");
				resourceStop();
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
				resourceStart();
				gf = resource.getGridFile(location.getPath());
				gf.setLastModified(sdf.format(d));
				return true;
			} catch (GeneralException e) {
				throw new GATInvocationException(e.getMessage());
			} catch (FileNotFoundException e) {
				throw new GATInvocationException(e.getMessage());
			} finally {
				resourceStop();
			}
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
				resourceStart();
				gf = resource.getGridFile(location.getPath());
				Permissions perm = gf.getUserPermissions();
				perm.setWrite(false);
				gf.setUserPermissions(perm);
				return true;
			} catch (FileNotFoundException e) {
				throw new GATInvocationException(e.getMessage());
			} catch (GeneralException e) {
				throw new GATInvocationException(e.getMessage());
			} finally {
				resourceStop();
			}
		} else {
			return super.setReadOnly();
		}
	}

	/**
	 * opens a connection to the resource
	 * 
	 * @throws GATInvocationException
	 */
	protected void resourceStart() throws GATInvocationException {
		int max_tries = 100;
		int i = 0;
		GATInvocationException error = null;
		while (true) {
			i++;
			try {
				logger.debug("resource status (try " + i + "): "
						+ (resource == null ? "null" : "" + resource.isStarted()));
				if ((resource != null) && (resource.isStarted())) {
					try {
						resource.stop();
					} catch (Exception e) {
						// ignore exception on closing
					}
					resource = null;
				}
				if ((resource == null) || (!resource.isStarted())) {
					resource = AbstractionFactory.newFileResource(srcProvider);
					resource.setName("gt4file: " + Math.random());
					ServiceContact serviceContact = new ServiceContactImpl(location.getHost(), location.getPort());
					resource.setServiceContact(serviceContact);

					SecurityContext securityContext = null;
					securityContext = AbstractionFactory.newSecurityContext(srcProvider);
					GSSCredential cred = null;
					try {
						cred = getCredential(srcProvider, location);
						logger.debug("old cred");
						if (cred != null) {
							if (cred.getRemainingLifetime() <= 0)
								cred = null;
						}
					} catch (Exception e) {
						// ignore --> try again with the original context
						logger.info("cannot find valid credential", e);
					}
					if (cred == null) {
						// try with the original context
						logger.debug("new Cred");
						gatContext = (GATContext) gatContextCopy.clone();
						cred = getCredential(srcProvider, location);
					}
					try {
						logger.debug("Remaining Lifetime: " + cred.getRemainingLifetime());
					} catch (GSSException e) {
						logger.debug("ERROR getting remaining Lifetime");
					}
					securityContext.setCredentials(cred);
					resource.setSecurityContext(securityContext);

					resource.start();
					if (resource.isStarted())
						return;
					logger.info("resource Started (" + location.getPath() + "/try " + i + "): " + resource.isStarted());
				}
			} catch (IllegalHostException e) {
				logger.warn("XXXXX_EX IllegalHostException raised (" + location.getPath() + "/try " + i + "): "
						+ e.getMessage(), e);
				error = new GATInvocationException(e.getMessage());
				logger.warn("XXXXX_EX IllegalHostException raised (" + location.getPath() + "/try " + i + "): "
						+ e.getMessage());
			} catch (InvalidSecurityContextException e) {
				logger.error("XXXXX_EX InvalidSecurityContextException raised (" + location.getPath() + "/try " + i
						+ "): " + e.getMessage(), e);
				error = new GATInvocationException(e.getMessage());
				logger.warn("XXXXX_EX InvalidSecurityContextException raised (" + location.getPath() + "/try "
						+ i + "): " + e.getMessage());
			} catch (GeneralException e) {
				logger.error("XXXXX_EX GeneralException raised(" + location.getPath() + "/try " + i + "): "
						+ e.getMessage(), e);
				error = new GATInvocationException(e.getMessage());
				if (e.getCause() instanceof EOFException) {
					logger.warn("EOFException");
				} else {
					logger.warn("XXXXX_EX GeneralException raised(" + location.getPath() + "/try " + i + "): "
							+ e.getMessage() + "START STACKTRACE -----");					
					logger.warn("---------- END STACKTRACE ---------");
				}
			} catch (InvalidProviderException e) {
				logger.warn("XXXXX_EX GATInvocationException raised(" + location.getPath() + "/try " + i + "): "
						+ e.getMessage(), e);
				error = new GATInvocationException(e.getMessage());
				logger.warn("XXXXX_EX GATInvocationException raised(" + location.getPath() + "/try " + i + "): "
						+ e.getMessage());
			} catch (ProviderMethodException e) {
				logger.warn("XXXXX_EX ProviderMethodException raised(" + location.getPath() + "/try " + i + "): "
						+ e.getMessage(), e);
				error = new GATInvocationException(e.getMessage());
				logger.warn("XXXXX_EX ProviderMethodException raised(" + location.getPath() + "/try " + i
						+ "): " + e.getMessage());
			}
			if (i < max_tries) {
				try {
					Random generator = new Random();
					// waiting a random time (1..6 seconds)
					Thread.sleep(generator.nextInt(6000) + 1000);
				} catch (InterruptedException e1) {
					// ignore
				}
				logger.warn("Exception in try " + i + " for location " + location.toString());
				continue;
			} else
				throw new GATInvocationException("ERROR");
		}
	} // protected void resourceStart() throws GATInvocationException

	/**
	 * closes the connection to the resource
	 * 
	 * @throws GATInvocationException
	 */
	protected void resourceStop() throws GATInvocationException {
		if (resource.isStarted()) {
			try {
				resource.stop();
			} catch (GeneralException e) {
				logger.error("XXXXX error while closing socket: " + e.getMessage(), e);
				throw new GATInvocationException(e.getMessage());
			}
		}
	} // protected void resourceStop() throws GATInvocationException

}
