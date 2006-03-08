// @@@ TODO: marshal / unmarshal niet alleen eerste adaptor, maar allemaal!

package org.gridlab.gat.engine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.gridlab.gat.AdaptorCreationException;
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
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

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

	/** Keys are cpiClass names, elements are AdaptorLists. */
	private Hashtable adaptors;

	/** elements are of type Class */
	private static Vector marshallers = new Vector();

	/** elements are of type MetricListenerNode */
	private Vector metricListeners = new Vector();

	/** elements are of type MetricNode */
	private Vector metricTable = new Vector();

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
	 * Constructs a default GATEngine instance
	 */
	protected GATEngine() {
		adaptors = new Hashtable();
		readJarFiles();

		if (VERBOSE) {
			printAdaptorList();
		}
	}

	protected void printAdaptorList() {
		System.err.println("------------LOADED ADAPTORS------------");
		Enumeration keys = adaptors.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();

			System.err.println("Adaptor type: " + key + ":");
			AdaptorList l = (AdaptorList) adaptors.get(key);
			for (int i = 0; i < l.size(); i++) {
				System.err.println("    " + l.get(i));
			}
			System.err.println();
		}
		System.err.println("---------------------------------------");
	}

	/**
	 * Singleton method to construct a GATEngine
	 * 
	 * @return A GATEngine instance
	 */
	public static synchronized GATEngine getGATEngine() {
		if (gatEngine == null)
			gatEngine = new GATEngine();
		return gatEngine;
	}

	/**
	 * Returns an instance of the specified XXXCpi class consistent with the
	 * passed XXXCpi class name, preferences, and parameters
	 * 
	 * @param cpiClass
	 *            The Cpi Class for which to look.
	 * @param preferences
	 *            The Preferences used to construct the Cpi class.
	 * @param parameters
	 *            The Parameters for the Cpi Constructor null means no
	 *            parameters.
	 * @param gatContext
	 *            the context
	 * @throws GATObjectCreationException
	 *             creation of the adaptor failed
	 * @return The specified Cpi class or null if no such adaptor exists
	 */
	public Object[] getAdaptorList(Class cpiClass, GATContext gatContext,
			Preferences preferences, Object[] parameters)
			throws GATObjectCreationException {

		GATObjectCreationException exc = null;

		ArrayList result = new ArrayList();

		if (VERBOSE) {
			System.err
					.println("getAdaptorList: trying to get an adaptor for type "
							+ cpiClass.getName());
		}

		if (preferences == null) { // No preferences.
			preferences = new Preferences(new Hashtable());
		}

		if (parameters == null) {
			parameters = new Object[0];
		}

		// Add the context and the preferences as parameters
		Object[] newParameters = new Object[parameters.length + 2];
		newParameters[0] = gatContext;
		newParameters[1] = preferences;
		for (int i = 0; i < parameters.length; i++) {
			newParameters[i + 2] = parameters[i];
		}

		// Create an array with the parameter types
		Class[] parameterTypes = new Class[newParameters.length];
		for (int count = 0; count < parameterTypes.length; count++) {
			parameterTypes[count] = newParameters[count].getClass();
		}

		// Get an adaptor list for this type.
		AdaptorList l = (AdaptorList) adaptors.get(cpiClass.getName());
		if (l == null) { // no adaptors for this type loaded.
			if (VERBOSE) {
				System.err
						.println("getAdaptorList: No adaptors loaded for type "
								+ cpiClass.getName());
			}

			throw new GATObjectCreationException(
					"getAdaptorList: No adaptors loaded for type "
							+ cpiClass.getName());
		}

		/*
		 * for (int i = 0; i &lt; l.size(); i++) { Adaptor a = l.get(i); if
		 * (a.satisfies(preferences)) { Object o = null; try { o =
		 * a.newInstance(parameterTypes, newParameters); if (VERBOSE) {
		 * System.err .println(&quot;getAdaptorList: Created adaptor instance of
		 * type &quot; + a.getName()); } result.add(o); } catch (Throwable t) {
		 * if (exc == null) { exc = new GATObjectCreationException(); }
		 * exc.add(a.toString(), t); if (VERBOSE) { System.err
		 * .println(&quot;getAdaptorList: Could not create an instance of
		 * Adaptor &quot; + a.getName()); } } } else { // it does not satisfy
		 * prefs. if (exc == null) { exc = new GATObjectCreationException(); }
		 * exc.add(a.toString(), new GATInvocationException( &quot;adaptor does
		 * not satisfy preferences&quot;)); } }
		 */

		/////////////////////
		// the adaptors that can't fulfill the preferences
		ArrayList secondChanceAdaptors = new ArrayList();
		for (int i = 0; i < l.size(); i++) {
			Adaptor a = l.get(i);
			Object o = null;
			try {
				o = a.newInstance(parameterTypes, newParameters);
				if (VERBOSE) {
					System.err.println("    Created adaptor instance of type "
							+ a.getName());
				}
				if (a.satisfies(preferences)) {
					result.add(o);
				} else { // it does not satisfy prefs.
					if (exc == null) {
						exc = new GATObjectCreationException();
					}
					exc.add(a.toString(), new GATInvocationException(
							"adaptor does not satisfy preferences"));

					secondChanceAdaptors.add(o);
				}

			} catch (Throwable t) {
				if (exc == null) {
					exc = new GATObjectCreationException();
				}
				exc.add(a.toString(), t);
				if (VERBOSE) {
					System.err
							.println("    Could not create an instance of Adaptor "
									+ a.getName());
				}
			}
		}
		// the order of the adaptors is the important issue
		result.addAll(secondChanceAdaptors);
		/////////////////

		if (result.size() == 0) {
			if (VERBOSE) {
				System.err
						.println("getAdaptorList: No adaptor could be instantiated.");
			}

			if (exc != null) {
				throw exc;
			}

			throw new GATObjectCreationException(
					"None of the loaded adaptors satisfies the requested user preferences.");
		}

		if (VERBOSE) {
			System.err.println("getAdaptorList: instantiated " + result.size()
					+ " adaptors for type " + cpiClass.getName());
		}

		return result.toArray();
	}

	/**
	 * This method periodically populates the Map returned from a call to the
	 * method getCpiClasses().
	 */
	protected void readJarFiles() {
		// Obtain JarFiles in the optional directory that are GAT jars
		List jarFiles = getJarFiles(getOptionalPkgDirectory());

		String adaptorPath = System.getProperty("gat.adaptor.path");
		if (adaptorPath != null) {
			StringTokenizer st = new StringTokenizer(adaptorPath,
					File.pathSeparator);
			for (int i = 0; i < st.countTokens(); i++) {
				String dir = st.nextToken();
				List l = getJarFiles(dir);
				jarFiles.addAll(l);
			}
		}

		if (DEBUG) {
			System.err.println("List of GAT jar files is: ");
			printJars(jarFiles);
		}

		// Populate cpiClasses
		loadJarFiles(jarFiles);
	}

	protected void printJars(List l) {
		for (int i = 0; i < l.size(); i++) {
			JarFile f = (JarFile) l.get(i);
			System.err.println("    " + f.getName());
		}
	}

	/**
	 * Gets the optional packages directories
	 * 
	 * @return the directory
	 */
	protected String getOptionalPkgDirectory() {
		return System.getProperty("java.home") + File.separator + "lib"
				+ File.separator + "ext";
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
		Attributes attributes = null;

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
						attributes = manifest.getMainAttributes();
						jarFiles.add(jarFile);
					}
				} catch (IOException ioException) {
					// Ignore IOException
				}
			}
		}

		return jarFiles;
	}

	protected void loadCpiClass(Manifest manifest, Attributes attributes,
			String className, Class cpiClazz) {

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
		try {
			clazz = Class.forName(clazzString);
		} catch (ClassNotFoundException e) {
			if (DEBUG) {
				System.err.println("Could not load Adaptor for " + className
						+ ": " + e);
			}
			return;
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
		AdaptorList s = (AdaptorList) adaptors.get(cpiClazz.getName());
		if (s == null) {
			s = new AdaptorList(cpiClazz);
			adaptors.put(cpiClazz.getName(), s);
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

		loadCpiClass(manifest, attributes, "Endpoint", EndpointCpi.class);
		loadCpiClass(manifest, attributes, "AdvertService",
				AdvertServiceCpi.class);
		loadCpiClass(manifest, attributes, "File", FileCpi.class);
		loadCpiClass(manifest, attributes, "LogicalFile", LogicalFileCpi.class);
		loadCpiClass(manifest, attributes, "RandomAccessFile",
				RandomAccessFileCpi.class);
		loadCpiClass(manifest, attributes, "FileInputStream",
				FileInputStreamCpi.class);
		loadCpiClass(manifest, attributes, "FileOutputStream",
				FileOutputStreamCpi.class);
		loadCpiClass(manifest, attributes, "ResourceBroker",
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

	public Advertisable unmarshalAdvertisable(String input)
			throws GATInvocationException {
		if (input == null) {
			throw new NullPointerException("cannot unmarshal null String");
		}

		for (int i = 0; i < marshallers.size(); i++) {
			Class c = (Class) marshallers.get(i);

			try {
				Method m = c.getMethod("unmarshal",
						new Class[] { String.class });
				Advertisable res = (Advertisable) m.invoke(null,
						new Object[] { input });
				if (res != null) {
					// success!
					return res;
				}
			} catch (Exception e) {
				throw new GATInvocationException(
						"could not find or execute unmarshal method", e);
			}
		}

		throw new GATInvocationException("could not find suitable marshaller");
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

	public static void registerAdvertisable(Class marshaller) {
		// test for marshal and unmarshall methods.
		try {
			//			Method m = marshaller.getMethod("marshal", new Class[]
			// {Advertisable.class});
			Method m2 = marshaller.getMethod("unmarshal",
					new Class[] { String.class });
		} catch (Exception e) {
			throw new Error("could not find marshal method: " + e);
		}

		marshallers.add(marshaller);
	}

	public static void addMetricListener(Object adaptor,
			MetricListener metricListener, Metric metric)
			throws GATInvocationException {
		GATEngine e = getGATEngine();

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

		e.metricListeners.add(new MetricListenerNode(adaptor, metricListener,
				metric));
	}

	public static void removeMetricListener(Object adaptor,
			MetricListener metricListener, Metric metric)
			throws NoSuchElementException {
		GATEngine e = getGATEngine();
		if (!e.metricListeners.remove(new MetricListenerNode(adaptor,
				metricListener, metric)))
			throw new NoSuchElementException();
	}

	public static void registerMetric(Object adaptor, String methodName,
			MetricDefinition definition) {
		GATEngine e = getGATEngine();
		e.metricTable.add(new MetricNode(adaptor, methodName, definition));
	}

	public static List getMetricDefinitions(Object adaptor) {
		GATEngine e = getGATEngine();
		Vector res = new Vector();

		for (int i = 0; i < e.metricTable.size(); i++) {
			MetricNode n = (MetricNode) e.metricTable.get(i);

			if (n.adaptor == adaptor)
				res.add(n.definition);
		}

		return res;
	}

	public static MetricDefinition getMetricDefinitionByName(Object adaptor,
			String name) throws GATInvocationException {
		GATEngine e = getGATEngine();

		for (int i = 0; i < e.metricTable.size(); i++) {
			MetricNode n = (MetricNode) e.metricTable.get(i);

			if (n.adaptor == adaptor
					&& name.equals(n.definition.getMetricName()))
				return n.definition;
		}

		throw new GATInvocationException("the metric name is incorrect");
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
						n.metricListener.ProcessMetricEvent(v);
					} catch (Throwable t) {
						System.err
								.println("WARNING, user callback threw exception: "
										+ e);
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

	public static void checkName(Preferences preferences, String adaptorType,
			String adaptorName) throws AdaptorCreationException {
		String local = (String) preferences.get("adaptors.local");
		if (local != null && local.equals("true")
				&& !adaptorName.equals("local")) {
			throw new AdaptorCreationException(
					"this adaptor ("
							+ adaptorName
							+ ") was not selected by the user.\n"
							+ "Only local adaptors will be used (property adaptors.local was set to true)");
		}

		String name = (String) preferences.get(adaptorType + ".adaptor.name");
		if (name != null && !name.equals(adaptorName)) {
			throw new AdaptorCreationException("this adaptor (" + adaptorName
					+ ") was not selected by the user.");
		}
	}
}