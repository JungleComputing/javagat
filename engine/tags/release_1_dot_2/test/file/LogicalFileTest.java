/*
 * Created on Dec 7, 2004
 */
package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.LogicalFile;

/**
 * @author rob
 */
public class LogicalFileTest {
    public static void main(String[] args) {

        // create the GATContext.
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("LogicalFile.adaptor.name", "replicaService");

        // declaring variables.
        LogicalFile logFile = null;
        URI logUri = null;

        try {
            // Create URI's for all files.
            logUri = new URI("gsiftp://fs0.das2.cs.vu.nl/aap");

            // Create the LogicalFile and File object.
            logFile = GAT.createLogicalFile(context, prefs, "myTestFile",
                LogicalFile.TRUNCATE);
            logFile.addURI(logUri);
        } catch (Exception e) {
            System.err.println("error: " + e);
            e.printStackTrace();
        }
    }
}
