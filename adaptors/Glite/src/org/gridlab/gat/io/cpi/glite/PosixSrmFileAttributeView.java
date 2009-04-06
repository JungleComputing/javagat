package org.gridlab.gat.io.cpi.glite;

import gov.lbl.srm.StorageResourceManager.ArrayOfTGroupPermission;
import gov.lbl.srm.StorageResourceManager.TGroupPermission;
import gov.lbl.srm.StorageResourceManager.TPermissionMode;
import gov.lbl.srm.StorageResourceManager.TPermissionType;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.glite.srm.SrmConnector;
import org.gridlab.gat.io.permissions.AbstractPosixFileAttributeView;
import org.gridlab.gat.io.permissions.attribute.*;
import org.gridlab.gat.security.glite.GliteSecurityUtils;

public class PosixSrmFileAttributeView  extends AbstractPosixFileAttributeView{
	
	private SrmConnector srmConnector;
	private GATContext gatContext;

	public PosixSrmFileAttributeView(URI location, boolean followSymbolicLinks, SrmConnector srmConnector, GATContext gatContext) {
		super(location, followSymbolicLinks);
		this.srmConnector = srmConnector;
		this.gatContext = gatContext;
	}
	
	public String name() {
		return "posix";
	}

	public void setGroup(GroupPrincipal group) throws IOException {
		throw new UnsupportedOperationException("Not supported by the SRM.");
	}

	public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
		//Create the VOMS proxy if needed
    	try {
			GliteSecurityUtils.touchVomsProxy(gatContext);
		} catch (GATInvocationException e) {
			throw new IOException(e.getMessage());
		}
		if(perms.contains(null)){
			throw new NullPointerException();
		}
		
		GroupPrincipal groupPrincipal = srmConnector.ls(location).group();
		
		TPermissionMode ownerTPermissionMode = null;
		if(perms.contains(PosixFilePermission.OWNER_READ) && perms.contains(PosixFilePermission.OWNER_WRITE) && perms.contains(PosixFilePermission.OWNER_EXECUTE)){
			ownerTPermissionMode = TPermissionMode.RWX;
		}else if(perms.contains(PosixFilePermission.OWNER_READ) && perms.contains(PosixFilePermission.OWNER_WRITE)){
			ownerTPermissionMode = TPermissionMode.RW;
		}else if(perms.contains(PosixFilePermission.OWNER_READ) && perms.contains(PosixFilePermission.OWNER_EXECUTE)){
			ownerTPermissionMode = TPermissionMode.RX;
		}else if(perms.contains(PosixFilePermission.OWNER_WRITE) && perms.contains(PosixFilePermission.OWNER_EXECUTE)){
			ownerTPermissionMode = TPermissionMode.WX;
		}else if(perms.contains(PosixFilePermission.OWNER_READ)){
			ownerTPermissionMode = TPermissionMode.R;
		}else if(perms.contains(PosixFilePermission.OWNER_WRITE)){
			ownerTPermissionMode = TPermissionMode.W;
		}else if(perms.contains(PosixFilePermission.OWNER_EXECUTE)){
			ownerTPermissionMode = TPermissionMode.X;
		}else{
			ownerTPermissionMode = TPermissionMode.NONE;
		}
		
		TGroupPermission tgroupPermission = new TGroupPermission();
		tgroupPermission.setGroupID(groupPrincipal.getName());
		if(perms.contains(PosixFilePermission.GROUP_READ) && perms.contains(PosixFilePermission.GROUP_WRITE) && perms.contains(PosixFilePermission.GROUP_EXECUTE)){
			tgroupPermission.setMode(TPermissionMode.RWX);
		}else if(perms.contains(PosixFilePermission.GROUP_READ) && perms.contains(PosixFilePermission.GROUP_WRITE)){
			tgroupPermission.setMode(TPermissionMode.RW);
		}else if(perms.contains(PosixFilePermission.GROUP_READ) && perms.contains(PosixFilePermission.GROUP_EXECUTE)){
			tgroupPermission.setMode(TPermissionMode.RX);
		}else if(perms.contains(PosixFilePermission.GROUP_WRITE) && perms.contains(PosixFilePermission.GROUP_EXECUTE)){
			tgroupPermission.setMode(TPermissionMode.WX);
		}else if(perms.contains(PosixFilePermission.GROUP_READ)){
			tgroupPermission.setMode(TPermissionMode.R);
		}else if(perms.contains(PosixFilePermission.GROUP_WRITE)){
			tgroupPermission.setMode(TPermissionMode.W);
		}else if(perms.contains(PosixFilePermission.GROUP_EXECUTE)){
			tgroupPermission.setMode(TPermissionMode.X);
		}else{
			tgroupPermission.setMode(TPermissionMode.NONE);
		}
		
		TPermissionMode otherTPermissionMode = null;
		if(perms.contains(PosixFilePermission.OTHERS_READ) && perms.contains(PosixFilePermission.OTHERS_WRITE) && perms.contains(PosixFilePermission.OTHERS_EXECUTE)){
			otherTPermissionMode = TPermissionMode.RWX;
		}else if(perms.contains(PosixFilePermission.OTHERS_READ) && perms.contains(PosixFilePermission.OTHERS_WRITE)){
			otherTPermissionMode = TPermissionMode.RW;
		}else if(perms.contains(PosixFilePermission.OTHERS_READ) && perms.contains(PosixFilePermission.OTHERS_EXECUTE)){
			otherTPermissionMode = TPermissionMode.RX;
		}else if(perms.contains(PosixFilePermission.OTHERS_WRITE) && perms.contains(PosixFilePermission.OTHERS_EXECUTE)){
			otherTPermissionMode = TPermissionMode.WX;
		}else if(perms.contains(PosixFilePermission.OTHERS_READ)){
			otherTPermissionMode = TPermissionMode.R;
		}else if(perms.contains(PosixFilePermission.OTHERS_WRITE)){
			otherTPermissionMode = TPermissionMode.W;
		}else if(perms.contains(PosixFilePermission.OTHERS_EXECUTE)){
			otherTPermissionMode = TPermissionMode.X;
		}else{
			otherTPermissionMode = TPermissionMode.NONE;
		}
		
        srmConnector.setPermissions(location, TPermissionType.CHANGE, ownerTPermissionMode, new ArrayOfTGroupPermission(new TGroupPermission[]{tgroupPermission}), otherTPermissionMode, null);
	}

	public void setTimes(Long lastModifiedTime, Long lastAccessTime, Long createTime, TimeUnit unit) throws IOException {
		throw new UnsupportedOperationException("Not supported by the SRM.");
	}

	public void setOwner(UserPrincipal owner) throws IOException {
		throw new UnsupportedOperationException("Not supported by the SRM.");
	}
	
	public PosixFileAttributes readAttributes() throws IOException {
		//Create the VOMS proxy if needed
    	try {
			GliteSecurityUtils.touchVomsProxy(gatContext);
		} catch (GATInvocationException e) {
			throw new IOException(e.getMessage());
		}
		return srmConnector.ls(location);
	}
}
