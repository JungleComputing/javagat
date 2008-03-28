/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.advert;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class describes meta data that can be attached to an
 * {@link Advertisable} object. MetaData consists of a number of key value
 * tuples, where both the keys and the values are strings.
 * 
 * @author rob
 */
@SuppressWarnings("serial")
public class MetaData implements Serializable {
    private Hashtable<String, String> data = new Hashtable<String, String>();

    public MetaData() {
        // do nothing
    }

    /**
     * Put an entry in the {@link MetaData} object.
     * 
     * @param key
     *                the key that corresponds to the given value
     * @param value
     *                the value that corresponds to the given key
     */
    public void put(String key, String value) {
        data.put(key, value);
    }

    /**
     * Gets the value associated to the provided key.
     * 
     * @param key
     *                the key for which the associated value should be retrieved
     * @return the associated value
     */
    public String get(String key) {
        return data.get(key);
    }

    /**
     * Removes an entry specified by the provided key.
     * 
     * @param key
     *                the key for which the entry should be removed
     * @return the associated value of the provided key
     */
    public String remove(String key) {
        return (String) data.remove(key);
    }

    /**
     * Gets the i-th key of the MetaData.
     * 
     * @param i
     *                the position of the key
     * @return the key at position i
     */
    public String getKey(int i) {
        return (String) data.keySet().toArray()[i];
    }

    /**
     * Gets the value associated to the key retrieved by getKey(i).
     * 
     * @param i
     *                the position of the value
     * @return the value associated with the key at position i
     */
    @SuppressWarnings("unchecked")
    public String getData(int i) {
        return (String) ((Map.Entry<String, String>) (data.entrySet())
                .toArray()[i]).getValue();
    }

    /**
     * Returns the number of entries in the {@link MetaData}.
     * 
     * @return the number of entries in the MetaData
     */
    public int size() {
        return data.size();
    }

    /**
     * Match two {@link MetaData} objects. <b>Used internally by the GAT. GAT
     * users should not call this method.</b>
     * 
     * @param query
     *                the {@link MetaData} object to compare to.
     * @return <code>true</code>: the two objects match. <code>false</code>:
     *         otherwise.
     * 
     */
    public boolean match(MetaData query) {
        Enumeration<String> e = query.data.keys();

        while (e.hasMoreElements()) {
            String key = e.nextElement();

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
