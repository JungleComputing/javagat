package services.AdvertServiceEntry.impl;

import java.net.URL;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.mds.servicegroup.client.ServiceGroupRegistrationParameters;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.config.ContainerConfig;
import org.globus.wsrf.container.ServiceHost;
import org.globus.wsrf.impl.ResourceHomeImpl;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.impl.servicegroup.client.ServiceGroupRegistrationClient;
import org.globus.wsrf.utils.AddressingUtils;

import stubs.AdvertServiceEntryService.MetaDataType;

import commonj.timers.Timer;

public class AdvertServiceEntryResourceHome extends ResourceHomeImpl
{
    /* Added for logging */
    static final Log logger = LogFactory.getLog(AdvertServiceEntryResourceHome.class);
    
    public ResourceKey create(String path, MetaDataType metaData, byte[] serializedAdvertisable) throws Exception
    {
	// Create a resource and initialize it
	AdvertServiceEntryResource resource = (AdvertServiceEntryResource) this.createNewInstance();
	resource.initialize(path, metaData, serializedAdvertisable);
	// Get key
	ResourceKey key = new SimpleResourceKey(this.getKeyTypeName(), resource.getID());
	// Add the resource to the list of resources in this home
	this.add(key, resource);
	return key;
    }
    
    /*
     * We override the add method to register our AdvertServiceEntryFileResources with the
     * index (the DefaultIndexService)
     */
    protected void add(ResourceKey key, Resource resource)
    {
	// Call the parent "add" method
	super.add(key, resource);
	
	// Get registration client
	ServiceGroupRegistrationClient regClient = ServiceGroupRegistrationClient.getContainerClient();
	
	// Get resource context
	ResourceContext ctx;
	try
	    {
		ctx = ResourceContext.getResourceContext();
	    }
	catch(ResourceContextException e)
	    {
		logger.error("Could not get ResourceContext: " + e);
		throw new RuntimeException("Could not get ResourceContext", e);
	    }
	
	// Construct EPR of WS-Resource we want to register in the index.
	EndpointReferenceType epr;
	try
	    {
		URL baseURL = ServiceHost.getBaseURL();
		AdvertServiceEntryConfiguration config = AdvertServiceEntryConfiguration.getConfObject();
		String instanceService = config.getInstanceServicePath();
		String instanceURI = baseURL.toString() + instanceService;
		epr = AddressingUtils.createEndpointReference(instanceURI, key);
	    }
	catch(Exception e)
	    {
		logger.error("Could not form EPR: " + e);
		throw new RuntimeException("Could not form EPR", e);
	    }
	
	// Get registration options from registration.xml file
	// Remember kids: Hard-coding is bad karma
	String regPath = ContainerConfig.getGlobusLocation() + "/etc/services_AdvertServiceEntry/registration.xml";
	
	/*
	 * REGISTER WS-RESOURCE IN INDEX
	 */
	try
	    {
		AdvertServiceEntryResource entryResource = (AdvertServiceEntryResource) resource;
		ServiceGroupRegistrationParameters params = ServiceGroupRegistrationClient.readParams(regPath);
		params.setRegistrantEPR(epr);
		Timer regTimer = regClient.register(params);
		entryResource.setRegTimer(regTimer);
	    }
	catch(Exception e)
	    {
		logger.error("ERROR: Couldn't register AdvertServiceEntryResource in local index.");
		throw new RuntimeException("ERROR: Couldn't register AdvertServiceEntryResource in local index.", e);
	    }
    }
    
    public void remove(ResourceKey key) throws ResourceException
    {
	// Call the parent "remove" method
	super.remove(key);
    }
}
