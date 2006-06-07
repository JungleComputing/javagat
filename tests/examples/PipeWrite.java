/**********************************************************
 *
 * @file: examples/PipeWrite.java
 *
 * @description:
 *   example number #: How to use GAT EndPoint and Pipe.
 *                     Part 2: The writing.
 *
 * Copyright (C) GridLab Project (http://www.gridlab.org/)
 *
 * Contributed by Marco de Reus   <mjfdreus@cs.vu.nl>.
 *
 **********************************************************/
/*** LICENSE ***/
/*******************************************************************************
 * This programs gets an GAT Endpoint out of the advert service, connects to it,
 * and writes a message to the obtained GAT Pipe.
 ******************************************************************************/
package examples;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.io.Endpoint;
import org.gridlab.gat.io.Pipe;

public class PipeWrite {
    public static void main(String[] args) {
        // Create the GAT Context.
        GATContext context = new GATContext();

        try {
            System.out.println("PW: PipeWrite started...");

            // Create an Endpoint and the AdvertService.
            Endpoint end = GAT.createEndpoint(context);
            AdvertService advert = GAT.createAdvertService(context);

            // Create the data to search the Endpoint in the AdvertService.
            MetaData meta = new MetaData();
            meta.put("name", "myEndpoint");

            // Find the data in the AdvertService. If a matching one is found,
            // it is added to szPaths.
            String[] szPaths;
            szPaths = advert.find(meta);

            // Just to check (as there should only be one path), print the
            // number
            // of found paths and print them all.
            System.out.println("PW: Founded Paths = " + szPaths.length);

            for (int i = 0; i < szPaths.length; i++) {
                System.out.println("PW: " + szPaths[i]);
            }

            // Get the first (and only) path from the AdverService and print it.
            // That path should be "/home/mjfdreus/endpoint" (see
            // PipeRead.java).
            end = (Endpoint) advert.getAdvertisable(szPaths[0]);
            System.err.println("PW: Got endpoint back: " + end);

            // As 'Endpoints obtained from the AdvertService cannot be listened
            // to' (see GatDocs, Endpoint), use 'connect' instead of 'listen'.
            Pipe pipe = end.connect();
            System.out.println("PW: Connection made to Endpoint.");

            // A PrintWriter is used to write to the Pipe's OutputStream.
            OutputStream outstream = pipe.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                outstream));

            // Writing the message(s).
            // In this small example, the writer.flush() is not really needed,
            // as writer.close() also does a flush. It's just added to give a
            // complete picture.
            String szWrite = null;
            szWrite = "Hello GAT Endpoint...";
            writer.println(szWrite);
            writer.flush();
            System.out.println("PW: " + szWrite);
            szWrite = "... how is life on the other side?";
            writer.println(szWrite);
            writer.flush();
            System.out.println("PW: " + szWrite);

            // Done writing, so close the stream.
            writer.close();
        } catch (Exception e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        }
    }
}
