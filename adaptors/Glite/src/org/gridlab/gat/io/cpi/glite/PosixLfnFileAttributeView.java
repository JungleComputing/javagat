package org.gridlab.gat.io.cpi.glite;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnector;
import org.gridlab.gat.io.permissions.AbstractPosixFileAttributeView;
import org.gridlab.gat.io.permissions.attribute.*;
import org.gridlab.gat.security.glite.GliteSecurityUtils;

public class PosixLfnFileAttributeView  extends AbstractPosixFileAttributeView{
	
	private LfcConnector lfcConnector;
	private GATContext gatContext;

	public PosixLfnFileAttributeView(URI location, boolean followSymbolicLinks, LfcConnector lfcConnector, GATContext gatContext) {
		super(location, followSymbolicLinks);
		this.lfcConnector = lfcConnector;
		this.gatContext = gatContext;
	}
	
	public String name() {
		return "posix";
	}

	public void setGroup(GroupPrincipal group) throws IOException {
		//Create the VOMS proxy if needed
    	try {
			GliteSecurityUtils.touchVomsProxy(gatContext);
		} catch (GATInvocationException e) {
			throw new IOException(e.getMessage());
		}
		lfcConnector.chown(location.getPath(),false, followSymbolicLinks,null,group.getName());
	}

	public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
		//Create the VOMS proxy if needed
    	try {
			GliteSecurityUtils.touchVomsProxy(gatContext);
		} catch (GATInvocationException e) {
			throw new IOException(e.getMessage());
		}
		int mode = 0;
        for (PosixFilePermission perm: perms) {
            if (perm == null)
                throw new NullPointerException();
            switch (perm) {
                case OWNER_READ :     mode |= LfcConnection.S_IRUSR; break;
                case OWNER_WRITE :    mode |= LfcConnection.S_IWUSR; break;
                case OWNER_EXECUTE :  mode |= LfcConnection.S_IXUSR; break;
                case GROUP_READ :     mode |= LfcConnection.S_IRGRP; break;
                case GROUP_WRITE :    mode |= LfcConnection.S_IWGRP; break;
                case GROUP_EXECUTE :  mode |= LfcConnection.S_IXGRP; break;
                case OTHERS_READ :    mode |= LfcConnection.S_IROTH; break;
                case OTHERS_WRITE :   mode |= LfcConnection.S_IWOTH; break;
                case OTHERS_EXECUTE : mode |= LfcConnection.S_IXOTH; break;
            }
        }
        lfcConnector.chmod(location.getPath(),mode);
	}

	public void setTimes(Long lastModifiedTime, Long lastAccessTime, Long createTime, TimeUnit unit) throws IOException {
		throw new UnsupportedOperationException("Not supported by the LFC.");
	}

	public void setOwner(UserPrincipal owner) throws IOException {
		//Create the VOMS proxy if needed
    	try {
			GliteSecurityUtils.touchVomsProxy(gatContext);
		} catch (GATInvocationException e) {
			throw new IOException(e.getMessage());
		}
		lfcConnector.chown(location.getPath(),false, followSymbolicLinks,owner.getName(),null);
	}
	
	public PosixFileAttributes readAttributes() throws IOException {
		//Create the VOMS proxy if needed
    	try {
			GliteSecurityUtils.touchVomsProxy(gatContext);
		} catch (GATInvocationException e) {
			throw new IOException(e.getMessage());
		}
		return lfcConnector.stat(location.getPath(),followSymbolicLinks);
	}
	
	public Map<String, ?> readAttributes(String first, String... rest) throws IOException {
		//Create the VOMS proxy if needed
    	try {
			GliteSecurityUtils.touchVomsProxy(gatContext);
		} catch (GATInvocationException e) {
			throw new IOException(e.getMessage());
		}
		Set<String> requestedAttributesNames = new HashSet<String>();
		boolean copyAll = false;
		if (first.equals("*")) {
            copyAll = true;
        } else {
        	requestedAttributesNames.add(first);
            for (String attribute: rest) {
                if (attribute.equals("*")) {
                    copyAll = true;
                    break;
                }
                requestedAttributesNames.add(attribute);
            }
        }
		
		PosixFileAttributes posixFileAttributes = readAttributes();
		Map<String, Object> attributes = getAttributesMap(posixFileAttributes);
		if(copyAll){
			return attributes;
		}
		
		for (Iterator<String> iterator = attributes.keySet().iterator(); iterator.hasNext();) {
			String attributeName = (String) iterator.next();
			if(!requestedAttributesNames.contains(attributeName)){
				iterator.remove();
			}
		}
		return attributes;
	}
}
