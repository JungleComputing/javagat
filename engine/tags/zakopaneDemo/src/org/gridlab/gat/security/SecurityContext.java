package org.gridlab.gat.security;

import java.util.HashMap;

/**
 * A container for security Information. Each context has a data object
 * associated with it. The data object is opaque to the GAT API and is used and
 * manipulated by adaptors based upon their interpretation of the type.
 * <p>
 * Currently we provide additional auxiliary methods to create a context based
 * upon password information or upon credentials stored in a file. Contexts
 * based upon these mechanisms can be used by adaptors to create further
 * contexts containing opaque data objects, e.g. GSSAPI credentials.
 */
/**
 * @author rob
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
/**
 * @author rob
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public abstract class SecurityContext implements Cloneable {

	protected Object dataObject;

	protected HashMap notes = new HashMap();

	public abstract boolean equals(Object obj);

	public abstract int hashCode();

	/**
	 * Returns a clone of this context.
	 * 
	 * @return the clone of this security context
	 */
	public abstract Object clone() throws CloneNotSupportedException;

	// the three methods restrict the access to the HashMap because:
	// - nobody should be able to list all entries
	// - the adaptor developers should know what they are looking for
	// of course, we need some usage conventions,
	// i.e. the GAT API user must know the keys (and their semantics)
	// the adaptors will check

	/**
	 * Add notes to this context. Notes are used to give information to the
	 * adaptors. An example is addNote("host", "fs0.cs.vu.nl"), to indicate that
	 * this context is only valid for the machine above. Notes restrict and
	 * precise the usage of the authentication data. So an adaptor can verify in
	 * advance the usefulness of the authentication data for the host it want to
	 * access (e.g. for job submission).
	 * 
	 * the set of notes known so far is: <BR> 
	 * "host", "hostname" <BR>
	 * "adaptor", "adaptorName" <BR> 
	 * "port", "portnr" <BR>
	 * 
	 * @param key
	 *            the name of the note
	 * @param value
	 *            the value of the note
	 */
	public final void addNote(String key, String value) {
		notes.put(key, value);
	}

	/**
	 * @param key
	 *            the key to look for
	 * @return true if a note exists with the given key
	 */
	public final boolean containsNoteKey(String key) {
		return this.notes.containsKey(key);
	}

	/**
	 * @param key
	 *            the key to look for
	 * @return the note attached to this key
	 */
	public final String getNoteValue(String key) {
		return (String) this.notes.get(key);
	}
}