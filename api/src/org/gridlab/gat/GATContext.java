package org.gridlab.gat;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.naming.ContextNotEmptyException;

import org.gridlab.gat.security.SecurityContext;

/**
 * An instance of this class is the primary GAT state object.
 */
public class GATContext implements Cloneable, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This member variable holds the preferences for this GATContext
	 */
	private Preferences preferences = new Preferences();

	/**
	 * This member variable holds the SecurityContext's for this GATcontext
	 */
	private Vector<SecurityContext> securityContexts = new Vector<SecurityContext>();

	/**
	 * This no arguments constructor creates an instance of a GATContext.
	 */
	public GATContext() {
		super();
	}

	/**
	 * Adds the passed {@link SecurityContext}.
	 * 
	 * @param securityContext
	 *            A {@link SecurityContext}.
	 */
	public void addSecurityContext(SecurityContext securityContext) {
		securityContexts.add(securityContext);
	}

	/**
	 * Removes the passed {@link SecurityContext}.
	 * 
	 * @param securityContext
	 *            A {@link SecurityContext}.
	 */
	public void removeSecurityContext(SecurityContext securityContext) {
		securityContexts.remove(securityContext);
	}

	/**
	 * Gets the list of {@link SecurityContext}s associated with this
	 * {@link GATContext}.
	 * 
	 * @return {@link java.util.List} of {@link SecurityContext}s.
	 */
	public List<SecurityContext> getSecurityContexts() {
		return securityContexts;
	}

	/**
	 * Gets a list of {@link SecurityContext}s of the specified type associated
	 * with this {@link GATContext}.
	 * 
	 * @param type
	 *            A {@link SecurityContext} type, a {@link java.lang.String},
	 *            e.g., org.gridlab.gat.security.PasswordSecurityContext;
	 * @return {@link java.util.List} of {@link SecurityContext}s.
	 */
	public List<SecurityContext> getSecurityContextsByType(String type) {
		SecurityContext nextSecurityContext;
		Vector<SecurityContext> typedSecurityContexts = new Vector<SecurityContext>();

		Enumeration<SecurityContext> enumeration = securityContexts.elements();

		while (enumeration.hasMoreElements()) {
			nextSecurityContext = (SecurityContext) enumeration.nextElement();

			if (type.equals(nextSecurityContext.getClass().getName())) {
				typedSecurityContexts.add(nextSecurityContext);
			}
		}

		return typedSecurityContexts;
	}

	/**
	 * Adds a {@link Preferences} object to the {@link GATContext} which will be
	 * used to choose between adaptors if the constructor of an object is not
	 * called with a {@link Preferences} object. Only one such object may be
	 * associated with the {@link GATContext} at any one time.
	 * 
	 * @param newPreferences
	 *            A {@link Preferences} object.
	 */
	public void addPreferences(Preferences newPreferences) {
		if (newPreferences != null) {
			preferences.putAll(newPreferences);
		}
	}

	/**
	 * Adds a single preference to the {@link GATContext} which will be used to
	 * choose between adaptors if the constructor of an object is not called
	 * with a {@link Preferences} object.
	 * 
	 * @param key
	 *            The key of the single preference
	 * @param value
	 *            The value that belongs to the key of the single preference
	 */
	public void addPreference(String key, Object value) {
		preferences.put(key, value);
	}

	/**
	 * Removes the {@link Preferences} object associated with the
	 * {@link GATContext}.
	 */
	public void removePreferences() {
		preferences = null;
	}

	/**
	 * Returns the {@link Preferences} object associated with the
	 * {@link GATContext}.
	 * 
	 * @return the {@link Preferences} object
	 */
	public Preferences getPreferences() {
		return (Preferences) preferences.clone();
	}

	/**
	 * Returns a clone of this {@link GATContext}.
	 * 
	 * @return the new clone
	 */
	public Object clone() {
		//super.clone();

		GATContext c = new GATContext();
		c.preferences = (Preferences) preferences.clone();
		c.securityContexts = new Vector<SecurityContext>(securityContexts);

		return c;
	}

	/**
	 * Deserialize this {@link GATContext}, by reading the {@link Preferences}
	 * 
	 * @param stream
	 *            the stream to write to
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		preferences = (Preferences) stream.readObject();
		securityContexts = new Vector<SecurityContext>();
	}

	/**
	 * Serialize this {@link ContextNotEmptyException}, by just writing only
	 * the {@link Preferences}.
	 * 
	 * Don't write the {@link SecurityContext}s because of security issues.
	 * 
	 * @param stream
	 *            the stream to write to
	 * @throws IOException
	 */
	private void writeObject(java.io.ObjectOutputStream stream)
			throws IOException {
		stream.writeObject(preferences);
	}
}
