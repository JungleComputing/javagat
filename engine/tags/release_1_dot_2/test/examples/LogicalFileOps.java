/**********************************************************
 *
 * @file: examples/LogicalFileOps.java
 *
 * @description:
 *   example #: how to use GAT LogicalFile interfacing
 *   to replica catalogs and its main methods.
 *
 * Copyright (C) GridLab Project (http://www.gridlab.org/)
 *
 * Contributed by Marco de Reus   <mjfdreus@cs.vu.nl>.
 *
 **********************************************************/

/*** LICENSE ***/

/*******************************************************************************
 * This program shows how to create a logical file, to associate multiple
 * physical files with it, to query it for associated files, to replicate files
 * and to delete it.
 ******************************************************************************/

package examples;

import java.util.List;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.LogicalFile;

public class LogicalFileOps {

    public static void main(String[] args) {

        // Checking for right commandline invocation.
        // Check is <3 because there can be multiple src's.
        if (args.length < 3) {
            System.out.println("\tUsage: run_gat_app examples.LogicalFileOps "
                + "<logical_file> <src(s)> <target>\n\n"
                + "\tprogram creates <logical_file> in replica catalog "
                + "and associates <src(s)> with it. It then\n"
                + "\tlists all known physical files, replicates to "
                + "<target> and deletes the whole thing.\n");
            System.exit(1);
        } else {

            // create the GATContext.
            GATContext context = new GATContext();

            // declaring variables.
            LogicalFile logFile = null;
            try {
                // Declaring the src-file in this 'try', so it will be destroyed
                // when this block goes out of scope.
                File srcFile = null;
                URI srcUri = null;

                // Create logical file object.
                logFile = GAT.createLogicalFile(context, args[0],
                    LogicalFile.TRUNCATE);

                // Create and add the possible multiple File object to
                // LogicalFile.
                // 'arg.length-2' to 'remove' <log_file> and <target> from
                // commandline.
                for (int i = 1; i <= args.length - 2; i++) {
                    srcUri = new URI(args[i]);
                    srcFile = GAT.createFile(context, srcUri);

                    // Associates the two files
                    logFile.addFile(srcFile);
                }

            } catch (Exception e) {
                System.err.println("error: " + e);
                e.printStackTrace();
            }

            try {
                // Getting all Files associated with a LogicalFile.
                List listFiles = logFile.getFiles();

                // Printing the List.
                int nSize = listFiles.size();
                System.out.println("size: " + nSize);

                for (int i = 0; i < nSize; i++) {
                    File tmpFile = (File) listFiles.get(i);
                    System.out.println("element " + i + ": "
                        + tmpFile.getPath());
                }
            } catch (Exception e) {
                System.err.println("error: " + e);
                e.printStackTrace();
            }

            try {
                // Create a replica at <target> of the LogicalFile.
                // <target> is the last parameter, hence the 'args.length-1'.
                URI tarURI = new URI(args[args.length - 1]);
                logFile.replicate(tarURI);

                // Delete the first <src> from the LogicalFile.
                URI srcURI = new URI(args[1]);
                logFile.removeURI(srcURI);

                // Printing all the Files again.
                List listFiles = logFile.getFiles();
                int nSize = listFiles.size();
                System.out.println("size: " + nSize);

                for (int i = 0; i < nSize; i++) {
                    File tmpFile = (File) listFiles.get(i);
                    System.out.println("element " + i + ": "
                        + tmpFile.getPath());
                }
            } catch (Exception e) {
                System.err.println("error: " + e);
                e.printStackTrace();
            }
        } // else
    } // main()

}
