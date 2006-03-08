/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.io.cpi.sftp;

import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;

/**
 * @author rob
 */
public class SftpUserInfo {
    String username;
    String password;
    SshPrivateKey privateKey;
}
