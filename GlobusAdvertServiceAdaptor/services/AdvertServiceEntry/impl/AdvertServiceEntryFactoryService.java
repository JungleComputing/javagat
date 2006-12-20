package services.AdvertServiceEntry.impl;

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.wsrf.Constants;
import org.globus.wsrf.NoResourceHomeException;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.ResourceHome;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.container.ServiceHost;
import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.utils.AddressingUtils;
import stubs.AdvertServiceEntryFactoryService.MetaDataType;
import stubs.AdvertServiceEntryFactoryService.AddAdvertServiceEntry;
import stubs.AdvertServiceEntryFactoryService.AddAdvertServiceEntryResponse;

public class AdvertServiceEntryFactoryService implements AdvertServiceEntryConstants
{
    /* Added for logging */
    static final Log logger = LogFactory.getLog(AdvertServiceEntryFactoryService.class);    
    
    /* Implementation of AddFile operation */
    public AddAdvertServiceEntryResponse addAdvertServiceEntry(AddAdvertServiceEntry params) throws RemoteException
    {
	AdvertServiceEntryResourceHome home = null;
	ResourceKey key = null;
	
	// Retrieve parameters
	String path = params.getPath();
	MetaDataType metaData = params.getMetaData();
	byte[] serializedAdvertisable = params.getSerializedAdvertisable();
	
	logger.debug("addAdvertServiceEntry invoked with path=" + path + ",metaData=" + metaData + ",serializedAdvertisable=" + serializedAdvertisable);
	
	/*
	 * CREATE A AdvertServiceEntryResource
	 */
	
	// We create a new AdvertServiceEntryResource through the AdvertServiceEntryResourceHome
	try
	    {
		home = (AdvertServiceEntryResourceHome) getInstanceResourceHome();
		key = home.create(path, MetaDataTypeUtils.toAdvertServiceEntryServiceMetaDataType(metaData), serializedAdvertisable);
	    }
	catch(Exception e)
	    {
		logger.error("ERROR: Couldn't create new AdvertServiceEntryResource");
		throw new RemoteException("ERROR: Couldn't create AdvertServiceEntryResource EPR", e);
	    }
	EndpointReferenceType epr = null;
	
	// We construct the EPR for the recently created WS-Resource.
	// The AdvertServiceEntryResource will be accessed through an instance service.
	try
	    {
		URL baseURL = ServiceHost.getBaseURL();
		// The instance service path is contained in the configuration
		// object
		AdvertServiceEntryConfiguration config = AdvertServiceEntryConfiguration.getConfObject();
		String instanceService = config.getInstanceServicePath();
		String instanceURI = baseURL.toString() + instanceService;
		// The endpoint reference includes the instance's URI and the
		// resource key
		epr = AddressingUtils.createEndpointReference(instanceURI, key);
	    }
	catch(Exception e)
	    {
		logger.error("ERROR: Couldn't create AdvertServiceEntryResource EPR");
		throw new RemoteException("ERROR: Couldn't create AdvertServiceEntryResource EPR", e);
	    }
	
	logger.info("Added new AdvertServiceEntryResource. PATH=" + path + ", METADATA=" + metaData + ", SERIALIZEDADVERTISABLE=" + serializedAdvertisable);
	logger.info("AdvertServiceEntryResource has been added by user '" + SecurityManager.getManager().getCaller() + "'");
	
	/*
	 * RETURN ENDPOINT REFERENCE TO NEW WS-RESOURCE
	 */
	
	AddAdvertServiceEntryResponse response = new AddAdvertServiceEntryResponse();
	response.setEndpointReference(epr);
	return response;
    }
    
    /* Retrieves the resource home for the instance service */
    protected ResourceHome getInstanceResourceHome() throws NoResourceHomeException, ResourceContextException
    {
	ResourceHome home;
	ResourceContext ctx;
	AdvertServiceEntryConfiguration config;
	
	// The path for the instance service is contained in the configuration object
	try
	    {
		config = AdvertServiceEntryConfiguration.getConfObject();
	    }
	catch(Exception e)
	    {
		throw new NoResourceHomeException("Unable to access configuration object to retrieve instance service path.");
	    }
	String instanceService = config.getInstanceServicePath();
	
	// Lookup resource home
	String homeLoc = Constants.JNDI_SERVICES_BASE_NAME + instanceService + "/home";
	try
	    {
		Context initialContext = new InitialContext();
		home = (ResourceHome) initialContext.lookup(homeLoc);
	    }
	catch(NameNotFoundException e)
	    {
		throw new NoResourceHomeException();
	    }
	catch(NamingException e)
	    {
		throw new ResourceContextException("", e);
	    }
	
	return home;
    }
}
