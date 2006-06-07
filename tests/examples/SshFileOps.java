package examples;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class SshFileOps {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out
                .println("\tUsage: run_gat_app examples.SshFileOps <file1> <file2> <file3> <identityFile>\n\n"
                    + "\tprogram does:\n"
                    + "\t\tcp <file1> <file2>\n"
                    + "\t\trm <file1>\n" + "\t\tmv <file2> <file3>\n");
        } else {
            GATContext context = new GATContext();
            Preferences preferences = new Preferences();

            File file1 = null;
            File file2 = null;

            URI src1 = null;
            URI src2 = null;
            URI src3 = null;

            try {
                preferences.put("defaultIdentityFile", args[3]);

                src1 = new URI(args[0]);
                src2 = new URI(args[1]);
                src3 = new URI(args[2]);

                file1 = GAT.createFile(context, preferences, src1);
                file2 = GAT.createFile(context, preferences, src2);

                System.out.println("Checking <file1> existance ...");

                if (!file1.exists()) {
                    System.out.println(src1 + ": unknown file");
                    System.out.println("getAbsolutePath():"
                        + file1.getAbsolutePath());
                    System.exit(1);
                }

                System.out.println("Copying <file1> to <file2> ...");
                file1.copy(src2);

                System.out.println("Removing <file1> ...");
                file1.delete();

                System.out.println("Moving <file2> to <file3>");
                file2.move(src3);
            } catch (Exception e) {
                System.err.println("error: " + e);
                e.printStackTrace();
            }
        }
    }
}
