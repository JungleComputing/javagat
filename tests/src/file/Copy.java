package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class Copy {
    public static void main(String[] args) {
        GATContext context = new GATContext();
        //context.addPreference("file.create", "true");
        context.addPreference("File.adaptor.name", "gridftp");
        context.addPreference("gridftp.chmod", args[0]);
        System.err.println("------------FILE COPY TEST------------");

        URI src = null;
        URI dest = null;
        File file = null;

        try {
            src = new URI(
                "file://fs0.das3.cs.vu.nl/test");
            dest = new URI(
                "gsiftp://fs2.das3.science.uva.nl//home0/rkemp/dest");
            file = GAT.createFile(context, src);
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
            System.err.println("------------FILE COPY TEST OK---------");
        } catch (Exception e) {
            System.err.println("Could not copy file: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
