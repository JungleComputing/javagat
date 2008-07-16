/*
 * Created on Nov 8, 2005
 */
package org.gridlab.gat.io.cpi.sftpGanymed;

import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SFTPv3Client;

public class SftpGanymedConnection {

    Connection connection;

    SFTPv3Client sftpClient;

    SftpGanymedUserInfo userInfo;

    URI remoteMachine;

    Preferences preferences;
}
