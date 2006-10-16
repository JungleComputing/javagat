package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

class RandomAccessFile {
    public static void main(String[] args) {
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();

        try {
            URI src = new URI("file:////bin/echo");
            org.gridlab.gat.io.RandomAccessFile rf = GAT.createRandomAccessFile(context, prefs, src, "r");
            System.err.println("len = " + rf.length());
            System.err.println("-----REMOTE-FILE COPY TEST-OK---------");
        } catch (Exception e) {
            System.err.println("STACK TRACE:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
