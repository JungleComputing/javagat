/*
 * Created on Oct 25, 2005
 */
package file;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileOutputStream;
import org.gridlab.gat.security.CertificateSecurityContext;
import org.gridlab.gat.security.PasswordSecurityContext;

public class CatToRemote {
    public static void main(String[] args) throws Exception {
        GATContext context = new GATContext();
        if (args[0].startsWith("gsiftp")) {
            String passphrase = null;
            JPasswordField pwd = new JPasswordField();
            Object[] message = { "grid-proxy-init\nPlease enter your passphrase.",
                            pwd };
            JOptionPane.showMessageDialog(null, message, "Grid-Proxy-Init",
                            JOptionPane.QUESTION_MESSAGE);
            passphrase = new String(pwd.getPassword());
            CertificateSecurityContext securityContext = new CertificateSecurityContext(
                            new URI(System.getProperty("user.home") + "/.globus/userkey.pem"), new URI(
                                    System.getProperty("user.home") + "/.globus/usercert.pem"), passphrase);
            context.addSecurityContext(securityContext);
        } else if (args[0].startsWith("ftp")) {
            String username = null;
            JPasswordField usr = new JPasswordField();
            Object[] message = { "ftp\nPlease enter your username.",
                            usr };
            JOptionPane.showMessageDialog(null, message, "FTP-1",
                            JOptionPane.QUESTION_MESSAGE);
            username = new String(usr.getPassword());
            String password = null;
            JPasswordField pass = new JPasswordField();
            Object[] message2 = { "ftp\nPlease enter your password.",
                            pass };
            JOptionPane.showMessageDialog(null, message2, "FTP-2",
                            JOptionPane.QUESTION_MESSAGE);
            password = new String(pass.getPassword());
            PasswordSecurityContext securityContext = new PasswordSecurityContext(username, password);
            context.addSecurityContext(securityContext);
        }
        URI loc = new URI(args[0]);

        FileOutputStream out = GAT.createFileOutputStream(context, loc, true);
        out.write("Hello World!\n".getBytes());
        out.flush();
        out.close();
        GAT.end();
    }
}
