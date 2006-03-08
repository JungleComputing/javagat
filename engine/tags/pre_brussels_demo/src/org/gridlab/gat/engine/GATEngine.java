// @@@ TODO: marshal / unmarshal niet alleen eerste adaptor, maar allemaal! 
package org.gridlab.gat.engine;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.io.cpi.EndpointCpi;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
import org.gridlab.gat.io.cpi.LogicalFileCpi;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 * @author rob
 */
/**
 * This class make the various GAT adaptors available to GAT
 */
public class GATEngine {

	public static final boolean DEBUG = propertySet("gat.debug");
	public static final boolean VERBOSE = propertySet("gat.debug") || propertySet("gat.verbose");
	
	/**
	 * This member variable holds reference to the single GATEngine
	 */
	private static GATEngine gatEngine = null;

	/** Keys are cpiClass names, elements are AdaptorLists. */
	private Hashtable adaptors;

	/** elements are of type Class */
	private static Vector marshallers = new Vector();
	
	static boolean propertySet(String name) {
		Properties p = System.getProperties();
		if(p != null) {
			String s = p.getProperty(name);
			if(s != null) {
				if(!s.equals("false")) {
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

		if(VERBOSE) {
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
	 * @param gatContext the context
	 * @param preferences
	 *            The Preferences used to construct the Cpi class.
	 * @param parameters an array containing the parameters to pass to the adaptor 
	 * @return The specified Cpi class or null if no such adaptor exists
	 */
	public Object getAdaptor(Class cpiClass, GATContext gatContext,
			Preferences preferences, Object[] parameters) {

		if (DEBUG) {
			System.err.println("getAdaptor: trying to get an adaptor for type "
					+ cpiClass.getName());
		}

		if (parameters == null) {
			parameters = new Object[0];
		}

		if (preferences == null) { // No preferences.
			preferences = new Preferences(new Hashtable());
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
			if (DEBUG) {
				System.err.println("getAdaptor: No adaptors loaded for type "
						+ cpiClass.getName());
			}
			return null;
		}

		for (int i = 0; i < l.size(); i++) {
			Adaptor a = l.get(i);
			if (a.satisfies(preferences)) {
				Object o = a.newInstance(parameterTypes, newParameters);
				if (o != null) {
					if (DEBUG) {
						System.err.println("getAdaptor: Created adaptor instance of type "
								+ a.getName());
					}
					return o;
				} else {
					if (DEBUG) {
						System.err
								.println("getAdaptor: Could not create an instance of Adaptor "
										+ a.getName());
					}
				}
			}
		}

		if (DEBUG) {
			System.err
					.println("getAdaptor: None of the loaded adaptors satisfies the requested user preferences.");
		}

		return null;
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
	 * @param gatContext the context
	 * @throws AdaptorCreationException creation of the adaptor failed
	 * @return The specified Cpi class or null if no such adaptor exists
	 */
	public Object[] getAdaptorList(Class cpiClass, GATContext gatContext,
			Preferences preferences, Object[] parameters)
			throws AdaptorCreationException {

		ArrayList result = new ArrayList();

		if (DEBUG) {
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
			if (DEBUG) {
				System.err.println("getAdaptorList: No adaptors loaded for type "
						+ cpiClass.getName());
			}

			throw new AdaptorCreationException("getAdaptorList: No adaptors loaded for type "
					+ cpiClass.getName());
		}

		for (int i = 0; i < l.size(); i++) {
			Adaptor a = l.get(i);
			if (a.satisfies(preferences)) {
				Object o = a.newInstance(parameterTypes, newParameters);
				if (o != null) {
					if (DEBUG) {
						System.err.println("getAdaptorList: Created adaptor instance of type "
								+ a.getName());
					}
					result.add(o);
				} else {
					if (DEBUG) {
						System.err
								.println("getAdaptorList: Could not create an instance of Adaptor "
										+ a.getName());
					}
				}
			}
		}

		if (result.size() == 0) {
			if (DEBUG) {
				System.err
						.println("getAdaptorList: None of the loaded adaptors satisfies the requested user preferences.");
			}

			throw new AdaptorCreationException(
					"None of the loaded adaptors satisfies the requested user preferences.");
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
	 * @return the directory
	 */
	protected String getOptionalPkgDirectory() {
		return System.getProperty("java.home") + File.separator + "lib"
				+ File.separator + "ext";
	}

	/**
	 * Obtains File's in the optional directory.
	 * @param f a directory to list
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
	 * @param dir the directory to get the jar files from
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

		String attributeName = className + "Cpi-class";

		// Get info for the adaptor
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

		Preferences preferences = new Preferences(attributes);

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
	 * @param jarFiles the list of JarFile objects to load
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

	public Advertisable unmarshalAdvertisable(String input) throws GATInvocationException {
		if(input == null) {
			throw new NullPointerException("cannot unmarshal null String");
		}
		
		for(int i=0; i<marshallers.size(); i++) {
			Class c = (Class) marshallers.get(i);
			
			try {
				Method m = c.getMethod("unmarshal", new Class[] {String.class});
				Advertisable res = (Advertisable) m.invoke(null, new Object[] {input});
				if(res != null) {
					// success!
					return res;
				}
			} catch (Exception e) {
				throw new GATInvocationException("could not find or execute unmarshal method", e);
			}
		}

		throw new GATInvocationException("could not find suitable marshaller");
	}
	
	public String marshalAdvertisable(Advertisable advert) throws GATInvocationException {

		if(advert == null) {
			throw new NullPointerException("cannot marshal null Advertisable");
		}
		
		String res = advert.marshal();
		return res;
		
		/*
		for(int i=0; i<marshallers.size(); i++) {
			Class c = (Class) marshallers.get(i);
			
			try {
				Method m = c.getMethod("marshal", new Class[] {Advertisable.class});
				String res = (String) m.invoke(null, new Object[] {advert});
				if(res != null) {
					// success!
					return res;
				}
			} catch (Exception e) {
				throw new GATInvocationException("could not find or execute marshal method: " + e);
			}
		}

		throw new GATInvocationException("could not find suitable marshaller");
		*/
	}
	
	public static void registerAdvertisable(Class marshaller) {
		// test for marshal and unmarshall methods.
		try {
//			Method m = marshaller.getMethod("marshal", new Class[] {Advertisable.class});
			Method m2 = marshaller.getMethod("unmarshal", new Class[] {String.class});
		} catch (Exception e) {
			throw new Error("could not find marshal method: " + e);
		}

		marshallers.add(marshaller);
	}
}