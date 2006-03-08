/*
 * Created on May 18, 2004
 */
package org.gridlab.gat;

import java.lang.reflect.Proxy;
import java.net.URI;

import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.engine.AdaptorInvocationHandler;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.Endpoint;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.LogicalFile;
import org.gridlab.gat.io.RandomAccessFile;
import org.gridlab.gat.io.cpi.EndpointCpi;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.LogicalFileCpi;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 * @author rob
 */
public class GAT {

	protected static Object getAdaptorProxy(Class cpiClass,
			Class interfaceClass, GATContext gatContext,
			Preferences preferences, Object[] tmpParams)
			throws AdaptorCreationException {

		GATEngine gatEngine = GATEngine.getGATEngine();

		Object[] adaptors = gatEngine.getAdaptorList(cpiClass, gatContext,
				preferences, tmpParams);
		if (adaptors == null) {
			return null;
		}

		AdaptorInvocationHandler handler = new AdaptorInvocationHandler(
				adaptors);

		Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class[] { interfaceClass }, handler);

		return proxy;
	}

	/**
	 * This method creates a LogicalFile corresponding to the passed URI
	 * instance and uses the passed GATContext to broker resources.
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @return The logical file object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static LogicalFile createLogicalFile(GATContext gatContext)
			throws AdaptorCreationException {
		return createLogicalFile(gatContext, null, null);
	}

	/**
	 * This method creates a LogicalFile corresponding to the passed URI
	 * instance and uses the passed GATContext to broker resources.
	 * 
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param preferences
	 *            The Preferences for this instance
	 * @return The logical file object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static LogicalFile createLogicalFile(GATContext gatContext,
			Preferences preferences) throws AdaptorCreationException {
		return createLogicalFile(gatContext, preferences, null);
	}

	/**
	 * This method creates a LogicalFile corresponding to the passed URI
	 * instance and uses the passed GATContext to broker resources.
	 * 
	 * @param location
	 *            The URI of one physical file in this LogicalFile
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @return The logical file object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static LogicalFile createLogicalFile(GATContext gatContext,
			URI location) throws AdaptorCreationException {
		return createLogicalFile(gatContext, null, location);
	}

	/**
	 * This method creates a LogicalFile corresponding to the passed URI
	 * instance and uses the passed GATContext to broker resources.
	 * 
	 * @param location
	 *            The URI of one physical file in this LogicalFile
	 * @param gatContext
	 *            The GATContext used to broker resources
	 * @param preferences
	 *            The Preferences for this instance
	 * @return The logical file object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static LogicalFile createLogicalFile(GATContext gatContext,
			Preferences preferences, URI location)
			throws AdaptorCreationException {
		Object[] array = { location };
		return (LogicalFile) getAdaptorProxy(LogicalFileCpi.class,
				LogicalFile.class, gatContext, preferences, array);
	}

	/**
	 * Constructs a File instance which corresponds to the physical file
	 * identified by the passed URI and whose access rights are determined by
	 * the passed GATContext.
	 * 
	 * @param location
	 *            A URI which represents the URI corresponding to the physical
	 *            file.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @return The file object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static File createFile(GATContext gatContext, URI location)
			throws AdaptorCreationException {
		return createFile(gatContext, null, location);
	}

	/**
	 * Constructs a File instance which corresponds to the physical file
	 * identified by the passed URI and whose access rights are determined by
	 * the passed GATContext.
	 * 
	 * @param location
	 *            A URI which represents the URI corresponding to the physical
	 *            file.
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @param preferences
	 *            A Preferences which is used to determine the user's
	 *            preferences for this File.
	 * @return The file object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static File createFile(GATContext gatContext,
			Preferences preferences, URI location)
			throws AdaptorCreationException {
		Object[] array = { location };
		return (File) getAdaptorProxy(FileCpi.class, File.class, gatContext,
				preferences, array);
	}

	/**
	 * Create an endpoint for a gat pipe
	 *  
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @param preferences
	 *            A Preferences which is used to determine the user's
	 *            preferences for this File.
	 * @return The endpoint object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static Endpoint createEndpoint(GATContext gatContext,
			Preferences preferences)
			throws AdaptorCreationException {

		return (Endpoint) getAdaptorProxy(EndpointCpi.class, Endpoint.class, gatContext,
				preferences, null);
	}

	/**
	 * Create an endpoint for a gat pipe
	 *  
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @return The endpoint object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static Endpoint createEndpoint(GATContext gatContext)
			throws AdaptorCreationException {

		return createEndpoint(gatContext, null);
	}

	/**
	 * Create an advert service object
	 *  
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @param preferences
	 *            A Preferences which is used to determine the user's
	 *            preferences for this File.
	 * @return The file object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static AdvertService createAdvertService(GATContext gatContext,
			Preferences preferences)
			throws AdaptorCreationException {
		return (AdvertService) getAdaptorProxy(AdvertServiceCpi.class, AdvertService.class, gatContext,
				preferences, null);
	}

	/**
	 * Create an advert service object
	 *  
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @return The file object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static AdvertService createAdvertService(GATContext gatContext)
			throws AdaptorCreationException {
		return (AdvertService) getAdaptorProxy(AdvertServiceCpi.class, AdvertService.class, gatContext,
				null, null);
	}

	/**
	 * Constructs a RandomAccessFile instance which corresponds to the physical file
	 * identified by the passed URI and whose access rights are determined by
	 * the passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @param file The file to access
	 * @param mode The mode to open the file with. See java.io.RandomAccessFile for details.
	 * @return The random access file object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static File createRandomAccessFile(GATContext gatContext,
			File file, String mode)
			throws AdaptorCreationException {
		Object[] array = { file, mode };
		return (File) getAdaptorProxy(RandomAccessFileCpi.class, RandomAccessFile.class, gatContext,
				null, array);
	}

	/**
	 * Constructs a RandomAccessFile instance which corresponds to the physical file
	 * identified by the passed URI and whose access rights are determined by
	 * the passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which is used to determine the access rights for
	 *            this File.
	 * @param preferences
	 *            A Preferences which is used to determine the user's
	 *            preferences for this File.
	 * @param file The file to access
	 * @param mode The mode to open the file with. See java.io.RandomAccessFile for details.
	 * @return The random access file object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static File createRandomAccessFile(GATContext gatContext,
			Preferences preferences, File file, String mode)
			throws AdaptorCreationException {
		Object[] array = { file, mode };
		return (File) getAdaptorProxy(RandomAccessFileCpi.class, RandomAccessFile.class, gatContext,
				preferences, array);
	}

	public static ResourceBroker createResourceBroker(GATContext gatContext)
			throws AdaptorCreationException {
		return createResourceBroker(gatContext, null);
	}

	/**
	 * This method constructs a ResourceBroker instance corresponding to the
	 * passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which will be used to broker resources
	 * @param preferences
	 *            The Preferences for this instance
	 * @return The resource broker object
	 * @throws AdaptorCreationException
	 *             Thrown upon creation problems
	 */
	public static ResourceBroker createResourceBroker(GATContext gatContext,
			Preferences preferences) throws AdaptorCreationException {
		return (ResourceBroker) getAdaptorProxy(ResourceBrokerCpi.class,
				ResourceBroker.class, gatContext, preferences, null);
	}
}