//@@@@ todo: .. in paths
//@@@@ TODO: query language

/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.advert.cpi;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.MetaData;

/**
 * @author rob
 */
public class DefaultAdvertServiceAdaptor extends AdvertServiceCpi {
	class Entry {
		Advertisable a;

		MetaData m;
	}

	static final String SEPERATOR = "/";

	String pwd = SEPERATOR;

	Hashtable hash = new Hashtable();

	/**
	 * @param gatContext
	 * @param preferences
	 */
	public DefaultAdvertServiceAdaptor(GATContext gatContext,
			Preferences preferences) {
		super(gatContext, preferences);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#add(org.gridlab.gat.advert.Advertisable,
	 *      java.util.Map, java.lang.String)
	 */
	public void add(Advertisable advert, MetaData metaData, String path) {
		if (!path.startsWith(SEPERATOR)) {
			path = pwd + SEPERATOR + path;
		}

		Entry e = new Entry();
		e.a = advert;
		e.m = metaData;

		hash.put(path, e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#delete(java.lang.String)
	 */
	public void delete(String path) throws NoSuchElementException {
		if (!path.startsWith(SEPERATOR)) {
			path = pwd + SEPERATOR + path;
		}

		hash.remove(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#find(java.util.Map)
	 */
	public String[] find(MetaData query) {
		Vector res = new Vector();

		Enumeration keys = hash.keys();
		Enumeration data = hash.elements();

		while (data.hasMoreElements()) {
			Entry e = (Entry) data.nextElement();
			String key = (String) keys.nextElement();
			MetaData m = e.m;

			if (m.match(query)) {
				res.add(key);
			}
		}

		String[] s = new String[res.size()];
		for (int i = 0; i < s.length; i++) {
			s[i] = (String) res.get(i);
		}

		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#getMetaData(java.lang.String)
	 */
	public MetaData getMetaData(String path) throws NoSuchElementException {
		if (!path.startsWith(SEPERATOR)) {
			path = pwd + SEPERATOR + path;
		}

		Entry e = (Entry) hash.get(path);
		return e.m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#getPWD()
	 */
	public String getPWD() {
		return pwd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#setPWD(java.lang.String)
	 */
	public void setPWD(String path) {
		pwd = path;
	}
}