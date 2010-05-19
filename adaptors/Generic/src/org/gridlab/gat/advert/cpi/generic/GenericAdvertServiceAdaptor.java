/*
 * Created on May 20, 2004
 */
package org.gridlab.gat.advert.cpi.generic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.FileInterface;

/**
 * @author rob
 */
public class GenericAdvertServiceAdaptor extends AdvertServiceCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = AdvertServiceCpi
                .getSupportedCapabilities();
        capabilities.put("add", true);
        capabilities.put("delete", true);
        capabilities.put("getMetaData", true);
        capabilities.put("getAdvertisable", true);
        capabilities.put("find", true);
        capabilities.put("setPWD", true);
        capabilities.put("getPWD", true);
        return capabilities;
    }

    static final String SEPARATOR = "/";

    String pwd = SEPARATOR;

    Hashtable<String, Entry> database = new Hashtable<String, Entry>();

    // File f;

    /**
     * @param gatContext
     */
    public GenericAdvertServiceAdaptor(GATContext gatContext)
            throws GATObjectCreationException {
        super(gatContext);

        String home = System.getProperty("user.home");

        if (home == null) {
            throw new GATObjectCreationException("could not get user home dir");
        }

        // String filename = home + File.separator + ".JavaGAT-advert-database-"
        // + Math.random();
        // f = new File(filename);
        // f.deleteOnExit();
    }
    
    public Advertisable getAdvertisable(GATContext context, String path)
            throws GATInvocationException, NoSuchElementException {
        path = normalizePath(path);
        // load();

        Entry entry = database.get(path);

        if (entry == null) {
            throw new NoSuchElementException("No such element: " + path);
        }

        if (entry.getPath() == null) {
            throw new NoSuchElementException("No such element: " + path);
        }

        return GATEngine.getGATEngine().unmarshalAdvertisable(
                context, entry.getPath());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#add(org.gridlab.gat.advert.Advertisable,
     *      java.util.Map, java.lang.String)
     */
    public void add(Advertisable advert, MetaData metadata, String path)
            throws GATInvocationException {
        path = normalizePath(path);
        try {
            String advertString = null;
            if (advert != null) {
                advertString = advert.marshal();
                if (advertString == null) {
                    throw new GATInvocationException("could not marshal object");
                }
            }
            // load();
            database.put(path, new Entry(advertString, metadata));
            // save();
        } catch (Exception e) {
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
        // load();
        database.remove(path);
        // save();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#find(java.util.Map)
     */
    public String[] find(MetaData query) throws GATInvocationException {
        // load();

        Vector<String> res = new Vector<String>();

        Enumeration<String> keys = database.keys();
        Enumeration<Entry> data = database.elements();

        while (data.hasMoreElements()) {
            Entry entry = data.nextElement();
            String key = keys.nextElement();
            MetaData m = entry.metadata;

            if (m.match(query)) {
                res.add(key);
            }
        }

        return res.toArray(new String[res.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#getMetaData(java.lang.String)
     */
    public MetaData getMetaData(String path) throws NoSuchElementException,
            GATInvocationException {
        path = normalizePath(path);

        // load();

        return database.get(path).getMetaData();
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
        if (path.startsWith(SEPARATOR)) {
            pwd = path;
        } else {
            try {
                pwd = new java.net.URI(pwd + path).normalize().getPath();
            } catch (URISyntaxException e) {
                // ignore
            }
        }
    }

    public void exportDataBase(org.gridlab.gat.URI target)
            throws GATInvocationException {
        FileInterface sourceFile = null;
        try {
            sourceFile = GAT.createFile(gatContext,
                    new org.gridlab.gat.URI("file:///" + save())).getFileInterface();
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("failed to create source file", e);
        } catch (URISyntaxException e) {
            throw new GATInvocationException("failed to create source file", e);
        }
        sourceFile.copy(target);
        sourceFile.delete();
    }

    public synchronized void importDataBase(org.gridlab.gat.URI source)
            throws GATInvocationException {
        FileInterface sourceFile = null;
        try {
            sourceFile = GAT.createFile(gatContext, source).getFileInterface();
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("failed to create source file", e);
        }
        try {
            File tempLocalFile = File.createTempFile("JavaGAT",
                    "advert-database");
            sourceFile.copy(new org.gridlab.gat.URI("file:///" + tempLocalFile.getPath()));
            load(tempLocalFile);
            tempLocalFile.delete();

        } catch (URISyntaxException e) {
            throw new GATInvocationException("failed to copy", e);
        } catch (IOException e) {
            throw new GATInvocationException("failed to copy", e);
        }
    }

    private synchronized String save() throws GATInvocationException {
        ObjectOutputStream out = null;
        FileLock lock = null;
        File f = null;
        try {
            f = File.createTempFile("JavaGAT", "advert-database");
        } catch (IOException e) {
            throw new GATInvocationException("local advert", e);
        }
        try {
            FileOutputStream fout = new FileOutputStream(f);
            FileChannel fc = fout.getChannel();
            lock = fc.lock();

            BufferedOutputStream bout = new BufferedOutputStream(fout);
            out = new ObjectOutputStream(bout);

            out.writeObject(database);
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
        return f.getPath();
    }

    @SuppressWarnings("unchecked")
    private synchronized void load(File f) throws GATInvocationException {
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

            database = (Hashtable<String, Entry>) in.readObject();
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
            if (!path.startsWith(SEPARATOR)) {
                if (pwd.endsWith(SEPARATOR)) {
                    path = pwd + path;
                } else {
                    path = pwd + SEPARATOR + path;
                }
            }
            URI u = new URI(path);
            return u.normalize().getPath();
        } catch (Exception e) {
            throw new GATInvocationException("local advert", e);
        }
    }

    @SuppressWarnings("serial")
    static class Entry implements Serializable {

        private String path;

        private MetaData metadata;

        public Entry(String path, MetaData metadata) {
            this.path = path;
            this.metadata = metadata;
        }

        public String getPath() {
            return path;
        }

        public MetaData getMetaData() {
            return metadata;
        }
    }
}
