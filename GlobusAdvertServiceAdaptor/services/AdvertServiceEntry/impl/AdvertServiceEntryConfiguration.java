package services.AdvertServiceEntry.impl;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.globus.wsrf.Constants;
import org.globus.wsrf.ResourceContext;

public class AdvertServiceEntryConfiguration
{
    private String instanceServicePath;
    
    public String getInstanceServicePath()
    {
	return instanceServicePath;
    }
    
    public void setInstanceServicePath(String instanceServicePath)
    {
	this.instanceServicePath = instanceServicePath;
    }
    
    static final AdvertServiceEntryConfiguration getConfObject() throws Exception
    {
	AdvertServiceEntryConfiguration conf;
	ResourceContext ctx = ResourceContext.getResourceContext();
	String confLoc = Constants.JNDI_SERVICES_BASE_NAME + ctx.getService() + "/configuration";

	try
	    {
		Context initialContext = new InitialContext();
		conf = (AdvertServiceEntryConfiguration)initialContext.lookup(confLoc);
	    }
	catch(NameNotFoundException e)
	    {
		throw new Exception("Unable to find configuration object", e);
	    }
	catch(NamingException e)
	    {
		throw new Exception(e);
	    }
	
	return conf;
    }
}
