package services.AdvertServiceEntry.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import stubs.GAT.*;
import stubs.AdvertServiceEntryService.*;

import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.InvalidResourceKeyException;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.PersistentResource;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceProperties;
import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.impl.ReflectionResourceProperty;
import org.globus.wsrf.impl.SimpleResourcePropertySet;
import org.globus.wsrf.utils.FilePersistenceHelper;

import commonj.timers.Timer;

public class AdvertServiceEntryResource implements PersistentResource, ResourceProperties
{
    /* Added for logging */
    static final Log logger = LogFactory.getLog(AdvertServiceEntryResource.class);

    /* Resource Property set */
    private ResourcePropertySet propSet;
    
    /* Resource key. This uniquely identifies this resource. */
    private Object key;
    
    /* UUID generator to generate unique resource key */
    private static final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
    
    /* Persistence helper class */
    private FilePersistenceHelper persistenceHelper;
    
    /* Used to register in the local index */
    Timer regTimer;
    
    /* Resource properties */
    private String path;
    // Map<String,String>
    private MetaData metaData;
    private byte[] serializedAdvertisable;

    /* Initializes RPs and returns a unique identifier for this resource */
    public Object initialize(Object key) throws Exception
    {
	this.key = key;
	this.propSet = new SimpleResourcePropertySet(AdvertServiceEntryConstants.RESOURCE_PROPERTIES);

	ResourceProperty pathRP = new ReflectionResourceProperty(AdvertServiceEntryConstants.RP_PATH, "Path", this);
	this.propSet.add(pathRP);

	ResourceProperty metaDataRP = new ReflectionResourceProperty(AdvertServiceEntryConstants.RP_METADATA, "MetaData", this);
	this.propSet.add(metaDataRP);

	ResourceProperty serializedAdvertisableRP = new ReflectionResourceProperty(AdvertServiceEntryConstants.RP_SERIALIZEDADVERTISABLE, "SerializedAdvertisable", this);
	this.propSet.add(serializedAdvertisableRP);

	return key;
    }

    /* Initializes resource and returns a unique identifier for this resource */
    public Object initialize(String path, MetaData metaData, String serializedAdvertisable) throws Exception
    {
	String key = uuidGen.nextUUID();
	initialize(key);
	setPath(path);
	setMetaData(metaData);
	setSerializedAdvertisable(serializedAdvertisable);
	store();
	return key;
    }
    
    /* Get/Setters for the RPs */
    public String getPath()
    {
	return path;
    }
    
    public synchronized void setPath(String path)
    {
	this.path = path;
    }
    
    public MetaData getMetaData()
    {
	return metaData;
    }

    public synchronized void setMetaData(MetaData metaData) throws ResourceException
    {
	if(MetaDataUtils.isMap(metaData))
	    this.metaData = metaData;
	else
	    throw new ResourceException("failed to initialize resource because of corrupt MetaData");
    }
    
    public byte[] getSerializedAdvertisable()
    {
	return serializedAdvertisable;
    }
    
    public synchronized void setSerializedAdvertisable(byte[] serializedAdvertisable)
    {
	this.serializedAdvertisable = serializedAdvertisable;
    }

    public synchronized void setSerializedAdvertisable(String serializedAdvertisable) throws java.io.UnsupportedEncodingException
    {
	this.serializedAdvertisable = serializedAdvertisable.getBytes("UTF8");
    }

    /* Required to access the registration timer for this resource */
    public void setRegTimer(Timer regTimer)
    {
	this.regTimer=regTimer;
    }
    
    public Timer getRegTimer()
    {
	return regTimer;
    }
    
    /* Required by interface ResourceProperties */
    public ResourcePropertySet getResourcePropertySet()
    {
	return this.propSet;
    }
    
    /* Required by interface ResourceIdentifier */
    public Object getID()
    {
	return this.key;
    }
    
    /* Required by interface RemoveCallback */
    public void remove() throws ResourceException
    {
	logger.info("Resource " + this.getID() + " is going to be removed.");
	regTimer.cancel();
	getPersistenceHelper().remove(this.key);
    }
    
    /* Required by interface PersistenceCallback */
    public void load(ResourceKey key) throws ResourceException
    {
	/* Try to retrieve the persisted resource from disk */
	File file = getKeyAsFile(key.getValue());
	/*
	 * If the file does not exist, no resource with that key was ever
	 * persisted
	 */
	if(!file.exists())
	    {
		throw new NoSuchResourceException();
	    }
	
	/*
	 * We try to initialize the resource. This places default values in the
	 * RPs. We still have to load the values of the RPs from disk
	 */
	try
	    {
		initialize(key.getValue());
	    }
	catch (Exception e)
	    {
		throw new ResourceException("Failed to initialize resource", e);
	    }

	/* Now, we open the resource file and load the values */
	logger.info("Attempting to load resource " + key.getValue());
	
	/* We will use this to read from the file */
	FileInputStream fis = null;
	
	/* We will store the RPs in these variables */
	String path;
	MetaData metaData;
	String serializedAdvertisable;
	
	try
	    {
		/* Open the file */
		fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		/* Read the RPs */
		path = ois.readUTF();
		metaData = (MetaData)ois.readObject();
		serializedAdvertisable = ois.readUTF();
		
		logger.info("Successfully loaded resource with path=" + path);

		/* Assign the RPs to the resource class's attributes */
		setPath(path);
		setMetaData(metaData);
		setSerializedAdvertisable(serializedAdvertisable);
	    }
	catch(Exception e)
	    {
		throw new ResourceException("Failed to load resource", e);
	    }
	finally
	    {
		/* Make sure we clean up, whether the load succeeds or not */
		if(fis != null)
		    {
			try
			    {
				fis.close();
			    }
			catch(Exception ee)
			    {}
		    }
	    }
    }

    /* Required by interface PersistenceCallback */
    public synchronized void store() throws ResourceException
    {
	/* We will use these two variables to write the resource to disk */
	FileOutputStream fos = null;
	File tmpFile = null;
	
	logger.info("Attempting to store resource " + this.getID());
	try
	    {
		/* We start by creating a temporary file */
		tmpFile = File.createTempFile("GATAdvert", ".tmp", getPersistenceHelper().getStorageDirectory());
		/* We open the file for writing */
		fos = new FileOutputStream(tmpFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		/* We write the RPs in the file */
		oos.writeUTF(this.path);
		oos.writeObject(this.metaData);
		oos.writeUTF(new String(this.serializedAdvertisable, "UTF8"));
		oos.flush();
		logger.info("Successfully stored resource with Path=" + this.path);
	    }
	catch(Exception e)
	    {
		/* Delete the temporary file if something goes wrong */
		tmpFile.delete();
		throw new ResourceException("Failed to store resource", e);
	    }
	finally
	    {
		/* Clean up */
		if(fos != null)
		    {
			try
			    {
				fos.close();
			    }
			catch(Exception ee)
			    {}
		    }
	    }
	
	/*
	 * We have successfully created a temporary file with our resource's
	 * RPs. Now, if there is a previous copy of our resource on disk, we
	 * first have to delete it. Next, we rename the temporary file to the
	 * file representing our resource.
	 */
	File file = getKeyAsFile(this.key);
	if(file.exists())
	    {
		file.delete();
	    }
	if(!tmpFile.renameTo(file))
	    {
		tmpFile.delete();
		throw new ResourceException("Failed to store resource");
	    }
    }
    
    /*
     * Given a key, this method returns a File object representing the persisted
     * resource.
     */
    private File getKeyAsFile(Object key) throws InvalidResourceKeyException
    {
	/*
	 * If the key is a String, we use the FilePersistenceHelper to retrieve
	 * the resource
	 */
	if(key instanceof String)
	    {
		return getPersistenceHelper().getKeyAsFile(key);
		/* Otherwise, an exception is thrown */
	    }
	else
	    {
		throw new InvalidResourceKeyException();
	    }
    }

    /* Returns this resource's FilePersistenceHelper object */
    protected synchronized FilePersistenceHelper getPersistenceHelper()
    {
	/* If the persistenceHelper has not been created, create it */
	if(this.persistenceHelper == null)
	    {
		try
		    {
			this.persistenceHelper = new FilePersistenceHelper(this.getClass(), ".data");
		    }
		catch(Exception e)
		    {
			throw new RuntimeException(e.getMessage());
		    }
	    }
	
	/* Return the persistenceHelper */
	return this.persistenceHelper;
    }
}
