package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class CopyRemoteDir {

    public static void main(String[] args) {

        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        //		prefs.put("File.adaptor.name", "gridftp");

        System.err.println("------------REMOTE DIR COPY TEST------------");

        URI src = null;
        URI dest = null;
        File file = null;

        try {
            src = new URI("any://fs0.das2.cs.vu.nl//home/rob/amolf");
            dest = new URI("file:///testDir");
            file = GAT.createFile(context, prefs, src);
        } catch (Exception e) {
            System.err.println("File creation failed: " + e);
            System.exit(1);
        }

        if (file == null) {
            System.err.println("Could not create a file object.");
            System.exit(1);
        }

        try {
            file.copy(dest);
            System.err.println("------------REMOTE DIR COPY TEST OK---------");

        } catch (Exception e) {
            System.err.println("Could not copy file: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        GAT.end();
    }
}
