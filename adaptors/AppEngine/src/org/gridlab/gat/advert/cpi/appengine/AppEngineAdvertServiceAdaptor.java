/*
 * Created 15 Apr. 2009 by Bas Boterman.
 */

package org.gridlab.gat.advert.cpi.appengine;

import ibis.advert.Advert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.advert.cpi.AdvertServiceCpi;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.security.PasswordSecurityContext;

/**
 * The adaptor for use with the Google App Engine.
 * 
 * @author Bas Boterman
 */

public class AppEngineAdvertServiceAdaptor extends AdvertServiceCpi {

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = 
          AdvertServiceCpi.getSupportedCapabilities();
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

    Advert advertService = null;

    /**
     * Constructor creates a new {@link AppEngineAdvertServiceAdaptor}. 
     * Username and password are provided in the {@link GATContext}. A new 
     * {@link Advert} object is created and pwd is set to the user's home
     * directory.
     * 
     * @param gatContext
     * 	Preferences for using the {@link AppEngineAdvertServiceAdaptor}.
     * @throws GATObjectCreationException
     * 	Failed to create an {@link AppEngineAdvertServiceAdaptor}.
     */
    public AppEngineAdvertServiceAdaptor(GATContext gatContext)
      throws GATObjectCreationException {
    	super(gatContext);
    	
    	String server = null;
    	String user = null; 
    	String pass = null;
    	
    	/* Try to fetch username/password from gatContext. */
    	for (int i = 0; i < gatContext.getSecurityContexts().size(); i++) {
    		if (gatContext.getSecurityContexts().get(i) instanceof 
    		  PasswordSecurityContext) {
	    		user = ((PasswordSecurityContext) 
	    		  gatContext.getSecurityContexts().get(i)).getUsername();
	    		pass = ((PasswordSecurityContext) 
	    		  gatContext.getSecurityContexts().get(i)).getPassword();
	    		
	    		try {
	    			advertService = new Advert(server, user, pass);
	    			break;
	    		}
	    		catch (Exception e) {
	    			throw new GATObjectCreationException("Constructor failed", 
	    			  e);
	    		}
    		}
    	}
    	
        String home = System.getProperty("user.home");

        if (home == null) {
            throw new GATObjectCreationException("Could not get user home dir.");
        }
        
        pwd = home;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.advert.AdvertService#getAdvertisable(java.lang.String).
     */
    public Advertisable getAdvertisable(String path)
	  throws GATInvocationException, NoSuchElementException {
		path = normalizePath(path);
		byte b[] = null;

		try {
			b = advertService.get(path);
		}
		catch (NoSuchElementException nsee) {
			throw new NoSuchElementException("No such element: " + path);
		}
		catch (Exception e) {
			throw new GATInvocationException("Get failed", e);
		}
		
		Advertisable advert = GATEngine.getGATEngine().unmarshalAdvertisable(
		  gatContext, b.toString());

		return advert;
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
					throw new 
					  GATInvocationException("Could not marshal object.");
				}
			}
			
			advertService.add(advertString.getBytes(), 
			  toAdvertMetaData(metadata), path);
		} catch (Exception e) {
			throw new GATInvocationException("Local advert", e);
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
		
		try {
			advertService.delete(path);
		}
		catch (NoSuchElementException nsee) {
			throw new NoSuchElementException("No such element: " + path);
		}
		catch (Exception e) {
			throw new GATInvocationException("Delete failed", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#find(java.util.Map)
	 */
	public String[] find(MetaData query) throws GATInvocationException {
		try {
			return advertService.find(toAdvertMetaData(query));
		}
		catch (NoSuchElementException nsee) {
			return null;
		}
		catch (Exception e) {
			throw new GATInvocationException("Find failed", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.advert.AdvertService#getMetaData(java.lang.String)
	 */
	public MetaData getMetaData(String path) throws NoSuchElementException,
	  GATInvocationException {
		path = normalizePath(path);
		
		try {
			return fromAdvertMetaData(advertService.getMetaData(path));
		}
		catch (NoSuchElementException nsee) {
			throw new NoSuchElementException("No such element: " + path);
		}
		catch (Exception e) {
			throw new GATInvocationException("Get MetaData failed", e);
		}
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

	/**
	 * Private function to normalize relative path and PWD to absolute path.
	 * 
	 * @param path
	 *   The relative path that needs to be formatted to an absolute path.
	 * @throws GATInvocationException
	 *   The path can't be normalized.
	 */
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
	
	/**
	 * Private function to convert any {@link MetaData} object from 
	 * org.gridlab.gat to a {@link ibis.advert.MetaData} object.
	 * 
	 * @param metadata
	 *   The {@link MetaData} to be converted.
	 * @return
	 *   An {@link ibis.advert.MetaData} object.
	 */
	private ibis.advert.MetaData toAdvertMetaData(MetaData metadata) {
		ibis.advert.MetaData result = new ibis.advert.MetaData();
		
		for (int i = 0; i < metadata.size(); i++) {
			result.put(metadata.getKey(i), metadata.getData(i));
		}
		
		return result;
	}

	/**
	 * Private function to convert any {@link ibis.advert.MetaData} object 
	 * from org.gridlab.gat to a {@link MetaData} object.
	 * 
	 * @param metadata
	 *   The {@link ibis.advert.MetaData} to be converted.
	 * @return
	 *   An {@link MetaData} object.
	 */
	private MetaData fromAdvertMetaData(ibis.advert.MetaData metadata) {
		MetaData result = new MetaData();
		
		Iterator<String> itr  = metadata.getAllKeys().iterator();
		
		while (itr.hasNext()) {
			String key   = itr.next();
			String value = metadata.get(key);

			if (key == null) {
				continue; //key can't be null (value can)
			}
			
			result.put(key, value);
		}
		return result;
	}
}
