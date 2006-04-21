package org.gridlab.gat.advert.cpi.storagebox;

import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.configuration.SimpleProvider;

import org.globus.axis.transport.GSIHTTPSender;
import org.globus.axis.util.Util;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.engine.GATEngine;

import org.storagebox.www.sbns.ObjectAttribute;
import org.storagebox.www.sbns.ObjectSet;
import org.storagebox.www.sbns.StorageBoxObject;
import org.storagebox.www.sbns.StorageBoxPortType;
import org.storagebox.www.sbns.StorageBoxServiceLocator;
import org.storagebox.www.sbns.StringSet;

import java.util.NoSuchElementException;

public class StorageBoxAdvertAdaptor extends AdvertServiceCpi {
    static final String NAMESPACE = "GAT_Adverts";

    static final String OBJ_STATE_ATTRIBUTE = "GAT_InternalObjectState";

    static final String USR_ATTRIBUTE_PREFIX = "USR_";

    static final String USR_OBJECT_PREFIX = "USR_PATH";

    static final String TYPE_STRING = "String";

    SimpleProvider p;

    StorageBoxPortType sbox;

    String pwd;

    GATContext gc;

    Preferences prefs;

    /**
     * @param gatContext
     * @param preferences
     */
    public StorageBoxAdvertAdaptor(GATContext gatContext,
            Preferences preferences) throws GATObjectCreationException {
        super(gatContext, preferences);

        try {
            // Prepare httpg handler. 
            p = new SimpleProvider();
            p.deployTransport("httpg", new SimpleTargetedChain(
                new GSIHTTPSender()));
            Util.registerTransport();

            StorageBoxServiceLocator s = new StorageBoxServiceLocator();
            s.setEngineConfiguration(p);
            sbox = s.getStorageBoxPort();

            //File log=new File("/home3/aagapi/RA/src_SB_Adaptor/AdaptorBenchmark","log");
            //FileWriter lw=new FileWriter(log);
            // turn on credential delegation, it is turned off by default.
            //			Stub stub = (Stub) sbox;
            //stub._setProperty(GSIHTTPTransport.GSI_MODE,
            //		GSIHTTPTransport.GSI_MODE_FULL_DELEG);
            pwd = null;

            this.gc = gatContext;
            this.prefs = preferences;
        } catch (Exception e) {
            e.printStackTrace();
            throw new GATObjectCreationException("storagebox", e);
        }
    }

    private String absolutePath(String path) {
        if (path.charAt(0) == '/') {
            return path;
        }

        if (pwd == null) {
            return "/" + path;
        }

        return pwd + "/" + path;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#add(org.gridlab.gat.advert.Advertisable,
     *      org.gridlab.gat.advert.MetaData, java.lang.String)
     */
    public void add(Advertisable advert, MetaData metaData, String path)
            throws GATInvocationException {
        String tmp = null;

        if ((metaData == null) || (metaData.size() == 0)) {
            throw new GATInvocationException(
                "metaData should contain at least one element");
        }

        try {
            sbox
                .createObject(NAMESPACE, USR_OBJECT_PREFIX + absolutePath(path));
            sbox.createAttribute(NAMESPACE, OBJ_STATE_ATTRIBUTE, TYPE_STRING,
                true);
            tmp = GATEngine.getGATEngine().marshalAdvertisable(advert);
            sbox.setAttribute(NAMESPACE,
                USR_OBJECT_PREFIX + absolutePath(path), OBJ_STATE_ATTRIBUTE,
                tmp);

            for (int i = 0; i < metaData.size(); i++) {
                sbox.createAttribute(NAMESPACE, USR_ATTRIBUTE_PREFIX
                    + metaData.getKey(i), TYPE_STRING, true);
                sbox.setAttribute(NAMESPACE, USR_OBJECT_PREFIX
                    + absolutePath(path), USR_ATTRIBUTE_PREFIX
                    + metaData.getKey(i), metaData.getData(i));
            }
        } catch (Exception e) {
            throw new GATInvocationException("storagebox", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#delete(java.lang.String)
     */
    public void delete(String path) throws NoSuchElementException,
            GATInvocationException {
        try {
            sbox
                .deleteObject(NAMESPACE, USR_OBJECT_PREFIX + absolutePath(path));
        } catch (Exception e) {
            throw new GATInvocationException("storagebox", e);
        }
    }

    private boolean isRegexp(String s) {
        return (s.indexOf('^') != -1) || (s.indexOf('*') != -1)
            || (s.indexOf('[') != -1) || (s.indexOf(']') != -1)
            || (s.indexOf('\\') != -1);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#getAdvertisable(java.lang.String)
     */
    public Advertisable getAdvertisable(String path)
            throws GATInvocationException, NoSuchElementException {
        String tmp = null;

        try {
            StringSet namespaces = createStringSet(NAMESPACE);
            StringSet objects = createStringSet(USR_OBJECT_PREFIX
                + absolutePath(path));
            StringSet attributes_names = createStringSet(OBJ_STATE_ATTRIBUTE);

            ObjectSet os = sbox.getAttributes(namespaces, objects,
                attributes_names);

            StorageBoxObject[] sbo = os.getItem();

            if (sbo.length == 0) {
                throw new NoSuchElementException();
            } else if (sbo.length != 1) {
                throw new Error(
                    "Internal error in storagebox adaptor, found multiple objects at path");
            }

            ObjectAttribute[] oa = sbo[0].getItem();

            if (oa.length == 0) {
                throw new NoSuchElementException();
            }

            tmp = oa[1].getAttribute_value();

            return GATEngine.getGATEngine().unmarshalAdvertisable(gc, prefs,
                tmp);
        } catch (Exception e) {
            throw new GATInvocationException("storagebox", e);
        }
    }

    private String extractPath(String name) {
        String rv = "";
        rv = name.substring(USR_OBJECT_PREFIX.length());

        return rv;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#find(org.gridlab.gat.advert.MetaData)
     */
    public String[] find(MetaData metaData) throws GATInvocationException {
        if ((metaData == null) || (metaData.size() == 0)) {
            throw new GATInvocationException(
                "metaData should contain at least one element");
        }

        String query = "";

        for (int i = 0; i < metaData.size(); i++) {
            if (!isRegexp(metaData.getKey(i))) {
                query += (USR_ATTRIBUTE_PREFIX + metaData.getKey(i) + "=" + metaData
                    .getData(i));

                if (i < (metaData.size() - 1)) {
                    query += "&";
                }
            } else {
                query += (USR_ATTRIBUTE_PREFIX + metaData.getKey(i) + "~" + metaData
                    .getData(i));

                if (i < (metaData.size() - 1)) {
                    query += "&";
                }
            }
        }

        try {
            ObjectSet res = sbox.query(createStringSet(NAMESPACE), query,
                createStringSet(""));

            StorageBoxObject[] sbo = res.getItem();

            if (sbo.length == 0) {
                return null;
            }

            ObjectAttribute[] oa = sbo[0].getItem();

            if (oa.length == 0) {
                return null;
            }

            String[] out = new String[oa.length];

            for (int i = 0; i < oa.length; i++) {
                out[i] = extractPath(oa[i].getAttribute_value());
            }

            return out;
        } catch (Exception e) {
            throw new GATInvocationException("storagebox", e);
        }
    }

    protected StringSet createStringSet(String val) {
        StringSet res = new StringSet();
        String[] items = new String[1];
        items[0] = val;
        res.setItem(items);

        return res;
    }

    private String extractMetaDataName(String name) {
        String rv = "";
        rv = name.substring(USR_ATTRIBUTE_PREFIX.length());

        return rv;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#getMetaData(java.lang.String)
     */
    public MetaData getMetaData(String path) throws GATInvocationException,
            NoSuchElementException {
        MetaData res = new MetaData();

        try {
            StringSet namespaces = createStringSet(NAMESPACE);
            StringSet objects = createStringSet(USR_OBJECT_PREFIX
                + absolutePath(path));
            StringSet attributes_names = createStringSet(""); // means all

            // attributes
            ObjectSet os = sbox.getAttributes(namespaces, objects,
                attributes_names);

            StorageBoxObject[] sbo = os.getItem();

            if (sbo.length == 0) {
                throw new NoSuchElementException();
            } else if (sbo.length != 1) {
                throw new Error(
                    "Internal error in storagebox adaptor, found multiple objects at path");
            }

            ObjectAttribute[] vals = sbo[0].getItem();

            for (int i = 0; i < vals.length; i++) {
                if (!vals[i].getAttribute_name().equals(OBJ_STATE_ATTRIBUTE)) {
                    res.put(extractMetaDataName(vals[i].getAttribute_name()),
                        vals[i].getAttribute_value());
                }
            }
        } catch (Exception e) {
            throw new GATInvocationException("storagebox", e);
        }

        return res;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#getPWD()
     */
    public String getPWD() throws GATInvocationException {
        return pwd;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#setPWD(java.lang.String)
     */
    public void setPWD(String path) throws GATInvocationException {
        pwd = path;
    }
}
