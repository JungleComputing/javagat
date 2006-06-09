package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class Exists {
    public static void main(String[] args) throws Exception {
        URI src = null;
        File file = null;

        GATContext context = new GATContext();
        Preferences prefs = new Preferences();

            src = new URI(args[0]);
            file = GAT.createFile(context, prefs, src);
            System.err.println(file.exists() ? "exists" : "does not exist");
    }
}
