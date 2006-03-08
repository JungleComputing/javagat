package org.gridlab.gat;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.gridlab.gat.security.SecurityContext;

/**
 * An instance of this class is the primary GAT state object.
 */
public class GATContext implements Cloneable {

	/**
	 * This member variable holds the preferences for this GATContext
	 */
	private Preferences preferences = null;

	/**
	 * This member variable holds the SecurityContext's for this GATcontext
	 */
	private Vector securityContexts = null;

	/**
	 * This no arguments constructor creates an instance of a GATContext.
	 */
	public GATContext() {
		super();

		securityContexts = new Vector();
	}

	/**
	 * Adds the passed SecurityContext.
	 * 
	 * @param securityContext
	 *            A SecurityContext.
	 */
	public void addSecurityContext(SecurityContext securityContext) {
		securityContexts.add(securityContext);
	}

	/**
	 * Removes the passed SecurityContext.
	 * 
	 * @param securityContext
	 *            A SecurityContext.
	 */
	public void removeSecurityContext(SecurityContext securityContext) {
		securityContexts.remove(securityContext);
	}

	/**
	 * Gets the list of SecurityContexts associated with this GATContext.
	 * 
	 * @return java.util.List of SecurityContexts.
	 */
	public List getSecurityContexts() {
		return securityContexts;
	}

	/**
	 * Gets a list of SecurityContexts of the specified type associated with
	 * this GATContext.
	 * 
	 * @param type
	 *            A SecurityContext type, a java.lang.String
	 * @return java.util.List of SecurityContexts.
	 */
	public List getSecurityContextsByType(String type) {
		SecurityContext nextSecurityContext;
		Vector typedSecurityContexts = new Vector();

		Enumeration enumeration = securityContexts.elements();
		while (enumeration.hasMoreElements()) {
			nextSecurityContext = (SecurityContext) enumeration.nextElement();

			if (type.equals(nextSecurityContext.getType())) {
				typedSecurityContexts.add(nextSecurityContext);
			}
		}

		return typedSecurityContexts;
	}

	/**
	 * Adds a Preferences object to the GATContext which will be used to choose
	 * between adaptors if the constructor of an object is not called with a
	 * Preferences object. Only one such object may be associated with the
	 * GATContext at any one time.
	 * 
	 * @param preferences
	 *            A Preferences object.
	 */
	public void addPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	/**
	 * Removes the Preferences object associated with the GATContext.
	 */
	public void removePreferences() {
		preferences = null;
	}

	/**
	 * Returns the Preferences object associated with the GATContext.
	 * @return the Preferences object
	 */
	public Preferences getPreferences() {
		return preferences;
	}

	/**
	 * Returns a clone of this context.
	 * @return the new clone
	 */
	public Object clone() throws CloneNotSupportedException {
		super.clone();
		GATContext c = new GATContext();
		c.preferences = (Preferences) preferences.clone();
		c.securityContexts = (Vector) securityContexts.clone();
		return c;
	}
}