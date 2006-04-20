/**********************************************************
 *
 * @file: examples/FileStreamSimple.java
 *
 * @description:
 *   example number 20: Simple example for the GAT FileOutputStream.
 *
 * Copyright (C) GridLab Project (http://www.gridlab.org/)
 *
 * Contributed by Marco de Reus   <mjfdreus@cs.vu.nl>.
 *
 **********************************************************/
/*** LICENSE ***/
/*******************************************************************************
 * This program demonstrates the usage of the GAT FileOutputStream. It creates
 * such an stream and the file as well, and write some data to it.
 ******************************************************************************/
package examples;

import java.io.PrintWriter;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileOutputStream;

public class FileStreamSimple {
    public static void main(String[] args) {
        // checking for right commandline invocation.
        if (args.length != 1) {
            System.out
                .println("\tUsage: run_gat_app examples.FileStreamSimple <file>\n"
                    + "\tprogram creates <file> and writes \n"
                    + "\t\"hello world\\n\" into it.\n");
            System.exit(1);
        } else {
            // create the GATContext.
            GATContext context = new GATContext();

            // declaring variables.
            URI src = null;
            FileOutputStream stream = null;
            PrintWriter p;

            try {
                // Create URI and FileOutputStream object.
                src = new URI(args[0]);
                stream = GAT.createFileOutputStream(context, null, src);

                // writing "hello world\n" to the stream.
                p = new PrintWriter(stream);

                String toBeStreamed = "hello world\n";
                p.println(toBeStreamed);
                p.close();

                // printing message.
                System.out.println("wrote " + toBeStreamed.length() + " bytes"
                    + "to " + args[0]);
            } catch (Exception e) {
                System.err.println("error: " + e);
                e.printStackTrace();
            }
        }
    }
}
