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
 * <p>
 * Please note: This adapter is to be considered experimental. It may or may not work for you, and there is no guarantee
 * that the interface or parameters will be compatible with future versions.
 * <p>
 * Please read the documentation in <tt>/doc/GliteAdaptor</tt> for further information!
 * 
 * @author: Max Berger
 * @author: Stefan Bozic
 */
@SuppressWarnings("serial")
public class GliteSrmFileAdaptor extends FileCpi {

	private static final String SRM_PROTOCOL = "srm";

	private static final String CANNOT_HANDLE_THIS_URI = "cannot handle this URI: ";

	private static final String GLITE_SRM_FILE_ADAPTOR = "GliteSrmFileAdaptor";

	private static final Logger LOGGER = LoggerFactory.getLogger(GliteSrmFileAdaptor.class);

	private final boolean localFile;

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

	/** {@inheritDoc} */
	public void copy(URI dest) throws GATInvocationException {

		// GridFtpClient: for 2-party transfer must be DataChannelAuthentication.SELF or DataChannelAuthentication.NONE
		Preferences fileAdapterPrefs = new Preferences();
		fileAdapterPrefs.put("ftp.server.noauthentication", "true");

		try {
			if (localFile) {
				if (!dest.isCompatible(GliteSrmFileAdaptor.SRM_PROTOCOL)) {
					throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR + ": "
							+ GliteSrmFileAdaptor.CANNOT_HANDLE_THIS_URI + dest);
				}

				GliteSecurityUtils.getVOMSProxy(gatContext, true);
				String proxyFile = GliteSecurityUtils.getPathToUserVomsProxy(gatContext);

				LOGGER.info("SRM/Copy: Uploading " + location + " to " + dest);
				String turl = connector.getTURLForFileUpload(location, dest);
				LOGGER.info("SRM/Copy: TURL: " + turl);

				GATContext newContext = (GATContext) gatContext.clone();
				newContext.addPreference("File.adaptor.name", "GridFTP");
				newContext.addPreferences(fileAdapterPrefs);
				GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext, proxyFile);

				File transportFile = GAT.createFile(newContext, location);
				transportFile.copy(new URI(turl));
				connector.finalizeFileUpload(dest);
			} else {
				GliteSecurityUtils.getVOMSProxy(gatContext, true);
				String proxyFile = GliteSecurityUtils.getPathToUserVomsProxy(gatContext);

				LOGGER.info("SRM/Copy: Downloading " + location + " to " + dest);
				String turl = connector.getTURLForFileDownload(location);
				LOGGER.info("SRM/Copy: TURL: " + turl);

				GATContext newContext = (GATContext) gatContext.clone();
				newContext.addPreferences(fileAdapterPrefs);
				newContext.addPreference("File.adaptor.name", "GridFTP");

				GliteSecurityUtils.replaceSecurityContextWithGliteContext(newContext, proxyFile);
				File transportFile = GAT.createFile(newContext, turl);
				transportFile.copy(new URI(dest.getPath()));
			}
		} catch (Exception e) {
			throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR, e);
		}
	}

	/** {@inheritDoc} */
	public boolean delete() throws GATInvocationException {
		if (localFile) {
			throw new GATInvocationException(GLITE_SRM_FILE_ADAPTOR + ": " + GliteSrmFileAdaptor.CANNOT_HANDLE_THIS_URI
					+ location);
		}
		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);
			connector.delete(location);
		} catch (IOException e) {
			LOGGER.info("An exception occurs during deletion of file " + toURI().toString(), e);
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean mkdir() throws GATInvocationException {
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
		try {
			GliteSecurityUtils.getVOMSProxy(gatContext, true);

			List<String> result = connector.realLs(location);
			return (String[]) result.toArray(new String[result.size()]);
		} catch (IOException e) {
			LOGGER.error("An error occurs during ls", e);
		}
		return null;
	}

	/**
	 * Returns the file informations (e.g. name, size, isDir, isFile, permissions).
	 * 
	 * @return Array with informations about the files.
	 * @throws GATInvocationException an exception that might occurs
	 */
	public FileInfo[] listFileInfo() throws GATInvocationException {
		String[] list = list();
		FileInfo[] fileInfos = null;

		if (null != list && list.length > 0) {
			fileInfos = new FileInfo[list.length];

			for (int i = 0 ; i < list.length; i++) {
				FileInfo info = new FileInfo(list[i]);
				fileInfos[i] = info;
			}
		}

		return fileInfos;
	} // public FileInfo[] listFileInfo() throws GATInvocationException

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
}
