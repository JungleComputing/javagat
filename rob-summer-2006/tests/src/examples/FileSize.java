/**********************************************************
 *
 * @file: examples/FileSize.java
 *
 * @description:
 *   example number 3: simple example for GATFile object.
 *
 * Copyright (C) GridLab Project (http://www.gridlab.org/)
 *
 * Contributed by Marco de Reus   <mjfdreus@cs.vu.nl>.
 *
 **********************************************************/
/*** LICENSE ***/
/*******************************************************************************
 * This program demonstrates the usage of the GATFile object. It creates such an
 * object, queries for the size of the file, and destroys the object.
 ******************************************************************************/
package examples;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class FileSize {
    public static void main(String[] args) {
        // checking for right commandline invocation.
        if (args.length != 1) {
            //	    throw new IllegalArgumentException("Wrong number of arguments");
            System.out.println("\tUsage: run_gat_app examples.FileSize <file>");
        } else {
            // create the GATContext.
            GATContext context = new GATContext();

            // declaring variables.
            File file = null;
            URI src = null;

            // try to create a File object
            try {
                src = new URI(args[0]);
                file = GAT.createFile(context, src);
            } catch (Exception e) {
                System.err.println("error: " + e);
                e.printStackTrace();
            }

            // check for existance and print the length.
            try {
                if (!file.exists()) {
                    System.out.println(args[0] + ": unknown file");
                } else {
                    System.out.println("Length is: " + file.length());
                }
            } catch (Exception e) {
                System.err.println("error: " + e);
                e.printStackTrace();
            }
        }
    } // main()
}
