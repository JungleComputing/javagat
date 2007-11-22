package file;

import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.security.CertificateSecurityContext;

class RemoteCopy2 {
    public static void main(String[] args) {
        URI src = null;
        // URI dest = null;
        // FileInputStream filestream = null;
        File file = null;
        GATContext context = new GATContext();
        Preferences prefs = new Preferences();
        prefs.put("File.adaptor.name", "gridftp");
        context.addPreferences(prefs);
        // PasswordSecurityContext securityContext = new
        // PasswordSecurityContext(
        // "rkemp", "");
        CertificateSecurityContext securityContext = null;
        try {
            securityContext = new CertificateSecurityContext(
                    new URI("/home/rkemp/.globus/userkey.pem"), new URI(
                            "/home/rkemp/.globus/usercert.pem"), "passphrase");
        } catch (URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        context.addSecurityContext(securityContext);
        try {
            src = new URI(args[0]);
        } catch (Exception e) {
            System.err.println("exception: " + e);
            System.err.println("cause:     " + e.getCause());
            // e.printStackTrace();
        }
        try {
             file = GAT.createFile(context, src);
//            filestream = GAT.createFileInputStream(context, src);

        } catch (Exception e) {
            System.err.println("create");
            System.err.println("exception: " + e);
        }
        try {
            System.err.println("copy");
             file.getFileInterface().copy(new URI(args[1]));
//            filestream.available();
        } catch (Exception e) {
            System.err.println("exception: " + e);
        }
        // context.removeSecurityContext(securityContext);
        // context.addSecurityContext(new PasswordSecurityContext("rkp400",
        // ""));
        //
        // try {
        // // file = GAT.createFile(context, src);
        // filestream = GAT.createFileInputStream(context, src);
        // System.out.println("success!");
        //
        // } catch (Exception e) {
        // System.err.println("createFileInputStream");
        // System.err.println("exception: " + e);
        // }
        // try {
        // // file.getFileInterface().exists();
        // System.err.println("available");
        // filestream.available();
        // } catch (Exception e) {
        // System.err.println("exception: " + e);
        // }
        // System.out.println(file.getParentFile());
        // System.out.println("exists: " + file.exists());
        // System.out.println("length: " + file.length());
        // /*try {
        // System.out.println("getCanonicalPath: " + file.getCanonicalPath());
        // } catch (Exception e) {
        // System.err.println("exception:" + e);
        // e.printStackTrace();
        // }*/
        //
        // System.out.println("getAbsolutePath: " + file.getAbsolutePath());
        // System.out.println("getName: " + file.getName());
        // System.out.println("getParent: " + file.getParent());
        // System.out.println("getPath: " + file.getPath());
        // System.out.println("lastModified: " + file.lastModified());
        // System.out.println("toString: " + file.toString());
        // System.out.println("canRead: " + file.canRead());
        // System.out.println("canWrite: " + file.canWrite());
        //        
        // /*System.out.println("getAbsoluteFile: "
        // + file.getAbsoluteFile().toString());
        // System.out.println("getParentFile "
        // + file.getParentFile().toString());*/
        // System.out.println("isAbsolute " + file.isAbsolute());
        // System.out.println("isDirectory " + file.isDirectory());
        // System.out.println("isFile " + file.isFile());
        // System.out.println("isHidden " + file.isHidden());
        // //System.out.println("list: " + file.list().toString());
        GAT.end();
    }
}
