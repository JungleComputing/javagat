package org.gridlab.gat.io.cpi.sftpnew;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.ssh.SSHSecurityUtils;
import org.gridlab.gat.io.cpi.ssh.SshUserInfo;

import java.util.Hashtable;

public class SftpNewFileAdaptor extends FileCpi {
    public static final int SSH_PORT = 22;

    //    private ChannelSftp channel;
    //    private Session session;

    /**
     * @param gatContext
     * @param preferences
     * @param location
     */
    public SftpNewFileAdaptor(GATContext gatContext, Preferences preferences,
            URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location);

        checkName("sftpnew");

        if (!location.isCompatible("sftp") && !location.isCompatible("file")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        //        channel = createChannel(gatContext, preferences, location);
    }

    protected static SftpConnection createChannel(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        JSch jsch = new JSch();
        Hashtable configJsch = new Hashtable();
        configJsch.put("StrictHostKeyChecking", "no");
        JSch.setConfig(configJsch);

        SshUserInfo sui = null;

        try {
            sui = SSHSecurityUtils.getSshCredential(gatContext, preferences,
                "ssh", location, SSH_PORT);
        } catch (Exception e) {
            System.out.println("SshFileAdaptor: failed to retrieve credentials"
                + e);
        }

        if (sui == null) {
            throw new GATObjectCreationException(
                "Unable to retrieve user info for authentication");
        }

        try {
            if (sui.getPrivateKeyfile() != null) {
                jsch.addIdentity(sui.getPrivateKeyfile());
            }

            if (location.getUserInfo() != null) {
                sui.username = location.getUserInfo();
            }

            Session session = jsch.getSession(sui.username, location.getHost(),
                location.getPort(SSH_PORT));
            session.setUserInfo(sui);
            session.connect();

            Channel c = session.openChannel("sftp");
            c.connect();

            SftpConnection res = new SftpConnection();
            res.channel = (ChannelSftp) c;
            res.session = session;
            res.jsch = jsch;
            res.userInfo = sui;

            return res;
        } catch (JSchException jsche) {
            throw new GATObjectCreationException(
                "internal error in SftpnewFileAdaptor: " + jsche);
        }
    }
}
