// @@@ TODO: post-filter query, implement cwd 
/*
 * Created on Aug 12, 2004
 */
package org.gridlab.gat.advert.cpi.storagebox;

import java.util.NoSuchElementException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Stub;
import org.apache.axis.configuration.SimpleProvider;
import org.globus.axis.gsi.GSIConstants;
import org.gridlab.gat.AdaptorCreationException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.engine.GATEngine;
import org.storagebox.www.sbns.AttributeSet;
import org.storagebox.www.sbns.AttributeValue;
import org.storagebox.www.sbns.StorageBoxPortType;
import org.storagebox.www.sbns.StorageBoxServiceLocator;
import org.storagebox.www.sbns.StringSet;

/**
 * @author rob
 */
public class StorageBoxAdvertAdaptor extends AdvertServiceCpi {
	static final String NAMESPACE = "Gridlab";

	static final String ATTRIBUTE = "GAT_InternalObjectState";

	static final String TYPE_STRING = "String";

	SimpleProvider p;

	StorageBoxPortType sbox;

	/**
	 * @param gatContext
	 * @param preferences
	 */
	public StorageBoxAdvertAdaptor(GATContext gatContext,
			Preferences preferences) throws AdaptorCreationException {
		super(gatContext, preferences);

		checkName("storagebox");

		try {
			/*
			 * // Prepare httpg handler. p = new SimpleProvider();
			 * p.deployTransport("httpg", new SimpleTargetedChain( new
			 * GSIHTTPSender())); Util.registerTransport();
			 */
			StorageBoxServiceLocator s = new StorageBoxServiceLocator();
			//s.setEngineConfiguration(p);
			sbox = s.getStorageBoxPort();

			// turn on credential delegation, it is turned off by default.
			Stub stub = (Stub) sbox;
			stub._setProperty(GSIConstants.GSI_MODE,
					GSIConstants.GSI_MODE_FULL_DELEG);

		} catch (ServiceException e) {
			throw new AdaptorCreationException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#add(org.gridlab.gat.advert.Advertisable,
	 *      org.gridlab.gat.advert.MetaData, java.lang.String)
	 */
	public void add(Advertisable advert, MetaData metaData, String path)
			throws GATInvocationException {
		if (metaData == null || metaData.size() == 0) {
			throw new GATInvocationException(
					"metaData should contain at least one element");
		}
		try {
			sbox.createObject(NAMESPACE, path);
			sbox.createAttribute(NAMESPACE, ATTRIBUTE, TYPE_STRING, true);
			sbox.setAttribute(NAMESPACE, path, ATTRIBUTE, GATEngine
					.getGATEngine().marshalAdvertisable(advert));

			for (int i = 0; i < metaData.size(); i++) {
				sbox.createAttribute(NAMESPACE, metaData.getKey(i),
						TYPE_STRING, true);
				sbox.setAttribute(NAMESPACE, path, metaData.getKey(i), metaData
						.getData(i));
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
			sbox.deleteObject(NAMESPACE, path);
		} catch (Exception e) {
			throw new GATInvocationException("storagebox", e);
		}
	}

	private boolean isRegexp(String s) {
		return s.indexOf('^') != -1 || s.indexOf('*') != -1
				|| s.indexOf('[') != -1 || s.indexOf(']') != -1
				|| s.indexOf('\\') != -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#getAdvertisable(java.lang.String)
	 */
	public Advertisable getAdvertisable(String path)
			throws GATInvocationException, NoSuchElementException {
		Advertisable res = null;

		try {
			StringSet namespaces = createStringSet(NAMESPACE);
			StringSet objects = createStringSet(path);
			StringSet attributes_names = createStringSet(ATTRIBUTE);

			AttributeSet s = sbox.getAttributes(namespaces, objects,
					attributes_names);

			AttributeValue[] vals = s.getItem();
			if (vals.length == 0) {
				throw new NoSuchElementException();
			} else if (vals.length != 1) {
				throw new Error(
						"Internal error in storagebox adaptor, found multiple objects at path");
			}

			AttributeValue v = vals[0];
			return GATEngine.getGATEngine().unmarshalAdvertisable(
					v.getAttribute_value());
		} catch (Exception e) {
			throw new GATInvocationException("storagebox", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#find(org.gridlab.gat.advert.MetaData)
	 */
	public String[] find(MetaData metaData) throws GATInvocationException {
		if (metaData == null || metaData.size() == 0) {
			throw new GATInvocationException(
					"metaData should contain at least one element");
		}

		String query = "";

		for (int i = 0; i < metaData.size(); i++) {
			if (!isRegexp(metaData.getKey(i))) {
				query += metaData.getKey(i) + "=" + metaData.getData(i) + "&";
			} else {
				query += metaData.getKey(i) + "~" + metaData.getData(i) + "&";
			}
		}

		try {
			AttributeSet res = sbox.query(createStringSet(NAMESPACE), query,
					createStringSet(""));
			AttributeValue[] vals = res.getItem();
			if (vals.length == 0) {
				return null;
			}
			String[] out = new String[vals.length];
			for (int i = 0; i < vals.length; i++) {
				out[i] = vals[i].getAttribute_value();
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
			StringSet objects = createStringSet(path);
			StringSet attributes_names = createStringSet(""); // means all
			// attributes

			AttributeSet s = sbox.getAttributes(namespaces, objects,
					attributes_names);

			AttributeValue[] vals = s.getItem();
			for (int i = 0; i < vals.length; i++) {
				AttributeValue v = vals[i];
				res.put(v.getAttribute_name(), v.getAttribute_value());
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
		// TODO Auto-generated method stub
		return super.getPWD();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#setPWD(java.lang.String)
	 */
	public void setPWD(String path) throws GATInvocationException {
		// TODO Auto-generated method stub
		super.setPWD(path);
	}
}