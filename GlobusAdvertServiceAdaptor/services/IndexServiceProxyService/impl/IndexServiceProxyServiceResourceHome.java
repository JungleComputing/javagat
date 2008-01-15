package services.IndexServiceProxyService.impl;

import org.globus.wsrf.Resource;
import org.globus.wsrf.impl.SingletonResourceHome;

public class IndexServiceProxyServiceResourceHome extends SingletonResourceHome
{
    public Resource findSingleton()
    {
	try
	    {
		// Create a resource and initialize it.
		IndexServiceProxyServiceResource resource = new IndexServiceProxyServiceResource();
		resource.initialize();
		return resource;
	    }
	catch(Exception e)
	    {
		return null;
	    }
    }
}
