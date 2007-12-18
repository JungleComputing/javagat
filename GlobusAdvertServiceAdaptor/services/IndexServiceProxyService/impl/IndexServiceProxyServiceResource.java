package services.IndexServiceProxyService.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.Resource;
import org.globus.wsrf.InvalidResourceKeyException;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceProperties;
import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.impl.ReflectionResourceProperty;
import org.globus.wsrf.impl.SimpleResourcePropertySet;

public class IndexServiceProxyServiceResource implements Resource, ResourceProperties
{
    /* Added for logging */
    static final Log logger = LogFactory.getLog(IndexServiceProxyServiceResource.class);

    /* Resource Property set */
    private ResourcePropertySet propSet;
    
    /* Resource key. This uniquely identifies this resource. */
    private Object key;
    
    /* UUID generator to generate unique resource key */
    private static final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
    
    /* Resource properties */

    /* Initializes RPs and returns a unique identifier for this resource */
    public Object initialize(Object key) throws Exception
    {
	this.key = key;
	this.propSet = new SimpleResourcePropertySet(IndexServiceProxyServiceConstants.RESOURCE_PROPERTIES);

	return key;
    }

    /* Initializes resource and returns a unique identifier for this resource */
    public Object initialize() throws Exception
    {
	String key = uuidGen.nextUUID();
	initialize(key);
	return key;
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
}
