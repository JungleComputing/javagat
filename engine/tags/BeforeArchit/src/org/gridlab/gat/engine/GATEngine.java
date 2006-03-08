package org.gridlab.gat.engine;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.io.cpi.EndpointCpi;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
import org.gridlab.gat.io.cpi.LogicalFileCpi;
import org.gridlab.gat.io.cpi.RandomAccessFileCpi;
import org.gridlab.gat.resources.cpi.JobCpi;
import org.gridlab.gat.resources.cpi.JobDescriptionCpi;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 * @author rob
 */
/**
 * This class make the various GAT adaptors available to GAT
 */
public class GATEngine {

	protected static final boolean DEBUG = true;

	/**
	 * This member variable holds reference to the single GATEngine
	 */
	private static GATEngine gatEngine = null;

	/** Keys are cpiClass names, elements are AdaptorLists. */
	private Hashtable adaptors;

	/**
	 * Constructs a default GATEngine instance
	 */
	protected GATEngine() {
		adaptors = new Hashtable();
		readJarFiles();

		printAdaptorList();
	}

	protected void printAdaptorList() {
		System.err.println("------------LOADED ADAPTORS------------");
		Enumeration keys = adaptors.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();

			System.err.println("Adaptor type: " + key);
			AdaptorList l = (AdaptorList) adaptors.get(key);
			for (int i = 0; i < l.size(); i++) {
				System.err.println("    " + l.get(i));
			}
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
	 * @return The specified Cpi class or null if no such adaptor exists
	 */
	public Object getAdaptor(Class cpiClass, GATContext gatContext,
			Preferences preferences, Object[] tmpParams) {

		if (DEBUG) {
			System.err.println("getAdaptor: trying to get an adaptor for type "
					+ cpiClass.getName());
		}

		if (tmpParams == null) {
			tmpParams = new Object[0];
		}

		if (preferences == null) { // No preferences.
			preferences = new Preferences(new Hashtable());
		}

		// Add the context and the preferences as parameters
		Object[] parameters = new Object[tmpParams.length + 2];
		parameters[0] = gatContext;
		parameters[1] = preferences;
		for (int i = 0; i < tmpParams.length; i++) {
			parameters[i + 2] = tmpParams[i];
		}

		// Create an array with the parameter types
		Class[] parameterTypes = new Class[parameters.length];
		for (int count = 0; count < parameterTypes.length; count++) {
			parameterTypes[count] = parameters[count].getClass();
		}

		// Get an adaptor list for this type.
		AdaptorList l = (AdaptorList) adaptors.get(cpiClass.getName());
		if (l == null) { // no adaptors for this type loaded.
			if (DEBUG) {
				System.err.println("No adaptors loaded for type "
						+ cpiClass.getName());
			}
			return null;
		}

		for (int i = 0; i < l.size(); i++) {
			Adaptor a = l.get(i);
			if (a.satisfies(preferences)) {
				Object o = a.newInstance(parameterTypes, parameters);
				if (o != null) {
					if (DEBUG) {
						System.err.println("Created adaptor instance of type "
								+ a.getName());
					}
					return o;
				} else {
					if (DEBUG) {
						System.err
								.println("Could not create an instance of Adaptor "
										+ a.getName());
					}
				}
			}
		}

		if (DEBUG) {
			System.err
					.println("None of the loaded adaptors satisfies the requested user preferences.");
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
	 * @return The specified Cpi class or null if no such adaptor exists
	 */
	public Object[] getAdaptorList(Class cpiClass, GATContext gatContext,
			Preferences preferences, Object[] tmpParams)
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

		if (tmpParams == null) {
			tmpParams = new Object[0];
		}

		// Add the context and the preferences as parameters
		Object[] parameters = new Object[tmpParams.length + 2];
		parameters[0] = gatContext;
		parameters[1] = preferences;
		for (int i = 0; i < tmpParams.length; i++) {
			parameters[i + 2] = tmpParams[i];
		}

		// Create an array with the parameter types
		Class[] parameterTypes = new Class[parameters.length];
		for (int count = 0; count < parameterTypes.length; count++) {
			parameterTypes[count] = parameters[count].getClass();
		}

		// Get an adaptor list for this type.
		AdaptorList l = (AdaptorList) adaptors.get(cpiClass.getName());
		if (l == null) { // no adaptors for this type loaded.
			if (DEBUG) {
				System.err.println("No adaptors loaded for type "
						+ cpiClass.getName());
			}

			throw new AdaptorCreationException("No adaptors loaded for type "
					+ cpiClass.getName());
		}

		for (int i = 0; i < l.size(); i++) {
			Adaptor a = l.get(i);
			if (a.satisfies(preferences)) {
				Object o = a.newInstance(parameterTypes, parameters);
				if (o != null) {
					if (DEBUG) {
						System.err.println("Created adaptor instance of type "
								+ a.getName());
					}
					result.add(o);
				} else {
					if (DEBUG) {
						System.err
								.println("Could not create an instance of Adaptor "
										+ a.getName());
					}
				}
			}
		}

		if (result.size() == 0) {
			if (DEBUG) {
				System.err
						.println("None of the loaded adaptors satisfies the requested user preferences.");
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
	 */
	protected String getOptionalPkgDirectory() {
		return System.getProperty("java.home") + File.separator + "lib"
				+ File.separator + "ext";
	}

	/**
	 * Obtains File's in the optional directory.
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

						// If a GATAdaptor add to jarFiles
						if ("true".equals(attributes.getValue("GATAdaptor"))) {
							jarFiles.add(jarFile);
						}
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
		loadCpiClass(manifest, attributes, "Job", JobCpi.class);
		loadCpiClass(manifest, attributes, "JobDescription",
				JobDescriptionCpi.class);
		loadCpiClass(manifest, attributes, "ResourceBroker",
				ResourceBrokerCpi.class);
	}

	/**
	 * Populate cpiClasses
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
}