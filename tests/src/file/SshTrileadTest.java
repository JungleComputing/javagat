package file;

import java.io.IOException;
import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;

class SshTrileadTest {
    public static void main(String[] args) throws GATObjectCreationException, URISyntaxException, IOException {
        GATContext context = new GATContext();
        Preferences preferences = new Preferences();
        preferences.put("file.adaptor.name", "sshtrilead");
        // args[0] = remote file
        File remote = GAT.createFile(context, preferences, new URI(args[0]));
        System.out.println("tests for remote file '" + args[0] + "'");
        System.out.println("exists: " + remote.exists());
        System.out.println("isdir:  " + remote.isDirectory());
        System.out.println("isfile: " + remote.isFile());
        System.out.println("ishidd: " + remote.isHidden());
        System.out.println("read:   " + remote.canRead());
        System.out.println("write:  " + remote.canWrite());
        System.out.println("length: " + remote.length());
        System.out.println("abspth: " + remote.getAbsolutePath());
        // args[1] = existing dir
        File dir = GAT.createFile(context, preferences, new URI(args[1]));
        System.out.println("tests for dir '" + args[1] + "'");
        String[] dirlist = dir.list();
        System.out.println("list:   " + dirlist.length);
        for (String entry: dirlist) {
            System.out.println("   " + entry);
        }
        // args[2] = file to delete
        File delete = GAT.createFile(context, preferences, new URI(args[2]));
        System.out.println("delete test for file/dir '" + args[2] + "'");
        System.out.println("delete: " + delete.delete());
        // args[3] = file to create
        File create = GAT.createFile(context, preferences, new URI(args[3]));
        System.out.println("create test for file '" + args[3] + "'");
        System.out.println("create: " + create.createNewFile());
        // args[4] = dir to create
        File createdir = GAT.createFile(context, preferences, new URI(args[4]));
        System.out.println("create test for dir '" + args[4] + "'");
        System.out.println("mkdir:  " + createdir.mkdir());
        // setLastModified()
        //createdir.setReadOnly()
    }
}
