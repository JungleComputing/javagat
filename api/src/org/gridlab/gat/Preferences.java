package org.gridlab.gat;

import java.util.Hashtable;
import java.util.Set;

/**
 * An instance of this class represents user preferences for selecting adaptors.
 * For an overview of the generic preferences used by JavaGAT and well known adaptors
 * see: <a href="../../../../preferences.html">generic preferences</a>.
 */
public class Preferences {
    
    Hashtable t = new Hashtable();
    
    /**
     * Constructs a new Preferences with no mappings.
     */
    public Preferences() {
    }

    public Preferences(Preferences p) {
        putAll(p);
    }

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation). If the map previously contained a mapping for this
     * key, the old value is replaced.
     *
     * @param key
     *            key with which the specified value is to be associated.
     * @param value
     *            value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key. A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key, if the implementation supports
     *         <tt>null</tt> values.
     *
     * @throws UnsupportedOperationException
     *             if the <tt>put</tt> operation is not supported by this map.
     * @throws ClassCastException
     *             if the class of the specified key or value prevents it from
     *             being stored in this map.
     * @throws IllegalArgumentException
     *             if some aspect of this key or value prevents it from being
     *             stored in this map.
     * @throws NullPointerException
     *             this map does not permit <tt>null</tt> keys or values, and
     *             the specified key or value is <tt>null</tt>.
     */
    public Object put(String key, Object value) {
        return t.put(key.toLowerCase(), value);
    }

    public Object get(String key) {
        return t.get(key.toLowerCase());
    }
    
    public void putAll(Preferences p) {
        t.putAll(p.t);
    }
    
    public Object clone() {
        Preferences res = new Preferences();
        res.t = (Hashtable) t.clone();
        return res;
    }
    
    public Object remove(String key) {
        return t.remove(key.toLowerCase());
    }
    
    public boolean containsKey(String key) {
        return t.containsKey(key.toLowerCase());
    }
    
    public Set keySet() {
        return t.keySet();
    }
    
    public boolean containsValue(Object value) {
        return t.containsValue(value);
    }
    
    public String toString() {
        return t.toString();
    }
}
