package org.gridlab.gat.resources.cpi.prestage;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.JobDescription;

public class PreStageWrapper {
    public static void main(String[] args) {
        System.err.println("PreStageWrapper started");
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();

        JobDescription description = null;
        try {
            System.err.println("opening descriptor file: " + args[0]);
            FileInputStream tmp = new FileInputStream(args[0]);
            ObjectInputStream in = new ObjectInputStream(tmp);
            description = (JobDescription) in.readObject();
            in.close();
        } catch (Exception e) {
            System.err.println("an error occurred: " + e);
            System.exit(1);
        }

        System.err.println("job description: " + description);
        
        /*
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
        */
    }
}
