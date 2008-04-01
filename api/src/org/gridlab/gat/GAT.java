/*
 * Created on May 18, 2004
 */
package org.gridlab.gat;

import java.lang.reflect.Method;
import java.net.URISyntaxException;

import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.io.Endpoint;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileInputStreamInterface;
import org.gridlab.gat.io.FileInterface;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.io.FileOutputStreamInterface;
import org.gridlab.gat.io.LogicalFile;
import org.gridlab.gat.io.RandomAccessFile;
import org.gridlab.gat.io.RandomAccessFileInterface;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.steering.SteeringManager;

/**
 * @author rob
 */
/**
 * The GAT class is used to create GAT objects.
 */
public class GAT {

	static Class<?> engineClass;

	static Method createProxyMethod;

	static {
		try {
			engineClass = Class.forName("org.gridlab.gat.engine.GATEngine");
			createProxyMethod = engineClass.getMethod("createAdaptorProxy",
					new Class[] { String.class, Class.class, GATContext.class,
							Preferences.class, Class[].class, Object[].class });
		} catch (Exception e) {
			System.out.println(e);
			throw new Error(e);

			// logger.fatal()
		}
	}

	/**
	 * Constructs a {@link File} instance which corresponds to the physical file
	 * identified by the passed {@link URI} and whose access rights are
	 * determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            A {@link URI} which represents the URI corresponding to the
	 *            physical file.
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link File} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static org.gridlab.gat.io.File createFile(GATContext gatContext,
			URI location) throws GATObjectCreationException {
		return createFile(gatContext, null, location);
	}

	/**
	 * Constructs a {@link File} instance which corresponds to the physical file
	 * identified by the passed URI (in {@link String} format) and whose access
	 * rights are determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            A URI (in {@link String} format) which represents the URI
	 *            corresponding to the physical file.
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link File} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static org.gridlab.gat.io.File createFile(GATContext gatContext,
			String location) throws GATObjectCreationException {
		try {
			return createFile(gatContext, null, new URI(location));
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("file", e);
		}
	}

	/**
	 * Constructs a {@link File} instance which corresponds to the physical file
	 * identified by the passed URI (in {@link String} format) and whose access
	 * rights are determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            A URI (in {@link String} format) which represents the URI
	 *            corresponding to the physical file.
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @return The {@link File} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static org.gridlab.gat.io.File createFile(GATContext gatContext,
			Preferences preferences, String location)
			throws GATObjectCreationException {
		try {
			return createFile(gatContext, preferences, new URI(location));
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("file", e);
		}
	}

	/**
	 * Constructs a {@link File} instance which corresponds to the physical file
	 * identified by the passed {@link URI} and whose access rights are
	 * determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            A {@link URI} which represents the URI corresponding to the
	 *            physical file.
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @return The {@link File} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static org.gridlab.gat.io.File createFile(GATContext gatContext,
			Preferences preferences, URI location)
			throws GATObjectCreationException {
		Object[] array = { location };
		FileInterface f = (FileInterface) getAdaptorProxy("File",
				FileInterface.class, gatContext, preferences,
				new Class[] { URI.class }, array);

		return new org.gridlab.gat.io.File(f);
	}

	/**
	 * This method creates a {@link LogicalFile} corresponding to the passed URI
	 * (in {@link String} format) instance.
	 * 
	 * @param gatContext
	 *            The {@link GATContext}
	 * @param name
	 *            The name in the logical name space
	 * @param mode
	 *            The mode to use for opening this logical file. Choose from
	 *            {@link LogicalFile#OPEN}, {@link LogicalFile#CREATE},
	 *            {@link LogicalFile#TRUNCATE}
	 * @return The {@link LogicalFile} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static LogicalFile createLogicalFile(GATContext gatContext,
			String name, int mode) throws GATObjectCreationException {
		return createLogicalFile(gatContext, null, name, mode);
	}

	/**
	 * This method creates a {@link LogicalFile} corresponding to the passed URI
	 * (in {@link String} format) instance.
	 * 
	 * @param gatContext
	 *            The {@link GATContext}
	 * @param preferences
	 *            The {@link Preferences} for this instance
	 * @param name
	 *            The name in the logical name space
	 * @param mode
	 *            The mode to use for opening this logical file. Choose from
	 *            {@link LogicalFile#OPEN}, {@link LogicalFile#CREATE},
	 *            {@link LogicalFile#TRUNCATE}
	 * @return The {@link LogicalFile} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static LogicalFile createLogicalFile(GATContext gatContext,
			Preferences preferences, String name, int mode)
			throws GATObjectCreationException {
		Integer modeTmp = new Integer(mode);
		Object[] array = { name, modeTmp };

		return (LogicalFile) getAdaptorProxy("LogicalFile", LogicalFile.class,
				gatContext, preferences, new Class[] { String.class,
						Integer.TYPE }, array);
	}

	/**
	 * Constructs a {@link FileInputStream} instance which corresponds to the
	 * physical file identified by the passed {@link File} and whose access
	 * rights are determined by the {@link GATContext} and {@link Preferences}
	 * of this {@link File}.
	 * 
	 * @param file
	 *            the {@link File} to read from
	 * @return The {@link FileInputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileInputStream createFileInputStream(File file)
			throws GATObjectCreationException {
		return createFileInputStream(file.getFileInterface().getGATContext(),
				file.getFileInterface().getPreferences(), file.toGATURI());
	}

	/**
	 * Constructs a {@link FileInputStream} instance which corresponds to the
	 * physical file identified by the passed {@link URI} and whose access
	 * rights are determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            location of the {@link File} to read from
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileInputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileInputStream createFileInputStream(GATContext gatContext,
			URI location) throws GATObjectCreationException {
		return createFileInputStream(gatContext, null, location);
	}

	/**
	 * Constructs a {@link FileInputStream} instance which corresponds to the
	 * physical file identified by the passed {@link URI} and whose access
	 * rights are determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            {@link URI} of the location of the {@link File} to read from
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @return The {@link FileInputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileInputStream createFileInputStream(GATContext gatContext,
			Preferences preferences, URI location)
			throws GATObjectCreationException {
		Object[] array = { location };
		FileInputStreamInterface res = (FileInputStreamInterface) getAdaptorProxy(
				"FileInputStream", FileInputStreamInterface.class, gatContext,
				preferences, new Class[] { URI.class }, array);

		return new FileInputStream(res);
	}

	/**
	 * Constructs a {@link FileInputStream} instance which corresponds to the
	 * physical file identified by the passed {@link File} and whose access
	 * rights are determined by the passed {@link GATContext}.
	 * 
	 * @param file
	 *            the {@link File} to read from
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileInputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileInputStream createFileInputStream(GATContext gatContext,
			File file) throws GATObjectCreationException {
		return createFileInputStream(gatContext, null, file.toGATURI());
	}

	/**
	 * Constructs a {@link FileInputStream} instance which corresponds to the
	 * physical file identified by the passed file and whose access rights are
	 * determined by the passed {@link GATContext}.
	 * 
	 * @param file
	 *            the {@link File} to read from
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @return The {@link FileInputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileInputStream createFileInputStream(GATContext gatContext,
			Preferences preferences, File file)
			throws GATObjectCreationException {
		return createFileInputStream(gatContext, preferences, file.toGATURI());
	}

	/**
	 * Constructs a {@link FileInputStream} instance which corresponds to the
	 * physical file identified by the passed URI (in {@link String} format) and
	 * whose access rights are determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            URI (in {@link String} format) of the location of the
	 *            {@link File} to read from
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileInputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileInputStream createFileInputStream(GATContext gatContext,
			String location) throws GATObjectCreationException {
		try {
			return createFileInputStream(gatContext, null, new URI(location));
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("file input stream", e);
		}
	}

	/**
	 * Constructs a {@link FileInputStream} instance which corresponds to the
	 * physical file identified by the passed URI (in {@link String} format) and
	 * whose access rights are determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            URI (in {@link String} format) of the location of the
	 *            {@link File} to read from
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @return The {@link FileInputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileInputStream createFileInputStream(GATContext gatContext,
			Preferences preferences, String location)
			throws GATObjectCreationException {
		try {
			return createFileInputStream(gatContext, preferences, new URI(
					location));
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("file input stream", e);
		}
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed URI (in {@link String} format) and
	 * whose access rights are determined by the passed {@link GATContext}.
	 * This stream overwrites the existing file.
	 * 
	 * @param location
	 *            URI (in {@link String} format) of the location of the
	 *            {@link File} to write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, String location)
			throws GATObjectCreationException {
		try {
			return createFileOutputStream(gatContext, null, new URI(location),
					false);
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("file output stream", e);
		}
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed URI (in {@link String} format) and
	 * whose access rights are determined by the passed {@link GATContext}.
	 * This stream overwrites the existing file.
	 * 
	 * @param location
	 *            URI (in {@link String} format) of the location of the file to
	 *            write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, Preferences preferences, String location)
			throws GATObjectCreationException {
		try {
			return createFileOutputStream(gatContext, preferences, new URI(
					location), false);
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("file output stream", e);
		}
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed URI (in {@link String} format) and
	 * whose access rights are determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            URI (in {@link String} format) of the location of the file to
	 *            write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param append
	 *            if <code>true</code>, then bytes will be written to the end
	 *            of the file rather than the beginning
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, String location, boolean append)
			throws GATObjectCreationException {
		try {
			return createFileOutputStream(gatContext, null, new URI(location),
					append);
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("file output stream", e);
		}
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed URI (in {@link String} format) and
	 * whose access rights are determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            URI (in {@link String} format) of the location of the file to
	 *            write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @param append
	 *            if <code>true</code>, then bytes will be written to the end
	 *            of the file rather than the beginning
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, Preferences preferences, String location,
			boolean append) throws GATObjectCreationException {
		try {
			return createFileOutputStream(gatContext, preferences, new URI(
					location), append);
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("file output stream", e);
		}
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed {@link File} and whose access
	 * rights are determined by the {@link GATContext} and {@link Preferences}
	 * of this {@link File}. This stream overwrites the existing file.
	 * 
	 * @param file
	 *            the {@link File} to write to
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(File file)
			throws GATObjectCreationException {
		return createFileOutputStream(file.getFileInterface().getGATContext(),
				file.getFileInterface().getPreferences(), file.toGATURI(),
				false);
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed {@link File} and whose access
	 * rights are determined by the {@link GATContext} and {@link Preferences}
	 * of this {@link File}.
	 * 
	 * @param file
	 *            the {@link File} to write to
	 * @param append
	 *            <code>true</code> if the output stream should append,
	 *            <code>false</code> for overwrite
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(File file,
			boolean append) throws GATObjectCreationException {
		return createFileOutputStream(file.getFileInterface().getGATContext(),
				file.getFileInterface().getPreferences(), file.toGATURI(),
				append);
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed {@link File} and whose access
	 * rights are determined by the passed {@link GATContext}. This stream
	 * overwrites the existing file.
	 * 
	 * @param file
	 *            the <@link File> to write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, File file) throws GATObjectCreationException {
		return createFileOutputStream(gatContext, null, file.toGATURI(), false);
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed {@link File} and whose access
	 * rights are determined by the passed {@link GATContext}. This stream
	 * overwrites the existing file.
	 * 
	 * @param file
	 *            the <@link File> to write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, Preferences preferences, File file)
			throws GATObjectCreationException {
		return createFileOutputStream(gatContext, preferences, file.toGATURI(),
				false);
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed {@link File} and whose access
	 * rights are determined by the passed {@link GATContext}.
	 * 
	 * @param file
	 *            the <@link File> to write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, File file, boolean append)
			throws GATObjectCreationException {
		return createFileOutputStream(gatContext, null, file.toGATURI(), append);
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed file and whose access rights are
	 * determined by the passed {@link GATContext}.
	 * 
	 * @param file
	 *            the <@link File> to write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, Preferences preferences, File file,
			boolean append) throws GATObjectCreationException {
		return createFileOutputStream(gatContext, preferences, file.toGATURI(),
				append);
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed {@link URI} and whose access
	 * rights are determined by the passed {@link GATContext}. This stream
	 * overwrites the existing file.
	 * 
	 * @param location
	 *            URI of the location the <@link File> to write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, URI location)
			throws GATObjectCreationException {
		return createFileOutputStream(gatContext, null, location, false);
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed {@link URI} and whose access
	 * rights are determined by the passed {@link GATContext}. This stream
	 * overwrites the existing file.
	 * 
	 * @param location
	 *            URI of the location the <@link File> to write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, Preferences preferences, URI location)
			throws GATObjectCreationException {
		return createFileOutputStream(gatContext, preferences, location, false);
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed {@link URI} and whose access
	 * rights are determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            URI of the location the <@link File> to write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, URI location, boolean append)
			throws GATObjectCreationException {
		return createFileOutputStream(gatContext, null, location, append);
	}

	/**
	 * Constructs a {@link FileOutputStream} instance which corresponds to the
	 * physical file identified by the passed {@link URI} and whose access
	 * rights are determined by the passed {@link GATContext}.
	 * 
	 * @param location
	 *            URI of the location the <@link File> to write to
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @return The {@link FileOutputStream} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static FileOutputStream createFileOutputStream(
			GATContext gatContext, Preferences preferences, URI location,
			boolean append) throws GATObjectCreationException {
		Object[] array = { location, new Boolean(append) };
		FileOutputStreamInterface res = (FileOutputStreamInterface) getAdaptorProxy(
				"FileOutputStream", FileOutputStreamInterface.class,
				gatContext, preferences,
				new Class[] { URI.class, Boolean.class }, array);

		return new FileOutputStream(res);
	}

	/**
	 * Create an {@link Endpoint} for a {@link Pipe}
	 * 
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @return The {@link Endpoint} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static Endpoint createEndpoint(GATContext gatContext,
			Preferences preferences) throws GATObjectCreationException {
		return (Endpoint) getAdaptorProxy("Endpoint", Endpoint.class,
				gatContext, preferences, null, null);
	}

	/**
	 * Create an {@link Endpoint} for a {@link Pipe}
	 * 
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @return The {@link Endpoint} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static Endpoint createEndpoint(GATContext gatContext)
			throws GATObjectCreationException {
		return createEndpoint(gatContext, null);
	}

	/**
	 * Create an {@link AdvertService} object
	 * 
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences.
	 * @return The {@link AdvertService} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static AdvertService createAdvertService(GATContext gatContext,
			Preferences preferences) throws GATObjectCreationException {
		return (AdvertService) getAdaptorProxy("AdvertService",
				AdvertService.class, gatContext, preferences, null, null);
	}

	/**
	 * Create an {@link AdvertService} object
	 * 
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights.
	 * @return The {@link AdvertService} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static AdvertService createAdvertService(GATContext gatContext)
			throws GATObjectCreationException {
		return createAdvertService(gatContext, null);
	}

	/**
	 * Create an (default) {@link Monitorable} object
	 * 
	 * @param gatContext
	 *            A {@link GATContext} object
	 * @return The default {@link Monitorable} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static Monitorable createMonitorable(GATContext gatContext)
			throws GATObjectCreationException {
		return createMonitorable(gatContext, null);
	}

	/**
	 * Create an (default) {@link Monitorable} object
	 * 
	 * @param gatContext
	 *            A {@link GATContext} object
	 * @param preferences
	 *            A {@link Preferences} object
	 * @return The default {@link Monitorable} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static Monitorable createMonitorable(GATContext gatContext,
			Preferences preferences) throws GATObjectCreationException {
		return (Monitorable) getAdaptorProxy("Monitorable", Monitorable.class,
				gatContext, preferences, null, null);
	}

	/**
	 * Create a {@link SteeringManager} object
	 * 
	 * @param gatContext
	 *            A {@link GATContext} object
	 * @return The {@link SteeringManager}
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static SteeringManager createSteeringManager(GATContext gatContext)
			throws GATObjectCreationException {
		return createSteeringManager(gatContext, null);
	}

	/**
	 * Create a {@link SteeringManager} object
	 * 
	 * @param gatContext
	 *            A {@link GATContext} object
	 * @param preferences
	 *            A {@link Preferences} object
	 * @return The {@link SteeringManager}
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static SteeringManager createSteeringManager(GATContext gatContext,
			Preferences preferences) throws GATObjectCreationException {
		return (SteeringManager) getAdaptorProxy("SteeringManager",
				SteeringManager.class, gatContext, preferences, null, null);
	}

	/**
	 * Constructs a {@link RandomAccessFile} instance which corresponds to the
	 * physical file identified by the passed URI (in {@link String} format) and
	 * whose access rights are determined by the passed {@link GATContext}.
	 * 
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param location
	 *            a URI (in {@link String} format) for the file to access.
	 * @param mode
	 *            The mode to open the file with. See
	 *            {@link java.io.RandomAccessFile} for details.
	 * @return The {@link RandomAccessFile} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static RandomAccessFile createRandomAccessFile(
			GATContext gatContext, String location, String mode)
			throws GATObjectCreationException {
		try {
			return createRandomAccessFile(gatContext, null, new URI(location),
					mode);
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("random access file", e);
		}
	}

	/**
	 * Constructs a {@link RandomAccessFile} instance which corresponds to the
	 * physical file identified by the passed URI (in {@link String} format) and
	 * whose access rights are determined by the passed {@link GATContext}.
	 * 
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @param location
	 *            the URI (in {@link String} format) of the file to access
	 * @param mode
	 *            The mode to open the file with. See
	 *            {@link java.io.RandomAccessFile} for details.
	 * @return The {@link RandomAccessFile} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static RandomAccessFile createRandomAccessFile(
			GATContext gatContext, Preferences preferences, String location,
			String mode) throws GATObjectCreationException {
		try {
			return createRandomAccessFile(gatContext, preferences, new URI(
					location), mode);
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("random access file", e);
		}
	}

	/**
	 * Constructs a {@link RandomAccessFile} instance which corresponds to the
	 * physical file identified by the passed {@link URI} and whose access
	 * rights are determined by the passed {@link GATContext}.
	 * 
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param location
	 *            a {@link URI} for the file to access.
	 * @param mode
	 *            The mode to open the file with. See
	 *            {@link java.io.RandomAccessFile} for details.
	 * @return The {@link RandomAccessFile} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static RandomAccessFile createRandomAccessFile(
			GATContext gatContext, URI location, String mode)
			throws GATObjectCreationException {
		return createRandomAccessFile(gatContext, null, location, mode);
	}

	/**
	 * Constructs a {@link RandomAccessFile} instance which corresponds to the
	 * physical file identified by the passed {@link URI} and whose access
	 * rights are determined by the passed {@link GATContext}.
	 * 
	 * @param gatContext
	 *            A {@link GATContext} which is used to determine the access
	 *            rights for this {@link File}.
	 * @param preferences
	 *            A {@link Preferences} which is used to determine the user's
	 *            preferences for this {@link File}.
	 * @param location
	 *            the {@link URISyntaxException} of the file to access
	 * @param mode
	 *            The mode to open the file with. See
	 *            {@link java.io.RandomAccessFile} for details.
	 * @return The {@link RandomAccessFile} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static RandomAccessFile createRandomAccessFile(
			GATContext gatContext, Preferences preferences, URI location,
			String mode) throws GATObjectCreationException {
		Object[] array = { location, mode };

		RandomAccessFileInterface f = (RandomAccessFileInterface) getAdaptorProxy(
				"RandomAccessFile", RandomAccessFileInterface.class,
				gatContext, preferences,
				new Class[] { URI.class, String.class }, array);
		try {
			return new RandomAccessFile(f);
		} catch (Exception e) {
			throw new GATObjectCreationException("GAT", e);
		}
	}

	// /**
	// * This method constructs a ResourceBroker instance corresponding to the
	// * passed {@link GATContext}.
	// *
	// * @param gatContext
	// * A {@link GATContext} which will be used to broker resources
	// * @return The resource broker object
	// * @throws GATObjectCreationException
	// * Thrown upon creation problems
	// */
	// public static ResourceBroker createResourceBroker(GATContext gatContext)
	// throws GATObjectCreationException {
	// return createResourceBroker(gatContext, null);
	// }

	/**
	 * This method constructs a {@link ResourceBroker} instance corresponding to
	 * the passed {@link GATContext}.
	 * 
	 * @param gatContext
	 *            A {@link GATContext} which will be used to broker resources
	 * @return The {@link ResourceBroker} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static ResourceBroker createResourceBroker(GATContext gatContext,
			URI brokerURI) throws GATObjectCreationException {
		return createResourceBroker(gatContext, null, brokerURI);
	}

	/**
	 * This method constructs a {@link ResourceBroker} instance corresponding to
	 * the passed {@link GATContext}.
	 * 
	 * @param gatContext
	 *            A {@link GATContext} which will be used to broker resources
	 * @param preferences
	 *            The {@link Preferences} for this instance
	 * @return The {@link ResourceBroker} object
	 * @throws GATObjectCreationException
	 *             Thrown upon creation problems
	 */
	public static ResourceBroker createResourceBroker(GATContext gatContext,
			Preferences preferences, URI brokerURI)
			throws GATObjectCreationException {
		Object[] array = { brokerURI };
		return (ResourceBroker) getAdaptorProxy("ResourceBroker",
				ResourceBroker.class, gatContext, preferences,
				new Class[] { URI.class }, array);
	}

	/**
	 * This method shuts down the GAT properly and will stop all threads of
	 * external libraries used by the GAT. This method should be invoked at the
	 * end of a program that uses JavaGAT.
	 */
	public static void end() {
		try {
			Method m = engineClass.getMethod("end", (Class<?>[]) null);
			m.invoke(null, (Object[]) null);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	protected static Object getAdaptorProxy(String cpiClassName,
			Class<?> interfaceClass, GATContext gatContext,
			Preferences preferences, Class<?>[] parameterTypes,
			Object[] tmpParams) throws GATObjectCreationException {

		/** Maybe we want to support a "default" context. */
		if (gatContext == null) {
			gatContext = new GATContext(); // get default context here, not a
			// new one
		}

		Preferences prefs = gatContext.getPreferences();

		if (preferences != null) {
			prefs.putAll(preferences); // local prefs override globals
		}

		try {
			return createProxyMethod.invoke(null, new Object[] { cpiClassName,
					interfaceClass, gatContext, prefs, parameterTypes,
					tmpParams });
		} catch (Exception e) {
			throw new GATObjectCreationException("", e);
		}
	}
}
