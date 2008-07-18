package file;

import java.io.PrintWriter;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.Preferences;

public class GATFileOutputStream {
    public static void main(String[] args) {

        PrintWriter pwriter=null;
        
        if ( args.length < 1 ) {
            System.out.println("\tUsage: GATFileOutputStream <source> <adaptor>\n");
            System.exit(1);
        } else {
            
            // create GATContext
            GATContext context = new GATContext();


            // add the adaptor name to the prefs; the second argument is dealed as adaptor to be invoked...

            if ( args.length == 2 ) {
                Preferences gatprefs = new Preferences();
                gatprefs.put("FileOutputStream.adaptor.name", args[1]);
                context.addPreferences(gatprefs);
            }
                

            // the files...
            
            File srcFile  = null;
            
            URI srcURI  = null;

            // create URIs for the files and GATFiles...
            
            try {
                
                srcURI  = new URI(args[0]);

                FileOutputStream fptr = GAT.createFileOutputStream(context, srcURI);

                pwriter = new PrintWriter(fptr);
                pwriter.println("Hi Folks");
                pwriter.println("welcome to the JavaGAT tutorial");
                pwriter.close();

            } catch (Exception e) {
                System.err.println("error: " + e);
                e.printStackTrace();
            }

            GAT.end();
            System.exit(0);
        }
    }
}
                