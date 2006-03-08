/**********************************************************
 *
 * @file: examples/AdvertServiceSimple.java
 *
 * @description:
 *   example number 30: Simple example for the GAT AdvertService.
 *
 * Copyright (C) GridLab Project (http://www.gridlab.org/)
 *
 * Contributed by Marco de Reus   <mjfdreus@cs.vu.nl>.
 *
 **********************************************************/
  
/*** LICENSE ***/
  
/**********************************************************
 * This program demonstrates the usage of the GAT AdvertService.
 * It creates a service, and publishes a URI location with
 * some meta data to it.
 **********************************************************/

package examples;

import java.net.URI;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.io.File;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;

import org.gridlab.gat.io.Endpoint;

public class AdvertServiceSimple {
  
    public static void main(String[] args) {
 
	// create the GATContext.
	GATContext context = new GATContext();

	// declaring variables.
	URI src   = null;
	File file = null;
	AdvertService advert = null;
	MetaData meta = null;

	String key = "Message";
	String value = "Hello world";
	String uri = "file://localhost//tmp//hello_world";
	String path = "home/mjfdreus/examples/hello_world.adv";

	try {

	    /* This doesn't seem to work.
	       It returns with some "Not implemented" errors.
	    // Create AdvertService object.
	    advert = GAT.createAdvertService(context);

	    // Create the object to be published.
	    src  = new URI(uri);
	    file = GAT.createFile(context, src);

	    // Create MetaData object and fill it with a key-value pair.
	    meta = new MetaData();
	    meta.put(key, value);

	    // Publish the object to the advert service.
	    advert.add(file, meta, path);

	    */


	    // This does work!
	    // It's taken from test/advert/AdvertTest.java:
            Endpoint e = GAT.createEndpoint(context);
            AdvertService a = GAT.createAdvertService(context);
            MetaData m = new MetaData();
            m.put("name", "testEndpoint");
            a.add(e, m, "/rob/testadvert");
 
            Endpoint other = (Endpoint) a.getAdvertisable("/rob/testadvert");
 
            System.err.println("got endpoint back: " + other);

	} catch (Exception e) {
	    System.err.println("error: " + e);
	    e.printStackTrace();
	}
    }


}
