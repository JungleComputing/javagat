package org.gridlab.gat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

/**
 * An instance of this class represents user preferences for selecting adaptors.
 * For an overview of the known preferences used by JavaGAT and known adaptors
 * see: <a href="../../../../preferences.html">known preferences</a>.
 */
public class Preferences implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    Hashtable<String, Object> t = new Hashtable<String, Object>();

    /**
     * Constructs a new Preferences with no mappings.
     */
    public Preferences() {
    }

    /**
     * Copy constructor.
     * 
     * @throws NullPointerException
     *                 if <code>p</code> is <code>null</code>, or contains
     *                 <code>null</code> keys or values
     */
    public Preferences(Preferences p) {
        putAll(p);
    }

    /**
     * Constructs a new {@link Preferences} object out of a Java
     * <code>propertyFile</code>. All the key value pairs in the
     * <code>propertyFile</code> will be added to the new Preferences object.
     * 
     * @param propertyFile
     *                the property file containing the preferences.
     */
    public Preferences(String propertyFile) {
        Properties properties = new Properties();
        if (propertyFile != null) {
            java.io.InputStream inputStream = null;
            try {
                inputStream = new java.io.FileInputStream(propertyFile);
                properties.load(inputStream);
                putAll(properties);
            } catch (FileNotFoundException e) {
                System.err.println("User specified preferences \""
                        + propertyFile + "\" not found!");
            } catch (IOException e) {
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e1) {
                }
            }
        }
    }

    /**
     * Constructs a new {@link Preferences} object out of a {@link Properties}
     * object. All the key value pairs in the {@link Properties} object will be
     * added to the new Preferences object.
     * 
     * @param properties
     *                the {@link Properties} object containing the preferences.
     */
    public Preferences(Properties properties) {
        putAll(properties);
    }

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation). If the map previously contained a mapping for this
     * key, the old value is replaced.
     * 
     * @param key
     *                key with which the specified value is to be associated.
     * @param value
     *                value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key. A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key, if the implementation supports
     *         <tt>null</tt> values.
     * 
     * @throws UnsupportedOperationException
     *                 if the <tt>put</tt> operation is not supported by this
     *                 map.
     * @throws ClassCastException
     *                 if the class of the specified key or value prevents it
     *                 from being stored in this map.
     * @throws IllegalArgumentException
     *                 if some aspect of this key or value prevents it from
     *                 being stored in this map.
     * @throws NullPointerException
     *                 this map does not permit <tt>null</tt> keys or values,
     *                 and the specified key or value is <tt>null</tt>.
     */
    public Object put(String key, Object value) {
        return t.put(key.toLowerCase(), value);
    }

    /**
     * Returns the value to which the specified key is mapped in the
     * preferences.
     * 
     * @param key
     *                a key in the preferences
     * @return the value to which the key is mapped in the preferences;
     *         <tt>null</tt> if the key is not mapped to any value
     * @throws NullPointerException
     *                 if the key or value is <tt>null</tt>.
     */
    public Object get(String key) {
        return t.get(key.toLowerCase());
    }

    /**
     * Returns the value to which the specified key is mapped in the
     * preferences.
     * 
     * @param key
     *                a key in the preferences
     * @return the value to which the key is mapped in the preferences;
     *         <tt>null</tt> if the key is not mapped to any value
     * @throws NullPointerException
     *                 if the key or value is <tt>null</tt>.
     */
    public Object get(String key, Object defaultValue) {
        if (t.get(key.toLowerCase()) != null) {
            return t.get(key.toLowerCase());
        } else {
            return defaultValue;
        }
    }

    /**
     * Copies all of the mappings from the specified Preferences to this
     * Preferences. These mappings will replace any mappings that this
     * Preferences had for any of the keys currently in the specified
     * Preferences.
     * 
     * @param p
     *                Preferences to be stored in this map.
     * @throws NullPointerException
     *                 if <code>p</code> is <code>null</code>, or contains
     *                 <code>null</code> keys or values
     */
    public void putAll(Preferences p) {
        t.putAll(p.t);
    }

    /**
     * Copies all of the mappings from the specified {@link Properties} to this
     * Preferences. These mappings will replace any mappings that this
     * Preferences had for any of the keys currently in the specified
     * Preferences.
     * 
     * @param properties
     *                {@link Properties} to be stored in this map.
     * @throws NullPointerException
     *                 if <code>properties</code> is <code>null</code>, or
     *                 contains <code>null</code> keys or values
     */
    public void putAll(Properties properties) {
        Set<Object> keys = properties.keySet();
        for (Object key : keys) {
            t.put((String) key, properties.get(key));
        }
    }

    /**
     * Returns a shallow clone of this Preferences. The Hashtable itself is
     * cloned, but its contents are not. This is O(n).
     * 
     * @return the clone
     */
    public Object clone() {
        Preferences res = new Preferences();
        res.t = new Hashtable<String, Object>(t);
        return res;
    }

    /**
     * Removes from the preferences and returns the value which is mapped by the
     * supplied key. If the key maps to nothing, then the table remains
     * unchanged, and <code>null</code> is returned.
     * 
     * @param key
     *                the key used to locate the value to remove
     * @return whatever the key mapped to, if present
     */
    public Object remove(String key) {
        return t.remove(key.toLowerCase());
    }

    /**
     * Returns true if the supplied object <code>equals()</code> a key in this
     * Preferences.
     * 
     * @param key
     *                the key to search for in this Preferences
     * @return true if the key is in the table
     * @throws NullPointerException
     *                 if key is null
     */
    public boolean containsKey(String key) {
        return t.containsKey(key.toLowerCase());
    }

    /**
     * Returns a "set view" of this Preferences' keys. The set is backed by the
     * hashtable, so changes in one show up in the other. The set supports
     * element removal, but not element addition. The set is properly
     * synchronized on the original hashtable.
     * 
     * @return a set view of the keys
     */
    public Set<String> keySet() {
        return t.keySet();
    }

    /**
     * Returns true if this Preferences contains a value <code>o</code>, such
     * that <code>o.equals(value)</code>. This is the new API for the old
     * <code>contains()</code>.
     * 
     * @param value
     *                the value to search for in this Preferences
     * @return true if at least one key maps to the value
     * @throws NullPointerException
     *                 if <code>value</code> is null
     */
    public boolean containsValue(Object value) {
        return t.containsValue(value);
    }

    /**
     * Converts this Preferences to a String, surrounded by braces, and with
     * key/value pairs listed with an equals sign between, separated by a comma
     * and space. For example, <code>"{a=1, b=2}"</code>.
     * <p>
     * 
     * NOTE: if the <code>toString()</code> method of any key or value throws
     * an exception, this will fail for the same reason.
     * 
     * @return the string representation
     */
    public String toString() {
        return t.toString();
    }
}
