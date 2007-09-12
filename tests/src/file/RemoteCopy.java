package file;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

public class RemoteCopy {
    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();
        GATContext context2 = new GATContext();

        context.addPreference("File.adaptor.name", "ssh, sftp, gridftp");
        
        URI src = new URI(args[0]);
        URI dest = new URI(args[1]);
        File file = GAT.createFile(context, src);
        File file2 = GAT.createFile(context2, src);

        file.copy(dest);
        file2.copy(dest);
        GAT.end();
    }
}
