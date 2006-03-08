/*
 * Created on Nov 8, 2005
 */
package org.gridlab.gat.io.cpi.sftpnew;

import org.gridlab.gat.io.cpi.ssh.SshUserInfo;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SftpConnection {
    JSch jsch;

    ChannelSftp channel;

    Session session;

    SshUserInfo userInfo;
}
