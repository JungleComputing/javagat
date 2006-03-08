package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.io.FileOutputStream;

import java.io.PrintWriter;

class StreamTest {
    public static void main(String[] args) {
        GATContext context = new GATContext();
        System.err.println("------------FILE STREAM TEST------------");

        URI loc = null;

        try {
            loc = new URI("test/file/test_stream.dat");
        } catch (Exception e) {
            System.err.println("URI creation failed: " + e);
            System.exit(1);
        }

        FileInputStream in = null;

        try {
            in = GAT.createFileInputStream(context, loc);
            System.err.println("avail = " + in.available());
            in.close();
        } catch (Exception e) {
            System.err.println("File input stream creation failed: " + e);
            System.exit(1);
        }

        FileOutputStream out = null;

        try {
            out = GAT.createFileOutputStream(context, loc);
        } catch (Exception e) {
            System.err.println("File output stream creation failed: " + e);
            System.exit(1);
        }

        try {
            PrintWriter p = new PrintWriter(out);
            p.println("Hello, World!");
            System.err.println("------------FILE STREAM TEST OK---------");
            p.close();
        } catch (Exception e) {
            System.err.println("Could not read file: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
