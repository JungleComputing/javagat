/**********************************************************
 *
 * @file: examples/LogicalFileSimple.java
 *
 * @description:
 *   example #40: how to use GAT Logical File interfacing to replica
 *   catalogs
 *
 * Copyright (C) GridLab Project (http://www.gridlab.org/)
 *
 * Contributed by Marco de Reus   <mjfdreus@cs.vu.nl>.
 *
 **********************************************************/
  
/*** LICENSE ***/
  
/**********************************************************
 * This program shows how to create a logical file, and to
 * associate a physical file with it.
 **********************************************************/


package examples;
 
import java.net.URI;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.io.LogicalFile;
import org.gridlab.gat.io.File;

public class LogicalFileSimple {
  
    public static void main(String[] args) {
 
        // Checking for right commandline invocation.
        if (args.length != 2) {
            System.out.println("\tUsage: run_gat_app examples.LogicalFileSimple " +
			       "<logical_file> <physical_file>\n\n" +
                               "\tprogram creates <logical_file> in replica catalog " +
                               "and associates <physical_file> with it.\n");
	    System.exit(1);
 	}
	else {

	    // create the GATContext.
	    GATContext context = new GATContext();

            // declaring variables.
	    LogicalFile logFile = null;
	    File phyFile = null;
            URI logUri = null;
            URI phyUri = null;

            try {
		// Create URI's for all files.
		logUri = new URI(args[0]);
		phyUri = new URI(args[1]);

		// Create the LogicalFile and File object.
		logFile = GAT.createLogicalFile(context, logUri);
		phyFile = GAT.createFile(context, phyUri);

		// Associates the two files
		logFile.addFile(phyFile);

	    } catch (Exception e) {
		System.err.println("error: " + e);
                e.printStackTrace();
	    }
  	}
    }

}
