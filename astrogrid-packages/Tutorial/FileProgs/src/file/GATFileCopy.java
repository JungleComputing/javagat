package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class GATFileCopy {
    public static void main(String[] args) {
        
        if ( args.length != 2 ) {
            System.out.println("\tUsage: GATFileCopy <source> <dest>\n");
            System.exit(1);
        } else {
            
            // create GATContext
            GATContext context = new GATContext();

            // the files...
            
            File srcFile  = null;
            
            URI srcURI  = null;
            URI destURI = null;

            // create URIs for the files anf GATFiles...
            
            try {
                
                srcURI  = new URI(args[0]);
                destURI = new URI(args[1]);

                srcFile  = GAT.createFile(context,srcURI);

                System.out.println("Checking existence of '" + args[0] + "'... ");
                if (!srcFile.exists()) {
                    System.out.println(args[0] + " ... unknown file");
                    System.out.println("getAbsolutePath():" + srcFile.getAbsolutePath());
                    GAT.end();
                    System.exit(1);
                }

                System.out.println("Copying " + args[0] + " to " + args[1] + " ...");
                
                srcFile.copy(destURI);
            } catch (Exception e) {
                System.err.println("error: " + e);
                e.printStackTrace();
            }

            GAT.end();
            System.exit(0);
        }
    }
}
                