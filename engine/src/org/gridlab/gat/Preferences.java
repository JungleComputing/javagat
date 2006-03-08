package org.gridlab.gat;

import java.util.Hashtable;
import java.util.Map;

/**
 * An instance of this class represents user preferences for selecting adaptors.
 */
public class Preferences extends Hashtable {
    /**
     * Constructs a new Preferences no mappings.
     */
    public Preferences() {
        super();
    }

    /**
     * Constructs a new Preferences with the same mappings as the given Map.
     *
     * @param map
     *            the map to copy the initial preferences from
     */
    public Preferences(Map map) {
        super(map);
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
    public Object put(Object key, Object value) {
        return super.put(key, value);
    }
}
