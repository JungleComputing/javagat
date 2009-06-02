package org.gridlab.gat.engine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.jar.Attributes;

import org.gridlab.gat.AdaptorInfo;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class make the various GAT adaptors available to GAT.
 */
public class GATEngine {

    private static Map<String, Attributes> adaptors = new HashMap<String, Attributes>();

    static {
        Attributes attributes = new Attributes();
        attributes
                .putValue(
                        "FileCpi-class",
                        "org.gridlab.gat.io.cpi.local.LocalFileAdaptor,org.gridlab.gat.io.cpi.srcToLocalToDestCopy.SrcToLocalToDestCopyFileAdaptor,org.gridlab.gat.io.cpi.streaming.StreamingFileAdaptor");
        attributes.putValue("FileInputStreamCpi-class",
                "org.gridlab.gat.io.cpi.local.LocalFileInputStreamAdaptor");
        attributes.putValue("FileOutputStreamCpi-class",
                "org.gridlab.gat.io.cpi.local.LocalFileOutputStreamAdaptor");
        attributes.putValue("RandomAccessFileCpi-class",
                "org.gridlab.gat.io.cpi.local.LocalRandomAccessFileAdaptor");
        attributes
                .putValue(
                        "ResourceBrokerCpi-class",
                        "org.gridlab.gat.resources.cpi.local.LocalResourceBrokerAdaptor,org.gridlab.gat.resources.cpi.localQ.LocalQResourceBrokerAdaptor");
        attributes.putValue("EndpointCpi-class",
                "org.gridlab.gat.io.cpi.sockets.SocketEndpointAdaptor");
        adaptors.put("local", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes.putValue("FileCpi-class",
                "org.gridlab.gat.io.cpi.sshtrilead.SshTrileadFileAdaptor");
        attributes
                .putValue("ResourceBrokerCpi-class",
                        "org.gridlab.gat.resources.cpi.sshtrilead.SshTrileadResourceBrokerAdaptor");
        adaptors.put("sshtrilead", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes
                .putValue("ResourceBrokerCpi-class",
                        "org.gridlab.gat.resources.cpi.zorilla.ZorillaResourceBrokerAdaptor");
        adaptors.put("zorilla", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes
                .putValue("FileInputStreamCpi-class",
                        "org.gridlab.gat.io.cpi.copyingFileInputStream.CopyingFileInputStreamAdaptor");
        attributes
                .putValue("FileCpi-class",
                        "org.gridlab.gat.io.cpi.commandlineSsh.CommandlineSshFileAdaptor");
        attributes
                .putValue(
                        "ResourceBrokerCpi-class",
                        "org.gridlab.gat.resources.cpi.commandlineSsh.CommandlineSshResourceBrokerAdaptor");
        adaptors.put("commandlinessh", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes.putValue("LogicalFileCpi-class",
                "org.gridlab.gat.io.cpi.generic.GenericLogicalFileAdaptor");
        attributes
                .putValue("AdvertServiceCpi-class",
                        "org.gridlab.gat.advert.cpi.generic.GenericAdvertServiceAdaptor");
        adaptors.put("generic", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes
                .putValue("ResourceBrokerCpi-class",
                        "org.gridlab.gat.resources.cpi.glite.GliteResourceBrokerAdaptor");
        attributes
                .putValue(
                        "FileCpi-class",
                        "org.gridlab.gat.io.cpi.glite.GliteGuidFileAdaptor,org.gridlab.gat.io.cpi.glite.GliteSrmFileAdaptor,org.gridlab.gat.io.cpi.glite.GliteLfnFileAdaptor");
        adaptors.put("glite", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes
                .putValue(
                        "FileInputStreamCpi-class",
                        "org.gridlab.gat.io.cpi.globus.FTPFileInputStreamAdaptor,org.gridlab.gat.io.cpi.globus.GridFTPFileInputStreamAdaptor,org.gridlab.gat.io.cpi.globus.HTTPFileInputStreamAdaptor,org.gridlab.gat.io.cpi.globus.HTTPSFileInputStreamAdaptor");
        attributes
                .putValue(
                        "FileOutputStreamCpi-class",
                        "org.gridlab.gat.io.cpi.globus.FTPFileOutputStreamAdaptor,org.gridlab.gat.io.cpi.globus.GridFTPFileOutputStreamAdaptor,org.gridlab.gat.io.cpi.globus.HTTPFileOutputStreamAdaptor,org.gridlab.gat.io.cpi.globus.HTTPSFileOutputStreamAdaptor");
        attributes
                .putValue(
                        "ResourceBrokerCpi-class",
                        "org.gridlab.gat.resources.cpi.globus.GlobusResourceBrokerAdaptor,org.gridlab.gat.resources.cpi.wsgt4new.WSGT4newResourceBrokerAdaptor");
        attributes
                .putValue(
                        "FileCpi-class",
                        "org.gridlab.gat.io.cpi.globus.FTPFileAdaptor,org.gridlab.gat.io.cpi.globus.GridFTPFileAdaptor,org.gridlab.gat.io.cpi.gt4.GT4GridFTPFileAdaptor,org.gridlab.gat.io.cpi.rftgt4.RFTGT4FileAdaptor");
        adaptors.put("globus", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes
                .putValue("ResourceBrokerCpi-class",
                        "org.gridlab.gat.resources.cpi.zorilla.GridSAMResourceBrokerAdaptor");
        adaptors.put("gridsam", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes.putValue("ResourceBrokerCpi-class",
                "nl.vu.ibis.KoalaResourceBrokerAdaptor");
        adaptors.put("koala", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes
                .putValue("MonitorableCpi-class",
                        "org.gridlab.gat.monitoring.cpi.mercury.MercuryMonitorableAdaptor");
        adaptors.put("mercury", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes.putValue("FileCpi-class",
                "org.gridlab.gat.io.cpi.sftp.SftpFileAdaptor");
        attributes
                .putValue(
                        "FileOutputStreamCpi-class",
                        "org.gridlab.gat.io.cpi.sftp.org.gridlab.gat.io.cpi.sftp.SftpFileOutputStreamAdaptor");
        attributes.putValue("FileInputStreamCpi-class",
                "org.gridlab.gat.io.cpi.sftp.SftpFileInputStreamAdaptor");
        adaptors.put("sftp", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes.putValue("FileCpi-class",
                "org.gridlab.gat.io.cpi.sftpGanymed.SftpGanymedFileAdaptor");
        adaptors.put("sftpganymed", attributes);
    }

    static {
        Attributes attributes = new Attributes();
        attributes.putValue("ResourceBrokerCpi-class",
                "org.gridlab.gat.resources.cpi.sge.SgeResourceBrokerAdaptor");
        adaptors.put("sge", attributes);
    }

    protected static Logger logger = LoggerFactory.getLogger(GATEngine.class);

    /**
     * A helper class to compare file names, so that they can be sorted, and the
     * order becomes predictable and reproducable.
     */
    private static class FileComparator implements Comparator<File> {
        public int compare(File f1, File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }

    /**
     * A helper class to get the call context. It subclasses SecurityManager to
     * make getClassContext() accessible. Don't install this as an actual
     * security manager!
     */
    private static final class CallerResolver extends SecurityManager {
        protected Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }

    public static final boolean DEBUG = propertySet("gat.debug");

    public static final boolean VERBOSE = propertySet("gat.debug")
            || propertySet("gat.verbose");

    public static final boolean TIMING = propertySet("gat.timing");

    /**
     * This member variable holds reference to the single GATEngine
     */
    private static GATEngine gatEngine = null;

    /**
     * A list of methods that have been registered as unmarshallers for GAT
     * advertizable objects. Elements are of type Class
     */
    private static Vector<Class<?>> unmarshallers = new Vector<Class<?>>();

    private static final ClassLoader systemLoader = ClassLoader
            .getSystemClassLoader();

    private boolean ended = false;

    /** Keys are cpiClass names, elements are AdaptorLists. */
    // private AdaptorSet adaptors;
    private HashMap<String, List<Adaptor>> adaptorLists = new HashMap<String, List<Adaptor>>();

    /**
     * Constructs a default GATEngine instance.
     */
    protected GATEngine() {
        // the commandline parameters -Dgat.debug and -Dgat.verbose override the
        // settings in log4j.properties, so change the level of the parent
        // logger
        /*
         * Not supported by slf4j. if (VERBOSE)
         * logger.getParent().setLevel(Level.INFO); if (DEBUG)
         * logger.getParent().setLevel(Level.DEBUG);
         */
        if (logger.isDebugEnabled()) {
            logger.debug("creating the GAT engine START");
        }

        addAdaptors();

        if (adaptorLists.size() == 0) {
            throw new Error("GAT: No adaptors could be loaded");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("creating the GAT engine: adaptors loaded");
        }

        // order the lists!
        orderAdaptorLists();

        if (logger.isDebugEnabled()) {
            logger.debug("creating the GAT engine: adaptors ordered");
            logger.debug(getAdaptorListString());
        }

        if (logger.isInfoEnabled()) {
            logger.info("creating the GAT engine DONE");
        }

        // Don't add a shutdown hook, the application might add one that depends
        // on GAT
        // Runtime.getRuntime().addShutdownHook(new EndHook());
    }

    private void orderAdaptorLists() {
        AdaptorOrderPolicy adaptorOrderPolicy;

        String policy = System.getProperty("adaptor.order.policy");
        if (logger.isTraceEnabled()) {
            if (policy != null) {
                logger
                        .trace("custom adaptor order policy specified: "
                                + policy);
            } else {
                logger.trace("no custom adaptor order policy specified");
            }

        }

        if (policy != null) {
            Class<?> c;

            try {
                c = Class.forName(policy);
            } catch (ClassNotFoundException e) {
                throw new Error("adaptor policy " + policy + " not found: " + e);
            }

            try {
                adaptorOrderPolicy = (AdaptorOrderPolicy) c.newInstance();
            } catch (Exception e) {
                throw new Error("adaptor policy " + policy
                        + " could not be instantiated: " + e);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("using adaptor ordering policy: " + policy);
            }
        } else {
            adaptorOrderPolicy = new DefaultAdaptorOrderPolicy();
        }
        adaptorOrderPolicy.order(adaptorLists);
    }

    private String getAdaptorListString() {
        String output = "Adaptor Lists:";
        Set<String> keys = adaptorLists.keySet();
        for (String key : keys) {
            output += "\n" + key;
            for (Adaptor adaptor : adaptorLists.get(key)) {
                output += "\n  " + adaptor;
            }
        }
        return output;
    }

    static boolean propertySet(String name) {
        Properties p = System.getProperties();

        if (p != null) {
            String s = p.getProperty(name);

            if (s != null) {
                if (!s.equals("false")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Singleton method to construct a GATEngine
     * 
     * @return A GATEngine instance
     */
    public static synchronized GATEngine getGATEngine() {
        if (gatEngine == null) {
            gatEngine = new GATEngine();
        }

        return gatEngine;
    }

    public static String[] getAdaptorTypes() {
        GATEngine gatEngine = GATEngine.getGATEngine();
        return gatEngine.adaptorLists.keySet().toArray(
                new String[gatEngine.adaptorLists.keySet().size()]);
    }

    @SuppressWarnings("unchecked")
    public static AdaptorInfo[] getAdaptorInfos(String cpiName) {
        GATEngine gatEngine = GATEngine.getGATEngine();
        List<Adaptor> adaptors = gatEngine.adaptorLists.get(cpiName);
        if (adaptors == null) {
            return null;
        }
        AdaptorInfo[] result = new AdaptorInfo[adaptors.size()];
        int i = 0;
        for (Adaptor adaptor : adaptors) {
            Map<String, Boolean> capabilities = null;
            try {
                Class<?> adaptorClass = adaptor.getAdaptorClass();
                Method infoMethod = adaptorClass.getMethod(
                        "getSupportedCapabilities", (Class[]) null);
                capabilities = (Map<String, Boolean>) infoMethod.invoke(null,
                        (Object[]) null);
            } catch (Exception e) {
            }
            Preferences preferences = null;
            try {
                Class<?> adaptorClass = adaptor.getAdaptorClass();
                Method infoMethod = adaptorClass.getMethod(
                        "getSupportedPreferences", (Class[]) null);
                preferences = (Preferences) infoMethod.invoke(null,
                        (Object[]) null);
            } catch (Exception e) {
            }
            String description = "not available";
            try {
                Class<?> adaptorClass = adaptor.getAdaptorClass();
                Method infoMethod = adaptorClass.getMethod("getDescription",
                        (Class[]) null);
                description = (String) infoMethod.invoke(null, (Object[]) null);
            } catch (Exception e) {
            }
            result[i++] = new AdaptorInfo(adaptor.getName(), adaptor
                    .getAdaptorClass().getSimpleName(), cpiName, preferences,
                    capabilities, description);

        }
        return result;
    }

    protected void addAdaptors() {
        for (String adaptor : adaptors.keySet()) {
            loadAdaptor(adaptor);
        }
    }

    protected void loadAdaptor(String adaptor) {
        for (Object key : adaptors.get(adaptor).keySet()) {

            // now get the cpi name (for 'FileCpi' the cpi name is 'File')
            String cpiName = ((Attributes.Name) key).toString().replace(
                    "Cpi-class", "");
            String[] adaptorClasses = adaptors.get(adaptor).getValue(
                    (Attributes.Name) key).split(",");
            if (logger.isTraceEnabled()) {
                logger.trace("found " + adaptorClasses.length
                        + " adaptors implementing cpi: " + cpiName);
            }
            for (String adaptorClass : adaptorClasses) {
                try {
                    if (logger.isTraceEnabled()) {
                        logger.trace("\tloading adaptor: " + adaptorClass);
                    }
                    Class<?> clazz = getClass().getClassLoader().loadClass(
                            adaptorClass);

                    if (containsUnmarshaller(clazz)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("\t\tadaptor " + adaptorClass
                                    + " contains unmarshaller");
                        }
                        unmarshallers.add(clazz);
                    }

                    if (containsInitializer(clazz)) {
                        callInitializer(clazz);
                    }

                    // if there are no adaptors loaded for this cpi name,
                    // make a new list of adaptors that are loaded for this
                    // cpi name
                    if (!adaptorLists.containsKey(cpiName)) {
                        adaptorLists.put(cpiName, new ArrayList<Adaptor>());
                    }
                    // it's guaranteed that we've a list of adaptors
                    // belonging to this cpi name. So get this list and add
                    // the new adaptor to it. Also pass through the
                    // attributes in a preferences object.
                    adaptorLists.get(cpiName).add(new Adaptor(cpiName, clazz));
                } catch (Throwable e) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("\t\tfailed loading adaptor: "
                                + adaptorClass + " (" + e + ")");
                    }
                }
            }

        }
    }

    public static void registerUnmarshaller(Class<?> clazz) {
        if (logger.isTraceEnabled()) {
            logger.trace("\t\tregister marshaller for: " + clazz);
        }
        unmarshallers.add(clazz);
    }

    /**
     * This method unmarshals an advertizable GAT object. The unmarshal method
     * must be registered first
     * 
     * @param input
     * @return
     * @throws GATInvocationException
     */
    public Advertisable unmarshalAdvertisable(GATContext gatContext,
            String input) throws GATInvocationException {
        if (input == null) {
            throw new NullPointerException("cannot unmarshal null String");
        }

        for (int i = 0; i < unmarshallers.size(); i++) {
            Class<?> c = (Class<?>) unmarshallers.get(i);

            try {
                Method m = c.getMethod("unmarshal", new Class[] {
                        GATContext.class, String.class });
                Advertisable res = (Advertisable) m.invoke(null, new Object[] {
                        gatContext, input });

                if (res != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("unmarshalAdvert: returning: " + res);
                    }

                    // success!
                    return res;
                }
            } catch (InvocationTargetException e1) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unmarshaller for "
                            + c.getName()
                            + " failed:"
                            + e1.getTargetException()
                            + "\n"
                            + e1.getTargetException().getStackTrace()
                                    .toString());
                }
                // ignore and try next unmarshaller
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    StringWriter writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    logger.debug("unmarshaller for " + c.getName() + " failed:"
                            + e + "\n" + writer.toString());
                }
                // ignore and try next unmarshaller
            }
        }

        throw new GATInvocationException("could not find suitable unmarshaller");
    }

    public String marshalAdvertisable(Advertisable advert) {
        if (advert == null) {
            throw new NullPointerException("cannot marshal null Advertisable");
        }

        String res = advert.marshal();

        return res;

        /*
         * for(int i=0; i <marshallers.size(); i++) { Class c = (Class)
         * marshallers.get(i);
         * 
         * try { Method m = c.getMethod("marshal", new Class[]
         * {Advertisable.class}); String res = (String) m.invoke(null, new
         * Object[] {advert}); if(res != null) { // success! return res; } }
         * catch (Exception e) { throw new GATInvocationException("could not
         * find or execute marshal method: " + e); } }
         * 
         * throw new GATInvocationException("could not find suitable
         * marshaller");
         */
    }

    private static boolean containsUnmarshaller(Class<?> clazz) {
        // test for marshal and unmarshal methods.
        try {
            clazz.getMethod("unmarshal", new Class[] { GATContext.class,
                    String.class });

            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean containsInitializer(Class<?> clazz) {
        // test for marshal and unmarshal methods.
        try {
            clazz.getMethod("init", (Class[]) null);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private void callInitializer(Class<?> clazz) {
        try {
            Method m = clazz.getMethod("init", (Class[]) null);
            m.invoke((Object) null, (Object[]) null);
        } catch (Throwable t) {
            if (logger.isInfoEnabled()) {
                logger.info("initialization of " + clazz + " failed: " + t);
            }
        }
    }

    public static String defaultMarshal(Object o) {
        throw new Error("cannot marshal");

    }

    public static Advertisable defaultUnmarshal(Class<?> type, String s) {
        throw new Error("cannot unmarshal");
    }

    /**
     * This method should not be called by the user, it is called by the GAT
     * class. Use GAT.end() instead.
     */
    public static void end() {
        GATEngine engine = getGATEngine();

        synchronized (engine) {
            if (engine.ended) {
                return;
            }

            engine.ended = true;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("shutting down the GAT engine START");
        }

        if (logger.isDebugEnabled()) {
            logger
                    .debug("shutting down the GAT engine: shutting down adaptors");
        }

        for (List<Adaptor> adaptorList : engine.adaptorLists.values()) {
            for (Adaptor adaptor : adaptorList) {
                Class<?> c = adaptor.adaptorClass;

                // invoke the "end" static method of the class
                try {
                    Method m = c.getMethod("end", (Class[]) null);
                    m.invoke((Object) null, (Object[]) null);
                } catch (Throwable t) {
                    // ignore
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("shutting down the GAT engine DONE");
        }
    }

    public static Object createAdaptorProxy(String cpiName,
            Class<?> interfaceClass, GATContext gatContext,
            Class<?>[] parameterTypes, Object[] tmpParams)
            throws GATObjectCreationException {
        GATEngine gatEngine = GATEngine.getGATEngine();
        List<Adaptor> adaptors = gatEngine.adaptorLists.get(cpiName);
        if (adaptors == null) {
            throw new GATObjectCreationException("could not find any adaptors");
        }

        if (gatContext.getPreferences().containsKey(cpiName + ".adaptor.name")) {
            if (logger.isDebugEnabled()) {
                logger.debug("old adaptor order: \n" + adaptors.toString());
            }
            adaptors = reorderAdaptorList(adaptors, cpiName, gatContext
                    .getPreferences());
            if (logger.isDebugEnabled()) {
                logger.debug("new adaptor order: \n" + adaptors.toString());
            }
        }

        boolean singleAdaptorPerProxy = false;
        Class<?>[] implementedInterfaces = interfaceClass.getInterfaces();
        if (implementedInterfaces != null) {
            for (Class<?> implementedInterface : implementedInterfaces) {
                if (implementedInterface.getName().equals(
                        "org.gridlab.gat.SingleAdaptorPerProxy")) {
                    singleAdaptorPerProxy = true;
                    break;
                }
            }
        }

        AdaptorInvocationHandler handler = new AdaptorInvocationHandler(
                adaptors, gatContext, parameterTypes, tmpParams,
                singleAdaptorPerProxy);

        Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[] { interfaceClass }, handler);
        return proxy;
    }

    private static List<Adaptor> reorderAdaptorList(List<Adaptor> adaptors,
            String cpiName, Preferences preferences)
            throws GATObjectCreationException {
        logger.debug("reordering, cpiname: " + cpiName + ", preferences: "
                + preferences);

        // parse the orderingString
        // all adaptor names are separated by a ',' and adaptors that should
        // not be used are prefixed with a '!'
        // the case of the adaptor's name doesn't matter
        int insertPosition = 0;
        // make a new result adaptor list and fill it according to the global
        // adaptorlist (to which adaptors refers)
        List<Adaptor> result = new ArrayList<Adaptor>();
        for (int i = 0; i < adaptors.size(); i++) {
            result.add(i, adaptors.get(i));
        }
        // retrieve the adaptor type from the cpiClass
        String nameString;
        // the adaptors.local preference overrides the xxx.adaptor.name
        // preference
        String local = (String) preferences.get("adaptors.local");
        if ((local != null) && local.equalsIgnoreCase("true")) {
            logger.debug("setting to local!");
            nameString = "local";
        } else {
            nameString = (String) preferences.get(cpiName + ".adaptor.name");
        }
        // split the nameString into individual names
        String[] names = nameString.split(",");
        for (int i = 0; i < names.length; i++) {
            names[i] = names[i].trim(); // remove the whitespace
            // names of adaptors that should not be used start with a '!'
            if (names[i].startsWith("!")) {
                names[i] = names[i].substring(1); // remove the '!'
                int pos = result
                        .indexOf(getAdaptor(names[i], cpiName, adaptors));
                // if the adaptor is found, remove it from the list
                if (pos >= 0) {
                    result.remove(pos);
                    // the insert position changes when an adaptor before
                    // this position is removed, so adjust the insertPosition
                    // administration.
                    if (pos < insertPosition)
                        insertPosition--;
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("Found non existing adaptor in " + cpiName
                                + ".adaptor.name preference: " + names[i]);
                    }
                }
            } else if (names[i].equals("")) {
                // which means there is an empty name string. All the remaining
                // adaptors can be added in random order, so just return the
                // result
                return result;
            } else {
                // when the current position is before the insert position, it
                // means that the adaptor is already inserted, so don't insert
                // it again
                int currentPosition = result.indexOf(getAdaptor(names[i],
                        cpiName, adaptors));
                if (currentPosition >= insertPosition) {
                    // place the adaptor on the proper position
                    result.add(insertPosition, result.remove(currentPosition));
                    insertPosition++;
                }
            }
        }
        // when at least one adaptor has been replaced properly (without being
        // removed) the other adaptors are removed from the list unless, the
        // namestring ends with a ','
        if (insertPosition > 0 && !nameString.trim().endsWith(",")) {
            int endPosition = result.size();
            for (int i = insertPosition; i < endPosition; i++) {
                result.remove(insertPosition);
            }
        } else if (insertPosition == 0) {
            throw new GATObjectCreationException(
                    "no adaptors available for preference: \"" + cpiName
                            + ".adaptor.name\", \"" + nameString + "\"");
        }
        return result;
    }

    private static Adaptor getAdaptor(String shortName, String cpiName,
            List<Adaptor> adaptors) {
        for (Adaptor adaptor : adaptors) {
            if (adaptor.getShortAdaptorClassName().equalsIgnoreCase(
                    shortName + cpiName + "Adaptor")) {
                return adaptor;
            }
        }
        return null;
    }

    public static String getLocalHostName() {
        try {
            InetAddress a = InetAddress.getLocalHost();
            if (a != null) {
                return a.getCanonicalHostName();
            }
        } catch (IOException e) {
            // ignore
        }
        return "localhost";
    }

    public static InetAddress getLocalHostAddress() {
        try {
            InetAddress a = InetAddress.getLocalHost();
            return a;
        } catch (IOException e) {
            // ignore
        }
        return null;
    }
}
