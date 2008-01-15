package services.IndexServiceProxyService.impl;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.globus.wsrf.Constants;
import org.globus.wsrf.ResourceContext;

public class IndexServiceProxyServiceConfiguration
{
    private String indexURI;
    
    public String getIndexURI()
    {
	return this.indexURI;
    }
    
    public void setIndexURI(String indexURI)
    {
	this.indexURI = indexURI;
    }
    
    static final IndexServiceProxyServiceConfiguration getConfObject() throws Exception
    {
	IndexServiceProxyServiceConfiguration conf;
	ResourceContext ctx = ResourceContext.getResourceContext();
	String confLoc = Constants.JNDI_SERVICES_BASE_NAME + ctx.getService() + "/configuration";

	try
	    {
		Context initialContext = new InitialContext();
		conf = (IndexServiceProxyServiceConfiguration)initialContext.lookup(confLoc);
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
