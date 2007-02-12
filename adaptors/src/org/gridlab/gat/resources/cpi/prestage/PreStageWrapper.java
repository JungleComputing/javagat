package org.gridlab.gat.resources.cpi.prestage;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class PreStageWrapper {
    public static void main(String[] args) {
        System.err.println("PreStageWrapper started");
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();

        for (int i = 0; i < args.length; i += 2) {
            String srcName = args[i];
            String destName = args[i + 1];

            System.err.println("PreStageWrapper copy: " + srcName + " to " + destName);

            try {
                URI src = new URI(srcName);
                URI dest = new URI(destName);

                File f = GAT.createFile(context, prefs, src);
                f.copy(dest);
            } catch (Exception e) {
                System.err.println("PreStageWrapper: could not copy file:");
                System.err.println("  src:  " + srcName);
                System.err.println("  dest: " + destName);
                System.err.println("  error: " + e);
                System.exit(1);
            }
            System.err.println("PreStageWrapper done");
        }
    }
}
