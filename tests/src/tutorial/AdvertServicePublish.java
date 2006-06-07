package tutorial;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.io.Endpoint;
import org.gridlab.gat.io.File;

public class AdvertServicePublish {
    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();

        AdvertService advert = GAT.createAdvertService(context);

        // Create the object to be published.
        File file = GAT.createFile(context, "foo");

        // Create MetaData object and fill it with a key-value pair.
        MetaData meta = new MetaData();
        meta.put("name", "myTestFile");
        meta.put("purpose", "tutorial");

        // Publish the object to the advert service. 
        advert.add(file, meta, "/home/rob/tutorialFile");

        // Create an Endpoint
        Endpoint endpoint = GAT.createEndpoint(context);
        meta = new MetaData();
        meta.put("name", "myTestEndpoint");
        meta.put("purpose", "tutorial");

        // Publish the object to the advert service. 
        advert.add(endpoint, meta, "/home/rob/tutorialEndpoint");
    }
}
