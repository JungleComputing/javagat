/*
 * Created on Oct 25, 2005
 */
package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileOutputStream;

public class CatToRemote {
    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();
        URI loc = new URI(args[0]);

        FileOutputStream out = GAT.createFileOutputStream(context, loc, true);
        out.write("Hello World!\n".getBytes());
        out.flush();
        out.close();
        GAT.end();
    }
}
