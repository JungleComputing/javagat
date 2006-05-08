package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInputStream;

class RemoteStreamPerformanceTest {
    public static void main(String[] args) {
        URI src = null;
        FileInputStream fin = null;
        
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();

        if (args.length == 2) {
            prefs.put("File.adaptor.name", args[1]);
        }

        byte[] buf = new byte[1024*1024];
        
        for (int i = 0; i < 5; i++) {
            try {
                src = new URI(args[0]);
                long start = System.currentTimeMillis();
                fin = GAT.createFileInputStream(context, prefs, src);

                System.err.print("Reading file...");

                int read = 0;
                while(true) {
//                    System.err.print("Reading block...");
                    int res = fin.read(buf);
                    if(res < 0) {
//                        System.err.println("DONE, EOF");
                        break;
                    } else {
//                        System.err.println("DONE, read " + (res/1024) + " KBytes");
                        read += res;
                    }
                }

                fin.close();
                
                long time = System.currentTimeMillis() - start;
                System.err.println("DONE, read " + (read/1024) + " KBytes");
                double kb = read / 1024.0;
                double secs = time / 1000.0;
                double speed = kb / secs;
                System.err.println("read " + kb + " KBytes in " + secs
                    + " seconds: " + speed + " KByte/s");
            } catch (Exception e) {
                System.err.println("Could not copy file:" + e);
                System.err.println("STACK TRACE:");
                e.printStackTrace();
                GAT.end();
                System.exit(1);
            }
        }

        GAT.end();
    }
}
