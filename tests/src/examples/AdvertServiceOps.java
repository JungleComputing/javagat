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
 * service, publishes a URI location with some meta data to it, searches for it,
 * and deletes the entry.
 ******************************************************************************/
package examples;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.io.Endpoint;
import org.gridlab.gat.io.File;

public class AdvertServiceOps {
    public static void main(String[] args) {
        // create the GATContext.
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();

        //prefs.put("adaptors.local", "true");
        prefs.put("AdvertService.adaptor.name", "local");

        // declaring variables.
        URI src = null;
        File file = null;
        AdvertService advert = null;
        MetaData meta = null;

        String key = "Message";
        String value = "Hello world";
        String uri = "file:////tmp/hello_world";
        String path = "/testadvert/example_file";

        try {
            // Create AdvertService object.
            advert = GAT.createAdvertService(context, prefs);

            // Create the object to be published.
            src = new URI(uri);
            file = GAT.createFile(context, prefs, src);

            // Create MetaData object and fill it with a key-value pair.
            meta = new MetaData();
            meta.put(key, value);

            // Publish the object to the advert service.
            advert.add(file, meta, path);

            File res = (File) advert
                .getAdvertisable("/testadvert/example_file");
            System.err.println("got file back: " + res);

            // It's taken from test/advert/AdvertTest.java:
            Endpoint e = GAT.createEndpoint(context, prefs);
            MetaData m = new MetaData();
            m.put("name", "testEndpoint");
            advert.add(e, m, "/testadvert/example_endpoint");

            // Searching
            MetaData metaSearch = new MetaData();
            metaSearch.put("name", "testEndpoint");

            //	    a.setPWD("/rob");
            String[] szPaths;
            szPaths = advert.find(metaSearch);

            println("szPaths.length = " + szPaths.length);

            for (int i = 0; i < szPaths.length; i++) {
                println(szPaths[i]);
            }
        } catch (Throwable e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        }
    }

    public static void println(String szPrint) {
        System.out.println(szPrint);
    }
}
