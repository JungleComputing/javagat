/**********************************************************
 * 
 * @file: examples/PipeRead.java
 * 
 * @description: 
 *   example number #: How to use GAT EndPoint and Pipe.
 *                     Part 1: The reading.
 *   
 * Copyright (C) GridLab Project (http://www.gridlab.org/)
 * 
 * Contributed by Marco de Reus   <mjfdreus@cs.vu.nl>.
 * 
 **********************************************************/

/*** LICENSE ***/

/*******************************************************************************
 * This programs creates an GAT Endpoint, advertises it to the advert service,
 * and listens for incomming connections / messages.
 ******************************************************************************/

package examples;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.io.Endpoint;
import org.gridlab.gat.io.Pipe;

public class PipeRead {

    public static void main(String[] args) {

        // Create the GAT Context.
        GATContext context = new GATContext();

        try {
            System.out.println("PR: PipeRead started...");

            // Create an Endpoint and AdvertService
            // and add it to the AdvertService.
            Endpoint end = GAT.createEndpoint(context);
            AdvertService advert = GAT.createAdvertService(context);
            MetaData meta = new MetaData();
            meta.put("name", "myEndpoint");
            advert.add(end, meta, "/home/mjfdreus/endpoint");

            // To listen to incoming connections, get a Pipe from the Endpoint.
            // Because this program is the creator of the Endpoint, use
            // 'listen'.
            // In contrast to PipeWrite.java, which uses 'connect'.
            Pipe pipe = end.listen(); // Blocking call!
            System.out.println("PR: Connection made to Endpoint.");

            // A BufferedReader is used to read the Pipe's InputStream.
            InputStream instream = pipe.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    instream));

            // Reading the message(s).
            // The last message will be 'null', so reading is done
            // till that specific message is read.
            String szRead;
            do {
                szRead = reader.readLine();
                System.out.println("PR: " + szRead);
            } while (szRead != null);

            // Delete the Endpoint from the AdvertService, as the AdvertService
            // is persistant.
            advert.delete("/home/mjfdreus/endpoint");

            // Done reading, so close the stream.
            reader.close();

        } catch (Exception e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        }
    }
}