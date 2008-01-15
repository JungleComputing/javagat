/*
 * Created on Oct 25, 2005
 */
package file;

import java.net.URISyntaxException;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.FileInterface;

public class FileTest {

    public static String testHome, testDir, testFile;
    public static FileInterface home, dir, file;

    public static void addSecurityContexts(GATContext context, String location) {
        /*
         * if (location.startsWith("gsiftp") || location.startsWith("any")) {
         * String passphrase = null; JPasswordField pwd = new JPasswordField();
         * Object[] message = { "grid-proxy-init\nPlease enter your
         * passphrase.", pwd }; JOptionPane.showMessageDialog(null, message,
         * "Grid-Proxy-Init", JOptionPane.QUESTION_MESSAGE); passphrase = new
         * String(pwd.getPassword()); CertificateSecurityContext securityContext =
         * null; try { securityContext = new CertificateSecurityContext(new
         * URI(System .getProperty("user.home") + "/.globus/userkey.pem"), new
         * URI(System .getProperty("user.home") + "/.globus/usercert.pem"),
         * passphrase); } catch (URISyntaxException e) { e.printStackTrace();
         * System.exit(1); } context.addSecurityContext(securityContext); }
         */
        /*
         * if (location.startsWith("ftp") || location.startsWith("any")) {
         * String username = null; JPasswordField usr = new JPasswordField();
         * Object[] message = { "ftp\nPlease enter your username.", usr };
         * JOptionPane.showMessageDialog(null, message, "FTP-1",
         * JOptionPane.QUESTION_MESSAGE); username = new
         * String(usr.getPassword()); String password = null; JPasswordField
         * pass = new JPasswordField(); Object[] message2 = { "ftp\nPlease enter
         * your password.", pass }; JOptionPane.showMessageDialog(null,
         * message2, "FTP-2", JOptionPane.QUESTION_MESSAGE); password = new
         * String(pass.getPassword()); PasswordSecurityContext securityContext =
         * new PasswordSecurityContext( username, password);
         * context.addSecurityContext(securityContext); }
         */
    }

    public static void setupTestEnvironment(GATContext context) {
        System.out.println("Trying to setup test environment at: " + testHome);
        try {
            home = GAT.createFile(context, testHome).getFileInterface();
        } catch (GATObjectCreationException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("---------------------------");
        System.out.println("Analyzing test environment");
        try {
            System.out.println("> exists:    " + home.exists());
        } catch (GATInvocationException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            dir = GAT.createFile(context, testHome).getFileInterface();
        } catch (GATObjectCreationException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            if (dir.exists()) {
                System.out.println("> isDir:    " + dir.isDirectory());
                if (dir.isDirectory()) {
                    testDir = testDir + "/testdir";
                } else {
                    System.out.println("> isFile:   " + dir.isFile());
                    if (dir.isFile()) {
                        System.out.println("> getParent: " + dir.getParent());
                        if (dir.getParent() != null) {
                            testDir = dir.getParent() + "/testdir";
                        } else {
                            System.out
                                    .println("Cannot run test program for given destination");
                            System.exit(1);
                        }
                    }
                }
                dir = GAT.createFile(context, testDir).getFileInterface();
            } else {
                dir = GAT.createFile(context, testDir).getFileInterface();
                dir.mkdirs();
                testDir = testDir + "/testdir";
                dir = GAT.createFile(context, testDir).getFileInterface();
            }
        } catch (GATInvocationException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (GATObjectCreationException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("---------------------------");
        testFile = testDir + "/testfile";
        try {
            file = GAT.createFile(context, testFile).getFileInterface();
        } catch (GATObjectCreationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void testDirExists() {
        System.out
                .print("exists test for non-existing dir '" + testDir + "': ");
        try {
            if (!dir.exists()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
        System.out.print("mkdir test for dir '" + testDir + "': ");
        try {
            if (dir.mkdir()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
        System.out.print("exists test for existing dir '" + testDir + "': ");
        try {
            if (dir.exists()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    public static void testFileExists() {
        System.out.print("exists test for non-existing file '" + testFile
                + "': ");
        try {
            if (!file.exists()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
        System.out.print("create new file test for file '" + testFile + "': ");
        try {
            if (file.createNewFile()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
        System.out.print("exists test for existing file '" + testFile + "': ");
        try {
            if (file.exists()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    public static void testFileDelete() {
        System.out.print("delete test for existing file '" + testFile + "': ");
        try {
            if (file.delete()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
        System.out.print("delete test for non-existing file '" + testFile
                + "': ");
        try {
            if (!file.delete()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    public static void testFileCopyDelete() {
        System.out.print("delete test for existing file-copy '" + testFile + "-copy': ");
        try {
            if (GAT.createFile(new GATContext(), testFile + "-copy").getFileInterface().delete()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        } catch (GATObjectCreationException e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    public static void testDirDelete() {
        System.out.print("delete test for existing dir '" + testDir + "': ");
        try {
            if (dir.delete()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
        System.out
                .print("delete test for non-existing dir '" + testDir + "': ");
        try {
            if (!dir.delete()) {
                System.out.println("OK");
            } else {
                System.out.println("FAILURE");
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    public static void testFileCanRead() {
        System.out.print("can read test for file '" + testFile + "': ");
        try {
            System.out.print(file.canRead());
            System.out.println(" (OK)");
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    public static void testFileCanWrite() {
        System.out.print("can write test for file '" + testFile + "': ");
        try {
            System.out.print(file.canWrite());
            System.out.println(" (OK)");
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    public static void testDirCanRead() {
        System.out.print("can read test for dir '" + testDir + "': ");
        try {
            System.out.print(dir.canRead());
            System.out.println(" (OK)");
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    public static void testDirCanWrite() {
        System.out.print("can write test for dir '" + testDir + "': ");
        try {
            System.out.print(dir.canWrite());
            System.out.println(" (OK)");
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    public static void testFileCopy() {
        String testFile2 = testFile + "-copy";
        System.out.print("copy test for file '" + testFile + "' to '"
                + testFile2 + "': ");
        try {
            file.copy(new URI(testFile2));
            System.out.println("OK");
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        } catch (URISyntaxException e) {
            System.out.println("EXCEPTION: " + e);
        }
    }

    public static void testDirList() {
        System.out.print("list test for dir '" + testDir + "': ");
        try {
            String[] files = dir.list();
            System.out.println("OK");
            for (String s : files) {
                System.out.println("> " + s);
            }
        } catch (GATInvocationException e) {
            System.out.println("EXCEPTION: " + e);
        }

    }

    public static void main(String[] args) {
        GATContext context = new GATContext();
        args[1] = args[1].replace('\\', ' ');
        context.addPreference("File.adaptor.name", args[1]);
        context.addPreference("FileInputStream.adaptor.name", args[1] + ",");
        context.addPreference("FileOutputStream.adaptor.name", args[1] + ",");
        System.out.println(args[1]);
        testHome = args[0];
        testDir = args[0];
        testFile = "";
        addSecurityContexts(context, args[0]);
        setupTestEnvironment(context);
        testDirExists();
        testDirCanRead();
        testDirCanWrite();
        testFileExists();
        testFileCanRead();
        testFileCanWrite();
        testDirList();
        testFileCopy();
        testDirList();
        testFileCopyDelete();
        testFileDelete();
        testDirDelete();
        GAT.end();
    }
}
