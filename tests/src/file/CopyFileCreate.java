package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class CopyFileCreate {
    public static void main(String[] args) {
        GATContext context = new GATContext();
        context.addPreference("File.adaptor.name", args[0]);
        context.addPreference("file.create", args[1]);

        System.err.println("------------FILE COPY TEST------------");

        String[] sources = new String[] { "local/src/file", "local/src/dir",
                "any://fs2.das3.science.uva.nl/remote/src/file",
                "any://fs2.das3.science.uva.nl/remote/src/dir" };
        String[] destinations = new String[] {
                "local/dest/?/existing/otherfile",
                "local/dest/?/existing/otherdir",
                "any://fs2.das3.science.uva.nl/remote/dest/?/existing/otherfile",
                "any://fs2.das3.science.uva.nl/remote/dest/?/existing/otherdir",
                "local/dest/?/nonexisting/dir1/to/file",
                "local/dest/?/nonexisting/dir2/to/dir/",
                "any://fs2.das3.science.uva.nl/remote/dest/?/nonexisting/dir1/to/file",
                "any://fs2.das3.science.uva.nl/remote/dest/?/nonexisting/dir2/to/dir/" };
        for (int i = 0; i < sources.length; i++) {
            for (String destination : destinations) {
                destination = destination.replace('?', ("" + i).charAt(0));
                URI src = null;
                URI dest = null;
                File file = null;
                try {
                    src = new URI(sources[i]);
                    dest = new URI(destination);
                    file = GAT.createFile(context, src);
                } catch (Exception e) {
                    System.err.println(e);
                    break;
                }
                try {
                    System.out.print(sources[i] + "->" + destination + ": ");
                    file.copy(dest);
                    System.out.println("SUCCESS");
                } catch (Exception e) {
                    System.out.println("FAILURE " + e);
                    if (args[1].equals("debug")) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.err.println("------------FILE COPY TEST OK---------");
        GAT.end();
    }
}
