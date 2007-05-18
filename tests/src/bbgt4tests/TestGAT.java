package bbgt4tests;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.Preferences;

public class TestGAT {
    public static void main(String[] args) throws Exception {
	URI uri = new URI(args[0]);
	System.out.println(uri.isLocal());
	System.out.println(uri.refersToLocalHost());
    }
}
