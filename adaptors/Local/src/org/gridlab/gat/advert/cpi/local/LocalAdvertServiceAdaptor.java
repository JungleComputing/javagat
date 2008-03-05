/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.advert.cpi.local;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.engine.GATEngine;

/**
 * @author rob
 */
public class LocalAdvertServiceAdaptor extends AdvertServiceCpi {
    static final String SEPERATOR = "/";

    String pwd = SEPERATOR;

    Hashtable<String, Entry> hash = new Hashtable<String, Entry>();

    File f;

    /**
     * @param gatContext
     * @param preferences
     */
    public LocalAdvertServiceAdaptor(GATContext gatContext,
        Preferences preferences) throws GATObjectCreationException {
        super(gatContext, preferences);

        String home = System.getProperty("user.home");

        if (home == null) {
            throw new GATObjectCreationException("could not get user home dir");
        }

        String filename = home + File.separator + ".gatAdvertDB";
        f = new File(filename);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#getAdvertisable(java.lang.String).
     */
    public Advertisable getAdvertisable(String path)
        throws GATInvocationException, NoSuchElementException {
        path = normalizePath(path);
        load();

        Entry e = (Entry) hash.get(path);

        if (e == null) {
            throw new NoSuchElementException("No such element: " + path);
        }
        
        if(e.a == null) {
            throw new NoSuchElementException("No such element: " + path);
        }

        Advertisable advert = GATEngine.getGATEngine().unmarshalAdvertisable(
            gatContext, preferences, e.a);

        return advert;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#add(org.gridlab.gat.advert.Advertisable,
     *      java.util.Map, java.lang.String)
     */
    public void add(Advertisable advert, MetaData metaData, String path)
        throws GATInvocationException {
        path = normalizePath(path);

        try {
            String advertString = null;
            if(advert != null) {
                advertString = advert.marshal();
                if(advertString == null) {
                    throw new GATInvocationException("could not marshal object");
                }
            }
            Entry e = new Entry();
            e.a = advertString;
            e.m = metaData;

            load();
            hash.put(path, e);
            save();
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace();

            if (e.getCause() != null) {
                System.err.println("CAUSE: " + e.getCause());
                e.getCause().printStackTrace();
            }

            throw new GATInvocationException("local advert", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#delete(java.lang.String)
     */
    public void delete(String path) throws NoSuchElementException,
        GATInvocationException {
        path = normalizePath(path);

        load();
        hash.remove(path);
        save();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#find(java.util.Map)
     */
    public String[] find(MetaData query) throws GATInvocationException {
        load();

        Vector<String> res = new Vector<String>();

        Enumeration<String> keys = hash.keys();
        Enumeration<Entry> data = hash.elements();

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
    public MetaData getMetaData(String path) throws NoSuchElementException,
        GATInvocationException {
        path = normalizePath(path);

        load();

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

    private synchronized void save() throws GATInvocationException {
        ObjectOutputStream out = null;
        FileLock lock = null;

        try {
            FileOutputStream fout = new FileOutputStream(f);
            FileChannel fc = fout.getChannel();
            lock = fc.lock();

            BufferedOutputStream bout = new BufferedOutputStream(fout);
            out = new ObjectOutputStream(bout);

            out.writeObject(hash);
        } catch (Exception e) {
            throw new GATInvocationException("local advert", e);
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (Exception e) {
                    // Ignore.
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    // Ignore.
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
	private synchronized void load() throws GATInvocationException {
        if (!f.exists()) {
            return;
        }

        ObjectInputStream in = null;
        FileLock lock = null;

        try {
            RandomAccessFile rf = new RandomAccessFile(f, "rw");
            FileChannel fc = rf.getChannel();
            lock = fc.lock();

            FileInputStream fin = new FileInputStream(rf.getFD());
            BufferedInputStream bin = new BufferedInputStream(fin);
            in = new ObjectInputStream(bin);

            hash = (Hashtable<String, Entry>) in.readObject();
        } catch (Exception e) {
            throw new GATInvocationException("local advert", e);
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (Exception e) {
                    // Ignore.
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // Ignore.
                }
            }
        }
    }

    private String normalizePath(String path) throws GATInvocationException {
        try {
            if (!path.startsWith(SEPERATOR)) {
                path = pwd + SEPERATOR + path;
            }

            URI u = new URI(path);

            return u.normalize().getPath();
        } catch (Exception e) {
            throw new GATInvocationException("local advert", e);
        }
    }

    @SuppressWarnings("serial")
	static class Entry implements Serializable {
        String a;

        MetaData m;
    }
}
