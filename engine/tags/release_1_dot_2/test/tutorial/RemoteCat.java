package tutorial;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInputStream;

class RemoteCat {
    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();
        URI loc = new URI(args[0]);

        FileInputStream in = GAT.createFileInputStream(context, loc);
        InputStreamReader reader = new InputStreamReader(in); // these are
        BufferedReader buf = new BufferedReader(reader);      // standard java classes 
        
        String result = buf.readLine();        
        System.err.println("result = " + result);
        in.close();
        GAT.end();
    }
}