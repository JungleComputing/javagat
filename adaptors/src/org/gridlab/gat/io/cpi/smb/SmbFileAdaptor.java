package org.gridlab.gat.io.cpi.smb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

import org.gridlab.gat.CouldNotInitializeCredentialException;
import org.gridlab.gat.CredentialExpiredException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.NtlmPasswordAuthentication;

/**
 * File adaptor for Samba filesystem.
 * @author      Balazs
 * @version     %I%, %G%
 * @since       1.0
 */
public class SmbFileAdaptor extends FileCpi {
    SmbFile smbf;
    public SmbFileAdaptor(GATContext gatContext, 
			  Preferences preferences,
			  URI location) 
	throws GATObjectCreationException, SmbException, MalformedURLException {
        super(gatContext, preferences, location);
	
        if (!location.isCompatible("smb") ) {
            throw new GATObjectCreationException("cannot handle this URI");
        }
	try {
	    smbf = createFile();
	} catch(Exception e) {
	    throw new GATObjectCreationException("SmbFile: "+e);
	}
    }
    
    protected SmbFile createFile() throws GATInvocationException {
	SmbFile f = null;
	List l = 
	    SecurityContextUtils.getValidSecurityContextsByType(
               gatContext, preferences,
	       "org.gridlab.gat.security.PasswordSecurityContext", 
	       "smb", location.getHost(), 
	       location.getPort(SmbFile.DEFAULT_PORT));
	if((l==null) || (l.size() == 0)) {
	    try {
		f = new SmbFile(location.toString());
		return f;
	    } catch(Exception e) {
		throw new GATInvocationException("SmbFile: "+e);
	    }
	}
	
	PasswordSecurityContext c = (PasswordSecurityContext) l.get(0);
        String user = c.getUsername();
        String password = c.getPassword();

        String host = location.getHost();
        String path = location.getPath();
	NtlmPasswordAuthentication auth =  
	    new NtlmPasswordAuthentication( host, user, password );
	try {
	    f = new SmbFile( location.toString(), auth );
	    return f;
	} catch(Exception e) {
	    throw new GATInvocationException("SmbFile: "+e);
	}
    }
	
    public String[] list() throws GATInvocationException {
	String[] res;
	try {
	    res = smbf.list();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return res;
    }
    
    public boolean exists() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.exists();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return res;
    }

    public boolean isDirectory() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.isDirectory();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return res;
    }
    
    public long length() throws GATInvocationException {
	long res;
	try {
	    res = smbf.length();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return res;
    }

    public boolean mkdir() throws GATInvocationException {
	try {
	    smbf.mkdir();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return true;
    }

    public boolean mkdirs() throws GATInvocationException {
	try {
	    smbf.mkdirs();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}
	return true;
    }
    
    public boolean delete() throws GATInvocationException {
	try {
	    smbf.delete();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return true;
    }

    public boolean canRead() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.canRead();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return res;
    }

    public boolean canWrite() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.canWrite();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return res;
    }
    
    public boolean createNewFile() throws GATInvocationException {
	try {
	    smbf.createNewFile();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	}  
	return true;
    }

    public boolean isFile() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.isFile();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return res;
    }
    
    public boolean isHidden() throws GATInvocationException {
	boolean res;
	try {
	    res = smbf.isHidden();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return res;	
    }
    
    public long lastModified() throws GATInvocationException {
	long res;
	try {
	    res = smbf.lastModified();
	} catch( SmbException e ) {
	    throw new GATInvocationException();
	} 
	return res;
    }
    
    public void copy(URI dest) throws GATInvocationException {
	if( dest.getScheme()!=null &&
	    dest.getScheme().compareToIgnoreCase("smb") == 0 ) {
	    if(GATEngine.DEBUG) {
		System.err.println("smb file: copy remote to remote with smbfile");
	    }
	    copySmbRemote(dest);
	    return;
	}

	if(determineIsDirectory()) {
	    copyDirectory(gatContext, preferences, toURI(), dest);
            return;
        }

	if (dest.refersToLocalHost()) {
	    if(GATEngine.DEBUG) {
		System.err.println("smb file: copy remote to local");
            }
	    copyToLocal(toURI(),dest);
	    return;
	}
	
	if (toURI().refersToLocalHost()) {
            if (GATEngine.DEBUG) {
                System.err.println("smb file: copy local to remote");
            }
            copyToRemote(toURI(), dest);
            return;
        } else {
	    throw new GATInvocationException("smb cannot copy file");
	}
    }
    
    protected void copySmbRemote( URI dest ) throws GATInvocationException {
	try {
	    SmbFile smbdest =  new SmbFile( dest.toString() );
	    smbf.copyTo(smbdest);
	} catch( Exception e ) {
	    System.err.println("smb file: copySmbRemote failed");
	    throw new GATInvocationException();
	} 
    }
    
    protected void copyToLocal(URI src, URI dest)
	throws GATInvocationException {
	try {
	    SmbFileInputStream fis  = new SmbFileInputStream(smbf);
	    java.io.FileOutputStream fos = new java.io.FileOutputStream(dest.getPath());
	    byte[] buf = new byte[1024];
	    int i = 0;
	    while((i=fis.read(buf))!=-1) {
		fos.write(buf, 0, i);
	    }
	    fis.close();
	    fos.close();
	} catch( Exception e ) {
	    System.err.println("smb file: copyToLocal failed");
	    throw new GATInvocationException();
	}   
	     
    }

    protected void copyToRemote(URI src, URI dest)
	throws GATInvocationException {
	try {
	    java.io.FileInputStream fis = new java.io.FileInputStream(src.getPath());
	    SmbFileOutputStream fos = new SmbFileOutputStream(smbf);
	    byte[] buf = new byte[1024];
	    int i = 0;
	    while((i=fis.read(buf))!=-1) {
		fos.write(buf, 0, i);
	    }
	    fis.close();
	    fos.close();
	} catch( Exception e ) {
	    System.err.println("smb file: copyToRemote failed");
	    throw new GATInvocationException();
	}   
    }
}
