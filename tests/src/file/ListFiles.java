package file;

import java.io.File;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

class ListFiles {
    public static void main(String[] args) {
        URI src = null;
        File file = null;

        System.err.println("-----REMOTE-FILE LIST TEST------------");

        GATContext context = new GATContext();
        Preferences prefs = new Preferences();

        if(args.length == 2) {
            prefs.put("File.adaptor.name", args[1]);
        }

        try {
            src = new URI(args[0]);
            file = GAT.createFile(context, prefs, src);
        } catch (Exception e) {
            System.err.println("File creation failed: " + e);
            System.exit(1);
        }

        try {
            File[] res = file.listFiles();

            for (int i = 0; i < res.length; i++) {
                System.err.println("    " + res[i].getName() + (res[i].isDirectory() ? " = dir " : " = file"));
            }

            System.err.println("-----REMOTE-FILE LIST TEST-OK---------");
        } catch (Exception e) {
            System.err.println("Could not list file:" + e);
            e.printStackTrace();
            GAT.end();
            System.exit(1);
        }
        
        GAT.end();
    }
}
