package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class SshCaching {

    public static void main(String[] args) {
        URI src = null;
        URI src2 = null;
        File file = null;
        File file2 = null;
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("File.adaptor.name", "ssh");
        if (args.length > 3) prefs.put("caching", "true");
        context.addPreferences(prefs);
        try {
            src = new URI(args[0]);
            src2 = new URI(args[1]);
        } catch (Exception e) {
            System.err.println("exception: " + e);
        }
        try {
            file = GAT.createFile(context, src);
            file2 = GAT.createFile(context, src2);
        } catch (Exception e) {
            System.err.println("create");
            System.err.println("exception: " + e);
            e.printStackTrace();
        }
        File testFile;
        long start = System.currentTimeMillis();
        for (int i = 0; i < Integer.parseInt(args[2]); i++) {
            testFile = (i%2==0) ?  file: file2;  
            try {
                testFile.getFileInterface().exists();
            } catch (Exception e) {
                System.err.println("exception: " + e);
                e.printStackTrace();
            }
            try {
                testFile.getFileInterface().length();
            } catch (Exception e) {
                System.err.println("exception: " + e);
            }
        }
        long stop = System.currentTimeMillis();
        System.err.println("total time: " + (stop - start) / 1000.0);
        GAT.end();
    }
}
