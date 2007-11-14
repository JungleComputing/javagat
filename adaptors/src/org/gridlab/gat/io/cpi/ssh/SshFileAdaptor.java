package org.gridlab.gat.io.cpi.ssh;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@SuppressWarnings("serial")
public class SshFileAdaptor extends FileCpi {

    protected static Logger logger = Logger.getLogger(SshFileAdaptor.class);

    private static final int TIMEOUT = 5000; // millis

    private static final int XOS = 1;

    private static final int WOS = 2;

    private static final int UNKNOWN = 0;

    private static final int TRUE = 1;

    private static final int FALSE = 2;

    protected static final byte LINE_FEED = 0x0a;

    private static final int BUFFER_SIZE = 1024;

    public static final int SSH_PORT = 22;

    public static final int IN = 0, ERR = 1, OUT = 2;

    File localFile; /* used only if this org.gat.gridlab.io.File is local */

    private JSch jsch;

    private Session session;

    private Channel channel;

    private SshUserInfo sui;

    private int port;

    private int osType = UNKNOWN;

    private boolean isLocalFile = false;

    private int isDir = UNKNOWN;

    private int itExists = UNKNOWN;

    /**
     * @param gatContext
     * @param preferences
     * @param location
     * @throws GATInvocationException 
     */
    public SshFileAdaptor(GATContext gatContext, Preferences preferences,
            URI location) throws GATObjectCreationException, GATInvocationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("ssh")) {
            throw new AdaptorNotApplicableException("cannot handle this URI");
        }

        if (location.refersToLocalHost()) {
            isLocalFile = true;
            localFile = new File(getPath());

            if (logger.isDebugEnabled()) {
                logger.debug("SshFileAdaptor: local file LOCATION = "
                        + location);
            }

            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("SshFileAdaptor: remote file LOCATION = " + location);
        }

        prepareSession(location);

        if (logger.isDebugEnabled()) {
            logger.debug("SshFileAdaptor: started session with "
                    + location.resolveHost() + " using username: "
                    + sui.username + " on port: " + port + " for file: "
                    + location.getPath());
        }
    }

    protected void prepareSession(URI loc) throws GATObjectCreationException,
            GATInvocationException {
        String host = loc.resolveHost();

        /* it will be changed for the Security Context */
        /*
         * if(preferences == null) { throw new GATObjectCreationException( "The
         * SshFileAdaptor needs a Preferences object to specify at least the
         * identity file."); }
         */
        // opens a ssh connection (using jsch)
        jsch = new JSch();

        Hashtable<String, String> configJsch = new Hashtable<String, String>();
        configJsch.put("StrictHostKeyChecking", "no");
        JSch.setConfig(configJsch);

        sui = null;

        try {
            sui = SSHSecurityUtils.getSshCredential(gatContext, preferences,
                    "ssh", loc, SSH_PORT);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) {
                logger.info("SshFileAdaptor: failed to retrieve credentials"
                        + e);
            }
        }

        if (sui == null) {
            throw new GATObjectCreationException("ssh", new Exception(
                    "Unable to retrieve user info for authentication"));
        }

        try {
            if (sui.privateKeyfile != null) {
                jsch.addIdentity(sui.privateKeyfile);
            }

            // to be modified, this part goes inside the SSHSecurityUtils
            if (loc.getUserInfo() != null) {
                sui.username = loc.getUserInfo();
            }

            if (sui.username == null) {
                sui.username = System.getProperty("user.name");
            }

            // no passphrase
            /* allow port override */
            port = loc.getPort();

            /* it will always return -1 for user@host:path */
            if (port == -1) {
                port = SSH_PORT;
            }

            // portNumber = port + "";
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Prepared session for location " + loc
                                + " with username: " + sui.username
                                + "; host: " + host);
            }

            session = jsch.getSession(sui.username, host, port);
            session.setUserInfo(sui);
        } catch (Exception e) {
            doException(e);
        }
    }

    protected static void cleanSession(Session s, Channel c) {
        if (c != null) {
            c.disconnect();
        }
        if (s != null) {
            s.disconnect();
        }
    }

    private void waitForEOF() throws GATInvocationException {
        long start = System.currentTimeMillis();
        while (true) {
            long time = System.currentTimeMillis() - start;
            if (time > TIMEOUT) {
                throw new GATInvocationException("ssh", new Exception(
                        "timeout waiting for EOF"));
            }
            if (channel.isEOF()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    protected int determineRemoteOS() {
        try {
            Object[] streams = execCommand("ls");
            if (((InputStream) streams[ERR]).available() != 0) {
                cleanSession(session, channel);
                Object[] streams2 = execCommand("dir");
                if (((InputStream) streams2[ERR]).available() != 0) {
                    cleanSession(session, channel);
                    throw new GATObjectCreationException("Unknown remote OS");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("SshFileAdaptor: remote OS for " + location
                            + " is windows");
                }
                cleanSession(session, channel);
                return WOS;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("SshFileAdaptor: remote OS for " + location
                        + " is unix");
            }
            cleanSession(session, channel);
            return XOS;
        } catch (Exception e) {
            cleanSession(session, channel);
            if (logger.isDebugEnabled()) {
                logger
                        .debug("SshFileAdaptor: could not determine remote OS for "
                                + location + ", assuming unix");
            }
            // just assume it is unix-like --Rob
            return XOS;
        }
    }

    // Make life a bit easier for the programmer:
    protected URI correctURI(URI in) throws GATInvocationException {
        if (in.getScheme() == null) {
            try {
                return new URI("ssh:" + in.toString());
            } catch (URISyntaxException e) {
                throw new GATInvocationException("ssh", e);
            }
        }

        return in;
    }

    /**
     * This method copies the physical file represented by this File instance to
     * a physical file identified by the passed URI.
     * 
     * @param loc
     *                The new location
     * @throws java.io.IOException
     *                 Upon non-remote IO problem
     */
    public void copy(URI loc) throws GATInvocationException {
        /*
         * if both files are local fail if same machine if same user if same
         * file do nothing else copy oldfile newfile else if auth succeeded copy
         * oldfile absolutePath(newfile) else fail else if loc is local scp
         * remoteSource to local (ScpFrom) else not implemented yet if auth
         * succeeded scp remoteSource to remoteDestination else fail
         */

        if (isLocalFile) {
            if (loc.refersToLocalHost()) {
                throw new GATInvocationException(
                        "ssh",
                        new Exception(
                                "SshFileAdaptor:the source file is local ("
                                        + getPath()
                                        + "), then the destination file must be remote, path = "
                                        + loc.getPath()));
            }

            if (!localFile.exists()) {
                throw new GATInvocationException("ssh", new Exception(
                        "SshFileAdaptor:the local source file does not exist, path = "
                                + getPath()));
            }

            scpFromLocalToRemote(loc);
            return;
        }

        if (itExists == UNKNOWN) {
            exists();
        }

        if (itExists == FALSE) {
            throw new GATInvocationException("ssh", new Exception(
                    "the remote source file does not exist, path = " + toURI()));
        }

        /* get destination user info if destination is not on local machine */
        SshUserInfo dui = null;
        String destUserName = null;

        if (!loc.refersToLocalHost()) {
            try {
                // has to be modified after proper modified SSHSecurityUtils
                SshUserInfo tmpsui = SSHSecurityUtils.getSshCredential(
                        gatContext, preferences, "ssh", loc, SSH_PORT);
                dui = new SshUserInfo();
                dui.username = tmpsui.username;
                dui.password = tmpsui.password;
                dui.privateKeyfile = tmpsui.privateKeyfile;

                if (loc.getUserInfo() != null) {
                    dui.username = loc.getUserInfo();
                }
            } catch (Exception e) {
                logger.info("SshFileAdaptor: failed to retrieve credentials"
                        + e);
            }

            if (dui == null) {
                throw new GATInvocationException("ssh", new Exception(
                        "Unable to retrieve user info for authentication"));
            }

            destUserName = dui.username;
        }

        if (location.resolveHost().equals(loc.resolveHost())) {
            /*
             * as both location and loc are not local, destUserName has to be
             * not null
             */
            if (destUserName.equals(sui.username)) {
                if (loc.getPath().equals(location.getPath())) {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("remote copy, source is the same file as dest.");
                    }

                    return;
                } else {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("remote copy, source is on the same host and of same user as dest.");
                    }

                    /*
                     * same user name, will be a simple copy on the same remote
                     * machine and same remote account
                     */
                    copyOnSameHost(loc.getPath());

                    return;
                }
            } else {
                /*
                 * don't think it's such a good idea, unless the user has
                 * write-access for the other account as well a special case of
                 * remote to remote
                 */
                try {
                    org.gridlab.gat.io.File dest = GAT.createFile(gatContext,
                            preferences, loc);
                    copyOnSameHost(dest.getAbsolutePath());
                } catch (Exception e) {
                    thirdPartyTransfer(loc);

                    if (logger.isDebugEnabled()) {
                        StringWriter writer = new StringWriter();
                        e.printStackTrace(new PrintWriter(writer));
                        logger.debug("failed remote copy, source: user "
                                + sui.username + " host "
                                + location.resolveHost() + " dest: user "
                                + destUserName + " host " + loc.resolveHost()
                                + "\n" + writer.toString());
                    }
                }

                return;
            }
        } else {
            if (loc.refersToLocalHost()) {
                /* the URI is local, on this machine */
                if (logger.isInfoEnabled()) {
                    logger.info("local copy of remote file " + toURI() + " to "
                            + loc.getPath());
                }

                scpFromRemoteToLocal(loc);

                if (logger.isDebugEnabled()) {
                    logger.debug("Finished scpFromRemoteToLocal");
                }

                return;
            } else {
                /* third party transfer: */
                if (logger.isInfoEnabled()) {
                    logger.info("remote copy, source: user " + sui.username
                            + " host " + location.resolveHost()
                            + " dest: user " + destUserName + " host "
                            + loc.resolveHost());
                }

                thirdPartyTransfer(loc);
            }
        }
    }

    protected static void sendAck(OutputStream os) throws IOException {
        byte[] buf = new byte[1];

        // send '\0' (ACK)
        buf[0] = 0;
        os.write(buf);
        os.flush();
    }

    protected void thirdPartyTransfer(URI loc) throws GATInvocationException {

        // first try to execute it remotely

        try {
            String isRecursive = "";

            if (isDir == UNKNOWN) {
                isDirectory();
            }

            if (isDir == TRUE) {
                isRecursive = "-r";
            }

            String remoteUser = loc.getUserInfo();

            if (remoteUser == null) {
                remoteUser = "";
            }

            Object[] streams = execCommand("scp " + isRecursive + " "
                    + getPath() + " " + remoteUser + "@" + loc.resolveHost()
                    + ":" + loc.getPath());
            if (((InputStream) streams[ERR]).available() == 0) {
                cleanSession(session, channel);
                return;
            } else {
                cleanSession(session, channel);
                try {
                    java.io.File tmp = null;
                    tmp = java.io.File.createTempFile("GATSCP", ".tmp");

                    scpFromRemoteToLocal(new URI(tmp.getPath()));

                    // now pretend this is a local file
                    // assume same credential
                    localFile = tmp;
                    isLocalFile = true;
                    scpFromLocalToRemote(loc);

                    // stop pretending local file
                    localFile = null;
                    isLocalFile = false;

                    // as scpFromLocalToRemote opens a session on remote
                    // destination
                    // the original session needs to be restored.

                    prepareSession(location);
                } catch (Exception e) {
                    throw new GATInvocationException("ssh", e);
                }
            }
        } catch (Exception e1) {
            throw new GATInvocationException("ssh", e1);
        }
    }

    /* sends file to remote machine */
    protected void sendFileToRemote(File lf, InputStream in, OutputStream out)
            throws IOException {
        // send "C0644 filesize filename", where filename should not include '/'
        long filesize = lf.length();
        String command;
        command = "C0644 " + filesize + " ";
        command += lf.getName();
        command += "\n";
        out.write(command.getBytes());
        out.flush();

        if (checkAck(in) != 0) {
            throw new IOException("failed to receive ack after sending header"
                    + " for transfer file to remote machine");
        }

        // send a content of lfile
        FileInputStream fis = new FileInputStream(lf.getPath());
        byte[] buf = new byte[1024];

        while (true) {
            int len = fis.read(buf, 0, buf.length);

            if (len <= 0) {
                break;
            }

            out.write(buf, 0, len);
        }

        out.flush();

        sendAck(out);

        // wait for ACK
        if (checkAck(in) != 0) {
            fis.close();

            /*
             * should also close the channel=> return a bool which is checked in
             * the calling function
             */
            throw new IOException("failed to receive ack after sending"
                    + " file to remote machine");
        }

        fis.close();
    }

    /* copies local file to remote loc file */
    protected void doSingleUpload(URI loc) throws GATInvocationException {
        try {
            /* prepare Session for remote location of destination file */
            prepareSession(loc);

            if (logger.isDebugEnabled()) {
                logger.debug("remote user: " + sui.username + " remote host: "
                        + loc.resolveHost());
            }
            // exec 'scp -t rfile' remotely
            Object[] streams = execCommand("scp -p -t " + loc.getPath());

            if (checkAck(((InputStream) streams[IN])) != 0) {
                cleanSession(session, channel);
                throw new GATInvocationException("ssh", new Exception(
                        "SshFileAdaptor: failed checkAck after sending scp command"
                                + " in doSingleUpload " + localFile.getPath()
                                + " to " + loc));
            }

            try {
                /* f is this local file, is a an attribute of this object */
                sendFileToRemote(localFile, ((InputStream) streams[IN]),
                        ((OutputStream) streams[OUT]));
            } catch (IOException ioe) {
                cleanSession(session, channel);
                doException(ioe);
            }

            cleanSession(session, channel);

            return;
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
        }
    }

    protected void sendDirectoryToRemote(File dir, InputStream in,
            OutputStream out) throws IOException {
        String command = "D0755 0 ";
        command += dir.getName();
        command += "\n";

        out.write(command.getBytes());
        out.flush();

        if (checkAck(in) != 0) {
            throw new IOException("failed to receive ack after sending header"
                    + " for transfer directory to remote machine");
        }

        sendDirectory(dir, in, out);
        out.write("E\n".getBytes());

        if (checkAck(in) != 0) {
            throw new IOException("failed to receive ack after sending"
                    + " directory to remote machine");
        }
    }

    protected void sendDirectory(File lf, InputStream in, OutputStream out)
            throws IOException {
        File[] entries = lf.listFiles();

        for (int entry = 0; entry < entries.length; entry++) {
            File current = entries[entry];

            if (current.isDirectory()) {
                sendDirectoryToRemote(current, in, out);
            } else {
                sendFileToRemote(current, in, out);
            }
        }
    }

    protected void doMultipleUpload(URI loc) throws GATInvocationException {
        try {
            /* prepare Session for remote location of destination file */
            prepareSession(loc);

            session.connect();

            /* -p -> preserve time, -t -> target, -r -> recursive */
            Object[] streams = execCommand("scp -p -r -t " + loc.getPath());
            if (checkAck(((InputStream) streams[IN])) != 0) {
                cleanSession(session, channel);
                throw new GATInvocationException("ssh", new Exception(
                        "SshFileAdaptor: failed checkAck after sending scp command"
                                + " in doMultipleUpload " + getPath() + " to "
                                + loc));
            }

            try {
                sendDirectoryToRemote(localFile, ((InputStream) streams[IN]),
                        ((OutputStream) streams[OUT]));
            } catch (IOException ioe) {
                cleanSession(session, channel);
                doException(ioe);
            }

            cleanSession(session, channel);

            return;
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
        }
    }

    /*
     * follows the model in
     * http://cvs.apache.org/viewcvs.cgi/ant/src/main/org/apache/tools/ant/taskdefs
     * /optional/ssh/ScpToMessage.java?view=markup
     */
    protected void scpFromLocalToRemote(URI loc) throws GATInvocationException {
        try {
            if (!localFile.isDirectory()) {
                doSingleUpload(loc);
            } else {
                doMultipleUpload(loc);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                logger.debug("SshFileAdaptor: for location " + location
                        + " throws error " + e + "\n" + writer.toString());
            }
            doException(e);
        }
    }

    private void fetchFile(File localFile, long filesize, OutputStream out,
            InputStream in) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        sendAck(out);

        // read a content of lfile
        FileOutputStream fos = new FileOutputStream(localFile);
        long length;
        int readResult;

        try {
            while (true) {
                length = (buf.length < filesize) ? buf.length : filesize;
                readResult = in.read(buf, 0, (int) length);

                if (readResult < 0) {
                    throw new IOException("Unexpected end of stream.");
                }

                fos.write(buf, 0, readResult);
                filesize -= readResult;

                if (filesize == 0) {
                    break;
                }
            }
        } finally {
            fos.flush();
            fos.close();

            if (logger.isDebugEnabled()) {
                logger.debug("scpFromRemoteToLocal: finished writing file"
                        + localFile.getPath());
            }
        }
    }

    private void parseAndFetchFile(String serverResponse, File localFile,
            OutputStream out, InputStream in) throws IOException {
        int start = 0;
        int end = serverResponse.indexOf(" ", start + 1);
        start = end + 1;
        end = serverResponse.indexOf(" ", start + 1);

        long filesize = Long.parseLong(serverResponse.substring(start, end));
        String filename = serverResponse.substring(end + 1);
        File transferFile = (localFile.isDirectory()) ? new File(localFile,
                filename) : localFile;
        fetchFile(transferFile, filesize, out, in);
        checkAck(in);
        sendAck(out);
    }

    private File parseAndCreateDirectory(String serverResponse, File localFile) {
        int start = serverResponse.indexOf(" ");

        // appears that the next token is not used and it's zero.
        start = serverResponse.indexOf(" ", start + 1);

        String directoryName = serverResponse.substring(start + 1);

        if (localFile.exists()) {
            if (localFile.isDirectory()) {
                File dir = new File(localFile, directoryName);
                dir.mkdir();

                return dir;
            } else {
                return null;
            }
        } else {
            localFile.mkdir();

            return localFile;
        }
    }

    protected void startRemoteCpProtocol(InputStream in, OutputStream out,
            File localFile) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("scpFromRemoteToLocal: remoteCpProtocol");
        }

        File startFile = localFile;

        while (true) {
            // C0644 filesize filename - header for a regular file
            // T time 0 time 0\n - present if perserve time.
            // D directory - this is the header for a directory.
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            while (true) {
                int read = in.read();

                if (read < 0) {
                    return;
                }

                if ((byte) read == LINE_FEED) {
                    break;
                }

                stream.write(read);
            }

            String serverResponse = stream.toString("UTF-8");

            if (serverResponse.charAt(0) == 'C') {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("scpFromRemoteToLocal: remote response is file");
                }

                parseAndFetchFile(serverResponse, startFile, out, in);
            } else if (serverResponse.charAt(0) == 'D') {
                startFile = parseAndCreateDirectory(serverResponse, startFile);

                if (logger.isDebugEnabled()) {
                    logger
                            .debug("scpFromRemoteToLocal: remote response is dir");
                }

                if (startFile != null) {
                    sendAck(out);
                } else {
                    throw new IOException("File " + localFile.getPath()
                            + ": Not a directory");
                }
            } else if (serverResponse.charAt(0) == 'E') {
                startFile = startFile.getParentFile();

                if (logger.isDebugEnabled()) {
                    logger.debug("scpFromRemoteToLocal: remote response is E");
                }

                sendAck(out);
            } else if ((serverResponse.charAt(0) == '\01')
                    || (serverResponse.charAt(0) == '\02')) {
                // this indicates an error.
                throw new IOException(serverResponse.substring(1));
            } else {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("remoteCpProtocol: read byte is none of the expected values");
                }
            }
        }
    }

    protected void scp(URI loc, String isRec) throws GATInvocationException {
        if (logger.isDebugEnabled()) {
            logger.debug("scpFromRemoteToLocal: scp " + isRec);
        }
        /*-f from fetch*/
        try {
            Object[] streams = execCommand("scp -f " + isRec + getPath());
            sendAck(((OutputStream) streams[OUT]));
            File localFile = new File(loc.getPath());
            startRemoteCpProtocol(((InputStream) streams[IN]),
                    ((OutputStream) streams[OUT]), localFile);
            cleanSession(session, channel);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                logger.debug("SshFileAdaptor: for location " + location
                        + " throws error " + e + "\n" + writer.toString());
            }

            cleanSession(session, channel);
            doException(e);
        }
    }

    /*
     * copies from this remote file to loc file follows the model in
     * http://cvs.apache.org/viewcvs.cgi/ant/src/main/org/apache/tools/ant/taskdefs
     * /optional/ssh/ScpToMessage.java?view=markup
     */
    protected void scpFromRemoteToLocal(URI loc) throws GATInvocationException {
        String isRecursive = "";

        try {
            if (isDir == UNKNOWN) {
                isDirectory();
            }

            if (isDir == TRUE) {
                isRecursive = "-r ";
            }

            scp(loc, isRecursive);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                logger.debug("SshFileAdaptor: for location " + location
                        + " throws error " + e + "\n" + writer.toString());
            }
            doException(e);
        }
    }

    protected void copyOnSameHost(String path) throws GATInvocationException {
        try {
            if (isDir == UNKNOWN) {
                isDirectory();
            }
            Object[] streams;
            switch (getOsType()) {
            case XOS:
                if (isDir == TRUE) {
                    streams = execCommand("cp -r " + getPath() + " " + path);
                } else {
                    streams = execCommand("cp " + getPath() + " " + path);
                }
                if (((InputStream) streams[ERR]).available() != 0) {
                    cleanSession(session, channel);
                    throw new GATInvocationException(
                            "ssh",
                            new Exception(
                                    "cannot copy remote file to another file on the same machine"));
                }
                cleanSession(session, channel);
                return;
            case WOS:
                if (isDir == TRUE) {
                    streams = execCommand("copy " + toMW(getPath()) + " "
                            + toMW(path));
                } else {
                    streams = execCommand("xcopy /I/E/H/K/O/Q/Y "
                            + toMW(getPath()) + " " + toMW(path));
                }
                if (((InputStream) streams[ERR]).available() != 0) {
                    cleanSession(session, channel);
                    throw new GATInvocationException(
                            "ssh",
                            new Exception(
                                    "cannot copy remote file to another file on the same machine"));
                }
                cleanSession(session, channel);
                return;
            }
            cleanSession(session, channel);
            throw new GATInvocationException("ssh", new Exception(
                    "Unknown remote OS type"));
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                logger.debug("SshFileAdaptor: for location " + location
                        + " throws error " + e + "\n" + writer.toString());
            }

            cleanSession(session, channel);
            doException(e);
        }
    }

    protected static int checkAck(InputStream in) throws IOException {
        int b = in.read();

        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,
        // -1
        if (b == 0) {
            return b;
        }

        if (b == -1) {
            return b;
        }

        if ((b == 1) || (b == 2)) {
            StringBuffer sb = new StringBuffer();
            int c;

            do {
                c = in.read();
                sb.append((char) c);
            } while (c != '\n');

            if (b == 1) { // error
                System.out.print(sb.toString());
            }

            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }

        return b;
    }

    private boolean canReadOrWrite(char flag) throws GATInvocationException {
        if (isLocalFile) {
            throw new Error(
                    "SshFileAdaptor for local files: only copy to remote machine");
        }
        try {
            if (getOsType() == WOS) {
                throw new Error("Not yet implemented");
            } else if (getOsType() == XOS) {
                Object[] streams = execCommand("test -" + flag + getPath()
                        + " && echo 0");
                if (((InputStream) streams[IN]).available() != 0) {
                    cleanSession(session, channel);
                    return true;
                }
                cleanSession(session, channel);
                return false;
            } else {
                throw new GATInvocationException("ssh", new Exception(
                        "Unknown remote OS type"));
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return false; // never executed
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#canRead()
     */
    public boolean canRead() throws GATInvocationException {
        return canReadOrWrite('r');
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#canWrite()
     */
    public boolean canWrite() throws GATInvocationException {
        return canReadOrWrite('w');
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#compareTo(gat.io.File)
     */
    public int compareTo(org.gridlab.gat.io.File other) {
        return super.compareTo(other);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#compareTo(java.lang.Object)
     */
    public int compareTo(Object other) {
        return super.compareTo(other);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#createNewFile()
     */
    public boolean createNewFile() throws GATInvocationException {
        isLocalFile();

        try {
            if (getOsType() == XOS) {
                Object[] streams = execCommand("test ! -d " + getPath()
                        + " && test ! -f " + getPath() + " && touch "
                        + getPath());
                boolean result = (((InputStream) streams[ERR]).available() == 0);
                cleanSession(session, channel);
                return result;
            } else if (getOsType() == WOS) {
                Object[] streams = execCommand("dir " + toMW(getPath())
                        + " || cd . > " + toMW(getPath()));
                boolean result = (((InputStream) streams[ERR]).available() == 0);
                cleanSession(session, channel);
                return result;
            } else {
                throw new GATInvocationException("ssh", new Exception(
                        "Unknown remote OS type"));
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return false; // never executed
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#delete()
     */
    public boolean delete() throws GATInvocationException {
        isLocalFile();
        try {
            if (isDir == UNKNOWN) {
                isDirectory();
            }
            if (getOsType() == XOS) {
                Object[] streams;
                if (isDir == TRUE) {
                    streams = execCommand("rmdir " + getPath());
                } else {
                    streams = execCommand("rm -f " + getPath());
                }
                itExists = (((InputStream) streams[ERR]).available() == 0) ? FALSE
                        : TRUE;
                cleanSession(session, channel);
                return itExists == TRUE;
            } else if (getOsType() == WOS) {
                Object[] streams;
                if (isDir == TRUE) {
                    streams = execCommand("rmdir " + toMW(getPath()));
                } else {
                    streams = execCommand("del /q" + toMW(getPath()));
                }
                itExists = (((InputStream) streams[ERR]).available() == 0) ? FALSE
                        : TRUE;
                cleanSession(session, channel);
                return itExists == TRUE;
            } else {
                throw new GATInvocationException("ssh", new Exception(
                        "Unknown remote OS type"));
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return false; // never executed
        }
    }

    private boolean deleteLocalRecursively(File localFile) {
        if (!localFile.isDirectory()) {
            return localFile.delete();
        }

        File[] entries = localFile.listFiles();
        boolean result = true;

        for (int i = 0; i < entries.length; i++) {
            if (entries[i].isDirectory()) {
                result = result && deleteLocalRecursively(entries[i]);
            } else {
                result = result && entries[i].delete();
            }
        }

        result = result && localFile.delete();

        return result;
    }

    private boolean deleteByForceRecursively() throws GATInvocationException {
        if (localFile != null) {
            return deleteLocalRecursively(localFile);
        }

        try {
            if (isDir == UNKNOWN) {
                isDirectory();
            }
            Object[] streams;
            switch (getOsType()) {
            case XOS:
                if (isDir == TRUE) {
                    streams = execCommand("rm -rf " + getPath());
                } else {
                    streams = execCommand("rm -f " + getPath());
                }
                if (((InputStream) streams[ERR]).available() != 0) {
                    itExists = TRUE;
                    cleanSession(session, channel);
                    return false;
                }
                itExists = FALSE;
                cleanSession(session, channel);
                return true;
            case WOS:

                if (isDir == TRUE) {
                    streams = execCommand("rmdir /S /Q " + toMW(getPath()));
                } else {
                    streams = execCommand("del /q " + toMW(getPath()));
                }

                if (((InputStream) streams[ERR]).available() != 0) {
                    itExists = TRUE;
                    cleanSession(session, channel);

                    return false;
                }

                itExists = FALSE;
                cleanSession(session, channel);

                return true;
            }
            cleanSession(session, channel);
            throw new GATInvocationException("ssh", new Exception(
                    "Unknown remote OS type"));
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return false; // never executed
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#exists()
     */
    public boolean exists() throws GATInvocationException {
        isLocalFile();
        Object[] streams = null;
        try {
            if (getOsType() == XOS) {
                streams = execCommand("ls -log " + getPath());
            } else if (getOsType() == WOS) {
                streams = execCommand("dir /B" + toMW(getPath()));
            } else {
                throw new GATInvocationException("ssh", new Exception(
                        "Unknown remote OS type"));
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                logger.debug("SshFileAdaptor: for location " + location
                        + " throws error " + e + "\n" + writer.toString());
            }
            cleanSession(session, channel);
            doException(e);
        }
        try {
            itExists = (((InputStream) streams[ERR]).available() != 0) ? FALSE
                    : TRUE;
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                logger.debug("SshFileAdaptor: for location " + location
                        + " throws error " + e + "\n" + writer.toString());
            }
            cleanSession(session, channel);
            doException(e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("SshFileAdaptor: for location " + location
                    + " file does exist=" + itExists);
        }
        cleanSession(session, channel);
        return itExists == TRUE;
    }

    protected String toMW(String path) {
        return path.replace('/', '\\');
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#getAbsoluteFile()
     */
    public org.gridlab.gat.io.File getAbsoluteFile()
            throws GATInvocationException {
        isLocalFile();

        // String uriString = location.toString();
        String absUri = "//" + sui.username + "@" + location.resolveHost()
                + ":" + port + "/" + getAbsolutePath();

        try {
            return GAT.createFile(gatContext, preferences, new URI(absUri));
        } catch (Exception e) {
            doException(e);
            return null; // never executed
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#getAbsolutePath()
     */
    public String getAbsolutePath() throws GATInvocationException {
        isLocalFile();
        try {
            if (getOsType() == XOS) {
                if (getPath().startsWith("/")) {
                    return getPath();
                }
                Object[] streams = execCommand("echo ~" + sui.username);
                if (((InputStream) streams[ERR]).available() != 0) {
                    throw new GATInvocationException(
                            "internal error in SshFileAdaptor: getAbsolutePath ");
                }
                StringBuffer buffer = new StringBuffer();
                int bufferLength = ((InputStream) streams[IN]).available() - 1;
                for (int i = 0; i < bufferLength; i++) {
                    buffer.append((char) ((InputStream) streams[IN]).read());
                }
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("SshFileAdaptor: getAbsolutePath for location "
                                    + location);
                }
                cleanSession(session, channel);
                return buffer.toString() + "/" + getPath();
            } else if (getOsType() == WOS) {
                throw new GATInvocationException("Not yet implemented");
            } else {
                throw new GATInvocationException("Unknown remote OS type");
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return null; // never executed
        }
    }

    private void isLocalFile() throws GATInvocationException {
        if (isLocalFile) {
            throw new GATInvocationException("ssh", new Exception(
                    "SshFileAdaptor for local files: only for remote machine"));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#getParent()
     */
    public String getParent() throws GATInvocationException {
        isLocalFile();

        String path = getPath();
        String parentPath;

        if (path.endsWith("/")) {
            parentPath = path.substring(0, path.lastIndexOf('/',
                    path.length() - 2));
        } else if (path.lastIndexOf('/') >= 0) {
            parentPath = path.substring(0, path.lastIndexOf('/'));
        } else {
            return null;
        }

        if (parentPath.length() == 0) {
            return null;
        }

        return parentPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#getParentFile()
     */
    public org.gridlab.gat.io.File getParentFile()
            throws GATInvocationException {
        isLocalFile();

        if (getParent() == null) {
            return null;
        }
        String uriString = location.toString();
        String parentUri = uriString.split(":")[0] + ":" + getParent();

        try {
            return GAT.createFile(gatContext, preferences, new URI(parentUri));
        } catch (Exception e) {
            throw new GATInvocationException("ssh", new Exception(
                    "getAbsoluteFile: " + e));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#isDirectory()
     */
    public boolean isDirectory() throws GATInvocationException {
        isLocalFile();

        try {
            if (getOsType() == XOS) {
                Object[] streams = execCommand("cd " + getPath());
                isDir = (((InputStream) streams[ERR]).available() == 0) ? TRUE
                        : FALSE;
                cleanSession(session, channel);
                return isDir == TRUE;
            } else if (getOsType() == WOS) {
                Object[] streams = execCommand("cd " + toMW(getPath()));
                isDir = (((InputStream) streams[ERR]).available() == 0) ? TRUE
                        : FALSE;
                cleanSession(session, channel);
                return isDir == TRUE;
            } else {
                cleanSession(session, channel);
                throw new GATInvocationException("ssh", new Exception(
                        "Unknown remote OS type"));
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return false; // will not happen doException throws exception
        }
    }

    private void doException(Exception e) throws GATInvocationException {
        if (e instanceof JSchException) {
            if (e.getMessage().equals("Auth fail")) {
                throw new InvalidUsernameOrPasswordException(e);
            }
        } else {
            throw new GATInvocationException("ssh", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#isFile()
     */
    public boolean isFile() throws GATInvocationException {
        /* at least for now */
        isLocalFile();
        return (!isDirectory());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#isHidden()
     */
    public boolean isHidden() throws GATInvocationException {
        isLocalFile();
        switch (getOsType()) {
        /* assume name of the file is not . or .. */
        case XOS:

            if (getName().startsWith(".")) {
                return true;
            } else {
                return false;
            }

        case WOS:
            throw new GATInvocationException("ssh", new Exception(
                    "Not implemented"));
        }

        throw new GATInvocationException("ssh", new Exception(
                "Unknown remote OS type"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#length()
     */
    public long length() throws GATInvocationException {
        isLocalFile();
        if (itExists == UNKNOWN)
            exists();
        if (itExists == FALSE)
            return -1L;
        try {
            if (getOsType() == XOS) {
                return executeLengthCommand("wc -c < " + getPath());
            } else if (getOsType() == WOS) {
                return executeLengthCommand("dir /-C " + toMW(getPath()));
            } else {
                throw new GATInvocationException("Unknown remote OS type");
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return 0L; // will not happen doException throws exception
        }
    }

    private long executeLengthCommand(String command) throws JSchException,
            IOException, GATInvocationException {
        Object[] streams = execCommand(command);
        if (((InputStream) streams[ERR]).available() != 0) {
            cleanSession(session, channel);
            return -1;
        }
        StringBuffer buffer = new StringBuffer();

        int bufferLength = ((InputStream) streams[IN]).available() - 1;
        for (int i = 0; i < bufferLength; i++) {
            buffer.append((char) ((InputStream) streams[IN]).read());
        }
        cleanSession(session, channel);
        return Long.parseLong(buffer.toString().replaceAll("[ \t\n\f\r]", ""));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#list()
     */
    public String[] list() throws GATInvocationException {
        isLocalFile();
        if (isDir == UNKNOWN)
            isDirectory();
        if (isDir == FALSE)
            return null;
        try {
            if (getOsType() == XOS) {
                return executeListCommand("ls -1 " + getPath());
            } else if (getOsType() == WOS) {
                return executeListCommand("dir /-C " + toMW(getPath()));
            } else {
                throw new GATInvocationException("Unknown remote OS type");
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return null; // will not happen doException throws exception;
        }
    }

    private String[] executeListCommand(String command) throws JSchException,
            IOException, GATInvocationException {
        Object[] streams = execCommand(command);
        if (((InputStream) streams[ERR]).available() != 0) {
            cleanSession(session, channel);
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        int bufferLength = ((InputStream) streams[IN]).available() - 1;
        for (int i = 0; i < bufferLength; i++) {
            buffer.append((char) ((InputStream) streams[IN]).read());
        }
        cleanSession(session, channel);
        if (logger.isDebugEnabled()) {
            logger.debug("result read from channel");
        }
        return buffer.toString().split("\n");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#list(java.io.FilenameFilter)
     */
    public String[] list(FilenameFilter filter) throws GATInvocationException {
        isLocalFile();

        if (filter == null) {
            return list();
        }

        String[] fileList = list();
        java.io.File dir = new java.io.File(getPath());
        List<String> resultList = new ArrayList<String>();

        for (int i = 0; i < fileList.length; i++) {
            if (filter.accept(dir, fileList[i])) {
                resultList.add(fileList[i]);
            }
        }

        return (String[]) resultList.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#listFiles()
     */
    public org.gridlab.gat.io.File[] listFiles() throws GATInvocationException {
        isLocalFile();
        String[] r = list();
        org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];
        String uri = location.toString();

        if (!uri.endsWith("/")) {
            uri += "/";
        }

        for (int i = 0; i < r.length; i++) {
            try {
                res[i] = GAT.createFile(gatContext, preferences, new URI(uri
                        + r[i]));
            } catch (Exception e) {
                doException(e);
            }
        }

        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#listFiles(java.io.FileFilter)
     */
    public org.gridlab.gat.io.File[] listFiles(FileFilter arg0)
            throws GATInvocationException {
        isLocalFile();
        String[] r = list();
        org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];
        String dir = getPath();
        String uri = location.toString();

        if (!dir.endsWith("/")) {
            dir += "/";
            uri += "/";
        }

        for (int i = 0; i < r.length; i++) {
            try {
                if (arg0.accept(new java.io.File(dir + r[i]))) {
                    res[i] = GAT.createFile(gatContext, preferences, new URI(
                            uri + r[i]));
                }
            } catch (Exception e) {
                doException(e);
            }
        }

        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#listFiles(java.io.FilenameFilter)
     */
    public org.gridlab.gat.io.File[] listFiles(FilenameFilter arg0)
            throws GATInvocationException {
        isLocalFile();
        String[] r = list(arg0);
        org.gridlab.gat.io.File[] res = new org.gridlab.gat.io.File[r.length];
        String uri = location.toString();

        if (!uri.endsWith("/")) {
            uri += "/";
        }

        for (int i = 0; i < r.length; i++) {
            try {
                res[i] = GAT.createFile(gatContext, preferences, new URI(uri
                        + r[i]));
            } catch (Exception e) {
                doException(e);
            }
        }

        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#mkdir()
     */
    public boolean mkdir() throws GATInvocationException {
        isLocalFile();
        try {
            if (getOsType() == XOS) {
                Object[] streams = execCommand("mkdir " + getPath());
                boolean result = (((InputStream) streams[ERR]).available() == 0);
                cleanSession(session, channel);
                return result;
            } else if (getOsType() == WOS) {
                throw new GATInvocationException("Not yet implemented");
            } else {
                throw new GATInvocationException("Unknown remote OS type");
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return false; // will not happen doException throws exception
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#mkdirs()
     */
    public boolean mkdirs() throws GATInvocationException {
        isLocalFile();

        try {
            if (getOsType() == XOS) {
                Object[] streams = execCommand("mkdir -p " + getPath());
                isDir = (((InputStream) streams[ERR]).available() == 0) ? TRUE
                        : FALSE;
                cleanSession(session, channel);
                return isDir == TRUE;
            } else if (getOsType() == WOS) {
                Object[] streams = execCommand("mkdir " + toMW(getPath()));
                isDir = (((InputStream) streams[ERR]).available() == 0) ? TRUE
                        : FALSE;
                cleanSession(session, channel);
                return isDir == TRUE;
            } else {
                throw new GATInvocationException("Unknown remote OS type");
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return false; // will not happen doException throws exception
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#move(java.net.URI)
     */
    public void move(URI destination) throws GATInvocationException {
        /*
         * may be improved in some cases (if both on the same machine and user,
         * or same machine and different users, but users have appropriate
         * rights), but this is a more general approach
         */
        copy(destination);

        if (!deleteByForceRecursively()) {
            throw new GATInvocationException(
                    "internal error in SshFileAdaptor: could not rename file "
                            + toURI() + " to " + destination);
        }

        try {
            updateLocation(destination);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("ssh", e);
        }

        return;
    }

    public void renameTo(URI destination) throws GATInvocationException {
        move(destination);
        return;
    }

    public boolean renameTo(org.gridlab.gat.io.File file)
            throws GATInvocationException {
        URI destination = file.toGATURI();

        if (logger.isDebugEnabled()) {
            logger.debug("SshFileAdaptor: trying to rename " + location
                    + " to " + destination);
        }

        copy(destination);

        if (!deleteByForceRecursively()) {
            return false;
        }

        try {
            updateLocation(destination);
        } catch (GATObjectCreationException e) {
            throw new GATInvocationException("ssh", e);
        }

        return true;
    }

    private void updateLocation(URI dest) throws GATObjectCreationException,
            GATInvocationException {
        if (isLocalFile) {
            isLocalFile = false;
            localFile = null;
        } else {
            if (dest.refersToLocalHost()) {
                isLocalFile = true;
            }
        }

        location = dest;
        prepareSession(location);

        try {
            session.connect();
        } catch (Exception e) {
            throw new GATObjectCreationException("ssh", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("SshFileAdaptor: started session with "
                    + location.resolveHost() + " using username: "
                    + sui.username + " on port: " + port + " for file: "
                    + location.getPath());
        }

        osType = UNKNOWN;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#setLastModified(long)
     */
    public boolean setLastModified(long lastModified)
            throws GATInvocationException {
        isLocalFile();

        try {
            if (getOsType() == XOS) {
                Object[] streams = execCommand("touch -t "
                        + toTouchDateFormat(lastModified) + " " + getPath());
                boolean result = (((InputStream) streams[ERR]).available() == 0);
                cleanSession(session, channel);
                return result;
            } else if (getOsType() == WOS) {
                throw new GATInvocationException("Not yet implemented");
            } else {
                throw new GATInvocationException("Unknown remote OS type");
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return false; // will not happen doException throws exception
        }
    }

    protected String toTouchDateFormat(long date) {
        Date d = new Date(date);
        // see man touch for the date format
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm.ss");
        return formatter.format(d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gridlab.gat.io.File#setReadOnly()
     */
    public boolean setReadOnly() throws GATInvocationException {
        isLocalFile();
        try {
            if (getOsType() == XOS) {
                Object[] streams = execCommand("chmod a-w " + getPath());
                boolean result = (((InputStream) streams[ERR]).available() == 0);
                cleanSession(session, channel);
                return result;
            } else if (getOsType() == WOS) {
                throw new GATInvocationException("Not yet implemented");
            } else {
                throw new GATInvocationException("Unknown remote OS type");
            }
        } catch (Exception e) {
            cleanSession(session, channel);
            doException(e);
            return false; // will not happen doException throws exception
        }
    }

    private int getOsType() {
        if (osType == UNKNOWN) {
            osType = determineRemoteOS();
        }
        return osType;
    }

    private Object[] execCommand(String command) throws JSchException,
            IOException, GATInvocationException {
        Object[] result = new Object[2];
        session = jsch.getSession(sui.username, location.getHost(), port);
        session.setUserInfo(sui);
        session.connect();
        channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        result[IN] = ((ChannelExec) channel).getInputStream();
        result[ERR] = ((ChannelExec) channel).getErrStream();
        /*
         * try { result[OUT] = ((ChannelExec) channel).getOutputStream(); }
         * catch (Exception e) { result[OUT] = null; }
         */
        channel.connect();
        waitForEOF();
        return result;
    }

}
