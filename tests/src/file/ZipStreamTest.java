/*
 * Created on Nov 7, 2006 by rob
 */
package file;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInputStream;

public class ZipStreamTest {

    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();
        URI loc = new URI(args[0]);

        FileInputStream in = GAT.createFileInputStream(context, loc);

        ZipInputStream zipIn = new ZipInputStream(in);

        InputStreamReader reader = new InputStreamReader(zipIn);
        BufferedReader buf = new BufferedReader(reader);

        while (true) {
            ZipEntry e = zipIn.getNextEntry();
            if(e == null) {
                break;
            }
            System.err.println("now reading file: " + e.getName());
            
            while (true) {
                String result = buf.readLine();
                if (result == null) {
                    break;
                }
                System.out.println(result);
            }
        }
        zipIn.close();
        GAT.end();
    }
}
