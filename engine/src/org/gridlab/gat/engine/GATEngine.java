package org.gridlab.gat.engine;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.gridlab.gat.AdaptorInfo;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.cpi.SerializedBase;
import org.gridlab.gat.engine.util.FileWaiter;
import org.gridlab.gat.engine.util.ScheduledExecutor;
import org.gridlab.gat.resources.cpi.JobCpi;

/**
 * @author rob
 */
/**
 * This class make the various GAT adaptors available to GAT.
 */
public class GATEngine {

    protected static Logger logger = LoggerFactory.getLogger(GATEngine.class);

    /**
     * A helper class to compare file names, so that they can be sorted,
     * and the order becomes predictable and reproducible.
     */
    private static class FileComparator implements Comparator<File> {
        public int compare(File f1, File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    }
    
    /**
     * A helper class to get the call context. It subclasses SecurityManager
     * to make getClassContext() accessible. Don't install this as an actual
     * security manager!
     */
    private static final class CallerResolver extends SecurityManager {
        protected Class<?>[] getClassContext() {
            return super.getClassContext ();
        }
    }

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
    
    private static final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();

    private boolean ended = false;

    /** Keys are cpiClass names, elements are AdaptorLists. */
    // private AdaptorSet adaptors;
    private HashMap<String, List<Adaptor>> adaptorLists = new HashMap<String, List<Adaptor>>();
   
    /** Classloader to be used as parent classloader for the URL classloaders. */
    private ClassLoader parentLoader;

    /** Set when the unmarshallers have been added to the list. */
    private boolean unmarshallersAdded;

    /**
     * Constructs a default GATEngine instance.
     */
    protected GATEngine() {
        // the commandline parameters -Dgat.debug and -Dgat.verbose override the
        // settings in log4j.properties, so change the level of the parent
        // logger
        /*
         * Not supported by slf4j.
        if (VERBOSE)
            logger.getParent().setLevel(Level.INFO);
        if (DEBUG)
            logger.getParent().setLevel(Level.DEBUG);
        */

        if (logger.isDebugEnabled()) {
            logger.debug("creating the GAT engine START");
        }
        
        ClassLoader superparentLoader = getParentClassLoader();
        
        String adaptorPath = System.getProperty("gat.adaptor.path");
        if (logger.isTraceEnabled()) {
            logger.trace("loading adaptors from adaptor path: " + adaptorPath);
        }
        if (adaptorPath == null) {
            throw new Error("gat.adaptor.path not set!");
        }
        StringTokenizer st = new StringTokenizer(adaptorPath,
                File.pathSeparator);
        while (st.hasMoreTokens()) {
            String dir = st.nextToken();
            logger.debug("readJarFiles: dir = " + dir);

            File adaptorRoot = new File(dir);
            if (!adaptorRoot.isDirectory()) {
                logger.warn("Specified gat.adaptor.path entry " + dir
                        + " is not a directory");
                continue;
            }
       
            ClassLoader sharedLoader;
            try {
                sharedLoader = loadDirectory(new File(adaptorRoot, "shared"),
                        superparentLoader, false);
            } catch (Throwable e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Got exception when loading shared directory", e);
                }
                sharedLoader = superparentLoader;
            }
            parentLoader = sharedLoader;
            readJarFiles(adaptorRoot);
        }

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
    
    /**
     * This method tries to determine a suitable classloader to be used
     * as parent classloader for the URLClassloaders of the adaptors.
     * Sometimes, the classloader that loaded the GATEngine class is not
     * a good candidate because this probably is just the system classloader.
     * A better candidate might be the classloader of the class that prompted
     * the loading of javaGAT in the first place, or the context classloader.
     * 
     * @return the classloader to be used.
     */
    private ClassLoader getParentClassLoader() {
        // Find the Class instance of the class that prompted the loading
        // of JavaGAT.
        Class<?>[] callers = (new CallerResolver()).getClassContext();
        Class<?> callerClass = null;
        for (Class<?> c : callers) {
            String name = c.getCanonicalName();
            if (name != null && name.startsWith("org.gridlab.gat")) {
                continue;
            }
            callerClass = c;
            break;
        }
        // If we cannot find it, use the GATEngine class instance, for lack of
        // a better choice.
        if (callerClass == null) {
            callerClass = GATEngine.class;
        }
        // Now, there are basically two choices: the classloader that loaded the
        // caller class, or the context classloader. If there is a parent-relation,
        // choose the child.
        ClassLoader callerLoader = callerClass.getClassLoader();
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader result;
        
        if (isChild(contextLoader, callerLoader)) {
            result = callerLoader;
        } else if (isChild(callerLoader, contextLoader)) {
            result = contextLoader;
        } else {
            // Apparently there is no relation. The following may not be right,
            // but then, there is no "right".
            result = contextLoader;
        }
        
        // If the system classloader is a child of the result found so far,
        // use the system classloader instead.
        if (isChild (result, systemLoader)) {
            result = systemLoader;
        }
        
        return result;
    }
    
    /**
     * Determines if loader l2 is a child of loader l1.
     * @param l1
     * @param l2
     * @return true if l2 is a child of l1.
     */
    private static boolean isChild(ClassLoader l1, ClassLoader l2) {
        if (l1 == null) {
            // Primordial loader is parent of all classloaders.
            return true;
        }
        while (l2 != null) {
            if (l1 == l2) {
                return true;
            }
            l2 = l2.getParent();
        }
        return false;
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

    protected void readJarFiles(File adaptorRoot) {
        // now get the adaptor dirs from the adaptor path, adaptor dirs are
        // of course directories and further will end with "Adaptor"
        File[] adaptorDirs = adaptorRoot.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() && file.getName().endsWith("Adaptor");
            }
        });
        if (adaptorDirs.length == 0) {
            logger.warn("gat.adaptor.path contains '" + adaptorRoot
                    + "', but it doesn't contain any adaptor");
        }
        
        // Sort the list of directories, to make sure that the order is deterministic.
        Arrays.sort(adaptorDirs, new FileComparator());
        
        HashMap<String, ClassLoader> adaptorClassLoaders = new HashMap<String, ClassLoader>();
        for (File adaptorDir : adaptorDirs) {
            try {
                adaptorClassLoaders.put(adaptorDir.getName(), loadDirectory(
                        adaptorDir, parentLoader, true));
                if (logger.isDebugEnabled()) {
                    logger.debug("loading adaptor SUCCESS: " + adaptorDir);
                }
            } catch (Throwable e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("loading adaptor FAILED: " + adaptorDir + " ("
                            + e + ")");
                }
            }
        }
    }

    private ClassLoader loadDirectory(File adaptorDir, ClassLoader parentForDir, boolean mustContainAdaptorJar) throws Exception {
        List<URL> adaptorPathURLs = new ArrayList<URL>();
        final Attributes attributes;
        if (mustContainAdaptorJar) {
            File adaptorJarFile = new File(adaptorDir.getPath()
                    + File.separator + adaptorDir.getName() + ".jar");
            if (!adaptorJarFile.exists()) {
                throw new Exception("found adaptor dir '"
                        + adaptorDir.getPath()
                        + "' that doesn't contain an adaptor named '"
                        + adaptorJarFile.getPath() + "'");
            }
            JarFile adaptorJar = new JarFile(adaptorJarFile, true);
            attributes = adaptorJar.getManifest()
                    .getMainAttributes();
            adaptorJar.close();
            adaptorPathURLs.add(adaptorJarFile.toURI().toURL());
        } else {
            attributes = new Attributes();
        }
        String[] externalJars = adaptorDir.list(new java.io.FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".jar");
            }
        });
        Arrays.sort(externalJars);
        if (logger.isTraceEnabled()) {
            if (externalJars != null) {
                logger.trace(adaptorDir + " contains external jar files: ");
                for (String externalJar : externalJars)
                    logger.trace("\t" + externalJar);
            } else {
                logger
                        .trace(adaptorDir
                                + " doesn't contain external jar files");
            }
        }
        if (externalJars != null) {
            for (String externalJar : externalJars) {
                adaptorPathURLs.add(new File(adaptorDir, externalJar).toURI()
                        .toURL());
            }
        }
        URL[] urls = adaptorPathURLs.toArray(new URL[adaptorPathURLs.size()]); 
        if (logger.isTraceEnabled()) {
            logger
                    .trace("URLs used for the URL class loader constructed from: "
                            + adaptorDir);
            if (urls != null) {
                for (URL url : urls) {
                    logger.trace("\t" + url);
                }
            } else {
                logger.trace("\tno URLs");
            }
        }
        URLClassLoader adaptorLoader = new URLClassLoader(urls, parentForDir);
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
                if (logger.isTraceEnabled()) {
                    logger.trace("found " + adaptorClasses.length
                            + " adaptors implementing cpi: " + cpiName);
                }
                for (String adaptorClass : adaptorClasses) {
                    try {
                        if (logger.isTraceEnabled()) {
                            logger.trace("\tloading adaptor: " + adaptorClass);
                        }
                        Thread.currentThread().setContextClassLoader(
                                adaptorLoader);
                        Class<?> clazz = adaptorLoader.loadClass(adaptorClass);
                       
                        callInitializer(clazz);
                        
                        String[] schemes = callGetSupportedSchemes(clazz);
                        

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
                        adaptorLists.get(cpiName).add(
                                new Adaptor(cpiName, clazz, schemes));
                    } catch (Exception e) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("\t\tfailed loading adaptor: "
                                    + adaptorClass + " (" + e + ")");
                        }
                    }
                }
            }
        }

        return adaptorLoader;
    }

    // private Preferences attributesToPreferences(Attributes attributes) {
    // Preferences preferences = new Preferences();
    //
    // Iterator<Object> i = attributes.keySet().iterator();
    //
    // while (i.hasNext()) {
    // Object key = i.next();
    // Object value = attributes.get(key);
    // preferences.put(key.toString(), value.toString());
    // }
    // return preferences;
    // }

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
     * @return the advertisable
     * @throws GATInvocationException
     */
    public Advertisable unmarshalAdvertisable(GATContext gatContext,
            String input) throws GATInvocationException {
        if (input == null) {
            throw new NullPointerException("cannot unmarshal null String");
        }
        synchronized(this) {
            if (! unmarshallersAdded) {
                unmarshallersAdded = true;
                // Find out which adaptors actually have unmarshallers, if we have
                // not done this yet.
                for (List<Adaptor> adaptorList : adaptorLists.values()) {
                    for (Adaptor adaptor : adaptorList) {
                        Class<?> c = adaptor.adaptorClass;
                        if (containsUnmarshaller(c)) {
                            unmarshallers.add(c);
                        }
                    }
                }
            }            
        }

        for (int i = 0; i < unmarshallers.size(); i++) {
            Class<?> c = unmarshallers.get(i);

            try {
                Method m = c.getMethod("unmarshal", unmarshalParams);
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
                            + " failed:", e1.getTargetException());
                }
                // ignore and try next unmarshaller
            } catch (Throwable e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("unmarshaller for "
                            + c.getName()
                            + " failed:", e);
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
    
    private static Class<?>[] unmarshalParams = new Class[] { GATContext.class,
        String.class };

    private static boolean containsUnmarshaller(Class<?> clazz) {
        // test for marshal and unmarshal methods.
        try {
            clazz.getMethod("unmarshal", unmarshalParams);

            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static void callInitializer(Class<?> clazz) {
        Method m;
        try {
            m = clazz.getMethod("init", (Class[]) null);
        } catch(Throwable e) {
            return;
        }
        try {
            m.invoke((Object) null, (Object[]) null);
        } catch (Throwable t) {
            if (logger.isInfoEnabled()) {
                logger.info("initialization of " + clazz + " failed: " + t);
            }
        }
    }
    
    private static String[] callGetSupportedSchemes(Class<?> clazz) {
        Method m;
        try {
            m = clazz.getMethod("getSupportedSchemes", (Class[]) null);
        } catch(Throwable e) {
            return null;
        }
        try {
            String[] result = (String[]) m.invoke((Object) null, (Object[]) null);
            if (logger.isInfoEnabled()) {
                logger.info("Supported schemes of class " + clazz.getName() + ": " + Arrays.toString(result));
            }
            return result;
        } catch (Throwable t) {
            if (logger.isInfoEnabled()) {
                logger.info("getSupportedSchemes of " + clazz + " failed: " + t);
            }
            return null;
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
    
    public static Advertisable defaultUnmarshal(Class<?> type, String s, String expectedClass) {
        Advertisable a = defaultUnmarshal(type, s);
        if (! (a instanceof SerializedBase)) {
            throw new Error("could not unmarshal object: not a SerializedBase.");
        }
        if (!((SerializedBase)a).checkClassname(expectedClass)) {
            throw new Error("could not unmarshal object: found " + ((SerializedBase)a).getClassname()
                    + " instead of " + expectedClass);
        }
        return a;
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
        
        // Try and terminate jobs before ending adaptors. Changed order. --Ceriel
        try {
            JobCpi.end();
        } catch(Throwable e) {
            // ignore, could be because JobCpi has never been instantiated and
            // GAT.end() is called from within shutdown hook.
        }
        
        FileWaiter.end();

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
        
        ScheduledExecutor.end();

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
        
        int removed = 0;
        
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
                removed++;
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
                } else if (currentPosition < 0) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Found non existing adaptor in " + cpiName
                                + ".adaptor.name preference: " + names[i]);
                    }
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
        } else if (insertPosition == 0 && removed < names.length) {
            // nothing inserted, but it was not all removals ...
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
