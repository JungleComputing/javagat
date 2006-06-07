package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class RemoteCopy {
    public static void main(String[] args) {
        URI src = null;
        URI dest = null;
        File file = null;

        System.err.println("-----REMOTE-FILE COPY TEST------------");

        GATContext context = new GATContext();
        Preferences prefs = new Preferences();

        //        prefs.put("File.adaptor.name", "dataMovement");
        try {
            src = new URI("any://skirit.ics.muni.cz//bin/echo");
            dest = new URI("any://fs0.das2.cs.vu.nl/hiha.dat");
            file = GAT.createFile(context, prefs, src);
        } catch (Exception e) {
            System.err.println("File creation failed: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        try {
            file.copy(dest);
            System.err.println("-----REMOTE-FILE COPY TEST-OK---------");
        } catch (Exception e) {
            System.err.println("Could not copy file:" + e);
            System.err.println("STACK TRACE:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
