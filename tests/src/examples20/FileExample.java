package examples20;

import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class FileExample {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out
                    .println("\tUsage: bin/run_gat_app examples20.FileExample <location1> <location2> <location3> (where location is a valid JavaGAT URI)\n\n"
                            + "\tprogram does:\n"
                            + "\t\tcp <location1> <location2>\n"
                            + "\t\trm <location1>\n"
                            + "\t\tmv <location2> <location3>\n");
            System.exit(1);
        }
        try {
            new FileExample().start(new URI(args[0]), new URI(args[1]),
                    new URI(args[2]));
        } catch (URISyntaxException e) {
            System.out.println(e);
            System.out
                    .println("\tUsage: bin/run_gat_app examples20.FileExample <location1> <location2> <location3> (where location is a valid JavaGAT URI)\n\n"
                            + "\tprogram does:\n"
                            + "\t\tcp <location1> <location2>\n"
                            + "\t\trm <location1>\n"
                            + "\t\tmv <location2> <location3>\n");
            System.exit(1);
        }
    }

    public void start(URI uri1, URI uri2, URI uri3) {
        File file1 = null;
        try {
            file1 = GAT.createFile(uri1);
        } catch (GATObjectCreationException e) {
            System.err.println("failed to create file1 at location '" + uri1
                    + "': " + e);
            System.exit(1);
        }
        try {
            file1.copy(uri2);
            System.out.println("file1 at location '" + uri1
                    + "' copied to file2 at location '" + uri2 + "'");
        } catch (GATInvocationException e) {
            System.err.println("failed to copy file1 at location '" + uri1
                    + "' to file2 at location '" + uri2 + "': " + e);
            System.exit(1);
        }
        file1.delete();
        System.out.println("file1 at location '" + uri1 + "' deleted");
        File file2 = null;
        try {
            file2 = GAT.createFile(uri2);
        } catch (GATObjectCreationException e) {
            System.err.println("failed to create file2 at location '" + uri2
                    + "': " + e);
            System.exit(1);
        }
        try {
            file2.move(uri3);
            System.out.println("file2 at location '" + uri2
                    + "' moved to file3 at location '" + uri3 + "'");
        } catch (GATInvocationException e) {
            System.err.println("failed to move file2 at location '" + uri2
                    + "' to file3 at location '" + uri3 + "': " + e);
            System.exit(1);
        }
    }
}
