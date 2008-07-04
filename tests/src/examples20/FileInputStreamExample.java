package examples20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.io.FileInputStream;

public class FileInputStreamExample {

    /**
     * The JavaGAT FileInputStream can be used in combination with standard
     * java.io classes (like the BufferedInputStream).
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("\tUsage: bin/run_gat_app examples20.FileInputStreamExample <location> (where location is a valid JavaGAT URI)\n");
            System.exit(1);
        }
        new FileInputStreamExample().start(args[0]);
    }

    public void start(String location) {
        FileInputStream in = null;
        try {
            in = GAT.createFileInputStream(location);
        } catch (GATObjectCreationException e) {
            System.err.println("failed to create inputstream at location '"
                    + location + "': " + e);
            System.exit(1);
        }
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(reader);
        try {
            System.out.println("read: " + bufferedReader.readLine());
        } catch (IOException e) {
            System.err
                    .println("failed to read a line from inputstream at location '"
                            + location + "': " + e);
            System.exit(1);
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            System.err.println("failed to close inputstream at location '"
                    + location + "': " + e);
            System.exit(1);
        }
    }

}
