/*
 * Created on May 18, 2004
 */
package org.gridlab.gat;

import java.lang.reflect.Proxy;
import java.net.URISyntaxException;

import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.engine.AdaptorInvocationHandler;
import org.gridlab.gat.engine.AdaptorList;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.Endpoint;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileInputStreamInterface;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.io.FileOutputStreamInterface;
import org.gridlab.gat.io.LogicalFile;
import org.gridlab.gat.io.RandomAccessFile;
import org.gridlab.gat.io.cpi.EndpointCpi;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
import org.gridlab.gat.io.cpi.LogicalFileCpi;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;
import org.gridlab.gat.steering.SteeringManager;
import org.gridlab.gat.steering.cpi.SteeringManagerCpi;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 * @author rob
 */
/** The GAT class is used to create GAT objects.
 */
public class GAT {
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
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static File createFile(GATContext gatContext, URI location)
            throws GATObjectCreationException {
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
     * @return The file object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static File createFile(GATContext gatContext, String location)
            throws GATObjectCreationException {
        try {
            return createFile(gatContext, null, new URI(location));
        } catch (URISyntaxException e) {
            throw new GATObjectCreationException("file", e);
        }
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
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static File createFile(GATContext gatContext,
            Preferences preferences, String location)
            throws GATObjectCreationException {
        try {
            return createFile(gatContext, preferences, new URI(location));
        } catch (URISyntaxException e) {
            throw new GATObjectCreationException("file", e);
        }
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
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static File createFile(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        Object[] array = { location };
        File f = (File) getAdaptorProxy(FileCpi.class, File.class, gatContext,
            preferences, array);

        return f;
    }

    /**
     * This method creates a LogicalFile corresponding to the passed URI
     * instance and uses the passed GATContext to broker resources.
     *
     * @param gatContext
     *            The GATContext used to broker resources
     * @param name The name in the logical name space
     * @param mode The mode to use for opening this logical file
     * Choose from LogicalFile.OPEN, LogicalFile.CREATE, LogicalFile.TRUNCATE
     * @return The logical file object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static LogicalFile createLogicalFile(GATContext gatContext,
            String name, int mode) throws GATObjectCreationException {
        return createLogicalFile(gatContext, null, name, mode);
    }

    /**
     * This method creates a LogicalFile corresponding to the passed URI
     * instance and uses the passed GATContext to broker resources.
     *
     * @param gatContext
     *            The GATContext used to broker resources
     * @param name The name in the logical name space
     * @param mode The mode to use for opening this logical file
     * Choose from LogicalFile.OPEN, LogicalFile.CREATE, LogicalFile.TRUNCATE
     * @param preferences
     *            The Preferences for this instance
     * @return The logical file object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static LogicalFile createLogicalFile(GATContext gatContext,
            Preferences preferences, String name, int mode)
            throws GATObjectCreationException {
        Integer modeTmp = new Integer(mode);
        Object[] array = { name, modeTmp };

        return (LogicalFile) getAdaptorProxy(LogicalFileCpi.class,
            LogicalFile.class, gatContext, preferences, array);
    }

    /**
     * Constructs a FileInputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext.
     *
     * @param location
     *            location of the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @return The fileInputStream object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static FileInputStream createFileInputStream(GATContext gatContext,
            URI location) throws GATObjectCreationException {
        return createFileInputStream(gatContext, null, location);
    }

    /**
     * Constructs a FileInputStream instance which corresponds to the physical
     * file identified by the passed file and whose access rights are determined
     * by the passed GATContext.
     *
     * @param location
     *            URI of the location the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @param preferences
     *            A Preferences which is used to determine the user's
     *            preferences for this File.
     * @return The file object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static FileInputStream createFileInputStream(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        Object[] array = { location };
        FileInputStreamInterface res = (FileInputStreamInterface) getAdaptorProxy(
            FileInputStreamCpi.class, FileInputStreamInterface.class,
            gatContext, preferences, array);

        return new FileInputStream(res);
    }

    /**
     * Constructs a FileInputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext.
     *
     * @param location
     *            location of the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @return The fileInputStream object
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
     * Constructs a FileInputStream instance which corresponds to the physical
     * file identified by the passed file and whose access rights are determined
     * by the passed GATContext.
     *
     * @param location
     *            URI of the location the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @param preferences
     *            A Preferences which is used to determine the user's
     *            preferences for this File.
     * @return The file object
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
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext. This stream overwrites the existing file.
     *
     * @param location
     *            location of the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @return The fileOutputStream object
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
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext.  This stream overwrites the existing file.
     *
     * @param location
     *            location of the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @return The fileOutputStream object
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
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext.
     *
     * @param location
     *            location of the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @param append if true, then bytes will be written to the end of the file rather than the beginning
     * @return The fileOutputStream object
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
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed file and whose access rights are determined
     * by the passed GATContext.
     *
     * @param location
     *            URI of the location the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @param preferences
     *            A Preferences which is used to determine the user's
     *            preferences for this File.
     * @param append if true, then bytes will be written to the end of the file rather than the beginning
     * @return The file object
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
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext. This stream overwrites the existing file.
     *
     * @param location
     *            location of the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @return The fileOutputStream object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, URI location)
            throws GATObjectCreationException {
        return createFileOutputStream(gatContext, null, location, false);
    }

    /**
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext.  This stream overwrites the existing file.
     *
     * @param location
     *            location of the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @return The fileOutputStream object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, Preferences preferences, URI location)
            throws GATObjectCreationException {
        return createFileOutputStream(gatContext, preferences, location, false);
    }

    /**
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext.
     *
     * @param location
     *            location of the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @return The fileOutputStream object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, URI location, boolean append)
            throws GATObjectCreationException {
        return createFileOutputStream(gatContext, null, location, append);
    }

    /**
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed file and whose access rights are determined
     * by the passed GATContext.
     *
     * @param location
     *            URI of the location the file to read from
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @param preferences
     *            A Preferences which is used to determine the user's
     *            preferences for this File.
     * @return The file object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, Preferences preferences, URI location,
            boolean append) throws GATObjectCreationException {
        Object[] array = { location, new Boolean(append) };
        FileOutputStreamInterface res = (FileOutputStreamInterface) getAdaptorProxy(
            FileOutputStreamCpi.class, FileOutputStreamInterface.class,
            gatContext, preferences, array);

        return new FileOutputStream(res);
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
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static Endpoint createEndpoint(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        return (Endpoint) getAdaptorProxy(EndpointCpi.class, Endpoint.class,
            gatContext, preferences, null);
    }

    /**
     * Create an endpoint for a gat pipe
     *
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @return The endpoint object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static Endpoint createEndpoint(GATContext gatContext)
            throws GATObjectCreationException {
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
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static AdvertService createAdvertService(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        return (AdvertService) getAdaptorProxy(AdvertServiceCpi.class,
            AdvertService.class, gatContext, preferences, null);
    }

    /**
     * Create an advert service object
     *
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @return The file object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static AdvertService createAdvertService(GATContext gatContext)
            throws GATObjectCreationException {
        return (AdvertService) getAdaptorProxy(AdvertServiceCpi.class,
            AdvertService.class, gatContext, null, null);
    }

    /**
     * Create an (default) Monitorable object
     *
     * @param gatContext
     *            A GATContext object
     * @return The default Monitorable object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static Monitorable createMonitorable(GATContext gatContext)
            throws GATObjectCreationException {
        return createMonitorable(gatContext, null);
    }

    /**
     * Create an (default) Monitorable object
     *
     * @param gatContext
     *            A GATContext object
     * @param preferences
     *            A Preferences object
     * @return The default Monitorable object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static Monitorable createMonitorable(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        return (Monitorable) getAdaptorProxy(MonitorableCpi.class,
            Monitorable.class, gatContext, preferences, null);
    }


    /**
     * Create a SteeringManager object
     *
     * @param gatContext
     *            A GATContext object
     * @return The SteeringManager
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static SteeringManager createSteeringManager(GATContext gatContext)
            throws GATObjectCreationException {
        return createSteeringManager(gatContext, null);
    }


    /**
     * Create a SteeringManager object
     *
     * @param gatContext
     *            A GATContext object
     * @param preferences
     *            A Preferences object
     * @return The SteeringManager
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static SteeringManager createSteeringManager(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        return (SteeringManager) getAdaptorProxy(SteeringManagerCpi.class,
            SteeringManager.class, gatContext, preferences, null);
    }




    /**
     * Constructs a RandomAccessFile instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     *
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @param file
     *            The file to access
     * @param mode
     *            The mode to open the file with. See java.io.RandomAccessFile
     *            for details.
     * @return The random access file object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static File createRandomAccessFile(GATContext gatContext, File file,
            String mode) throws GATObjectCreationException {
        Object[] array = { file, mode };

        return (File) getAdaptorProxy(RandomAccessFileCpi.class,
            RandomAccessFile.class, gatContext, null, array);
    }

    /**
     * Constructs a RandomAccessFile instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     *
     * @param gatContext
     *            A GATContext which is used to determine the access rights for
     *            this File.
     * @param preferences
     *            A Preferences which is used to determine the user's
     *            preferences for this File.
     * @param file
     *            The file to access
     * @param mode
     *            The mode to open the file with. See java.io.RandomAccessFile
     *            for details.
     * @return The random access file object
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static File createRandomAccessFile(GATContext gatContext,
            Preferences preferences, File file, String mode)
            throws GATObjectCreationException {
        Object[] array = { file, mode };

        return (File) getAdaptorProxy(RandomAccessFileCpi.class,
            RandomAccessFile.class, gatContext, preferences, array);
    }

    public static ResourceBroker createResourceBroker(GATContext gatContext)
            throws GATObjectCreationException {
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
     * @throws GATObjectCreationException
     *             Thrown upon creation problems
     */
    public static ResourceBroker createResourceBroker(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        return (ResourceBroker) getAdaptorProxy(ResourceBrokerCpi.class,
            ResourceBroker.class, gatContext, preferences, null);
    }

    public static void end() {
        GATEngine.end();
    }

    protected static Object getAdaptorProxy(Class cpiClass,
            Class interfaceClass, GATContext gatContext,
            Preferences preferences, Object[] tmpParams)
            throws GATObjectCreationException {
        GATEngine gatEngine = GATEngine.getGATEngine();

        AdaptorList adaptors = gatEngine.getAdaptorList(cpiClass);

        if (adaptors == null) {
            return null;
        }

        /** @@@ maybe we want to support a "default" context. */
        if (gatContext == null) {
            gatContext = new GATContext(); // get default context here, not a new one
        }

        Preferences prefs = gatContext.getPreferences();

        if (preferences != null) {
            prefs.putAll(preferences); // local prefs override globals
        }

        AdaptorInvocationHandler handler = new AdaptorInvocationHandler(
            adaptors, gatContext, prefs, tmpParams);

        Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(),
            new Class[] { interfaceClass }, handler);

        return proxy;
    }
}
