package org.gridlab.gat.io.cpi.glite;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.attributes.AbstractPosixFileAttributeView;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnector;
import org.gridlab.gat.io.cpi.glite.lfc.LfcUtil;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCReplica;
import org.gridlab.gat.io.cpi.glite.srm.SrmConnector;
import org.gridlab.gat.io.permissions.attribute.*;
import org.gridlab.gat.resources.cpi.glite.GliteConstants;
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
		if("true".equalsIgnoreCase((String)gatContext.getPreferences().get(GliteConstants.PREFERENCE_SYNCH_LFC_DPM_PERMS))){
			GATContext srmGatContext = LfcUtil.getSRMContext(gatContext);
			SrmConnector srmConnector = new SrmConnector(GliteSecurityUtils.getProxyPath(srmGatContext));
			Collection<LFCReplica> replicas = lfcConnector.listReplicas(location.getPath(), null);
			Iterator<LFCReplica> iterator = replicas.iterator();
			String log = null;
            while(iterator.hasNext()){
            	String uri = iterator.next().getSfn();
            	try{
            		new PosixSrmFileAttributeView(new URI(uri), followSymbolicLinks, srmConnector, srmGatContext).setGroup(group);
            	}catch (Exception e) {
            		if(log == null){
            			log = "Group modified on LFC but not on SRM:";
            		}
    				log +="\n\t- "+uri+"("+e.getMessage()+")";
    			}
            }
            if(log != null){
            	throw new IOException(log);
            }
		}
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
		if("true".equalsIgnoreCase((String)gatContext.getPreferences().get(GliteConstants.PREFERENCE_SYNCH_LFC_DPM_PERMS))){
			GATContext srmGatContext = LfcUtil.getSRMContext(gatContext);
			SrmConnector srmConnector = new SrmConnector(GliteSecurityUtils.getProxyPath(srmGatContext));
			Collection<LFCReplica> replicas = lfcConnector.listReplicas(location.getPath(), null);
			Iterator<LFCReplica> iterator = replicas.iterator();
			String log = null;
            while(iterator.hasNext()){
            	String uri = iterator.next().getSfn();
            	try{
            		new PosixSrmFileAttributeView(new URI(uri), followSymbolicLinks, srmConnector, srmGatContext).setPermissions(perms);
            	}catch (Exception e) {
            		if(log == null){
            			log = "Permissions modified on LFC but not on SRM:";
            		}
    				log +="\n\t- "+uri+"("+e.getMessage()+")";
    			}
            }
            if(log != null){
            	throw new IOException(log);
            }
		}
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
		if("true".equalsIgnoreCase((String)gatContext.getPreferences().get(GliteConstants.PREFERENCE_SYNCH_LFC_DPM_PERMS))){
			GATContext srmGatContext = LfcUtil.getSRMContext(gatContext);
			SrmConnector srmConnector = new SrmConnector(GliteSecurityUtils.getProxyPath(srmGatContext));
			Collection<LFCReplica> replicas = lfcConnector.listReplicas(location.getPath(), null);
			Iterator<LFCReplica> iterator = replicas.iterator();
			String log = null;
            while(iterator.hasNext()){
            	String uri = iterator.next().getSfn();
            	try{
            		new PosixSrmFileAttributeView(new URI(uri), followSymbolicLinks, srmConnector, srmGatContext).setOwner(owner);
            	}catch (Exception e) {
            		if(log == null){
            			log = "Owner modified on LFC but not on SRM:";
            		}
    				log +="\n\t- "+uri+"("+e.getMessage()+")";
    			}
            }
            if(log != null){
            	throw new IOException(log);
            }
		}
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
}
