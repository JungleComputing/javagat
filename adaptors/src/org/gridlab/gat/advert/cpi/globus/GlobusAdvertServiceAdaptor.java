package org.gridlab.gat.advert.cpi.globus;

import java.net.URI;
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

public class GlobusAdvertServiceAdaptor extends AdvertServiceCpi
{
    private static final String SEPERATOR = "/";
    private String pwd = SEPERATOR;
    private static IndexService indexService = new IndexService();
    
    public GlobusAdvertServiceAdaptor(GATContext gatContext, Preferences preferences) throws GATObjectCreationException
    {
        super(gatContext, preferences);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#getAdvertisable(java.lang.String).
     */
    public Advertisable getAdvertisable(String path) throws GATInvocationException, NoSuchElementException
    {
        path = normalizePath(path);
        Entry e = null;
	try
	    {
		e = indexService.get(path);
	    }
	catch(Exception exc)
	    {
		throw new GATInvocationException("Error in indexService.get()", exc);
	    }

        if(e == null)
            return null;
        else
	    {
		Advertisable advert = GATEngine.getGATEngine().unmarshalAdvertisable(gatContext, preferences, e.serializedAdvertisable);
		return advert;
	    }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#add(org.gridlab.gat.advert.Advertisable,
     *      java.util.Map, java.lang.String)
     */
    public void add(Advertisable advert, MetaData metaData, String path) throws GATInvocationException
    {
        path = normalizePath(path);
        try
            {
                Entry entry = new Entry();
                entry.serializedAdvertisable = advert.marshal();
                entry.metaData = metaData;
                entry.path = path;
                indexService.put(entry);
            }
        catch(Exception e)
            {
		System.err.println("ERROR: " + e);
		e.printStackTrace();

		if(e.getCause() != null)
		    {
			System.err.println("CAUSE: " + e.getCause());
			e.getCause().printStackTrace();
		    }
		
		throw new GATInvocationException("globus advert", e);
            }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#delete(java.lang.String)
     */
    public void delete(String path) throws NoSuchElementException, GATInvocationException
    {
        path = normalizePath(path);
	try
	    {
		indexService.remove(path);
	    }
	catch(Exception e)
	    {
		throw new GATInvocationException("Error in indexService.remove()", e);
	    }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#find(java.util.Map)
     */
    public String[] find(MetaData query) throws GATInvocationException
    {
        Vector res = new Vector();
        Entry[] entries = null;
	try
	    {
		entries = indexService.getEntries();
	    }
	catch(Exception e)
	    {
		throw new GATInvocationException("Error in indexService.getEntries()", e);
	    }

	for(int i=0;i<entries.length;i++)
	    {
		Entry entry = entries[i];
		String path = entry.path;
		MetaData metaData = entry.metaData;
		
		if(metaData.match(query))
		    res.add(path);
	    }
	
        String[] s = new String[res.size()];
        for(int i=0;i<s.length;i++)
            s[i] = (String) res.get(i);
	
        return s;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#getMetaData(java.lang.String)
     */
    public MetaData getMetaData(String path) throws NoSuchElementException, GATInvocationException
    {
        path = normalizePath(path);
        Entry e = null;
	try
	    {
		e = indexService.get(path);
	    }
	catch(Exception exc)
	    {
		throw new GATInvocationException("Error in indexService.get()", exc);
	    }
	
        if(e == null)
            return null;
        else
            return e.metaData;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#getPWD()
     */
    public String getPWD()
    {
        return this.pwd;
    }
    
    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.advert.AdvertService#setPWD(java.lang.String)
     */
    public void setPWD(String path)
    {
        this.pwd = path;
    }

    private String normalizePath(String path) throws GATInvocationException
    {
        try
	    {
		if(!path.startsWith(SEPERATOR))
		    path = pwd + SEPERATOR + path;
		
		URI u = new URI(path);		
		return u.normalize().getPath();
	    }
	catch(Exception e)
	    {
		throw new GATInvocationException("globus advert", e);
	    }
    }
}
