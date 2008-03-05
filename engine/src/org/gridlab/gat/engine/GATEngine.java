package org.gridlab.gat.engine;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;

/**
 * @author rob
 */
/**
 * This class make the various GAT adaptors available to GAT
 */
public class GATEngine {

    private static class FileComparator implements Comparator<File> {
        public int compare(File f1, File f2) {
            return f1.getName().compareTo(f2.getName());
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

    private boolean ended = false;

    /** Keys are cpiClass names, elements are AdaptorLists. */
    // private AdaptorSet adaptors;
    private HashMap<String, List<Adaptor>> adaptorLists = new HashMap<String, List<Adaptor>>();

    /** elements are of type MetricListenerNode */
    private Vector<MetricListenerNode> metricListeners = new Vector<MetricListenerNode>();

    /** elements are of type MetricNode */
    private Vector<MetricNode> metricTable = new Vector<MetricNode>();

    URLClassLoader gatClassLoader = null;

    protected static Logger logger = Logger.getLogger(GATEngine.class);

    /**
     * Constructs a default GATEngine instance
     */
    protected GATEngine() {
        // the commandline parameters -Dgat.debug and -Dgat.verbose override the
        // settings in log4j.properties, so change the level of the parent
        // logger
        if (VERBOSE)
            logger.getParent().setLevel(Level.INFO);
        if (DEBUG)
            logger.getParent().setLevel(Level.DEBUG);

        if (ended) {
            throw new Error("Getting gat engine while end was already called");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Before reading jar files");
        }

        readJarFiles();

        if (adaptorLists.size() == 0) {
            throw new Error("GAT: No adaptors could be loaded");
        }

        // order the lists!
        orderAdaptorLists();

        if (logger.isInfoEnabled()) {
            logger.info(getAdaptorListString());
        }
        // Don't add a shutdown hook, the application might add one that depends
        // on GAT
        // Runtime.getRuntime().addShutdownHook(new EndHook());
    }

    private void orderAdaptorLists() {
        AdaptorOrderPolicy adaptorOrderPolicy;

        String policy = System.getProperty("adaptor.order.policy");

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
            if (logger.isInfoEnabled()) {
                logger.info("using adaptor ordering policy: " + policy);
            }
        } else {
            adaptorOrderPolicy = new DefaultAdaptorOrderPolicy();

            if (logger.isInfoEnabled()) {
                logger.info("using default adaptor ordering policy");
            }
        }
        adaptorOrderPolicy.order(adaptorLists);
    }

    private String getAdaptorListString() {
        String output = "\nAdaptor Lists:\n\n";
        Set<String> keys = adaptorLists.keySet();
        for (String key : keys) {
            output += "\t" + key + "\n";
            for (Adaptor adaptor : adaptorLists.get(key)) {
                output += "\t\t" + adaptor + "\n";
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

    /**
     * Returns a list of adaptors for the specified cpiClass
     * 
     * @param cpiClass
     *                the cpi class for which to look
     * @return the list of adaptors
     */
    public List<Adaptor> getAdaptorList(String cpiName)
            throws GATObjectCreationException {
        List<Adaptor> result = adaptorLists.get(cpiName);
        if (result == null) {
            throw new GATObjectCreationException(
                    "getAdaptorList: No adaptors loaded for type " + cpiName);
        }
        return result;
    }

    // /**
    // * Returns a list of adaptors for the specified cpiClass
    // *
    // * @param cpiClass
    // * the cpi class for which to look
    // * @return the list of adaptors
    // */
    // public AdaptorList getAdaptorList(Class<?> cpiClass)
    // throws GATObjectCreationException {
    // if (adaptors.getAdaptorList(cpiClass.getName()) == null) {
    // // no adaptors for this type loaded.
    // if (logger.isInfoEnabled()) {
    // logger.info("getAdaptorList: No adaptors loaded for type "
    // + cpiClass.getName());
    // }
    //
    // throw new GATObjectCreationException(
    // "getAdaptorList: No adaptors loaded for type "
    // + cpiClass.getName());
    // } else {
    // return adaptors.getAdaptorList(cpiClass.getName());
    // }
    // }

    protected void readJarFiles() {
        // retrieve the path where the adaptors are located.
        String adaptorPath = System.getProperty("gat.adaptor.path");

        if (adaptorPath != null) {
            File adaptorRoot = new File(adaptorPath);
            if (!adaptorRoot.exists()) {
                throw new Error("gat.adaptor.path set to '" + adaptorPath
                        + "', but it doesn't exist!");
            }
            // now get the adaptor dirs from the adaptor path, adaptor dirs are
            // of course directories and further will end with "Adaptor"
            File[] adaptorDirs = adaptorRoot.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory()
                            && file.getName().endsWith("Adaptor");
                }
            });
            if (adaptorDirs.length == 0) {
                throw new Error("gat.adaptor.path set to '" + adaptorPath
                        + "', but it doesn't contain any adaptor");
            }
            HashMap<String, ClassLoader> adaptorClassLoaders = new HashMap<String, ClassLoader>();
            for (File adaptorDir : adaptorDirs) {
                try {
                    adaptorClassLoaders.put(adaptorDir.getName(),
                            loadDirectory(adaptorDir));
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unable to load adaptor '"
                                + adaptorDir.getName() + "': " + e);
                        e.printStackTrace();
                    }
                }
            }
            // TODO this should be fixed more general!
            Thread.currentThread().setContextClassLoader(
                    adaptorClassLoaders.get("WSGT4Adaptor"));
        } else {
            throw new Error("gat.adaptor.path not set!");
        }
    }

    private ClassLoader loadDirectory(File adaptorDir) throws Exception {
        File adaptorJarFile = new File(adaptorDir.getPath() + File.separator
                + adaptorDir.getName() + ".jar");
        if (!adaptorJarFile.exists()) {
            throw new Exception("found adaptor dir '" + adaptorDir.getPath()
                    + "' that doesn't contain an adaptor named '"
                    + adaptorJarFile.getPath() + "'");
        }
        JarFile adaptorJar = new JarFile(adaptorJarFile, true);
        Attributes attributes = adaptorJar.getManifest().getMainAttributes();
        String[] externalJars = adaptorDir.list(new java.io.FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".jar");
            }
        });
        Arrays.sort(externalJars);
        ArrayList<URL> adaptorPathURLs = new ArrayList<URL>();
        adaptorPathURLs.add(adaptorJarFile.toURI().toURL());
        if (externalJars != null) {
            for (String externalJar : externalJars) {
                adaptorPathURLs.add(new URL(adaptorJarFile.getParentFile()
                        .toURI().toURL().toString()
                        + externalJar));
            }
        }
        URL[] urls = new URL[adaptorPathURLs.size()];
        for (int i = 0; i < adaptorPathURLs.size(); i++) {
            urls[i] = (URL) adaptorPathURLs.get(i);
        }
        URLClassLoader adaptorLoader = new URLClassLoader(urls, this.getClass()
                .getClassLoader());
        // We've a class loader, now have a look at which adaptors are inside
        // this jar.
        Set<Object> keys = attributes.keySet();
        for (Object key : keys) {
            if (((Attributes.Name) key).toString().endsWith("Cpi-class")) {
                // this is an adaptor!

                // now get the cpi name (for 'FileCpi' the cpi name is 'File')
                String cpiName = ((Attributes.Name) key).toString().replace(
                        "Cpi-class", "");
                String[] adaptorClasses = attributes.getValue(
                        (Attributes.Name) key).split(",");
                for (String adaptorClass : adaptorClasses) {
                    try {
                        Thread.currentThread().setContextClassLoader(
                                adaptorLoader);
                        Class<?> clazz = adaptorLoader.loadClass(adaptorClass);

                        if (containsUnmarshaller(clazz)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Adaptor " + adaptorClass
                                        + " contains unmarshaller");
                            }
                            unmarshallers.add(clazz);
                        }

                        if (containsInitializer(clazz)) {
                            callInitializer(clazz);
                        }

                        // if there are no adaptors loaded for this cpi name,
                        // make a
                        // new list of adaptors that are loaded for this cpi
                        // name
                        if (!adaptorLists.containsKey(cpiName)) {
                            adaptorLists.put(cpiName, new ArrayList<Adaptor>());
                        }
                        // it's guaranteed that we've a list of adaptors
                        // belonging
                        // to this cpi name. So get this list and add the new
                        // adaptor to it. Also pass through the attributes in a
                        // preferences object.
                        adaptorLists.get(cpiName).add(
                                new Adaptor(cpiName, clazz,
                                        attributesToPreferences(attributes)));
                        // ok, now we're done loading this class and updating
                        // our
                        // administration
                    } catch (Exception e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Could not load Adaptor for " + key
                                    + ": " + e);
                        }
                    }
                }
            }
        }

        return adaptorLoader;
    }

    private Preferences attributesToPreferences(Attributes attributes) {
        Preferences preferences = new Preferences();

        Iterator<Object> i = attributes.keySet().iterator();

        while (i.hasNext()) {
            Object key = i.next();
            Object value = attributes.get(key);
            preferences.put(key.toString(), value.toString());
        }
        return preferences;
    }

    protected String getJarsAsString(URL[] urls) {
        String result = "";
        for (int i = 0; i < urls.length; i++) {
            result += "    " + urls[i].getFile();
        }
        return result;
    }

    /**
     * Obtains File's in the optional directory.
     * 
     * @param f
     *                a directory to list
     * @return a list of files in the passed directory
     */
    protected List<File> getFiles(File f) {
        Vector<File> vector = new Vector<File>();
        File[] files = f.listFiles();

        // Sort the list of files so that the classloader always sees the jar
        // files
        // in the same order. Then, at least, it is deterministic which version
        // of
        // a class is loaded. It could still be the wrong one for a specific
        // adaptor,
        // though, especially when different adaptors depend on different
        // versions
        // of a jar. I don't know what to do about that. Different classloader
        // for
        // each adaptor? --Ceriel
        Arrays.sort(files, new FileComparator());

        if (files == null) {
            // IO error or dir does not exist
            return vector;
        }

        for (int count = 0; count < files.length; count++)
            vector.add(files[count]);

        return vector;
    }

    /**
     * Obtains JarFile's in the optional directory that are GAT jar's
     * 
     * @param dir
     *                the directory to get the jar files from
     * @return a list of JarFile objects
     */
    protected List<JarFile> getJarFiles(String dir) {
        File nextFile = null;
        JarFile jarFile = null;
        Manifest manifest = null;

        // Obtain files in the optional directory.
        List<File> files = getFiles(new File(dir));

        Iterator<File> iterator = files.iterator();

        Vector<JarFile> jarFiles = new Vector<JarFile>();

        while (iterator.hasNext()) {
            nextFile = (File) iterator.next();

            if (nextFile.isFile()) {
                try {
                    jarFile = new JarFile(nextFile, true);
                    manifest = jarFile.getManifest();

                    if (null != manifest) {
                        manifest.getMainAttributes();
                        jarFiles.add(jarFile);
                    }
                } catch (IOException ioException) {
                    // Ignore IOException
                }
            }
        }

        return jarFiles;
    }

    public static void registerUnmarshaller(Class<?> clazz) {
        if (logger.isDebugEnabled()) {
            logger.debug("register marshaller for: " + clazz);
        }
        unmarshallers.add(clazz);
    }

    // protected void loadCpiClass(JarFile jarFile, Manifest manifest,
    // Attributes attributes, String className, Class<?> cpiClazz) {
    // if (logger.isDebugEnabled()) {
    // logger.debug("Trying to load adaptor for " + className);
    // }
    //
    // // Get info for the adaptor
    // String attributeName = className + "Cpi-class";
    // String clazzString = attributes.getValue(attributeName);
    // if (clazzString == null) {
    // if (logger.isDebugEnabled()) {
    // logger.debug("Adaptor for " + className
    // + " not found in Manifest");
    // }
    // return;
    // }
    // if (logger.isDebugEnabled()) {
    // logger.debug("Adaptor for " + className
    // + " found in Manifest, loading");
    // }
    //
    // Class<?> clazz = null;
    //
    // /*
    // * use a URL classloader to load the adaptors. This way, they don't have
    // * to be in the classpath
    // */
    // try {
    // clazz = gatClassLoader.loadClass(clazzString);
    // } catch (Exception e) {
    // if (logger.isDebugEnabled()) {
    // StringWriter writer = new StringWriter();
    // e.printStackTrace(new PrintWriter(writer));
    // logger.debug("Could not load Adaptor for " + className + ": "
    // + e + "\n" + writer.toString());
    // }
    // return;
    // }
    //
    // if (containsUnmarshaller(clazz)) {
    // if (logger.isDebugEnabled()) {
    // logger.debug("Adaptor " + clazzString
    // + " contains unmarshaller");
    // }
    //
    // unmarshallers.add(clazz);
    // }
    //
    // if (containsInitializer(clazz)) {
    // callInitializer(clazz);
    // }
    //
    // if (logger.isDebugEnabled()) {
    // logger.debug("Adaptor for " + className + " loaded");
    // }
    //
    // // /////////////
    // Preferences preferences = new Preferences();
    //
    // Iterator<Object> i = attributes.keySet().iterator();
    //
    // while (i.hasNext()) {
    // Object key = i.next();
    // Object value = attributes.get(key);
    // preferences.put(key.toString(), value.toString());
    // }
    //
    // // /////////////
    // Adaptor a = new Adaptor(cpiClazz, clazz, preferences);
    // AdaptorList s = adaptors.getAdaptorList(cpiClazz.getName());
    //
    // if (s == null) {
    // s = new AdaptorList(cpiClazz);
    // adaptors.add(cpiClazz.getName(), s);
    // }
    //
    // s.addAdaptor(a);
    // }

    // protected void loadCPIClassesFromJar(JarFile jarFile) {
    // Manifest manifest = null;
    // Attributes attributes = null;
    //
    // // Get info for all adaptors
    // try {
    // manifest = jarFile.getManifest();
    // } catch (IOException e) {
    // return;
    // }
    //
    // attributes = manifest.getMainAttributes();
    //
    // loadCpiClass(jarFile, manifest, attributes, "Endpoint",
    // EndpointCpi.class);
    // loadCpiClass(jarFile, manifest, attributes, "AdvertService",
    // AdvertServiceCpi.class);
    // loadCpiClass(jarFile, manifest, attributes, "Monitorable",
    // MonitorableCpi.class);
    // loadCpiClass(jarFile, manifest, attributes, "SteeringManager",
    // SteeringManagerCpi.class);
    // loadCpiClass(jarFile, manifest, attributes, "File", FileCpi.class);
    // loadCpiClass(jarFile, manifest, attributes, "LogicalFile",
    // LogicalFileCpi.class);
    // loadCpiClass(jarFile, manifest, attributes, "RandomAccessFile",
    // RandomAccessFileCpi.class);
    // loadCpiClass(jarFile, manifest, attributes, "FileInputStream",
    // FileInputStreamCpi.class);
    // loadCpiClass(jarFile, manifest, attributes, "FileOutputStream",
    // FileOutputStreamCpi.class);
    // loadCpiClass(jarFile, manifest, attributes, "ResourceBroker",
    // ResourceBrokerCpi.class);
    // }

    // /**
    // * load jar files in the list, looking for CPI classes
    // *
    // * @param jarFiles
    // * the list of JarFile objects to load
    // */
    // protected void loadJarFiles(List<JarFile> jarFiles) {
    // JarFile jarFile = null;
    //
    // Iterator<JarFile> iterator = jarFiles.iterator();
    //
    // // Iterate over JarFiles
    // while (iterator.hasNext()) {
    // jarFile = (JarFile) iterator.next();
    //
    // if (logger.isDebugEnabled()) {
    // logger.debug("loading adaptors from " + jarFile.getName());
    // }
    //
    // loadCPIClassesFromJar(jarFile);
    // }
    // }

    /**
     * This method unmarshals an advertizable GAT object. The unmarshal method
     * must be registered first
     * 
     * @param input
     * @return
     * @throws GATInvocationException
     */
    public Advertisable unmarshalAdvertisable(GATContext gatContext,
            Preferences preferences, String input)
            throws GATInvocationException {
        if (input == null) {
            throw new NullPointerException("cannot unmarshal null String");
        }

        for (int i = 0; i < unmarshallers.size(); i++) {
            Class<?> c = (Class<?>) unmarshallers.get(i);

            try {
                Method m = c.getMethod("unmarshal", new Class[] {
                        GATContext.class, Preferences.class, String.class });
                Advertisable res = (Advertisable) m.invoke(null, new Object[] {
                        gatContext, preferences, input });

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
                    Preferences.class, String.class });

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
        if (o == null) {
            throw new Error("cannot marshal a null object");
        }

        StringWriter sw = new StringWriter();

        try {
            Marshaller.marshal(o, sw);
        } catch (Throwable e) {
            throw new Error("could not marshal object: ", e);
        }

        return sw.toString();
    }

    public static Advertisable defaultUnmarshal(Class<?> type, String s) {
        if (s == null) {
            throw new Error("cannot unmarshal a null object");
        }

        StringReader sr = new StringReader(s);

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("default unmarshaller start, type = " + type
                        + " string = " + s);
            }

            Unmarshaller unmarshaller = new Unmarshaller(type);
            unmarshaller.setIgnoreExtraAttributes(false);
            unmarshaller.setIgnoreExtraElements(false);
            unmarshaller.setValidation(true);

            Advertisable res = (Advertisable) unmarshaller.unmarshal(sr);

            if (res == null) {
                throw new Error("cannot unmarshal this object");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("default unmarshaller returning " + res);
            }

            return res;
        } catch (Exception e) {
            throw new Error("could not unmarshal object: " + e);
        }
    }

    public static void addMetricListener(Object adaptor,
            MetricListener metricListener, Metric metric)
            throws GATInvocationException {
        GATEngine e = getGATEngine();

        synchronized (e) {
            // check whether the adaptor actually registered this metric
            boolean found = false;

            for (int i = 0; i < e.metricTable.size(); i++) {
                MetricNode n = (MetricNode) e.metricTable.get(i);

                if (n.adaptor == adaptor) {
                    if (n.definition.equals(metric.getDefinition())) {
                        found = true;

                        break;
                    }
                }
            }

            if (!found) {
                throw new GATInvocationException();
            }

            e.metricListeners.add(new MetricListenerNode(adaptor,
                    metricListener, metric));
        }
    }

    public static void removeMetricListener(Object adaptor,
            MetricListener metricListener, Metric metric)
            throws NoSuchElementException {
        GATEngine e = getGATEngine();

        synchronized (e) {
            if (!e.metricListeners.remove(new MetricListenerNode(adaptor,
                    metricListener, metric))) {
                throw new NoSuchElementException();
            }
        }
    }

    public static void registerMetric(Object adaptor, String methodName,
            MetricDefinition definition) {
        GATEngine e = getGATEngine();

        synchronized (e) {
            e.metricTable.add(new MetricNode(adaptor, methodName, definition));
        }
    }

    public static List<MetricDefinition> getMetricDefinitions(Object adaptor) {
        GATEngine e = getGATEngine();

        synchronized (e) {
            Vector<MetricDefinition> res = new Vector<MetricDefinition>();

            for (int i = 0; i < e.metricTable.size(); i++) {
                MetricNode n = (MetricNode) e.metricTable.get(i);

                if (n.adaptor == adaptor) {
                    res.add(n.definition);
                }
            }

            return res;
        }
    }

    public static MetricDefinition getMetricDefinitionByName(Object adaptor,
            String name) throws GATInvocationException {
        GATEngine e = getGATEngine();

        synchronized (e) {
            for (int i = 0; i < e.metricTable.size(); i++) {
                MetricNode n = (MetricNode) e.metricTable.get(i);
                if ((n.adaptor == adaptor)
                        && name.equals(n.definition.getMetricName())) {
                    return n.definition;
                }
            }

            throw new GATInvocationException("the metric name is incorrect: "
                    + name);
        }
    }

    public static void fireMetric(Object adaptor, MetricValue v) {
        // look for all callbacks that were installed for this metric, call
        // them.
        GATEngine e = getGATEngine();
        MetricListenerNode[] listenerNodes = (MetricListenerNode[]) e.metricListeners
                .toArray(new MetricListenerNode[e.metricListeners.size()]);

        for (int i = 0; i < listenerNodes.length; i++) {
            if (listenerNodes[i].adaptor == adaptor) {
                if (listenerNodes[i].metric.equals(v.getMetric())) {
                    // hiha, right adaptor and metric
                    // call the handler
                    try {
                        listenerNodes[i].metricListener.processMetricEvent(v);
                    } catch (Throwable t) {
                        StringWriter writer = new StringWriter();
                        t.printStackTrace(new PrintWriter(writer));
                        logger.warn("WARNING, user callback threw exception: "
                                + t + "\n" + writer.toString());
                    }
                }
            }
        }

        // now, also store the last value, a user might poll for it with the
        // getMeasurement call.
        for (int i = 0; i < e.metricTable.size(); i++) {
            MetricNode n = (MetricNode) e.metricTable.get(i);

            if (n.adaptor == adaptor) {
                if (n.definition.equals(v.getMetric().getDefinition())) {
                    n.setLastValue(v);

                    return;
                }
            }
        }

        throw new Error("Internal error: event fired for non-registered metric");
    }

    public static MetricValue getMeasurement(Object adaptor, Metric metric)
            throws GATInvocationException {
        if (metric.getDefinition().getMeasurementType() != MetricDefinition.DISCRETE) {
            throw new GATInvocationException(
                    "internal adaptor error: GATEngine.getMeasurement can only handle discrete metrics");
        }

        GATEngine e = getGATEngine();

        for (int i = 0; i < e.metricTable.size(); i++) {
            MetricNode n = (MetricNode) e.metricTable.get(i);

            if (n.adaptor == adaptor) {
                if (n.definition.equals(metric.getDefinition())) {
                    if (n.lastValue == null) {
                        throw new GATInvocationException(
                                "No data available for this metric");
                    }

                    return n.lastValue;
                }
            }
        }

        throw new GATInvocationException("No data available for this metric");
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
            logger.debug("shutting down GAT");
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

        if (logger.isDebugEnabled()) {
            logger.debug("shutting down GAT DONE");
        }
    }

    public static Object createAdaptorProxy(String cpiName,
            Class<?> interfaceClass, GATContext gatContext,
            Preferences preferences, Class<?>[] parameterTypes,
            Object[] tmpParams) throws GATObjectCreationException {

        GATEngine gatEngine = GATEngine.getGATEngine();

        List<Adaptor> adaptors = gatEngine.adaptorLists.get(cpiName);
        if (adaptors == null) {
            throw new GATObjectCreationException("could not find any adaptors");
        }

        if (preferences.containsKey(cpiName + ".adaptor.name")) {
            if (logger.isInfoEnabled()) {
                logger.info("old adaptor order: \n" + adaptors.toString());
            }
            adaptors = reorderAdaptorList(adaptors, cpiName, preferences);
            if (logger.isInfoEnabled()) {
                logger.info("new adaptor order: \n" + adaptors.toString());
            }
        }

        AdaptorInvocationHandler handler = new AdaptorInvocationHandler(
                adaptors, gatContext, preferences, parameterTypes, tmpParams);
        Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class[] { interfaceClass }, handler);
        return proxy;
    }

    private static List<Adaptor> reorderAdaptorList(List<Adaptor> adaptors,
            String cpiName, Preferences preferences)
            throws GATObjectCreationException {
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
