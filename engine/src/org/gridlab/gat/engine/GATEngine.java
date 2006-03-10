package org.gridlab.gat.engine;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

import org.gridlab.gat.AdaptorNotSelectedException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.io.cpi.EndpointCpi;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
import org.gridlab.gat.io.cpi.LogicalFileCpi;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricDefinition;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.monitoring.MetricValue;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author rob
 */
/**
 * This class make the various GAT adaptors available to GAT
 */
public class GATEngine {
    public static final boolean DEBUG = propertySet("gat.debug");

    public static final boolean VERBOSE = propertySet("gat.debug")
        || propertySet("gat.verbose");

    /**
     * This member variable holds reference to the single GATEngine
     */
    private static GATEngine gatEngine = null;

    /** A list of methods that have been registered as unmarshallers for GAT
     * advertizable objects. Elements are of type Class */
    private static Vector unmarshallers = new Vector();

    private boolean ended = false;

    /** Keys are cpiClass names, elements are AdaptorLists. */
    private AdaptorSet adaptors;

    /** elements are of type MetricListenerNode */
    private Vector metricListeners = new Vector();

    /** elements are of type MetricNode */
    private Vector metricTable = new Vector();

    URLClassLoader gatClassLoader = null;

    /**
     * Constructs a default GATEngine instance
     */
    protected GATEngine() {
        if (ended) {
            throw new Error("Getting gat engine while end was already called");
        }

        adaptors = new AdaptorSet();
        readJarFiles();

        if (adaptors.size() == 0) {
            throw new Error("GAT: No adaptors could be loaded");
        }

        adaptors.order();

        if (VERBOSE) {
            adaptors.printAdaptorList();
        }

        Runtime.getRuntime().addShutdownHook(new EndHook());
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
     * @param cpiClass the cpi class for which to look
     * @return the list of adaptors
     */
    public AdaptorList getAdaptorList(Class cpiClass)
            throws GATObjectCreationException {
        if (adaptors.getAdaptorList(cpiClass.getName()) == null) {
            // no adaptors for this type loaded.
            if (VERBOSE) {
                System.err
                    .println("getAdaptorList: No adaptors loaded for type "
                        + cpiClass.getName());
            }

            throw new GATObjectCreationException(
                "getAdaptorList: No adaptors loaded for type "
                    + cpiClass.getName());
        } else {
            return adaptors.getAdaptorList(cpiClass.getName());
        }
    }

    /**
     * This method periodically populates the Map returned from a call to the
     * method getCpiClasses().
     */
    protected void readJarFiles() {
        List adaptorPathList = new ArrayList();

        String adaptorPath = System.getProperty("gat.adaptor.path");

        if (adaptorPath != null) {
            StringTokenizer st = new StringTokenizer(adaptorPath,
                File.pathSeparator);

            while (st.hasMoreTokens()) {
                String dir = st.nextToken();
                List l = getJarFiles(dir);
                adaptorPathList.addAll(l);
            }
        }

        if (DEBUG) {
            System.err.println("List of GAT jar files is: ");
            printJars(adaptorPathList);
        }

        ArrayList adaptorPathURLs = new ArrayList();

        for (int i = 0; i < adaptorPathList.size(); i++) {
            JarFile jarFile = (JarFile) adaptorPathList.get(i);

            try {
                File f = new File(jarFile.getName());

                // this does not work for windows (slashes)
                //                String path = "file://" + f.getCanonicalPath();
                //                URL u = new URL(path);
                //                adaptorPathURLs.add(u);
                adaptorPathURLs.add(f.toURL());
            } catch (Exception e) {
                throw new Error(e);
            }
        }

        URL[] urls = new URL[adaptorPathURLs.size()];

        for (int i = 0; i < adaptorPathURLs.size(); i++) {
            urls[i] = (URL) adaptorPathURLs.get(i);
        }

        gatClassLoader = new URLClassLoader(urls);

        // Populate cpiClasses
        loadJarFiles(adaptorPathList);
    }

    protected void printJars(List l) {
        for (int i = 0; i < l.size(); i++) {
            JarFile f = (JarFile) l.get(i);
            System.err.println("    " + f.getName());
        }
    }

    /**
     * Obtains File's in the optional directory.
     *
     * @param f
     *            a directory to list
     * @return a list of files in the passed directory
     */
    protected List getFiles(File f) {
        Vector vector = new Vector();
        File[] files = f.listFiles();

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
     *            the directory to get the jar files from
     * @return a list of JarFile objects
     */
    protected List getJarFiles(String dir) {
        File nextFile = null;
        JarFile jarFile = null;
        Manifest manifest = null;

        // Obtain files in the optional directory.
        List files = getFiles(new File(dir));

        Iterator iterator = files.iterator();

        Vector jarFiles = new Vector();

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

    protected void loadCpiClass(JarFile jarFile, Manifest manifest,
            Attributes attributes, String className, Class cpiClazz) {
        if (DEBUG) {
            System.err.println("Trying to load adaptor for " + className);
        }

        // Get info for the adaptor
        String attributeName = className + "Cpi-class";
        String clazzString = attributes.getValue(attributeName);

        if (clazzString == null) {
            if (DEBUG) {
                System.err.println("Adaptor for " + className
                    + " not found in Manifest");
            }

            return;
        }

        if (DEBUG) {
            System.err.println("Adaptor for " + className
                + " found in Manifest, loading");
        }

        Class clazz = null;

        /* use a URL classloader to load the adaptors.
         * This way, they don't have to be in the classpath */
        try {
            clazz = gatClassLoader.loadClass(clazzString);
        } catch (Exception e) {
            if (DEBUG) {
                System.err.println("Could not load Adaptor for " + className
                    + ": " + e);
                e.printStackTrace();
            }

            return;
        }

        if (containsUnmarshaller(clazz)) {
            unmarshallers.add(clazz);
        }

        if (DEBUG) {
            System.err.println("Adaptor for " + className + " loaded");
        }

        ///////////////
        Preferences preferences = new Preferences();

        Iterator i = attributes.keySet().iterator();

        while (i.hasNext()) {
            Object key = i.next();
            Object value = attributes.get(key);
            preferences.put(key.toString(), value.toString());
        }

        ///////////////
        Adaptor a = new Adaptor(cpiClazz, clazz, preferences);
        AdaptorList s = adaptors.getAdaptorList(cpiClazz.getName());

        if (s == null) {
            s = new AdaptorList(cpiClazz);
            adaptors.add(cpiClazz.getName(), s);
        }

        s.addAdaptor(a);
    }

    protected void loadCPIClassesFromJar(JarFile jarFile) {
        Manifest manifest = null;
        Attributes attributes = null;

        // Get info for all adaptors
        try {
            manifest = jarFile.getManifest();
        } catch (IOException e) {
            return;
        }

        attributes = manifest.getMainAttributes();

        loadCpiClass(jarFile, manifest, attributes, "Endpoint",
            EndpointCpi.class);
        loadCpiClass(jarFile, manifest, attributes, "AdvertService",
            AdvertServiceCpi.class);
        loadCpiClass(jarFile, manifest, attributes, "Monitorable",
            MonitorableCpi.class);
        loadCpiClass(jarFile, manifest, attributes, "File", FileCpi.class);
        loadCpiClass(jarFile, manifest, attributes, "LogicalFile",
            LogicalFileCpi.class);
        loadCpiClass(jarFile, manifest, attributes, "RandomAccessFile",
            RandomAccessFileCpi.class);
        loadCpiClass(jarFile, manifest, attributes, "FileInputStream",
            FileInputStreamCpi.class);
        loadCpiClass(jarFile, manifest, attributes, "FileOutputStream",
            FileOutputStreamCpi.class);
        loadCpiClass(jarFile, manifest, attributes, "ResourceBroker",
            ResourceBrokerCpi.class);
    }

    /**
     * load jar files in the list, looking for CPI classes
     *
     * @param jarFiles
     *            the list of JarFile objects to load
     */
    protected void loadJarFiles(List jarFiles) {
        JarFile jarFile = null;

        Iterator iterator = jarFiles.iterator();

        // Iterate over JarFiles
        while (iterator.hasNext()) {
            jarFile = (JarFile) iterator.next();

            if (DEBUG) {
                System.err
                    .println("loading adaptors from " + jarFile.getName());
            }

            loadCPIClassesFromJar(jarFile);
        }
    }

    /**
     * This method unmarshals an advertizable GAT object.
     * The unmarshal method must be registered first
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
            Class c = (Class) unmarshallers.get(i);

            try {
                Method m = c.getMethod("unmarshal", new Class[] {
                    GATContext.class, Preferences.class, String.class });
                Advertisable res = (Advertisable) m.invoke(null, new Object[] {
                    gatContext, preferences, input });

                if (res != null) {
                    if (DEBUG) {
                        System.err
                            .println("unmarshalAdvert: returning: " + res);
                    }

                    // success!
                    return res;
                }
            } catch (InvocationTargetException e1) {
                if (DEBUG) {
                    System.err.println("unmarshaller for " + c.getName()
                        + " failed:" + e1.getTargetException());
                }

                // ignore and try next unmarshaller                
            } catch (Exception e) {
                if (DEBUG) {
                    System.err.println("unmarshaller for " + c.getName()
                        + " failed:" + e);
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

    public static boolean containsUnmarshaller(Class clazz) {
        // test for marshal and unmarshall methods.
        try {
            //          Method m = marshaller.getMethod("marshal", new Class[]
            // {Advertisable.class});
            clazz.getMethod("unmarshal", new Class[] { GATContext.class,
                Preferences.class, String.class });

            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static String defaultMarshal(Object o) {
        StringWriter sw = new StringWriter();

        try {
            Marshaller.marshal(o, sw);
        } catch (Throwable e) {
            throw new Error("could not marshal object: ", e);
        }

        return sw.toString();
    }

    public static Advertisable defaultUnmarshal(Class type, String s) {
        StringReader sr = new StringReader(s);

        try {
            if (DEBUG) {
                System.err.println("default unmarshaller start, type = " + type
                    + " string = " + s);
            }

            Unmarshaller unmarshaller = new Unmarshaller(type);
            unmarshaller.setIgnoreExtraAttributes(false);
            unmarshaller.setIgnoreExtraElements(false);
            unmarshaller.setValidation(true);

            Advertisable res = (Advertisable) unmarshaller.unmarshal(sr);

            if (DEBUG) {
                System.err.println("default unmarshaller returning " + res);
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

    public static List getMetricDefinitions(Object adaptor) {
        GATEngine e = getGATEngine();

        synchronized (e) {
            Vector res = new Vector();

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

            throw new GATInvocationException("the metric name is incorrect");
        }
    }

    public static void fireMetric(Object adaptor, MetricValue v) {
        // look for all callbacks that were installed for this metric, call
        // them.
        GATEngine e = getGATEngine();

        for (int i = 0; i < e.metricListeners.size(); i++) {
            MetricListenerNode n = (MetricListenerNode) e.metricListeners
                .get(i);

            if (n.adaptor == adaptor) {
                if (n.metric.equals(v.getMetric())) {
                    // hiha, right adaptor and metric
                    // call the handler
                    try {
                        n.metricListener.processMetricEvent(v);
                    } catch (Throwable t) {
                        System.err
                            .println("WARNING, user callback threw exception: "
                                + t);
                        t.printStackTrace();
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

    /** This method should not be called by the user, it is called by the GAT class. Use GAT.end() instead. */
    public static void end() {
        GATEngine engine = getGATEngine();

        synchronized (engine) {
            if (engine.ended) {
                return;
            }

            engine.ended = true;
        }

        if (GATEngine.DEBUG) {
            System.err.println("shutting down GAT");
        }

        for (int i = 0; i < engine.adaptors.size(); i++) {
            AdaptorList l = engine.adaptors.getAdaptorList(i);

            for (int j = 0; j < l.size(); j++) {
                Adaptor a = l.get(j);
                Class c = a.adaptorClass;

                // invoke the "end" static method of the class
                try {
                    Method m = c.getMethod("end", (Class[]) null);
                    m.invoke((Object) null, (Object[]) null);
                } catch (Throwable t) {
                    // ignore
                }
            }
        }

        if (GATEngine.DEBUG) {
            System.err.println("shutting down GAT DONE");
        }
    }

    /**
     * This method checks the preferences to see if the adaptor given in
     * adaptorName can be used. Maybe the user only wants local adaptors, or
     * maybe a specific  adaptor only.
     * @param preferences the preferences object
     * @param adaptorType the type of the adaptor (e.g., File or RecourceBroker)
     * @param adaptorName the name of the adaptor
     * @throws GATObjectCreationException
     */
    public static void checkName(Preferences preferences, String adaptorType,
            String adaptorName) throws GATObjectCreationException {
        String local = (String) preferences.get("adaptors.local");

        if ((local != null) && local.equalsIgnoreCase("true")
            && !adaptorName.equalsIgnoreCase("local")) {
            throw new AdaptorNotSelectedException("this adaptor (" + adaptorName
                + ") was not selected by the user.\n"
                + "Only local adaptors will be used "
                + "(property adaptors.local was set to true)");
        }

        String name = (String) preferences.get(adaptorType + ".adaptor.name");

        //		System.err.println("checkname of type " + adaptorType + " name " + adaptorName + " selected = " + name);
        if ((name != null) && !name.equalsIgnoreCase(adaptorName)) {
            throw new AdaptorNotSelectedException("this adaptor (" + adaptorName
                + ") was not selected by the user.");
        }
    }

    private static class EndHook extends Thread {
        public void run() {
            end();
        }
    }
}
