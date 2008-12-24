package org.gridlab.gat.io.cpi.glite.lfc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.GlobusGSSManagerImpl;
import org.gridforum.jgss.ExtendedGSSContext;
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

public class LfcConnection {
    protected static Logger logger = Logger.getLogger(LfcConnection.class);

    private static final int HEADER_SIZE = 12;

    private static final int BUF_SIZE = 10240;

    private static final int CSEC_TOKEN_MAGIC_1 = 0xCA03;
    private static final int CSEC_TOKEN_TYPE_PROTOCOL_REQ = 0x1;
    private static final int CSEC_TOKEN_TYPE_PROTOCOL_RESP = 0x2;
    private static final int CSEC_TOKEN_TYPE_HANDSHAKE = 0x3;
    private static final int CSEC_TOKEN_TYPE_HANDSHAKE_FINAL = 0x5;
    private static final int CSEC_TOKEN_TYPE_ERROR = 0x6;

    private static final int CNS_MAGIC = 0x030E1301;

    private static final int CNS_RESP_MSG_DATA = 2;
    private static final int CNS_RESP_RC = 3;
    private static final int CNS_RESP_IRC = 4;

    private static final int CNS_GETREPLICA = 72;
    private static final int CNS_GETREPLICAX = 77;
    private static final int CNS_PING = 82;

    private static final int CNS_MAGIC2 = 0x030E1302;
    private static final int CNS_LISTREPLICA = 45;

    private static final byte REQUEST_GSI_TOKEN[] = { 0x00, 0x00, 0x00, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x47, 0x53, 0x49,
            0x00, 0x49, 0x44, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
            0x02, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01 };

    private final ByteBuffer sendBuf = ByteBuffer.allocateDirect(BUF_SIZE);
    private final ByteBuffer recvBuf = ByteBuffer.allocateDirect(BUF_SIZE);
    private final ByteChannel channel;

    private final GSSCredential gssCredential;

    public LfcConnection(String host, int port) throws IOException {
        try {
            final String proxyPath = GliteSecurityUtils.getProxyPath();
            logger.info("Proxy is " + proxyPath);
            GlobusCredential credential = new GlobusCredential(proxyPath);
            gssCredential = new GlobusGSSCredentialImpl(credential, 0);
        } catch (GSSException e) {
            throw new IOException("Failed to load credentials", e);
        } catch (GlobusCredentialException e) {
            throw new IOException("Failed to load credentials", e);
        }
        channel = SocketChannel.open(new InetSocketAddress(host, port));
        logger.info("Establishing Connection with " + host + ":" + port);
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
        int sizeOrError = recvBuf.getInt();
        logger
                .info("Received M/T/S: " + magic + " " + type + " "
                        + sizeOrError);

        if (magic == CSEC_TOKEN_MAGIC_1) {
            if ((type != CSEC_TOKEN_TYPE_PROTOCOL_RESP)
                    && (type != CSEC_TOKEN_TYPE_HANDSHAKE)
                    && (type != CSEC_TOKEN_TYPE_HANDSHAKE_FINAL)) {
                throw new IOException("Received invalid CSEC Type: " + type);
            }
        } else if (magic == CNS_MAGIC) {
            if ((type == CNS_RESP_IRC) || (type == CNS_RESP_RC)) {
                throw new IOException("Recieved CNS Error " + sizeOrError);
            } else if (type != CNS_RESP_MSG_DATA) {
                throw new IOException("Received invalid CNS Type: " + type);
            }
        } else
            throw new IOException("Recieved invalid Magic/Type: " + magic + "/"
                    + type);

        assert sizeOrError < BUF_SIZE : "Buffer size must be at least "
                + sizeOrError;
        while (recvBuf.limit() < sizeOrError) {
            recvBuf.flip();
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
                logger.info("called initSecContext, doing another iteration");

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
            throw new IOException("Error processing credential", e);
        }
        logger.info("Secure Context established!");
    }

    private void putString(String s) {
        if (s != null)
            // TODO: Check if UTF-8 is correct!
            sendBuf.put(s.getBytes(Charset.forName("UTF-8")));
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


    public Collection<String> srmLookup(String path, String guid) throws IOException {
        preparePacket(CNS_MAGIC2, CNS_LISTREPLICA);
        addIDs();
        sendBuf.putShort((short) 0); // Size of nbentry
        long cwd = 0; // Current Working Directory
        sendBuf.putLong(cwd);
        putString(path);
        putString(guid);
        sendBuf.putShort((short) 1); // BOL = Beginning of List
        sendAndReceive(true);

        short count = recvBuf.getShort();
        
        Collection<String> srms = new ArrayList<String>(count);

        for (short i = 0; i < count; i++) {
            long fileId = recvBuf.getLong();
            logger.info("FileId: " + fileId);
            long nbaccesses = recvBuf.getLong();
            logger.info("nbaccesses: " + nbaccesses);

            long aTime = recvBuf.getLong();
            long pTime = recvBuf.getLong();

            logger.info("aTime: " + new Date(aTime * 1000));
            logger.info("pTime: " + new Date(pTime * 1000));

            byte status = recvBuf.get();
            logger.info("Status: " + status);
            byte f_type = recvBuf.get();
            logger.info("fType: " + f_type);
            String poolName = getString();
            logger.info("poolName: " + poolName);
            String host = getString();
            logger.info("host: " + host);
            String fs = getString();
            logger.info("fs: " + fs);
            String sfn = getString();
            logger.info("sfn: " + sfn);
            srms.add(sfn);
        }
        return srms;
    }

}