/*
 * Created on Nov 8, 2005
 */
package org.gridlab.gat.io.cpi.sftpTrilead;

import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPv3Client;

public class SftpTrileadConnection {

    Connection connection;

    SFTPv3Client sftpClient;

    SftpTrileadUserInfo userInfo;

    URI remoteMachine;

    Preferences preferences;
}
