/*
 * Created on Jun 28, 2005
 */
package org.gridlab.gat.io.cpi.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileInputStreamCpi;
import org.gridlab.gat.io.cpi.ssh.SshSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * @author rob
 */
public class SshFileInputStreamAdaptor extends FileInputStreamCpi {
	
	protected static Logger logger = Logger.getLogger(SshFileInputStreamAdaptor.class);
	
    InputStream inputStream;

    OutputStream outputStream;

    Session session;

    Channel channel;

    private long available;

    private long filesize;

    public SshFileInputStreamAdaptor(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("ssh")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (location.getHost() == null) {
            throw new GATObjectCreationException(
                "this adaptor cannot read local files");
        }

        // now try to create a stream.
        try {
            inputStream = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("ssh file inputstream", e);
        }
    }

    protected InputStream createStream() throws GATInvocationException {
        try {
            String host = location.resolveHost();
            JSch jsch = new JSch();
            java.util.Hashtable<String, String> configJsch = new java.util.Hashtable<String, String>();
            configJsch.put("StrictHostKeyChecking", "no");
            JSch.setConfig(configJsch);

            SshUserInfo sui = null;

            try {
                sui = SshSecurityUtils.getSshCredential(gatContext,
                    preferences, "ssh", location, SshFileAdaptor.SSH_PORT);
            } catch (Exception e) {
                logger.info("SshFileAdaptor: failed to retrieve credentials"
                        + e);
            }

            if (sui == null) {
                throw new GATObjectCreationException(
                    "Unable to retrieve user info for authentication");
            }

            if (sui.privateKeyfile != null) {
                jsch.addIdentity(sui.privateKeyfile);
            }

            //to be modified, this part goes inside the SSHSecurityUtils
            if (location.getUserInfo() != null) {
                sui.username = location.getUserInfo();
            }

            int port = location.getPort();

            if (port == -1) {
                port = SshFileAdaptor.SSH_PORT;
            }

            session = jsch.getSession(sui.username, host, port);
            session.setUserInfo(sui);
            session.connect();
            channel = session.openChannel("exec");

            String command = "scp -f " + location.getPath();
            //String command = "cat " + location.getPath();
            ((ChannelExec) channel).setCommand(command);

            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            outputStream = out;

            //channel.setOutputStream(new java.io.PipedOutputStream(in),true);

            channel.connect();

            SshFileAdaptor.sendAck(out);

            removeInputStreamHeader(in, out);
            SshFileAdaptor.sendAck(out);

            return in;
        } catch (Exception e) {
            throw new GATInvocationException("SshFileInputStream", e);
        }
    }

    protected void removeInputStreamHeader(InputStream in, OutputStream out)
            throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("SshFileInputStream: removeInputStreamHeader");
        }

        while (true) {
            // C0644 filesize filename - header for a regular file
            // T time 0 time 0\n - present if perserve time.
            // D directory - this is the header for a directory.
            java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();

            while (true) {
                int read = in.read();

                if (read < 0) {
                    return;
                }

                if ((byte) read == SshFileAdaptor.LINE_FEED) {
                    break;
                }

                stream.write(read);
            }

            String serverResponse = stream.toString("UTF-8");

            if (serverResponse.charAt(0) == 'C') {
                if (logger.isDebugEnabled()) {
                    logger.debug("SshFileInputStream: remote response is file");
                }
                int start = 0;
                int end = serverResponse.indexOf(" ", start + 1);
                start = end + 1;
                end = serverResponse.indexOf(" ", start + 1);

                filesize = Long.parseLong(serverResponse.substring(start, end));
                available = filesize;
                return;
            } else if (serverResponse.charAt(0) == 'D') {
                if (logger.isDebugEnabled()) {
                    logger.debug("SshFileInputStream: remote response is dir");
                }

                throw new IOException("File " + location
                    + ": Not a regular file");
            } else if (serverResponse.charAt(0) == 'E') {
                if (logger.isDebugEnabled()) {
                    logger.debug("scpFromRemoteToLocal: remote response is E");
                }

                throw new IOException("File " + location
                    + ": Not a regular file");
            } else if ((serverResponse.charAt(0) == '\01')
                || (serverResponse.charAt(0) == '\02')) {
                // this indicates an error.
                throw new IOException(serverResponse.substring(1));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("remoteCpProtocol: read byte is none of the expected values");
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#available()
     */
    public int available() throws GATInvocationException {
        /*
         try {
         return inputStream.available();
         } catch (IOException e) {
         throw new GATInvocationException("SshFileInputStream", e);
         }
         */
        return (int) available;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#close()
     */
    public void close() throws GATInvocationException {
        try {
            SshFileAdaptor.sendAck(outputStream);
            SshFileAdaptor.checkAck(inputStream);
            inputStream.close();
            SshFileAdaptor.cleanSession(session, channel);
        } catch (IOException e) {
            throw new GATInvocationException("SshFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#mark(int)
     */
    public synchronized void mark(int arg0) {
        inputStream.mark(arg0);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read()
     */
    public int read() throws GATInvocationException {
        try {
            int res = inputStream.read();
            if (res > -1) available--;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("SshFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int offset, int len)
            throws GATInvocationException {
        try {
            int res = inputStream.read(b, offset, len);
            available -= res;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("SshFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] arg0) throws GATInvocationException {
        try {
            int res = inputStream.read(arg0);
            available -= res;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("SshFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#reset()
     */
    public synchronized void reset() throws GATInvocationException {
        try {
            available = filesize;
            inputStream.reset();
        } catch (IOException e) {
            throw new GATInvocationException("SshFileInputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long arg0) throws GATInvocationException {
        try {
            long res = inputStream.skip(arg0);
            available -= res;
            return res;
        } catch (IOException e) {
            throw new GATInvocationException("SshFileInputStream", e);
        }
    }
}
