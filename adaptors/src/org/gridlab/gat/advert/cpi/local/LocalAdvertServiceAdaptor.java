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

    Hashtable hash = new Hashtable();

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
            return null;
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
            String advertString = advert.marshal();
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

    private void save() throws GATInvocationException {
        ObjectOutputStream out = null;

        try {
            FileOutputStream fout = new FileOutputStream(f);
            FileChannel fc = fout.getChannel();
            fc.lock();

            BufferedOutputStream bout = new BufferedOutputStream(fout);
            out = new ObjectOutputStream(bout);

            out.writeObject(hash);
        } catch (Exception e) {
            throw new GATInvocationException("local advert", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

    private void load() throws GATInvocationException {
        if (!f.exists()) {
            return;
        }

        ObjectInputStream in = null;

        try {
            RandomAccessFile rf = new RandomAccessFile(f, "rw");
            FileChannel fc = rf.getChannel();
            fc.lock();

            FileInputStream fin = new FileInputStream(rf.getFD());
            BufferedInputStream bin = new BufferedInputStream(fin);
            in = new ObjectInputStream(bin);

            hash = (Hashtable) in.readObject();
        } catch (Exception e) {
            throw new GATInvocationException("local advert", e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                // Ignore.
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

    static class Entry implements Serializable {
        String a;

        MetaData m;
    }
}
