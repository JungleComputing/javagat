/*
 * Created on May 11, 2004
 */
package org.gridlab.gat.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.gridlab.gat.Preferences;

/**
 * @author rob
 */
class Adaptor {

	String cpi;

	/** The fully qualified class name of the api we implement. */
	Class cpiClass;

	/** The class of the api this adaptor implements. */

	String adaptorName;

	/** The fully qualified class name of this adaptor. */
	Class adaptorClass;

	/** The actual class of this adaptor, must be a subclass of cpiClass. */
	Preferences preferences;

	/** Preferences associated with this adaptor. */

	/**
	 * @param cpiClass
	 *            The class of the api this adaptor implements.
	 * @param adaptorClass
	 *            The actual class of this adaptor, must be a subclass of
	 *            cpiClass.
	 * @param preferences
	 *            Preferences associated with this adaptor.
	 */
	public Adaptor(Class cpiClass, Class adaptorClass, Preferences preferences) {
		this.cpi = cpiClass.getName();
		this.cpiClass = cpiClass;
		this.adaptorName = adaptorClass.getName();
		this.adaptorClass = adaptorClass;
		this.preferences = preferences;
	}

	boolean satisfies(Preferences p) {
		// all requestes keys of p should be in my prefs,
		// and also have the same value.
		//@@@ todo

		return true;
	}

	Object newInstance(Class[] parameterTypes, Object[] parameters) {
		try {
			Constructor ctor = adaptorClass.getConstructor(parameterTypes);
			return ctor.newInstance(parameters);
		} catch (InvocationTargetException e) {
			if (GATEngine.DEBUG) {
				System.err.println("Adaptor constructor threw exception: "
						+ e.getTargetException());
				e.printStackTrace();
			}
			return null;
		} catch (Exception e) {
			if (GATEngine.DEBUG) {
				System.err
						.println("Could not construct adaptor object instance: "
								+ e + ": " + e.getMessage());
				e.printStackTrace();
			}
			return null;
		}
	}

	String getCpi() {
		return cpi;
	}
	
	Class getCpiClass() {
		return cpiClass;
	}

	String getName() {
		return adaptorName;
	}

	public String toString() {
		return getName();
	}
}