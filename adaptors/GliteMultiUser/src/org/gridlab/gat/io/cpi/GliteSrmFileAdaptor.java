package org.gridlab.gat.io.cpi;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInfo;
import org.gridlab.gat.io.attributes.FileAttributeView;
import org.gridlab.gat.io.attributes.PosixFileAttributeView;
import org.gridlab.gat.io.cpi.gliteMultiUser.srm.SrmConnector;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.security.gliteMultiUser.GliteSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for the SRM (v2) protocol for JavaGAT.
 * 
 * @author: Stefan Bozic
 */
@SuppressWarnings("serial")
public class GliteSrmFileAdaptor extends FileCpi {

	/** String constant for the protocol */
	private static final String SRM_PROTOCOL = "srm";

	/** String constant for the protocol */
	private static final String GSIFTP_PROTOCOL = "gsiftp";

	/** error message for incompatible URIs */
	private static final String CANNOT_HANDLE_THIS_URI = "cannot handle this URI: ";

	/** the adaptor name */
	private static final String GLITE_SRM_FILE_ADAPTOR = "GliteSrmFileAdaptor";

	/** logger instance */
	private static final Logger LOGGER = LoggerFactory.getLogger(GliteSrmFileAdaptor.class);

	/** flag that indicates wheter its a local file or not */
	private final boolean localFile;

	/** The connector to the storage resource. It handles the connection and security issues. */
	private final SrmConnector connector;

	/**
	 * Constructor
	 * 
	 * @param gatCtx the GAT Context
	 * @param location the srm location
	 * @throws GATObjectCreationException an exception that might occurs
	 * @throws GATInvocationException an exception that might occurs
	 */
	public GliteSrmFileAdaptor(GATContext gatCtx, URI location) throws GATObjectCreationException,
			GATInvocationException {
		super(gatCtx, location);

		if (location.isCompatible("file") && location.refersToLocalHost()) {
			localFile = true;
		} else {
			localFile = false;
			if (!location.isCompatible(GliteSrmFileAdaptor.SRM_PROTOCOL)) {
				throw new AdaptorNotApplicableException(GliteSrmFileAdaptor.CANNOT_HANDLE_THIS_URI + location);
			}
		}

		this.connector = new SrmConnector(GliteSecurityUtils.getPathToUserVomsProxy(gatContext));
		LOGGER.info("Instantiated gLiteSrmFileAdaptor for " + location);
	}

	/**
	 * @see FileCpi#getSupportedCapabilities()
	 * */
	public static Map<String, Boolean> getSupportedCapabilities() {
		Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
		capabilities.put("copy", true);
		capabilities.put("delete", true);
		return capabilities;
	}

	/**
	 * Used by CreateDefaultPropertiesFile to generate default javagat.properties.
	 * 
	 * @return Properties and their default values.
	 */
	public static Preferences getSupportedPreferences() {
		Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
		GliteSecurityUtils.addGliteSecurityPreferences(preferences);
		return preferences;
	}

	/**
	 * 
	 */
	public URI getTransportURI() throws GATInvocationException {
		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);
			URI transportURI = connector.getTURLForFileDownload(location);

			return transportURI;

		} catch (Exception e) {
			throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR, e);
		}
	}

	/** {@inheritDoc} */
	public void copy(URI dest) throws GATInvocationException {
		logger.info("copy(URI dest)");
		// GridFtpClient: for 2-party transfer must be DataChannelAuthentication.SELF or DataChannelAuthentication.NONE
		Preferences fileAdapterPrefs = new Preferences();
		fileAdapterPrefs.put("ftp.server.noauthentication", "true");

		try {

			if (toURI().refersToLocalHost() && dest.refersToLocalHost()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Globus file: copy local to local");
				}
				throw new GATInvocationException("cannot copy from local ('" + toURI() + "') to local ('" + dest + "')");
			}

			// localhost -> srm
			if (toURI().refersToLocalHost()) {
				if (!dest.isCompatible(GliteSrmFileAdaptor.SRM_PROTOCOL)
						&& !dest.isCompatible(GliteSrmFileAdaptor.GSIFTP_PROTOCOL)) {
					throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR + ": "
							+ GliteSrmFileAdaptor.CANNOT_HANDLE_THIS_URI + dest);
				}

				GliteSecurityUtils.getVOMSProxy(gatContext, true);
				String proxyFile = GliteSecurityUtils.getPathToUserVomsProxy(gatContext);

				LOGGER.info("SRM/Copy: Uploading " + location + " to " + dest);
				URI turl = connector.getTURLForFileUpload(location, dest);
				LOGGER.info("SRM/Copy: TURL: " + turl.toString());

				GATContext newContext = (GATContext) gatContext.clone();
				newContext.addPreference("File.adaptor.name", "GridFTP");
				newContext.addPreferences(fileAdapterPrefs);
				GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext, proxyFile);

				File transportFile = GAT.createFile(newContext, location);
				transportFile.copy(turl);
				connector.finalizeFileUpload(dest);
				return;
			}

			// srm -> localhost
			if (dest.refersToLocalHost()) {
				GliteSecurityUtils.getVOMSProxy(gatContext, true);
				String proxyFile = GliteSecurityUtils.getPathToUserVomsProxy(gatContext);

				LOGGER.info("SRM/Copy: Downloading " + location + " to " + dest);
				URI turl = connector.getTURLForFileDownload(location);
				LOGGER.info("SRM/Copy: TURL: " + turl);

				GATContext newContext = (GATContext) gatContext.clone();
				newContext.addPreferences(fileAdapterPrefs);
				newContext.addPreference("File.adaptor.name", "GridFTP");

				GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext, proxyFile);
				File transportFile = GAT.createFile(newContext, turl);
				transportFile.copy(dest);
				return;
			}

			// srm -> gsiftp
			if (dest.getScheme().equals("gsiftp")) {
				GliteSecurityUtils.getVOMSProxy(gatContext, true);
				String proxyFile = GliteSecurityUtils.getPathToUserVomsProxy(gatContext);

				LOGGER.info("SRM/Copy: Downloading " + location + " to " + dest);
				URI turl = connector.getTURLForFileDownload(location);
				LOGGER.info("SRM/Copy: TURL: " + turl);

				GATContext newContext = (GATContext) gatContext.clone();
				newContext.addPreferences(fileAdapterPrefs);
				newContext.addPreference("File.adaptor.name", "GridFTP");

				GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext, proxyFile);
				File transportFile = GAT.createFile(newContext, turl);

				transportFile.copy(dest);
			}

			// srm -> srm
			GliteSecurityUtils.getVOMSProxy(gatContext, true);
			String proxyFile = GliteSecurityUtils.getPathToUserVomsProxy(gatContext);

			URI srcUrl = connector.getTURLForFileDownload(location);
			URI destUrl = connector.getTURLForFileUpload(location, dest);

			GATContext newContext = (GATContext) gatContext.clone();
			newContext.addPreference("File.adaptor.name", "GridFTP");
			newContext.addPreferences(fileAdapterPrefs);
			GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext, proxyFile);

			File transportFile = GAT.createFile(newContext, srcUrl);
			File destFile = GAT.createFile(newContext, destUrl);
			transportFile.copy(destFile.toGATURI());
			connector.finalizeFileUpload(dest);
		} catch (Exception e) {
			throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR, e);
		}
	}

	/** {@inheritDoc} */
	public boolean delete() throws GATInvocationException {
		boolean deleteSuccess = false;

		logger.info("delete()");
		if (localFile) {
			throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR + ": " + GliteSrmFileAdaptor.CANNOT_HANDLE_THIS_URI
					+ location);
		}

		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);

			if (isFile()) {
				deleteSuccess = connector.deleteFile(location);
			} else if (isDirectory()) {
				deleteSuccess = connector.deleteDirectory(location);
			}

		} catch (IOException e) {
			LOGGER.info("An exception occurs during deletion of file " + toURI().toString(), e);
		}

		return deleteSuccess;
	}

	/** {@inheritDoc} */
	@Override
	public boolean mkdir() throws GATInvocationException {
		logger.info("mkdir()");
		if (localFile) {
			throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR + ": " + GliteSrmFileAdaptor.CANNOT_HANDLE_THIS_URI
					+ location);
		}

		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);
			connector.mkDir(location);
		} catch (IOException e) {
			LOGGER.info("An exception occurs during mkdir of " + toURI().toString(), e);
			return false;
		}
		return true;
	}

	/**
	 * @see FileCpi#list()
	 */
	public String[] list() throws GATInvocationException {
		logger.info("list()");
		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);

			List<String> result = connector.listFileNames(location);
			return (String[]) result.toArray(new String[result.size()]);
		} catch (IOException e) {
			LOGGER.error("An error occurs during list", e);
			throw new GATInvocationException("An error occurs during list", e);
		}
	}

	/**
	 * Returns the file informations (e.g. name, size, isDir, isFile, permissions).
	 * 
	 * @return Array with informations about the files.
	 * @throws GATInvocationException an exception that might occurs
	 */
	public FileInfo[] listFileInfo() throws GATInvocationException {
		logger.info("listFileInfo()");
		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);

			List<FileInfo> result = connector.listFileInfos(location);
			return (FileInfo[]) result.toArray(new FileInfo[result.size()]);
		} catch (IOException e) {
			LOGGER.error("An error occurs during listFileInfo", e);
			throw new GATInvocationException("An error occurs during listFileInfo", e);
		}
	} // public FileInfo[] listFileInfo() throws GATInvocationException

	/**
	 * @see org.gridlab.gat.io.cpi.FileCpi#exists()
	 */
	public boolean exists() throws GATInvocationException {
		logger.info("exists()");
		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);
			return connector.exists(location);
		} catch (IOException e) {
			LOGGER.error("An error occurs during exists", e);
			throw new GATInvocationException("An error occurs during exists", e);
		}
	}

	/**
	 * @see FileCpi#isFile()
	 */
	public boolean isFile() throws GATInvocationException {
		logger.info("isFile()");
		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);
			return connector.isFile(location);
		} catch (IOException e) {
			LOGGER.error("An error occurs during isFile", e);
			throw new GATInvocationException("An error occurs during isFile", e);
		}
	}

	/**
	 * @see FileCpi#isDirectory()
	 */
	public boolean isDirectory() throws GATInvocationException {
		logger.info("isDirectory()");
		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);
			return connector.isDirectory(location);
		} catch (IOException e) {
			LOGGER.error("An error occurs during isDirectory", e);
			throw new GATInvocationException("An error occurs during isDirectory", e);
		}
	}

	/**
	 * @see FileCpi#renameTo(File)
	 */
	public boolean renameTo(File dest) throws GATInvocationException {
		logger.info("renameTo(File dest)");
		try {
			if (location.getHost().equals(dest.toGATURI().getHost())) {
				GliteSecurityUtils.getVOMSProxy(gatContext, true);
				return connector.renameTo(location, dest.toGATURI());
			} else {
				LOGGER.error("Both files must be on the same host!");
				return false;
			}
		} catch (IOException e) {
			LOGGER.error("An error occurs during renameTo", e);
			throw new GATInvocationException("An error occurs during renameTo", e);
		}
	}

	/**
	 * @see FileCpi#length()
	 */
	public long length() throws GATInvocationException {
		logger.info("length()");
		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);
			return connector.length(location);
		} catch (IOException e) {
			LOGGER.error("An error occurs during isDirectory", e);
			throw new GATInvocationException("An error occurs during isDirectory", e);
		}
	}

	/**
	 * @see FileCpi#getFileAttributeView(Class, boolean)
	 */
	@SuppressWarnings("unchecked")
	public <V extends FileAttributeView> V getFileAttributeView(Class<V> type, boolean followSymbolicLinks)
			throws GATInvocationException {
		if (localFile) {
			throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR + ": " + CANNOT_HANDLE_THIS_URI + location);
		}
		if (PosixFileAttributeView.class.equals(type)) {
			return (V) new PosixSrmFileAttributeView(location, followSymbolicLinks, connector, gatContext);
		}
		return null;
	}

	/**
	 * @see org.gridlab.gat.io.cpi.FileCpi#getAbsolutePath()
	 */
	public String getAbsolutePath() throws GATInvocationException {
		logger.info("getAbsolutePath()");
		// for srm resources the path of the URI is equal to the Absolute path
		String path = getPath();

		// an uri path never starts with a slash, so add this to obtain a absolute path
		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		return path;
	}

	/**
	 * @see FileCpi#canRead()
	 */
	public boolean canRead() throws GATInvocationException {
		return true;
	}
}
