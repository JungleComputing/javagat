/*
 * Created on Nov 8, 2005
 */
package org.gridlab.gat.io.cpi.sftpnew;

import org.gridlab.gat.URI;
import org.gridlab.gat.security.sftp.SftpUserInfo;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SftpNewConnection {
    JSch jsch;

    ChannelSftp channel;

    Session session;

    SftpUserInfo userInfo;
    
    URI remoteMachine;
    
}
