package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class Copy2 {
	/*
	 * problem encountered with invalid proxy in /tmp/x509... and valid proxy
	 * using environment variable $X509...
	 */
    public static void main(String[] args) {
        GATContext context = new GATContext();
        if (args.length < 1) {
        	args = new String[]{"rftgt4"};
        }
        context.addPreference("File.adaptor.name", args[0]);
        System.err.println("------------FILE COPY TEST------------");

        URI src = null;
        URI dest = null;
        File file = null;
        File file2 = null;

        try {
            src = new URI(
                "any://fs0.das3.cs.vu.nl//home0/rkemp/script.sh");
            dest = new URI(
                "any://fs1.das3.liacs.nl//home0/rkemp/script_copy.sh");
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
        try {
        	Thread.sleep(5);
        	file2 = GAT.createFile(context, dest);
        	System.out.println(file2.delete());
        } catch (Exception e) {
        	e.printStackTrace();
		}
    }
}
