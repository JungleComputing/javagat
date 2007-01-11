/*
bin/run_gat_app advert.AdvertTest2 -add /dies/ist/ein/Pfad Name.des.File
bin/run_gat_app advert.AdvertTest2 -add /dies/ist/ein/weiterer/Pfad Name.des.File
bin/run_gat_app advert.AdvertTest2 -add /dies/ist/ein/weiterer/neuer/Pfad Name.des.File
bin/run_gat_app advert.AdvertTest2 -get /dies/ist/ein/Pfad
bin/run_gat_app advert.AdvertTest2 -get /dies/ist/ein/weiterer/Pfad
bin/run_gat_app advert.AdvertTest2 -get /dies/ist/ein/weiterer/neuer/Pfad
bin/run_gat_app advert.AdvertTest2 -find Name.des.File
bin/run_gat_app -Dgat.debug advert.AdvertTest2 -delete /dies/ist/ein/Pfad
*/
package advert;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.io.Endpoint;
import org.gridlab.gat.io.File;

public class AdvertTest2
{
    public static void main(String[] args)
    {
	try
	    {
		GATContext context = new GATContext();
		Preferences prefs = new Preferences();
		prefs.put("AdvertService.adaptor.name", "GlobusAdvertServiceAdaptor");
		//		prefs.put("AdvertService.globus.uri", "http://127.0.0.2:8443/wsrf/services/GAT/IndexServiceProxyService");
		prefs.put("AdvertService.globus.uri", "https://134.2.217.150:8443/wsrf/services/GAT/IndexServiceProxyService");
		//prefs.put("AdvertService.adaptor.name", "LocalAdvertServiceAdaptor");
		//prefs.put("adaptors.local", "true");
		
		AdvertService advertService = GAT.createAdvertService(context, prefs);
		
		if(args[0].equals("-add"))
		    {
			String path = args[1];
			String metaDataValue = args[2];
			MetaData metaData = new MetaData();
			metaData.put("name", metaDataValue);
			metaData.put("version-minor", "0815");
			metaData.put("version-major", "4711");
			//Endpoint gridObject = GAT.createEndpoint(context);
			File gridObject = GAT.createFile(context, "/tmp/DefaultIndexService.out");
			
			advertService.add(gridObject, metaData, path);
		    }
		else if(args[0].equals("-get"))
		    {
			String path = args[1];
			//Endpoint other = (Endpoint) advertService.getAdvertisable(path);
			Object other = advertService.getAdvertisable(path);
			MetaData metaData = advertService.getMetaData(path);
			System.out.println("Path: " + path);
			System.out.println("MetaData: ");
			if(metaData != null)
			    {
				int size = metaData.size();
				for(int i=0;i<size;i++)
				    {
					String key = metaData.getKey(i);
					String value = metaData.getData(i);
					System.out.println("[key = " + key + ", value = " + value + "]");
				    }
			    }
			System.out.println("other: " + other);
		    }
		else if(args[0].equals("-delete"))
		    {
			String path = args[1];
			advertService.delete(path);
		    }
		else if(args[0].equals("-find"))
		    {
			String metaDataValue = args[1];
			MetaData metaData = new MetaData();
			metaData.put("name", metaDataValue);
			String[] result = advertService.find(metaData);
			System.out.println("Paths:");
			for(int i=0;i<result.length;i++)
			    System.out.println(result[i]);
		    }
		else
		    {
			System.err.println("ERROR: Incorrect parameters");
			System.exit(1);
		    }
	    }
	catch(Exception e)
	    {
		System.err.println("error: " + e);
		e.printStackTrace();
	    }

	GAT.end();
    }
}
