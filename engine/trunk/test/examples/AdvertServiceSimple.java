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
/*******************************************************************************
 * This program demonstrates the usage of the GAT AdvertService. It creates a
 * service, and publishes a URI location with some meta data to it.
 ******************************************************************************/
package examples;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.io.Endpoint;
import org.gridlab.gat.io.File;

public class AdvertServiceSimple {
    public static void main(String[] args) {
        // create the GATContext.
        GATContext context = new GATContext();

        try {
            // Create AdvertService object. 
            AdvertService advert = GAT.createAdvertService(context);

            // Create the object to be published.
            File file = GAT.createFile(context, "foo");

            // Create MetaData object and fill it with a key-value pair.
            MetaData meta = new MetaData();
            meta.put("dataFile", "myTestApplication");

            // Publish the object to the advert service. 
            advert.add(file, meta, "/home/rob/applications/test");

            File file2 = (File) advert
                .getAdvertisable("/home/rob/applications/test");

            System.err.println("got file back: " + file2);

            // now publish an Endpoint
            Endpoint e = GAT.createEndpoint(context);
            MetaData m = new MetaData();
            m.put("name", "testEndpoint");
            advert.add(e, m, "/home/rob/testadvert");

            Endpoint other = (Endpoint) advert
                .getAdvertisable("/home/rob/testadvert");

            System.err.println("got endpoint back: " + other);
        } catch (Exception e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        }
    }
}
