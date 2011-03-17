package org.gridlab.gat.io.cpi.unicore6;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInfo;
import org.gridlab.gat.io.FileInfo.FileType;
import org.gridlab.gat.io.cpi.FileCpi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unicore.hila.Location;
import eu.unicore.hila.Resource;
import eu.unicore.hila.exceptions.HiLAException;
import eu.unicore.hila.grid.File;

/**
 * Adapter for Unicore6/HiLA files for JavaGAT.
 * 
 * @author Andreas Bender
 */
@SuppressWarnings("serial")
public class Unicore6FileAdaptor extends FileCpi {

	/**
	 * logger instance
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Unicore6FileAdaptor.class);

	/**
	 * String constant for the protocol
	 */
	private static final String UNICORE_PROTOCOL = "unicore6";

	/**
	 * flag that indicates whether its a local file or not
	 */
	private final boolean isLocalFile;

	/**
	 * Constructor.
	 * 
	 * @param gatContext the GAT context
	 * @param location the Unicore/HiLA file location
	 * @throws AdaptorNotApplicableException
	 */
	public Unicore6FileAdaptor(GATContext gatContext, URI location) throws AdaptorNotApplicableException {
		super(gatContext, location);

		if (location.isCompatible("file") && location.refersToLocalHost()) {
			isLocalFile = true;
		} else {
			isLocalFile = false;
			if (!location.isCompatible(UNICORE_PROTOCOL)) {
				throw new AdaptorNotApplicableException("Unicore6FileAdaptor: " + "Cannot handle this URI: " + location);
			}
		}

		// this.connector = new SrmConnector(GliteSecurityUtils.getPathToUserVomsProxy(gatContext));
		LOGGER.info("Instantiated Unicore6FileAdaptor for " + location);
	}

	/**
	 * @see FileCpi#getSupportedCapabilities()
	 * */
	public static Map<String, Boolean> getSupportedCapabilities() {
		Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
		capabilities.put("copy", true);
		capabilities.put("delete", true);
		capabilities.put("exists", true);
		capabilities.put("isDirectory", true);
		capabilities.put("isFile", true);
		capabilities.put("length", true);
		capabilities.put("list", true);
		capabilities.put("mkdir", true);
		return capabilities;
	}

	/**
	 * @see FileCpi#copy(URI)
	 */
	public void copy(URI dest) throws GATInvocationException {
		if (isLocalFile && dest.refersToLocalHost()) {
			LOGGER.debug("Unicore file: copy local to local");
			throw new GATInvocationException("cannot copy from local ('" + toURI() + "') to local ('" + dest + "')");
		}

		if (isLocalFile) {
			if (!dest.isCompatible(UNICORE_PROTOCOL)) {
				throw new GATInvocationException("Unicore6FileAdaptor: " + "Cannot handle this URI: " + dest);
			}
			try {
				File hiLAFile = getHiLAFileFromURI(dest);
				LOGGER.info("Unicore6FileAdaptor copy: Uploading " + location + " to " + dest);
				hiLAFile.importFromLocalFile(new java.io.File(location.toJavaURI()), true).block();
			} catch (HiLAException e) {
				throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during uploading.", e);
			}
		} else if (dest.refersToLocalHost()) {
			try {
				File hiLAFile = getHiLAFileFromURI(location);
				LOGGER.info("Unicore6FileAdaptor copy: Downloading " + location + " to " + dest);
				hiLAFile.exportToLocalFile(new java.io.File(dest.toJavaURI()), true).block();
			} catch (HiLAException e) {
				throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during downloading.", e);
			}
		}
	}

	/**
	 * @see FileCpi#delete()
	 */
	public boolean delete() throws GATInvocationException {
		boolean deleteSuccess = false;
		LOGGER.info("delete()");

		if (isLocalFile) {
			throw new GATInvocationException("Unicore6FileAdaptor: " + "Cannot handle this URI: " + location);
		}

		File hiLAFile = getHiLAFileFromURI(location);

		try {
			deleteSuccess = hiLAFile.delete(true);
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during deletion.", e);
		}

		return deleteSuccess;
	}

	/**
	 * @see FileCpi#mkdir()
	 */
	public boolean mkdir() throws GATInvocationException {
		boolean mkSuccess = false;
		LOGGER.info("mkdir()");

		if (isLocalFile) {
			throw new GATInvocationException("Unicore6FileAdaptor: " + "Cannot handle this URI: " + location);
		}

		File hiLAFile = getHiLAFileFromURI(location);
		try {
			mkSuccess = hiLAFile.mkdir();// TODO create also parents? override mkdirs?
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during creation of dir.", e);
		}

		return mkSuccess;
	}

	/**
	 * @see FileCpi#list()
	 */
	public String[] list() throws GATInvocationException {
		String[] fileNames = new String[0];
		LOGGER.info("list()");

		File hiLAFile = getHiLAFileFromURI(location);
		List<File> fileList = null;
		try {
			fileList = hiLAFile.ls();
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during list.", e);
		}

		if (fileList != null) {
			List<String> nameList = new ArrayList<String>();
			for (File file : fileList) {
				nameList.add(file.getName());
			}
			fileNames = nameList.toArray(new String[nameList.size()]);
		}

		return fileNames;
	}

	/**
	 * @see FileCpi#listFileInfo()
	 */
	public FileInfo[] listFileInfo() throws GATInvocationException {
		FileInfo[] fileInfos = new FileInfo[0];
		LOGGER.info("listFileInfo()");

		List<FileInfo> infoList = new ArrayList<FileInfo>();

		try {
			List<File> hiLAFiles = getHiLAFileFromURI(location).ls();
			FileInfo fileInfo;
			for (File file : hiLAFiles) {
				fileInfo = new FileInfo(file.getName());
				if (isDirectory()) {
					fileInfo.setFileType(FileType.directory);
				} else {
					fileInfo.setFileType(FileType.file);
				}
				fileInfo.setSize(file.size());

				Date lastModified = file.lastModified();
				DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
				DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.FULL);
				fileInfo.setDateTime(dateFormat.format(lastModified), timeFormat.format(lastModified));

				fileInfo.setUserPermissions(file.isReadable(), file.isWritable(), file.isExecutable());
				infoList.add(fileInfo);
			}
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during listFileInfo.", e);
		}

		fileInfos = infoList.toArray(new FileInfo[infoList.size()]);

		return fileInfos;
	}

	/**
	 * @see FileCpi#exists()
	 */
	public boolean exists() throws GATInvocationException {
		boolean exists = false;
		LOGGER.info("exists()");

		File hiLAFile = getHiLAFileFromURI(location);
		try {
			exists = hiLAFile.exists();
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during exists.", e);
		}

		return exists;
	}

	/**
	 * @see FileCpi#isFile()
	 */
	public boolean isFile() throws GATInvocationException {
		boolean isFile = false;
		LOGGER.info("isFile()");

		File hiLAFile = getHiLAFileFromURI(location);
		try {
			isFile = !hiLAFile.isDirectory();
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during isFile.", e);
		}

		return isFile;
	}

	/**
	 * @see FileCpi#isDirectory()
	 */
	public boolean isDirectory() throws GATInvocationException {
		boolean isDirectory = false;
		LOGGER.info("isDirectory()");

		File hiLAFile = getHiLAFileFromURI(location);
		try {
			isDirectory = hiLAFile.isDirectory();
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during isDirectory.", e);
		}

		return isDirectory;
	}

	/**
	 * @see FileCpi#renameTo(org.gridlab.gat.io.File)
	 */
	public boolean renameTo(org.gridlab.gat.io.File dest) throws GATInvocationException {
		LOGGER.info("renameTo(File dest)");

		// FIXME if (location.getHost().equals(dest.toGATURI().getHost())) {
		File hiLAFile = getHiLAFileFromURI(location);
		try {
			hiLAFile.moveTo(getHiLAFileFromURI(dest.toGATURI()), false);
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during file rename.", e);
		}
		return true;
		// } else {
		// LOGGER.error("Both files must be on the same host!");
		// return false;
		// }
	}

	/**
	 * @see FileCpi#length()
	 */
	public long length() throws GATInvocationException {
		Long length = 0L;
		LOGGER.info("length()");

		File hiLAFile = getHiLAFileFromURI(location);
		try {
			length = hiLAFile.size();
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during length.", e);
		}

		return length;
	}

	/**
	 * @see FileCpi#getAbsolutePath()
	 */
	public String getAbsolutePath() throws GATInvocationException {
		LOGGER.info("getAbsolutePath()");

		// for unicore resources the path of the URI is equal to the Absolute path
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
		try {
			return getHiLAFileFromURI(location).isReadable();
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during canRead.", e);
		}
	}

	/**
	 * @see FileCpi#canWrite()
	 */
	public boolean canWrite() throws GATInvocationException {
		try {
			return getHiLAFileFromURI(location).isWritable();
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: An exception occured during canWrite.", e);
		}
	}

	/**
	 * @see FileCpi#getParent()
	 */
	public String getParent() throws GATInvocationException {
		String path = getPath();

		int pos = path.lastIndexOf("/");
		if (pos == -1) {
			// no slash
			return null;
		}

		String res = UNICORE_PROTOCOL + ":" + path.substring(0, pos);

		if (logger.isDebugEnabled()) {
			logger.debug("GET PARENT: orig = " + path + " parent = " + res);
		}

		return res;
	}

	/**
	 * Returns a HiLA File object from a {@link org.gridlab.gat.URI}.
	 * 
	 * @param uri the GAT URI
	 * @return a HiLA File object
	 * @throws GATInvocationException
	 */
	private File getHiLAFileFromURI(URI uri) throws GATInvocationException {
		File hiLAFile;

		Location loc = new Location(uri.toJavaURI());
		try {
			Resource resource = loc.locate(loc);
			if (resource instanceof File) {
				hiLAFile = (File) resource;
			} else {
				throw new GATInvocationException("Unicore6FileAdaptor: URI does not match a HiLA file location");
			}
		} catch (HiLAException e) {
			throw new GATInvocationException("Unicore6FileAdaptor: ", e);
		}

		return hiLAFile;
	}
}