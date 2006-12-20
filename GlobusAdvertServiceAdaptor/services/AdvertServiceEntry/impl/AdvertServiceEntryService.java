package services.AdvertServiceEntry.impl;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceContextException;
import org.globus.wsrf.impl.security.authorization.NoAuthorization;
import org.globus.wsrf.security.Constants;
import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import stubs.AdvertServiceEntryService.MetaDataType;
import stubs.AdvertServiceEntryService.SetMetaDataResponse;
import stubs.AdvertServiceEntryService.SetSerializedAdvertisableResponse;

public class AdvertServiceEntryService
{
    /* Added for logging */
    static final Log logger = LogFactory.getLog(AdvertServiceEntryService.class);
	
    public SetMetaDataResponse setMetaData(MetaDataType metaData) throws RemoteException
    {
	AdvertServiceEntryResource resource = getResource();
	resource.setMetaData(metaData);
	return new SetMetaDataResponse();
    }

    public SetSerializedAdvertisableResponse setSerializedAdvertisable(byte[] serializedAdvertisable) throws RemoteException
    {
	AdvertServiceEntryResource resource = getResource();
	resource.setSerializedAdvertisable(serializedAdvertisable);
	return new SetSerializedAdvertisableResponse();
    }

    /*
     * Private method that gets a reference to the resource specified in the
     * endpoint reference.
     */
    private AdvertServiceEntryResource getResource() throws RemoteException
    {
	Object resource = null;
	try
	    {
		resource = ResourceContext.getResourceContext().getResource();
	    }
	catch(NoSuchResourceException e)
	    {
		throw new RemoteException("Specified resource does not exist", e);
	    }
	catch(ResourceContextException e)
	    {
		throw new RemoteException("Error during resource lookup", e);
	    }
	catch(Exception e)
	    {
		throw new RemoteException("", e);
	    }
	
	AdvertServiceEntryResource entryResource = (AdvertServiceEntryResource) resource;
	return entryResource;
    }
}
