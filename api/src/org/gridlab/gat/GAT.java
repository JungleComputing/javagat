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
            throw new Error(e);
        }
    }

    /**
     * Constructs a File instance which corresponds to the physical file
     * identified by the passed URI and whose access rights are determined by
     * the passed GATContext.
     * 
     * @param location
     *                A URI which represents the URI corresponding to the
     *                physical file.
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The file object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static org.gridlab.gat.io.File createFile(GATContext gatContext,
            URI location) throws GATObjectCreationException {
        return createFile(gatContext, null, location);
    }

    /**
     * Constructs a File instance which corresponds to the physical file
     * identified by the passed URI (in String format) and whose access rights
     * are determined by the passed GATContext.
     * 
     * @param location
     *                A URI (in String format) which represents the URI
     *                corresponding to the physical file.
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The file object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     * Constructs a File instance which corresponds to the physical file
     * identified by the passed URI (in String format) and whose access rights
     * are determined by the passed GATContext.
     * 
     * @param location
     *                A URI (in String format) which represents the URI
     *                corresponding to the physical file.
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @return The file object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     * Constructs a File instance which corresponds to the physical file
     * identified by the passed URI and whose access rights are determined by
     * the passed GATContext.
     * 
     * @param location
     *                A URI which represents the URI corresponding to the
     *                physical file.
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @return The file object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static org.gridlab.gat.io.File createFile(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        Object[] array = { location };
        FileInterface f = (FileInterface) getAdaptorProxy(
                "org.gridlab.gat.io.cpi.FileCpi", FileInterface.class,
                gatContext, preferences, new Class[] { URI.class }, array);

        return new org.gridlab.gat.io.File(f);
    }

    /**
     * This method creates a LogicalFile corresponding to the passed URI
     * instance and uses the passed GATContext to broker resources.
     * 
     * @param gatContext
     *                The GATContext used to broker resources
     * @param name
     *                The name in the logical name space
     * @param mode
     *                The mode to use for opening this logical file. Choose from
     *                LogicalFile.OPEN, LogicalFile.CREATE, LogicalFile.TRUNCATE
     * @return The logical file object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     *                The GATContext used to broker resources
     * @param preferences
     *                The Preferences for this instance
     * @param name
     *                The name in the logical name space
     * @param mode
     *                The mode to use for opening this logical file. Choose from
     *                LogicalFile.OPEN, LogicalFile.CREATE, LogicalFile.TRUNCATE
     * @return The logical file object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static LogicalFile createLogicalFile(GATContext gatContext,
            Preferences preferences, String name, int mode)
            throws GATObjectCreationException {
        Integer modeTmp = new Integer(mode);
        Object[] array = { name, modeTmp };

        return (LogicalFile) getAdaptorProxy(
                "org.gridlab.gat.io.cpi.LogicalFileCpi", LogicalFile.class,
                gatContext, preferences,
                new Class[] { String.class, Integer.TYPE }, array);
    }

    /**
     * Constructs a FileInputStream instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param location
     *                location of the file to read from
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileInputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileInputStream createFileInputStream(GATContext gatContext,
            URI location) throws GATObjectCreationException {
        return createFileInputStream(gatContext, null, location);
    }

    /**
     * Constructs a FileInputStream instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param location
     *                URI of the location the file to read from
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @return The FileInputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileInputStream createFileInputStream(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        Object[] array = { location };
        FileInputStreamInterface res = (FileInputStreamInterface) getAdaptorProxy(
                "org.gridlab.gat.io.cpi.FileInputStreamCpi",
                FileInputStreamInterface.class, gatContext, preferences, 
                new Class[] { URI.class }, array);

        return new FileInputStream(res);
    }

    /**
     * Constructs a FileInputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param file
     *                the file to read from
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileInputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileInputStream createFileInputStream(GATContext gatContext,
            File file) throws GATObjectCreationException {
        return createFileInputStream(gatContext, null, file.toGATURI());
    }

    /**
     * Constructs a FileInputStream instance which corresponds to the physical
     * file identified by the passed file and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param file
     *                the file to read from
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @return The FileInputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileInputStream createFileInputStream(GATContext gatContext,
            Preferences preferences, File file)
            throws GATObjectCreationException {
        return createFileInputStream(gatContext, preferences, file.toGATURI());
    }

    /**
     * Constructs a FileInputStream instance which corresponds to the physical
     * file identified by the passed URI (in String format) and whose access
     * rights are determined by the passed GATContext.
     * 
     * @param location
     *                URI (in String format) of the location of the file to read
     *                from
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileInputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     * file identified by the passed URI (in String format) and whose access
     * rights are determined by the passed GATContext.
     * 
     * @param location
     *                URI (in String format) of the location of the file to read
     *                from
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @return The FileInputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     * file identified by the passed URI (in String format) and whose access
     * rights are determined by the passed GATContext. This stream overwrites
     * the existing file.
     * 
     * @param location
     *                URI (in String format) of the location of the file to
     *                write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     * file identified by the passed URI (in String format) and whose access
     * rights are determined by the passed GATContext. This stream overwrites
     * the existing file.
     * 
     * @param location
     *                URI (in String format) of the location of the file to
     *                write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     * file identified by the passed URI (in String format) and whose access
     * rights are determined by the passed GATContext.
     * 
     * @param location
     *                URI (in String format) of the location of the file to
     *                write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param append
     *                if true, then bytes will be written to the end of the file
     *                rather than the beginning
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     * file identified by the passed URI (in String format) and whose access
     * rights are determined by the passed GATContext.
     * 
     * @param location
     *                URI (in String format) of the location of the file to
     *                write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @param append
     *                if true, then bytes will be written to the end of the file
     *                rather than the beginning
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     * @param file
     *                the file to write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, File file) throws GATObjectCreationException {
        return createFileOutputStream(gatContext, null, file.toGATURI(), false);
    }

    /**
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext. This stream overwrites the existing file.
     * 
     * @param file
     *                the file to write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, Preferences preferences, File file)
            throws GATObjectCreationException {
        return createFileOutputStream(gatContext, preferences, file.toGATURI(),
                false);
    }

    /**
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed File and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param file
     *                the file to write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, File file, boolean append)
            throws GATObjectCreationException {
        return createFileOutputStream(gatContext, null, file.toGATURI(), append);
    }

    /**
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed file and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param file
     *                the file to write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, Preferences preferences, File file,
            boolean append) throws GATObjectCreationException {
        return createFileOutputStream(gatContext, preferences, file.toGATURI(),
                append);
    }

    /**
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext. This stream overwrites the existing file.
     * 
     * @param location
     *                URI of the location the file to write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, URI location)
            throws GATObjectCreationException {
        return createFileOutputStream(gatContext, null, location, false);
    }

    /**
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext. This stream overwrites the existing file.
     * 
     * @param location
     *                URI of the location the file to write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, Preferences preferences, URI location)
            throws GATObjectCreationException {
        return createFileOutputStream(gatContext, preferences, location, false);
    }

    /**
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param location
     *                URI of the location the file to write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, URI location, boolean append)
            throws GATObjectCreationException {
        return createFileOutputStream(gatContext, null, location, append);
    }

    /**
     * Constructs a FileOutputStream instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param location
     *                URI of the location the file to write to
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @return The FileOutputStream object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static FileOutputStream createFileOutputStream(
            GATContext gatContext, Preferences preferences, URI location,
            boolean append) throws GATObjectCreationException {
        Object[] array = { location, new Boolean(append) };
        FileOutputStreamInterface res = (FileOutputStreamInterface) getAdaptorProxy(
                "org.gridlab.gat.io.cpi.FileOutputStreamCpi",
                FileOutputStreamInterface.class, gatContext, preferences,
                new Class[] { URI.class, Boolean.class }, array);

        return new FileOutputStream(res);
    }

    /**
     * Create an endpoint for a gat pipe
     * 
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @return The endpoint object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static Endpoint createEndpoint(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        return (Endpoint) getAdaptorProxy("org.gridlab.gat.io.cpi.EndpointCpi",
                Endpoint.class, gatContext, preferences, null, null);
    }

    /**
     * Create an endpoint for a gat pipe
     * 
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @return The endpoint object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static Endpoint createEndpoint(GATContext gatContext)
            throws GATObjectCreationException {
        return createEndpoint(gatContext, null);
    }

    /**
     * Create an advert service object
     * 
     * @param gatContext
     *                A GATContext which is used to determine the access rights.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences.
     * @return The advert service object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static AdvertService createAdvertService(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        return (AdvertService) getAdaptorProxy(
                "org.gridlab.gat.advert.cpi.AdvertServiceCpi",
                AdvertService.class, gatContext, preferences, null, null);
    }

    /**
     * Create an advert service object
     * 
     * @param gatContext
     *                A GATContext which is used to determine the access rights.
     * @return The advert service object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static AdvertService createAdvertService(GATContext gatContext)
            throws GATObjectCreationException {
        return createAdvertService(gatContext, null);
    }

    /**
     * Create an (default) Monitorable object
     * 
     * @param gatContext
     *                A GATContext object
     * @return The default Monitorable object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static Monitorable createMonitorable(GATContext gatContext)
            throws GATObjectCreationException {
        return createMonitorable(gatContext, null);
    }

    /**
     * Create an (default) Monitorable object
     * 
     * @param gatContext
     *                A GATContext object
     * @param preferences
     *                A Preferences object
     * @return The default Monitorable object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static Monitorable createMonitorable(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        return (Monitorable) getAdaptorProxy(
                "org.gridlab.gat.monitoring.cpi.MonitorableCpi",
                Monitorable.class, gatContext, preferences,
                null, null);
    }

    /**
     * Create a SteeringManager object
     * 
     * @param gatContext
     *                A GATContext object
     * @return The SteeringManager
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static SteeringManager createSteeringManager(GATContext gatContext)
            throws GATObjectCreationException {
        return createSteeringManager(gatContext, null);
    }

    /**
     * Create a SteeringManager object
     * 
     * @param gatContext
     *                A GATContext object
     * @param preferences
     *                A Preferences object
     * @return The SteeringManager
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static SteeringManager createSteeringManager(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        return (SteeringManager) getAdaptorProxy(
                "org.gridlab.gat.steering.cpi.SteeringManagerCpi",
                SteeringManager.class, gatContext, preferences, null, null);
    }

    /**
     * Constructs a RandomAccessFile instance which corresponds to the physical
     * file identified by the passed URI (in String format) and whose access
     * rights are determined by the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param location
     *                a URI (in String format) for the file to access The file
     *                to access
     * @param mode
     *                The mode to open the file with. See
     *                java.io.RandomAccessFile for details.
     * @return The random access file object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     * Constructs a RandomAccessFile instance which corresponds to the physical
     * file identified by the passed URI (in String format) and whose access
     * rights are determined by the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @param location
     *                the URI (in String format) of the file to access
     * @param mode
     *                The mode to open the file with. See
     *                java.io.RandomAccessFile for details.
     * @return The random access file object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
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
     * Constructs a RandomAccessFile instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param location
     *                a URI for the file to access The file to access
     * @param mode
     *                The mode to open the file with. See
     *                java.io.RandomAccessFile for details.
     * @return The random access file object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static RandomAccessFile createRandomAccessFile(
            GATContext gatContext, URI location, String mode)
            throws GATObjectCreationException {
        return createRandomAccessFile(gatContext, null, location, mode);
    }

    /**
     * Constructs a RandomAccessFile instance which corresponds to the physical
     * file identified by the passed URI and whose access rights are determined
     * by the passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which is used to determine the access rights
     *                for this File.
     * @param preferences
     *                A Preferences which is used to determine the user's
     *                preferences for this File.
     * @param location
     *                the URI of the file to access
     * @param mode
     *                The mode to open the file with. See
     *                java.io.RandomAccessFile for details.
     * @return The random access file object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static RandomAccessFile createRandomAccessFile(
            GATContext gatContext, Preferences preferences, URI location,
            String mode) throws GATObjectCreationException {
        Object[] array = { location, mode };

        RandomAccessFileInterface f = (RandomAccessFileInterface) getAdaptorProxy(
                "org.gridlab.gat.io.cpi.RandomAccessFileCpi",
                RandomAccessFileInterface.class, gatContext, preferences,
                new Class[] { URI.class, String.class }, array);
        try {
            return new RandomAccessFile(f);
        } catch (Exception e) {
            throw new GATObjectCreationException("GAT", e);
        }
    }

    /**
     * This method constructs a ResourceBroker instance corresponding to the
     * passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to broker resources
     * @return The resource broker object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static ResourceBroker createResourceBroker(GATContext gatContext)
            throws GATObjectCreationException {
        return createResourceBroker(gatContext, null);
    }

    /**
     * This method constructs a ResourceBroker instance corresponding to the
     * passed GATContext.
     * 
     * @param gatContext
     *                A GATContext which will be used to broker resources
     * @param preferences
     *                The Preferences for this instance
     * @return The resource broker object
     * @throws GATObjectCreationException
     *                 Thrown upon creation problems
     */
    public static ResourceBroker createResourceBroker(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        return (ResourceBroker) getAdaptorProxy(
                "org.gridlab.gat.resources.cpi.ResourceBrokerCpi",
                ResourceBroker.class, gatContext, preferences,
                null, null);
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
            Preferences preferences, Class<?>[] parameterTypes, Object[] tmpParams)
            throws GATObjectCreationException {

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
                    interfaceClass, gatContext, prefs, parameterTypes, tmpParams });
        } catch (Exception e) {
            throw new GATObjectCreationException("", e);
        }
    }
}
