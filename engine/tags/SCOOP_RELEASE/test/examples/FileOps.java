/**********************************************************
 *
 * @file: examples/FileOps.java
 *
 * @description:
 *   example: complex example for GATFile object.
 *
 * Copyright (C) GridLab Project (http://www.gridlab.org/)
 *
 * Contributed by Marco de Reus   <mjfdreus@cs.vu.nl>.
 *
 **********************************************************/

/*** LICENSE ***/

/*******************************************************************************
 * This program demonstrates the usage of the GATFile object. It creates such an
 * object, and performs all types of operations on it: create file, copy,
 * delete, .
 ******************************************************************************/

package examples;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class FileOps {

    public static void main(String[] args) {

        // checking for right commandline invocation.
        if (args.length != 3) {
            System.out
                    .println("\tUsage: run_gat_app examples.FileOps <file1> <file2> <file3>\n\n"
                            + "\tprogram does:\n"
                            + "\t\tcp <file1> <file2>\n"
                            + "\t\trm <file1>\n" + "\t\tmv <file2> <file3>\n");
            System.exit(1);
        } else {

            // Create the GATContext.
            GATContext context = new GATContext();

            // Ddeclaring variables.
            File file1 = null;
            File file2 = null;

            URI src1 = null;
            URI src2 = null;
            URI src3 = null;

            try {
                // Create URI's for all files.
                src1 = new URI(args[0]);
                src2 = new URI(args[1]);
                src3 = new URI(args[2]);

                // create the first and second File object.
                file1 = GAT.createFile(context, src1);
                file2 = GAT.createFile(context, src2);

                System.out.println("Checking <file1> existance...");
                // check for existance, exit if not exist.
                if (!file1.exists()) {
                    System.out.println(src1 + ": unknown file");
                    System.out.println("getAbsolutePath():"
                            + file1.getAbsolutePath());
                    System.exit(1);
                }
                System.out.println("Copying <file1> to <file2>...");
                // copy <file1> <file2>
                file1.copy(src2);

                System.out.println("Removing <file1>...");
                // remove <file1>
                file1.delete();

                System.out.println("Moving <file2> to <file3>...");
                // move <file2> <file3>
                file2.move(src3);

            } catch (Exception e) {
                System.err.println("error: " + e);
                e.printStackTrace();
            }

        }
    }

}