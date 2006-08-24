/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.advert;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author rob
 */
/** This class describes meta data that can be attached to an Advertizable object.
 * MetaData consists of a number of key value tuples, where both the keys and the values are strings.
 */
public class MetaData implements Serializable {
    private Hashtable data = new Hashtable();

    public MetaData() {
        // do nothing
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
        return (String) ((Map.Entry) data.entrySet().toArray()[i]).getValue();
    }

    public int size() {
        return data.size();
    }

    /** Match two metadata objects. Used internally by the GAT. GAT users should not call this method.
     * @param query the meta data object to compare to.
     * @return true: the two objects match.
     */
    public boolean match(MetaData query) {
        Enumeration e = query.data.keys();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();

            String myData = get(key);

            if (myData == null) {
                //				System.err.println("match: key " + key + " not present");
                return false;
            }

            String queryData = query.get(key);

            if (!myData.matches(queryData)) {
                //				System.err.println("not equal: " + myData + " and " + queryData);
                return false;
            }
        }

        return true;
    }
}
