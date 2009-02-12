/*
 * Created 02 Feb. 2009 by Bas Boterman.
 */

package ibis.advert;

/**
 * This class describes meta data that can be attached to any instance stored in
 * the App Engine. MetaData consists of a number of key-value pairs, which both
 * are of type {@link String}.
 * 
 * @author bbn230
 */

import java.util.HashMap;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

@SuppressWarnings("serial")
public class MetaData {
	private HashMap<String, String> hMap = new HashMap<String, String>();

	/**
	 * Constructor.
	 */
	public MetaData() {
	}

	/**
	 * Adds a key-value pair to the {@link MetaData} object.
	 * 
	 * @param key
	 *            The key that corresponds to a given value.
	 * @param value
	 *            The value that corresponds to a given key.
	 */
	public void put(String key, String value) {
		hMap.put(key, value);
	}

	/**
	 * Fetches a value from the {@link MetaData} object, given a certain key.
	 * 
	 * @param key
	 *            The key for which the associated value should be fetched.
	 * @return A {@link String} containing the associated value of the given
	 *         key.
	 */
	public String get(String key) {
		return (String) hMap.get(key);
	}

	/**
	 * Removes a key-value pair from the {@link MetaData} object.
	 * 
	 * @param key
	 *            The key for which the key-value pair should be removed.
	 * @return The associated value of the key given.
	 */
	public String remove(String key) {
		return (String) hMap.remove(key);
	}

	/**
	 * Gets a {@link Set} of all keys stored in the {@link MetaData} object.
	 * 
	 * @return A {@link Set} of all keys stored in the {@link MetaData} object.
	 */
	public Set<String> getAllKeys() {
		return hMap.keySet();
	}

	/**
	 * Gets a {@link Collection} of all values stored in the {@link MetaData}
	 * object.
	 * 
	 * @return A {@link Collection} of all keys stored in the {@link MetaData}
	 *         object.
	 */
	public Collection<String> getAllValues() {
		return hMap.values();
	}

	/**
	 * Returns the number of entries in the {@link MetaData} object.
	 * 
	 * @return The number of entries in the {@link MetaData} object.
	 */
	public int size() {
		return hMap.size();
	}

	/**
	 * Matches two {@link MetaData} objects by a given query.<b>Used internally
	 * by the GAT. GAT users should not call this method.</b>
	 * 
	 * @param query
	 *            A {@link MetaData} object that should be matched with with the
	 *            current object.
	 * @return Returns <code>True</code> if the two objects match; <code>
	 *     False</code>
	 *         otherwise.
	 */
	public boolean match(MetaData query) {
		Iterator<String> itr = query.getAllKeys().iterator();

		while (itr.hasNext()) {
			String key = itr.next();
			String myData = get(key);

			if (myData == null) {
				return false;
			}

			String queryData = query.get(key);

			if (!myData.matches(queryData)) {
				return false;
			}
		}

		return true;
	}
}
