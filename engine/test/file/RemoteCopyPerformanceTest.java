package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class RemoteCopyPerformanceTest {
    public static void main(String[] args) {
        URI src = null;
        URI dest = null;
        File file = null;

        GATContext context = new GATContext();
        Preferences prefs = new Preferences();

        try {
            src = new URI(args[0]);
            dest = new URI(args[1]);
            file = GAT.createFile(context, prefs, src);
        } catch (Exception e) {
            System.err.println("File creation failed: " + e);
            e.printStackTrace();
            GAT.end();
            System.exit(1);
        }

        
        try {
            long size = file.length();
            long start = System.currentTimeMillis();
            System.err.print("Copying file...");
            file.copy(dest);
            System.err.println("DONE");
            long time = System.currentTimeMillis() - start;
            double kb = size / 1024.0;
            double secs = time / 1000.0;
            double speed = kb / secs;    
            System.err.println("copied " + kb + " KBytes in " + secs + " seconds: " + speed + " KByte/s");
        } catch (Exception e) {
            System.err.println("Could not copy file:" + e);
            System.err.println("STACK TRACE:");
            e.printStackTrace();
            GAT.end();
            System.exit(1);
        }

        GAT.end();
    }
}
