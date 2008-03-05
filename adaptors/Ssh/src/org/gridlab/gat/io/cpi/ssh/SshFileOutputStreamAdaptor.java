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
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileOutputStreamCpi;
import org.gridlab.gat.io.cpi.ssh.SshSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author rob
 */
public class SshFileOutputStreamAdaptor extends FileOutputStreamCpi {

    protected static Logger logger = Logger
            .getLogger(SshFileOutputStreamAdaptor.class);

    private OutputStream outputStream;

    private InputStream inputStream;

    private Session session;

    private Channel channel;

    public SshFileOutputStreamAdaptor(GATContext gatContext,
            Preferences preferences, URI location, Boolean append)
            throws GATObjectCreationException {
        super(gatContext, preferences, location, append);

        if (!location.isCompatible("ssh")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        // We don't have to handle the local case, the GAT engine will select
        // the local adaptor.
        if (location.getHost() == null) {
            throw new GATObjectCreationException(
                    "this adaptor cannot handle local files");
        }

        try {
            prepareToWriteStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("SshFileOutputStream", e);
        }
    }

    protected void prepareToWriteStream() throws GATInvocationException {
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
                logger.info("SshFileOutputStream: "
                        + "failed to retrieve credentials" + e);
            }

            if (sui == null) {
                throw new GATObjectCreationException(
                        "Unable to retrieve user info for authentication");
            }

            if (sui.privateKeyfile != null) {
                jsch.addIdentity(sui.privateKeyfile);
            }

            // to be modified, this part goes inside the SSHSecurityUtils
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

            String command = "cat ";

            if (append) {
                command += ">> ";
            } else {
                command += "> ";
            }

            command += location.getPath();
            ((ChannelExec) channel).setCommand(command);
            outputStream = channel.getOutputStream();
            inputStream = channel.getInputStream();

            channel.connect();

            /*
             * if (SshFileAdaptor.checkAck(in) != 0) {
             * SshFileAdaptor.cleanSession(session, channel); throw new
             * GATInvocationException("SshFileOutputStreamAdaptor: " + "failed
             * checkAck after sending scp command for " + "stream to " +
             * location); }
             * 
             */
            /* try to cheat, as we don't know the filesize in advance */
            /*
             * long filesize = 1; command = "C0644 " + filesize + " "; command +=
             * new java.io.File(location.getPath()).getName(); command += "\n";
             * out.write(command.getBytes()); out.flush();
             * 
             * if (SshFileAdaptor.checkAck(in) != 0) { throw new
             * IOException("failed to receive ack after sending header" + " for
             * transfer file to remote machine"); }
             * 
             * outputStream = new ByteArrayOutputStream();
             * channel.setOutputStream(outputStream);
             * 
             */
        } catch (Exception e) {
            if (e instanceof JSchException) {
                if (e.getMessage().equals("Auth fail")) {
                    throw new InvalidUsernameOrPasswordException(e);
                }
            } else {
                throw new GATInvocationException("SshFileOutputStream", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#close()
     */
    public void close() throws GATInvocationException {
        try {
            outputStream.close();
            SshFileAdaptor.checkAck(inputStream);
            SshFileAdaptor.cleanSession(session, channel);
        } catch (IOException e) {
            SshFileAdaptor.cleanSession(session, channel);
            throw new GATInvocationException("SshFileOutputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#flush()
     */
    public void flush() throws GATInvocationException {
        try {
            outputStream.flush();
            SshFileAdaptor.sendAck(outputStream);
        } catch (IOException e) {
            throw new GATInvocationException("SshFileOutputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int offset, int len)
            throws GATInvocationException {
        try {
            outputStream.write(b, offset, len);
        } catch (IOException e) {
            throw new GATInvocationException("SshFileOutputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] arg0) throws GATInvocationException {
        try {
            outputStream.write(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("SshFileOutputStream", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(int)
     */
    public void write(int arg0) throws GATInvocationException {
        try {
            outputStream.write(arg0);
        } catch (IOException e) {
            throw new GATInvocationException("SshFileOutputStream", e);
        }
    }
}
