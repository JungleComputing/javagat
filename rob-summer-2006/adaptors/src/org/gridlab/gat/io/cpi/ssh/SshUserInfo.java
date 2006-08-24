/*
 * Created on Aug 2, 2005
 */
package org.gridlab.gat.io.cpi.ssh;

import com.jcraft.jsch.UserInfo;

/**
 * @author rob
 */
public class SshUserInfo implements UserInfo {
    public String username;

    public String password;

    public String privateKeyfile;

    public int privateKeySlot = -1;
    
    /*
     public String getPassword() {
     return password;
     }
     */
    public String getPassword() {
        if (password != null)
            return password;
        else
            return null;
    }

    public String getPrivateKeyfile() {
        return privateKeyfile;
    }

    public boolean promptYesNo(String str) {
        return true;
    }

    /*
     public String getPassphrase() {
     return null;
     }
     */
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

    public void purgePassword() {
        return;
    }

    public void purgePassphrase() {
        return;
    }

    public boolean promptYesNo(String str, String str1) {
        return true;
    }

	public int getPrivateKeySlot() {
		return privateKeySlot;
	}

	public void setPrivateKeySlot(int privateKeySlot) {
		this.privateKeySlot = privateKeySlot;
	}

}
