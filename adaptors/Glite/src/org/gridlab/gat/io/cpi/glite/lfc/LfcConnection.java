package org.gridlab.gat.io.cpi.glite.lfc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.X509Credential;
import org.globus.gsi.CredentialException;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.GlobusGSSManagerImpl;
import org.gridforum.jgss.ExtendedGSSContext;
import org.gridlab.gat.io.attributes.*;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

/**
 * Low-Level connection to an LFC server.
 * 
 * @author Max Berger
 * @author Jerome Revillard
 */
public class LfcConnection {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(LfcConnection.class);
    /**
     * Bitmask for <b>file mode</b>
     */
    public static final int S_IFMT = 0xF000;
    /**
     * Bitmask for <b>symbolic link</b>
     */
    public static final int S_IFLNK = 0xA000;
    /**
     * Bitmask for <b>regular file</b>
     */
    public static final int S_IFREG = 0x8000;
    /**
     * Bitmask for <b>directory</b>
     */
    public static final int S_IFDIR = 0x4000;
    /**
     * Bitmask for <b>set user ID on execution</b>
     */
    public static final int S_ISUID = 0004000;
    /**
     * Bitmask for <b>set group ID on execution</b>
     */
    public static final int S_ISGID = 0002000;
    /**
     * Bitmask for <b>sticky bit</b>
     */
    public static final int S_ISVTX = 0001000;
    /**
     * Bitmask for <b>read by owner</b>
     */
    public static final int S_IRUSR = 0000400;
    /**
     * Bitmask for <b>write by owner</b>
     */
    public static final int S_IWUSR = 0000200;
    /**
     * Bitmask for <b>execute/search by owner</b>
     */
    public static final int S_IXUSR = 0000100;
    /**
     * Bitmask for <b>read by group</b>
     */
    public static final int S_IRGRP = 0000040;
    /**
     * Bitmask for <b>write by group</b>
     */
    public static final int S_IWGRP = 0000020;
    /**
     * Bitmask for <b>execute/search by group</b>
     */
    public static final int S_IXGRP = 0000010;
    /**
     * Bitmask for <b>read by others</b>
     */
    public static final int S_IROTH = 0000004;
    /**
     * Bitmask for <b>write by others</b>
     */
    public static final int S_IWOTH = 0000002;
    /**
     * Bitmask for <b>execute/search by others</b>
     */
    public static final int S_IXOTH = 0000001;

    private static final int HEADER_SIZE = 12;

    private static final int BUF_SIZE = 10240;

    private static final int CSEC_TOKEN_MAGIC_1 = 0xCA03;
    private static final int CSEC_TOKEN_TYPE_PROTOCOL_REQ = 0x1;
    private static final int CSEC_TOKEN_TYPE_PROTOCOL_RESP = 0x2;
    private static final int CSEC_TOKEN_TYPE_HANDSHAKE = 0x3;
    private static final int CSEC_TOKEN_TYPE_HANDSHAKE_FINAL = 0x5;
    // private static final int CSEC_TOKEN_TYPE_ERROR = 0x6;

    private static final int CNS_MAGIC = 0x030E1301;

    private static final int CNS_RESP_MSG_ERROR = 1;
    private static final int CNS_RESP_MSG_DATA = 2;
    private static final int CNS_RESP_RC = 3;
    private static final int CNS_RESP_IRC = 4;
    private static final int CNS_RESP_MSG_GROUPS = 10;
    private static final int CNS_RESP_MSG_SUMMARY = 11;

    private static final int CNS_ACCESS = 0;
    // private static final int CNS_CHDIR = 1;
    private static final int CNS_CHMOD = 2;
    private static final int CNS_CHOWN = 3;
    private static final int CNS_CREAT = 4;
    private static final int CNS_MKDIR = 5;
    private static final int CNS_RENAME = 6;
    private static final int CNS_RMDIR = 7;
    private static final int CNS_STAT = 8;
    private static final int CNS_UNLINK = 9;
    private static final int CNS_OPENDIR = 10;
    private static final int CNS_READDIR = 11;
    private static final int CNS_CLOSEDIR = 12;
    // private static final int CNS_OPEN = 13;
    // private static final int CNS_CLOSE = 14;
    // private static final int CNS_SETATIME = 15;
    private static final int CNS_SETFSIZE = 16;
    // private static final int CNS_SHUTDOWN = 17;
    // private static final int CNS_GETSEGAT = 18;
    // private static final int CNS_SETSEGAT = 19;
    // private static final int CNS_LISTTAPE = 20;
    // private static final int CNS_ENDLIST = 21;
    // private static final int CNS_GETPATH = 22;
    private static final int CNS_DELETE = 23;
    // private static final int CNS_UNDELETE = 24;
    // private static final int CNS_CHCLASS = 25;
    // private static final int CNS_DELCLASS = 26;
    // private static final int CNS_ENTCLASS = 27;
    // private static final int CNS_MODCLASS = 28;
    // private static final int CNS_QRYCLASS = 29;
    // private static final int CNS_LISTCLASS = 30;
    // private static final int CNS_DELCOMMENT = 31;
    // private static final int CNS_GETCOMMENT = 32;
    // private static final int CNS_SETCOMMENT = 33;
    // private static final int CNS_UTIME = 34;
    // private static final int CNS_REPLACESEG = 35;
    // private static final int CNS_GETACL = 37;
    // private static final int CNS_SETACL = 38;
    private static final int CNS_LCHOWN = 39;
    private static final int CNS_LSTAT = 40;
    // private static final int CNS_READLINK = 41;
    // private static final int CNS_SYMLINK = 42;
    private static final int CNS_ADDREPLICA = 43;
    private static final int CNS_DELREPLICA = 44;
    private static final int CNS_LISTREPLICA = 45;
    // private static final int CNS_STARTTRANS = 46;
    // private static final int CNS_ENDTRANS = 47;
    // private static final int CNS_ABORTTRANS = 48;
    // private static final int CNS_LISTLINKS = 49;
    // private static final int CNS_SETFSIZEG = 50;
    // private static final int CNS_STATG = 51;
    // private static final int CNS_STATR = 52;
    // private static final int CNS_SETPTIME = 53;
    // private static final int CNS_SETRATIME = 54;
    // private static final int CNS_SETRSTATUS = 55;
    // private static final int CNS_ACCESSR = 56;
    // private static final int CNS_LISTREP4GC = 57;
    // private static final int CNS_LISTREPLICAX = 58;
    // private static final int CNS_STARTSESS = 59;
    // private static final int CNS_ENDSESS = 60;
    // private static final int CNS_DU = 61;
    private static final int CNS_GETGRPID = 62;
    private static final int CNS_GETGRPNAM = 63;
    // private static final int CNS_GETIDMAP = 64;
    private static final int CNS_GETUSRID = 65;
    private static final int CNS_GETUSRNAM = 66;
    // private static final int CNS_MODGRPMAP = 67;
    // private static final int CNS_MODUSRMAP = 68;
    // private static final int CNS_RMGRPMAP = 69;
    // private static final int CNS_RMUSRMAP = 70;
    // private static final int CNS_GETLINKS = 71;
    // private static final int CNS_GETREPLICA = 72;
    // private static final int CNS_ENTGRPMAP = 73;
    // private static final int CNS_ENTUSRMAP = 74;
    // private static final int CNS_SETRTYPE = 75;
    // private static final int CNS_MODREPLICA = 76;
    // private static final int CNS_GETREPLICAX = 77;
    // private static final int CNS_LISTREPSET = 78;
    // private static final int CNS_SETRLTIME = 79;
    // private static final int CNS_GETREPLICAS = 80;
    private static final int CNS_GETGRPNAMES = 81;
    // private static final int CNS_PING = 82;
    private static final int CNS_DELFILES = 83;
    // private static final int CNS_DELFILESBYP = 84;
    // private static final int CNS_DELREPLICAS = 85;
    // private static final int CNS_GETGRPMAP = 86;
    // private static final int CNS_GETUSRMAP = 87;
    // private static final int CNS_GETREPLICAL = 88;

    private static final int CNS_MAGIC2 = 0x030E1302;
    private static final int CNS_MAGIC4 = 0x030E1304;

    private static final byte REQUEST_GSI_TOKEN[] = { 0x00, 0x00, 0x00, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x47, 0x53, 0x49,
            0x00, 0x49, 0x44, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x02, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01 };

    /**
     * Messages that are returned when CNS_RESP_IRC or CNS_RESP_RC are received
     */
    private static final Map<Integer, String> CNS_ERRORS = new TreeMap<Integer, String>();

    /**
     * Values that can be passed to the access function.
     */
    public static enum AccessType {
        READ_OK(4), /* Test for read permission. */
        WRITE_OK(2), /* Test for write permission. */
        EXECUTE_OK(1), /* Test for execute permission. */
        EXIST_OK(0); /* Test for existence. */

        private int value = -1;

        private AccessType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private final ByteBuffer sendBuf = ByteBuffer.allocateDirect(BUF_SIZE);
    private ByteBuffer recvBuf = ByteBuffer.allocateDirect(BUF_SIZE);
    private final ByteChannel channel;

    private final GSSCredential gssCredential;
    private final String host;
    private final int port;
    private final String proxyPath;

    public LfcConnection(String host, int port, final String proxyPath)
            throws IOException {
        this.host = host;
        this.port = port;
        this.proxyPath = proxyPath;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Proxy is " + proxyPath);
            }
            X509Credential credential = new X509Credential(proxyPath);
            gssCredential = new GlobusGSSCredentialImpl(credential, 0);
        } catch (GSSException e) {
            LOGGER.warn(e.toString());
            throw new IOException("Failed to load credentials");
        } catch (CredentialException e) {
            LOGGER.warn(e.toString());
            throw new IOException("Failed to load credentials");
        }
        channel = SocketChannel.open(new InetSocketAddress(host, port));
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Establishing Connection with " + host + ":" + port);
        }
        authenticate();
    }

    private void preparePacket(int magic, int command) {
        sendBuf.clear();
        sendBuf.putInt(magic);
        sendBuf.putInt(command);
        sendBuf.mark();
        sendBuf.putInt(0);
    }

    private int sendAndReceive(boolean includeHeaderInLength)
            throws IOException {
        send(includeHeaderInLength);
        return receive();
    }

    private void send(boolean includeHeaderInLength) throws IOException {
        int posNow = sendBuf.position();
        sendBuf.reset();
        if (includeHeaderInLength)
            sendBuf.putInt(posNow);
        else
            sendBuf.putInt(posNow - HEADER_SIZE);
        sendBuf.position(posNow);
        sendBuf.flip();
        channel.write(sendBuf);
    }

    private int receive() throws IOException {
        recvBuf.clear();
        channel.read(recvBuf);
        recvBuf.flip();
        int magic = recvBuf.getInt();
        int type = recvBuf.getInt();
        // For whatever reason, the reply never includes the size of the Header.
        int sizeOrError = recvBuf.getInt();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received M/T/S: " + magic + " " + type + " "
                    + sizeOrError);
        }

        if (magic == CSEC_TOKEN_MAGIC_1) {
            if ((type != CSEC_TOKEN_TYPE_PROTOCOL_RESP)
                    && (type != CSEC_TOKEN_TYPE_HANDSHAKE)
                    && (type != CSEC_TOKEN_TYPE_HANDSHAKE_FINAL)) {
                throw new ReceiveException(sizeOrError,
                        "Received invalid CSEC Type: " + type);
            }
        } else if (magic >= CNS_MAGIC) { // Fix: was ==. Can also be CNS_MAGIC2?
            if ((type == CNS_RESP_IRC) || (type == CNS_RESP_RC)) {
                if (sizeOrError == 0)
                    return 0;
                String errorMessage = CNS_ERRORS.get(sizeOrError);
                if (errorMessage == null)
                    errorMessage = "Recieved Error " + sizeOrError;
                throw new ReceiveException(sizeOrError, errorMessage);
            } else if (type == CNS_RESP_MSG_ERROR) {
                String errorMessage = getString();
                throw new ReceiveException(sizeOrError, errorMessage);
            } else if ((type != CNS_RESP_MSG_DATA)
                    && (type != CNS_RESP_MSG_SUMMARY)
                    && (type != CNS_RESP_MSG_GROUPS)) {
                throw new ReceiveException(sizeOrError,
                        "Received invalid CNS Type: " + type);
            }
        } else {
            throw new ReceiveException(sizeOrError,
                    "Recieved invalid Magic/Type: " + magic + "/" + type);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Limit: " + recvBuf.limit() + ", Pos: "
                    + recvBuf.position() + " MinContent: " + sizeOrError
                    + " Avail " + recvBuf.remaining());
        }
        while (recvBuf.remaining() < sizeOrError) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Reading once more: " + recvBuf.remaining()
                        + " < " + sizeOrError);
            }
            // TODO: There must be an easier method of reading more data.
            byte[] temp = new byte[recvBuf.remaining()];
            recvBuf.get(temp);
            if (recvBuf.capacity() < sizeOrError) {
                recvBuf = ByteBuffer.allocateDirect(sizeOrError);
            } else {
                recvBuf.clear();
            }
            recvBuf.put(temp);
            channel.read(recvBuf);
            recvBuf.flip();
        }

        return sizeOrError;
    }

    private void addIDs() {
        int uid = 0;
        int gid = 0;
        sendBuf.putInt(uid);
        sendBuf.putInt(gid);
    }

    private void authenticate() throws IOException {
        preparePacket(CSEC_TOKEN_MAGIC_1, CSEC_TOKEN_TYPE_PROTOCOL_REQ);
        sendBuf.put(REQUEST_GSI_TOKEN);
        sendAndReceive(false);
        GSSManager manager = new GlobusGSSManagerImpl();
        try {
            ExtendedGSSContext secureContext = (ExtendedGSSContext) manager
                    .createContext(null, GSSConstants.MECH_OID, gssCredential,
                            12 * 3600);

            secureContext.requestMutualAuth(true);
            secureContext.requestAnonymity(false);
            secureContext.requestConf(false);
            secureContext.requestCredDeleg(false);
            secureContext.setOption(GSSConstants.GSS_MODE,
                    GSIConstants.MODE_GSI);
            secureContext.setOption(GSSConstants.REJECT_LIMITED_PROXY,
                    Boolean.FALSE);
            byte[] recvToken = new byte[0];
            while (!secureContext.isEstablished()) {
                byte[] sendToken = secureContext.initSecContext(recvToken, 0,
                        recvToken.length);
                LOGGER.debug("called initSecContext, doing another iteration");

                if (sendToken != null) {
                    preparePacket(CSEC_TOKEN_MAGIC_1, CSEC_TOKEN_TYPE_HANDSHAKE);
                    sendBuf.put(sendToken);
                    send(false);
                }

                if (!secureContext.isEstablished()) {
                    int l = receive();
                    recvToken = new byte[l];
                    recvBuf.get(recvToken);
                }
            }

        } catch (GSSException e) {
            LOGGER.warn(e.toString());
            throw new IOException("Error processing credential");
        }
        LOGGER.debug("Secure Context established!");
    }

    private void putString(String s) {
        try {
            if (s != null) {
                // TODO: Check if UTF-8 is correct!
                sendBuf.put(s.getBytes("UTF-8"));
            }
        } catch (java.io.UnsupportedEncodingException e) {
            LOGGER.warn(e.toString());
        }
        sendBuf.put((byte) 0);
    }

    private String getString() throws IOException {
        // TODO: This uses Latin-1!
        StringBuilder builder = new StringBuilder();
        byte b = recvBuf.get();
        while (b != 0) {
            builder.append((char) b);
            if (recvBuf.remaining() == 0) {
                recvBuf.clear();
                channel.read(recvBuf);
            }
            b = recvBuf.get();
        }
        return builder.toString();
    }

    /**
     * 
     */
    public LFCFile stat(String path) throws IOException {
        preparePacket(CNS_MAGIC, CNS_STAT);
        addIDs();
        long cwd = 0L;
        sendBuf.putLong(cwd);
        sendBuf.putLong(0L); // 0
        putString(path);
        sendAndReceive(true);

        LFCFile file = new LFCFile(recvBuf, false, false, false, false);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(file.toString());
        }
        return file;
    }

    public LFCFile lstat(String path) throws IOException {
        preparePacket(CNS_MAGIC, CNS_LSTAT);
        addIDs();
        long cwd = 0L;
        sendBuf.putLong(cwd);
        sendBuf.putLong(0L); // 0
        putString(path);
        sendAndReceive(true);

        LFCFile file = new LFCFile(recvBuf, false, false, false, false);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(file.toString());
        }
        return file;
    }

    public long opendir(String path, String guid) throws IOException {
        if (guid == null) {
            preparePacket(CNS_MAGIC, CNS_OPENDIR);
        } else {
            preparePacket(CNS_MAGIC2, CNS_OPENDIR);
        }
        addIDs();
        long cwd = 0L; // Current Working Directory
        sendBuf.putLong(cwd);
        putString(path);
        putString(guid);
        sendAndReceive(true);

        long fileId = recvBuf.getLong();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Directory opened - fileID: " + fileId);
        }
        return fileId;
    }

    public void closedir() throws IOException {
        preparePacket(CNS_MAGIC2, CNS_CLOSEDIR);
        sendAndReceive(false);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Directory closed");
        }
    }

    public int getGrpByName(String grpName) throws IOException {
        if ("root".equals(grpName)) {
            return 0;
        }
        preparePacket(CNS_MAGIC, CNS_GETGRPID);
        putString(grpName);
        sendAndReceive(true);

        int s = recvBuf.getInt();
        return s;
    }

    public String getGrpByGid(int gid) throws IOException {
        if (gid == 0) {
            return "root";
        }
        preparePacket(CNS_MAGIC, CNS_GETGRPNAM);
        sendBuf.putShort((short) 0);
        sendBuf.putShort((short) gid);
        sendAndReceive(true);

        return getString();
    }

    public Collection<String> getGrpByGids(int[] gids) throws IOException {
        ArrayList<Integer> rootGIDIndexes = new ArrayList<Integer>();
        ArrayList<Integer> newGids = new ArrayList<Integer>();
        for (int i = 0; i < gids.length; i++) {
            if (gids[i] == 0) {
                rootGIDIndexes.add(i);
            } else {
                newGids.add(gids[i]);
            }
        }

        gids = new int[newGids.size()];
        for (int i = 0; i < newGids.size(); i++) {
            gids[i] = newGids.get(i);
        }

        preparePacket(CNS_MAGIC, CNS_GETGRPNAMES);
        sendBuf.putShort((short) 0);
        sendBuf.putShort((short) gids.length);
        for (int i = 0; i < gids.length; i++) {
            sendBuf.putInt(gids[i]);
        }
        sendAndReceive(true);

        Collection<String> grpNames = new ArrayList<String>(gids.length);
        for (int i = 0; i < gids.length; i++) {
            if (rootGIDIndexes.contains(i)) {
                grpNames.add("root");
            } else {
                grpNames.add(getString());
            }
        }
        return grpNames;
    }

    public int getUsrByName(String usrName) throws IOException {
        if ("root".equals(usrName)) {
            return 0;
        }
        preparePacket(CNS_MAGIC, CNS_GETUSRID);
        putString(usrName);
        sendAndReceive(true);

        int s = recvBuf.getInt();
        return s;
    }

    public String getUsrByUid(int uid) throws IOException {
        if (uid == 0) {
            return "root";
        }
        preparePacket(CNS_MAGIC, CNS_GETUSRNAM);
        sendBuf.putShort((short) 0);
        sendBuf.putShort((short) uid);
        sendAndReceive(true);

        return getString();
    }

    /**
     * Test if a specific access is allowed.
     * 
     * @param path
     *            path that has to be tested
     * @param accessType
     *            The access to test
     * @throws IOException
     *             if something wrong occurs
     * @throws ReceiveException
     *             if received an error message from the LFC
     */
    public void access(String path, AccessType accessType) throws IOException,
            ReceiveException {
        preparePacket(CNS_MAGIC, CNS_ACCESS);
        addIDs();
        long cwd = 0L;
        sendBuf.putLong(cwd);
        putString(path);
        sendBuf.putInt(accessType.getValue());
        sendAndReceive(true);
    }

    /**
     * Read a directory entry
     * 
     * @param fileID
     *            The id of the directory
     * @return A collection of {@link LFCFile} which are inside the directory
     * @throws IOException
     *             if something wrong occurs
     */
    public Collection<LFCFile> readdir(long fileID) throws IOException {
        preparePacket(CNS_MAGIC, CNS_READDIR);
        addIDs();
        sendBuf.putShort((short) 1); // 1 = full list (w/o comments), 0 = names
                                     // only
        sendBuf.putShort((short) 0);
        sendBuf.putLong(fileID);
        sendBuf.putShort((short) 1);
        sendAndReceive(true);

        // Jerome: I don't know why I have to do that but at least it works....
        preparePacket(CNS_MAGIC, CNS_READDIR);
        sendAndReceive(true);

        short count = recvBuf.getShort();
        Collection<LFCFile> lfcFiles = new ArrayList<LFCFile>(count);

        String debug = null;
        for (short i = 0; i < count; i++) {
            LFCFile file = new LFCFile(recvBuf, true, false, false, false);
            lfcFiles.add(file);
            if (LOGGER.isDebugEnabled()) {
                if (i == 0) {
                    debug = "\nDirectory content (fileID: " + fileID + "):";
                }
                debug += "\n\t- " + (i + 1) + ")\t" + file.toString();
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(debug);
        }
        return lfcFiles;
    }

    /**
     * Use UNLINK command instead of DELETE to remove files. DELETE command is
     * for CASTOR entries only
     */
    public int delete(String path) throws IOException {
        preparePacket(CNS_MAGIC, CNS_DELETE);
        addIDs();
        long cwd = 0L;
        sendBuf.putLong(cwd);
        putString(path);
        sendAndReceive(true);

        int execResult = recvBuf.getInt();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("delete result: " + execResult);
        }
        return execResult;
    }

    /**
     * Use this command instead of DELETE to remove files. DELETE command is for
     * CASTOR entries only
     */
    public void unlink(String path) throws IOException {
        preparePacket(CNS_MAGIC, CNS_UNLINK);
        addIDs();
        long cwd = 0L; // Current Working Directory
        sendBuf.putLong(cwd);
        putString(path);
        sendAndReceive(true);
    }

    /**
     * Set the file size
     */
    public void setfsize(String path, long fileSize) throws IOException {
        preparePacket(CNS_MAGIC2, CNS_SETFSIZE);
        addIDs();
        long cwd = 0L; // Current Working Directory
        sendBuf.putLong(cwd);
        sendBuf.putLong(0L);
        putString(path);
        sendBuf.putLong(fileSize);
        putString(null);
        putString(null);
        sendAndReceive(true);
    }

    /**
     * Rename a file or a directory
     */
    public void rename(String oldPath, String newPath) throws IOException {
        preparePacket(CNS_MAGIC, CNS_RENAME);
        addIDs();
        long cwd = 0L; // Current Working Directory
        sendBuf.putLong(cwd);
        putString(oldPath);
        putString(newPath);
        sendAndReceive(true);
    }

    /**
     * Change the permissions of a file or a directory. If the path represent a
     * symbolic link, the pointed file/directory will be modified, not the
     * symbolic link itself. TODO: Check the symbolic link behavior.
     */
    public void chmod(String path, int mode) throws IOException {
        preparePacket(CNS_MAGIC, CNS_CHMOD);
        addIDs();
        long cwd = 0L; // Current Working Directory
        sendBuf.putLong(cwd);
        putString(path);
        mode &= 07777;
        sendBuf.putInt(mode);
        sendAndReceive(true);
    }

    /**
     * Change the owner and the group of a file or a directory. If the path
     * represent a symbolic link, the pointed file/directory will be modified,
     * not the symbolic link itself.
     */
    public void chown(String path, int new_uid, int new_gid) throws IOException {
        preparePacket(CNS_MAGIC, CNS_CHOWN);
        addIDs();
        long cwd = 0L; // Current Working Directory
        sendBuf.putLong(cwd);
        putString(path);
        sendBuf.putInt(new_uid);
        sendBuf.putInt(new_gid);
        sendAndReceive(true);
    }

    /**
     * Change the owner and the group of a file or a directory. If the path
     * represent a symbolic link, this later itself will be modified
     */
    public void lchown(String path, int new_uid, int new_gid)
            throws IOException {
        preparePacket(CNS_MAGIC, CNS_LCHOWN);
        addIDs();
        long cwd = 0L; // Current Working Directory
        sendBuf.putLong(cwd);
        putString(path);
        sendBuf.putInt(new_uid);
        sendBuf.putInt(new_gid);
        sendAndReceive(true);
    }

    /**
     * List all the file replicas
     */
    public Collection<LFCReplica> listReplica(String path, String guid)
            throws IOException {
        preparePacket(CNS_MAGIC2, CNS_LISTREPLICA);
        addIDs();
        sendBuf.putShort((short) 0); // Size of nbentry
        long cwd = 0L; // Current Working Directory
        sendBuf.putLong(cwd);
        putString(path);
        putString(guid);
        sendBuf.putShort((short) 1); // BOL = Beginning of List
        sendAndReceive(true);

        short count = recvBuf.getShort();

        Collection<LFCReplica> srms = new ArrayList<LFCReplica>(count);

        String debug = null;
        for (short i = 0; i < count; i++) {
            LFCReplica replica = new LFCReplica(recvBuf);
            srms.add(replica);
            if (LOGGER.isDebugEnabled()) {
                if (i == 0) {
                    debug = "\nFile replicas (" + count + "):";
                }
                debug += "\n\t- " + (i + 1) + "\t:" + replica.toString();
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(debug);
        }
        return srms;
    }

    /**
     * Use UNLINK command instead of DELFILES to remove files.
     */
    public boolean delFiles(String[] guids, boolean force) throws IOException {
        preparePacket(CNS_MAGIC, CNS_DELFILES);
        addIDs();
        final short argtype = 0;
        final short sforce;
        if (force) {
            sforce = 1;
        } else {
            sforce = 0;
        }
        sendBuf.putShort(argtype);
        sendBuf.putShort(sforce);
        sendBuf.putInt(guids.length); // nbguids
        for (int i = 0; i < guids.length; i++) {
            putString(guids[i]);
        }
        sendAndReceive(true);
        int nbstatuses = recvBuf.getInt();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Statuses: " + nbstatuses);
        }
        return nbstatuses == 0;
    }

    public void rmdir(String path) throws IOException {
        long cwd = 0L;
        preparePacket(CNS_MAGIC, CNS_RMDIR);
        addIDs();
        sendBuf.putLong(cwd);
        putString(path);
        sendAndReceive(true);
    }

    public void creat(String path, String guid) throws IOException {
        short mask = 0;
        long cwd = 0L;
        int mode = 0666;
        preparePacket(CNS_MAGIC2, CNS_CREAT);
        addIDs();
        sendBuf.putShort(mask);
        sendBuf.putLong(cwd);
        putString(path);
        sendBuf.putInt(mode);
        putString(guid);
        sendAndReceive(true);
        // TODO: Check status!
    }

    public void mkdir(String path, String guid) throws IOException {
        short mask = 0;
        long cwd = 0L;
        int mode = 0777;
        preparePacket(CNS_MAGIC2, CNS_MKDIR);
        addIDs();
        sendBuf.putShort(mask);
        sendBuf.putLong(cwd);
        putString(path);
        sendBuf.putInt(mode);
        putString(guid);
        sendAndReceive(true);
    }

    /**
     * It removes replica information from catalog, data stored on Storage
     * Element stays intact
     */
    public void delReplica(String guid, String replicaUri) throws IOException {
        long id = 0L;
        preparePacket(CNS_MAGIC, CNS_DELREPLICA);
        addIDs();
        sendBuf.putLong(id);
        putString(guid);
        putString(replicaUri);
        sendAndReceive(true);
    }

    public void addReplica(String guid, URI replicaUri) throws IOException {
        preparePacket(CNS_MAGIC4, CNS_ADDREPLICA);
        this.addIDs();
        sendBuf.putLong(0L); // uniqueId
        this.putString(guid);
        this.putString(replicaUri.getHost());
        this.putString(replicaUri.toString());
        sendBuf.put((byte) '-'); // status;
        sendBuf.put((byte) 'P'); // file type
        this.putString(null); // pool name
        this.putString(null); // fs
        sendBuf.put((byte) 'P'); // r_type
        this.putString(null); // setname
        this.sendAndReceive(true);
    }

    static {
        CNS_ERRORS.put(1, "Operation not permitted");
        CNS_ERRORS.put(2, "No such file or directory");
        CNS_ERRORS.put(3, "No such process");
        CNS_ERRORS.put(4, "Interrupted system call");
        CNS_ERRORS.put(5, "I/O error");
        CNS_ERRORS.put(6, "No such device or address");
        CNS_ERRORS.put(7, "Argument list too long");
        CNS_ERRORS.put(8, "Exec format error");
        CNS_ERRORS.put(9, "Bad file number");
        CNS_ERRORS.put(10, "No child processes");
        CNS_ERRORS.put(11, "Try again");
        CNS_ERRORS.put(12, "Out of memory");
        CNS_ERRORS.put(13, "Permission denied");
        CNS_ERRORS.put(14, "Bad address");
        CNS_ERRORS.put(15, "Block device required");
        CNS_ERRORS.put(16, "Device or resource busy");
        CNS_ERRORS.put(17, "File exists");
        CNS_ERRORS.put(18, "Cross-device link");
        CNS_ERRORS.put(19, "No such device");
        CNS_ERRORS.put(20, "Not a directory");
        CNS_ERRORS.put(21, "Is a directory");
        CNS_ERRORS.put(22, "Invalid argument");
        CNS_ERRORS.put(23, "File table overflow");
        CNS_ERRORS.put(24, "Too many open files");
        CNS_ERRORS.put(25, "Not a typewriter");
        CNS_ERRORS.put(26, "Text file busy");
        CNS_ERRORS.put(27, "File too large");
        CNS_ERRORS.put(28, "No space left on device");
        CNS_ERRORS.put(29, "Illegal seek");
        CNS_ERRORS.put(30, "Read-only file system");
        CNS_ERRORS.put(31, "Too many links");
        CNS_ERRORS.put(32, "Broken pipe");
        CNS_ERRORS.put(33, "Math argument out of domain of func");
        CNS_ERRORS.put(34, "Math result not representable");
    }

    /**
     * Representation of a File or a Directory in the file catalog
     * 
     * @author Jerome Revillard
     * 
     */
    public class LFCFile implements PosixFileAttributes {
        private String fileName; // name of the file/dir
        private String guid; // global unique id
        private String comment; // user comment of the file
        private String chksumType; // checksum type
        private String chksumValue; // checksum value
        private String userName;
        private String groupName;

        private long aDate; // last access time
        private long mDate; // last modification
        private long cDate; // last meta-data modification

        private long fileId; // unique id
        private long fileSize; // size of the file (dirs are sized 0)

        private int nLink; // number of children
        private int uid; // user id
        private int gid; // group id

        private short fileMode; // see description on the end of the file
        private short fileClass; // 1 = experiment, 2 = user (don't know what is
                                 // this for)
        private byte status; // '-' = online, 'm' = migrated (don't know what is
                             // this for)

        public LFCFile(ByteBuffer byteBuffer, final boolean readName,
                final boolean readGuid, final boolean readCheckSum,
                final boolean readComment) throws IOException {
            this.fileId = byteBuffer.getLong();
            if (readGuid) {
                this.guid = getString();
            }
            this.fileMode = byteBuffer.getShort();
            this.nLink = byteBuffer.getInt();
            this.uid = byteBuffer.getInt();
            this.gid = byteBuffer.getInt();
            this.fileSize = byteBuffer.getLong();

            this.aDate = byteBuffer.getLong() * 1000;
            this.mDate = byteBuffer.getLong() * 1000;
            this.cDate = byteBuffer.getLong() * 1000;

            this.fileClass = (byteBuffer.getShort());
            this.status = (byteBuffer.get());
            if (readCheckSum) {
                this.chksumType = getString();
                this.chksumValue = getString();
            }
            if (readName) {
                this.fileName = getString();
            }
            if (readComment) {
                this.comment = getString();
            }

            LfcConnection lfcConnection = new LfcConnection(host, port,
                    proxyPath);
            try {
                this.userName = lfcConnection.getUsrByUid(uid);
            } finally {
                lfcConnection.close();
            }
            lfcConnection = new LfcConnection(host, port, proxyPath);
            try {
                this.groupName = lfcConnection.getGrpByGid(gid);
            } finally {
                lfcConnection.close();
            }

        }

        public String getFileName() {
            return fileName;
        }

        public String getGuid() {
            return guid;
        }

        public String getComment() {
            return comment;
        }

        public String getChksumType() {
            return chksumType;
        }

        public String getChksumValue() {
            return chksumValue;
        }

        public long getFileId() {
            return fileId;
        }

        public short getFileClass() {
            return fileClass;
        }

        public byte getStatus() {
            return status;
        }

        /**
         * Creates <code>String</code> with linux-like permissions of this file.
         * 
         * @return linux-like permission description
         */
        private String getPermissions() {
            StringBuilder permisions = new StringBuilder(10);
            int mode = this.fileMode;

            if ((mode & S_IFDIR) != 0) {
                permisions.append('d');
            } else {
                permisions.append('-');
            }

            if ((mode & S_IRUSR) != 0) {
                permisions.append('r');
            } else {
                permisions.append('-');
            }

            if ((mode & S_IWUSR) != 0) {
                permisions.append('w');
            } else {
                permisions.append('-');
            }

            if ((mode & S_IXUSR) != 0) {
                permisions.append('x');
            } else {
                permisions.append('-');
            }

            if ((mode & S_IRGRP) != 0) {
                permisions.append('r');
            } else {
                permisions.append('-');
            }

            if ((mode & S_IWGRP) != 0) {
                permisions.append('w');
            } else {
                permisions.append('-');
            }

            if ((mode & S_IXGRP) != 0) {
                permisions.append('x');
            } else {
                permisions.append('-');
            }

            if ((mode & S_IROTH) != 0) {
                permisions.append('r');
            } else {
                permisions.append('-');
            }

            if ((mode & S_IWOTH) != 0) {
                permisions.append('w');
            } else {
                permisions.append('-');
            }

            if ((mode & S_IXOTH) != 0) {
                permisions.append('x');
            } else {
                permisions.append('-');
            }

            return permisions.toString();
        }

        /**
         * @return The string representation of the LFCFile object
         */
        @Override
        public String toString() {

            String tostring = "";
            if (fileName != null) {
                tostring += "Name: " + fileName;
            }
            if (guid != null)
                tostring += (tostring.equals("") ? "" : " - ") + "guid: "
                        + guid;
            if (comment != null)
                tostring += (tostring.equals("") ? "" : " - ") + "comment: "
                        + comment;
            if (chksumType != null) {
                tostring += (tostring.equals("") ? "" : " - ") + "chksumType: "
                        + chksumType;
                tostring += " - chksumValue: " + chksumValue;
            }

            tostring += (tostring.equals("") ? "" : " - ") + "aDate: " + aDate;
            tostring += " - mDate: " + mDate;
            tostring += " - cDate: " + cDate;

            tostring += " - fileId: " + fileId;
            tostring += " - fileSize: " + fileSize;
            tostring += " - nLink: " + nLink;
            tostring += " - uid: " + uid;
            tostring += " - gid: " + gid;

            tostring += " - fileMode: " + getPermissions();
            tostring += " - fileClass: " + fileClass;
            tostring += " - status: " + status;

            return tostring;
        }

        public GroupPrincipal group() {
            return new LFCGroup(gid, groupName);
        }

        public UserPrincipal owner() {
            return new LFCUser(uid, userName);
        }

        public Set<PosixFilePermission> permissions() {
            HashSet<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
            if ((this.fileMode & S_IRUSR) != 0)
                perms.add(PosixFilePermission.OWNER_READ);
            if ((this.fileMode & S_IWUSR) != 0)
                perms.add(PosixFilePermission.OWNER_WRITE);
            if ((this.fileMode & S_IXUSR) != 0)
                perms.add(PosixFilePermission.OWNER_EXECUTE);

            if ((this.fileMode & S_IRGRP) != 0)
                perms.add(PosixFilePermission.GROUP_READ);
            if ((this.fileMode & S_IWGRP) != 0)
                perms.add(PosixFilePermission.GROUP_WRITE);
            if ((this.fileMode & S_IXGRP) != 0)
                perms.add(PosixFilePermission.GROUP_EXECUTE);

            if ((this.fileMode & S_IROTH) != 0)
                perms.add(PosixFilePermission.OTHERS_READ);
            if ((this.fileMode & S_IWOTH) != 0)
                perms.add(PosixFilePermission.OTHERS_WRITE);
            if ((this.fileMode & S_IXOTH) != 0)
                perms.add(PosixFilePermission.OTHERS_EXECUTE);

            return perms;
        }

        public long creationTime() {
            return mDate;
        }

        public Object fileKey() {
            return null;
        }

        public boolean isOther() {
            return (!isRegularFile() && !isDirectory() && !isSymbolicLink());
        }

        public boolean isRegularFile() {
            return ((this.fileMode & S_IFMT) == S_IFREG);
        }

        public boolean isDirectory() {
            return ((this.fileMode & S_IFMT) == S_IFDIR);
        }

        public boolean isSymbolicLink() {
            return ((this.fileMode & S_IFMT) == S_IFLNK);
        }

        public long lastAccessTime() {
            return aDate;
        }

        public long lastModifiedTime() {
            return cDate;
        }

        public int linkCount() {
            return nLink;
        }

        public TimeUnit resolution() {
            return TimeUnit.MILLISECONDS;
        }

        public long size() {
            return this.fileSize;
        }
    }

    /**
     * Describes replica entries.
     * 
     * @author jerome / inspired by the gEclipse project
     * 
     */
    public class LFCReplica {

        private String poolName; // name of the pool
        private String guid; // global unique id
        private String host; // storage element host
        private String fs; // filesystem type
        private String sfn; // sfn value

        private long aDate; // last access time
        private long pDate; // pin time

        private long fileId; // unique id
        private long nbaccesses; // number of accesses???

        private byte status; // '-' = online, 'm' = migrated (don't know what is
                             // this for)
        private byte f_type; //

        public LFCReplica(ByteBuffer byteBuffer) throws IOException {
            this.fileId = byteBuffer.getLong();
            this.nbaccesses = byteBuffer.getLong();

            this.aDate = byteBuffer.getLong() * 1000;
            this.pDate = byteBuffer.getLong() * 1000;
            this.status = byteBuffer.get();
            this.f_type = byteBuffer.get();

            this.poolName = getString();
            this.host = getString();
            this.fs = getString();
            this.sfn = getString();
        }

        /**
         * @return global unique ID of the file/directory
         */
        public String getGuid() {
            return this.guid;
        }

        /**
         * @return fileId of the file/directory
         */
        public long getFileId() {
            return this.fileId;
        }

        /**
         * @return last access time
         */
        public long getADate() {
            return this.aDate;
        }

        /**
         * @return replica pin time
         */
        public long getPDate() {
            return this.pDate;
        }

        /**
         * @return '-' = online, 'm' = migrated (don't know what is this for)
         */
        public byte getStatus() {
            return this.status;
        }

        /**
         * @return the poolName
         */
        public String getPoolName() {
            return this.poolName;
        }

        /**
         * @return the host
         */
        public String getHost() {
            return this.host;
        }

        /**
         * @return the fs
         */
        public String getFs() {
            return this.fs;
        }

        /**
         * @return the sfn
         */
        public String getSfn() {
            return this.sfn;
        }

        /**
         * @return the nbaccesses
         */
        public long getNbaccesses() {
            return this.nbaccesses;
        }

        /**
         * @return the f_type
         */
        public byte getF_type() {
            return this.f_type;
        }

        /**
         * @return The string representation of the LFCReplica object
         */
        @Override
        public String toString() {
            String replica = "";
            replica += "fileId: " + this.fileId;
            replica += " - nbaccesses: " + this.nbaccesses;

            replica += " - aDate: " + this.aDate;
            replica += " - pDate: " + this.pDate;
            replica += " - status: " + this.status;
            replica += " - f_type: " + this.f_type;

            replica += " - poolName: " + this.poolName;
            replica += " - host: " + this.host;
            replica += " - fs: " + this.fs;
            replica += " - sfn: " + this.sfn;
            return replica;
        }
    }

    /**
     * Exception that can be thrown by the {@link LfcConnection#receive()}
     * function
     */
    public final class ReceiveException extends IOException {
        private static final long serialVersionUID = 1L;
        private int error = -1;

        public ReceiveException(int error) {
            super();
            this.error = error;
        }

        public ReceiveException(int error, String s) {
            super(s);
            this.error = error;
        }

        public int getError() {
            return error;
        }

    }

    /**
     * Try to close the connection to free resources.
     */
    public void close() {
        try {
            this.channel.close();
        } catch (IOException e) {
            LOGGER.warn(e.toString());
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void finalize() throws Throwable {
        try {
            this.channel.close();
        } catch (IOException e) {
            // ignore
        }
        super.finalize();
    }
}
