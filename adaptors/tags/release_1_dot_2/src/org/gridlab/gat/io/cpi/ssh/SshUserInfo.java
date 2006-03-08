/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.io.cpi.ssh;

import com.jcraft.jsch.UserInfo;

/**
 * @author rob
 */
public class SshUserInfo implements UserInfo {
    String username;

    String password;

    String privateKeyfile;

    public String getPassword() {
        return password;
    }

    public String getPrivateKeyfile() {
        return privateKeyfile;
    }

    public boolean promptYesNo(String str) {
        return true;
    }

    public String getPassphrase() {
        return null;
    }

    public boolean promptPassphrase(String message) {
        return true;
    }

    public boolean promptPassword(String message) {
        return true;
    }

    public void showMessage(String message) {
        return;
    }
}
