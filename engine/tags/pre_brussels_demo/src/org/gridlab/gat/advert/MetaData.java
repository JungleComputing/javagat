/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.advert;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author rob
 */
public class MetaData implements Serializable {

	private Hashtable data = new Hashtable();

	/**
	 *  
	 */
	public MetaData() {
	}

	public void put(String key, String value) {
		data.put(key, value);
	}

	public String get(String key) {
		return (String) data.get(key);
	}

	public String remove(String key) {
		return (String) data.remove(key);
	}

	public String getKey(int i) {
		return (String) data.keySet().toArray()[i];
	}

	public String getData(int i) {
		return (String) data.entrySet().toArray()[i];
	}

	public int size() {
		return data.size();
	}

	public boolean match(MetaData query) {
		Enumeration e = query.data.keys();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();

			String myData = get(key);
			if (myData == null) {
				return false;
			}

			String Querydata = query.get(key);
			if (!myData.equals(Querydata))
				return false;
		}

		return true;
	}
}