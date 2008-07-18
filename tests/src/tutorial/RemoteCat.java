package tutorial;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.gridlab.gat.GAT;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInputStream;

class RemoteCat {
    public static void main(String[] args) throws Exception {
        URI loc = new URI(args[0]);

        FileInputStream in = GAT.createFileInputStream(loc);
        InputStreamReader reader = new InputStreamReader(in); // these are
        BufferedReader buf = new BufferedReader(reader);      // standard java
                                                              // classes

        while (true) {
            String result = buf.readLine();
            if (result == null) {
                break;
            }
            System.out.println(result);
        }
        in.close();
        GAT.end();
    }
}
