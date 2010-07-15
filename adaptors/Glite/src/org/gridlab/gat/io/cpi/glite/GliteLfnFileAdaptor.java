package org.gridlab.gat.io.cpi.glite;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.attributes.*;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnector;
import org.gridlab.gat.io.cpi.glite.lfc.LfcUtil;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCFile;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCReplica;
import org.gridlab.gat.resources.cpi.glite.GliteConstants;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder.SEInfo;
import org.gridlab.gat.security.glite.GliteSecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for the Glite LFCs, accessed via lfn:// for JavaGAT.
 * 
 * @author Jerome Revillard
 */
@SuppressWarnings("serial")
public class GliteLfnFileAdaptor extends FileCpi {

    private static final String GLITE_LFC_FILE_ADAPTOR = "GliteLfnFileAdaptor";

    private static final String LFN = "lfn";
    private static final String CANNOT_HANDLE_THIS_URI = "cannot handle this URI: ";
    
    public static String[] getSupportedSchemes() {
        return new String[] { "glitelfn", LFN, "file", ""};
    }
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(GliteLfnFileAdaptor.class);

    private LfcConnector lfcConnector;

    private boolean localFile;

    private final String vo;
    
    public GliteLfnFileAdaptor(GATContext gatCtx, URI _location)
            throws GATObjectCreationException {
    	super(gatCtx, _location);
        vo = GliteConstants.getVO(gatContext);
        if (location.isCompatible("file") && location.refersToLocalHost()) {
            localFile = true;
        } else {
            localFile = false;
            lfcConnector = LfcUtil.initLfcConnector(gatContext, location, vo);
            try {
            	this.location = this.location.setHost(lfcConnector.getServer());
                this.location = this.location.setPort(lfcConnector.getPort());
            } catch (URISyntaxException e) {
                LOGGER.warn(e.getMessage());
            }
            LOGGER.info("Instantiated gliteLfnFileAdaptor for " + location);
        }
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = FileCpi.getSupportedCapabilities();
        capabilities.put("copy", true);
        capabilities.put("createNewFile", true);
        capabilities.put("delete", true);
        capabilities.put("getAbsolutePath", false);
        capabilities.put("getCanonicalPath", false);
        capabilities.put("copy", true);
        capabilities.put("canRead", true);
        capabilities.put("canWrite", true);
        capabilities.put("createNewFile", true);
        capabilities.put("delete", true);
        capabilities.put("exists", true);
        capabilities.put("getAbsoluteFile", false);
        capabilities.put("getCanonicalFile", false);
        capabilities.put("getFileAttributeView", true);
        capabilities.put("isDirectory", true);
        capabilities.put("isFile", true);
        capabilities.put("isHidden", true);
        capabilities.put("lastModified", true);
        capabilities.put("length", true);
        capabilities.put("list", true);
        capabilities.put("mkdir", true);
        capabilities.put("renameTo", true);
        capabilities.put("setLastModified", false);
        capabilities.put("setReadOnly", false);
        return capabilities;
    }
    
    /**
     * Used by CreateDefaultPropertiesFile to generate default
     * javagat.properties.
     * 
     * @return Properties and their default values.
     */
    public static Preferences getSupportedPreferences() {
        Preferences preferences = FileCpi.getSupportedPreferences();
        GliteSecurityUtils.addGliteSecurityPreferences(preferences);
        return preferences;
    }

    /** {@inheritDoc} */
    public void copy(URI dest) throws GATInvocationException {
        try {
            if (localFile) { //UPLOAD
                if (!dest.isCompatible(LFN)) {
                    throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR
                            + ": " + CANNOT_HANDLE_THIS_URI + dest);
                }
                if (lfcConnector == null) {
                    lfcConnector = LfcUtil.initLfcConnector(gatContext, dest, vo);
                }
                final java.io.File source = new java.io.File(location.getPath());
                final long filesize = source.length();
                
                List<SEInfo> ses = new LDAPResourceFinder(gatContext).fetchSEs(vo, filesize);
                if(ses.isEmpty()){
                	throw new GATInvocationException(
                			"Could not find any usable SE in the BDII " +
                				"(Possible reasons can be: no available SE, " +
                					"not enougth free space in the available SEs " +
                						"or available SEs are not part of the GATContext)!");
                }
                String guid = UUID.randomUUID().toString();
                URI target = LfcUtil.upload(location, guid, ses, gatContext);
                LOGGER.info("Registering file in the LFC...");
                try {
                	lfcConnector.create(dest, guid, filesize);
                    lfcConnector.addReplica(guid, target);
                } catch (IOException e) {
                	LOGGER.info(GLITE_LFC_FILE_ADAPTOR + ": Unable to add the replica to the LFC - "+e.getMessage());
                    // If an error occurs, remove the created file from the SE
                    org.gridlab.gat.io.File toDeleteFile = GAT.createFile(LfcUtil.getSRMContext(gatContext), target);
                    toDeleteFile.delete();
                    throw e;
                }
            } else { //DOWNLOAD
                String lfnPath = location.getPath();
                GliteSecurityUtils.touchVomsProxy(gatContext);
                LOGGER.info("Copying " + lfnPath + " to " + dest);
                Collection<LFCReplica> replicas = lfcConnector.listReplicas(lfnPath, null);
                LOGGER.info("Replicas: " + replicas);
                if(replicas.isEmpty()){
                	throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": No replicas found for "+ location);
                }
                Iterator<LFCReplica> iterator = replicas.iterator();
                boolean success = false;
                while(iterator.hasNext() && !success){
                	LFCReplica someReplica = iterator.next();
                	try{
	                    GliteSrmFileAdaptor srmFile = new GliteSrmFileAdaptor(gatContext, new URI(someReplica.getSfn()));
	                    srmFile.copy(dest);
	                    success = true;
                	}catch (GATInvocationException e) {
                		LOGGER.info(GLITE_LFC_FILE_ADAPTOR + ": Unable to donwload "+ location + " from: "+someReplica.toString()+" - "+e.getMessage());
					}
                }
                if(!success){
                	LOGGER.info(GLITE_LFC_FILE_ADAPTOR + ": Unable to donwload "+ location + " from any replica location");
                	throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": Unable to donwload "+ location + " any replica location");
                }
            }
        } catch (GATInvocationException e) {
			throw e;
        } catch (Exception e) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
        }
    }

    /** {@inheritDoc} */
    public boolean createNewFile() throws GATInvocationException {
        if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
        LOGGER.info("create new file: "+location);
        try {
            GliteSecurityUtils.touchVomsProxy(gatContext);
            String guid = UUID.randomUUID().toString();
            final java.io.File source = new java.io.File(location.getPath());
            final long filesize = source.length();
            lfcConnector.create(location, guid, filesize);
            this.location = new URI(GliteGuidFileAdaptor.GUID, null, lfcConnector.getServer(),
                    lfcConnector.getPort(), '/' + guid, null, null);
        } catch (IOException e) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
		} catch (URISyntaxException e) {
			throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
		}
        return true;
    }

    /** {@inheritDoc} */
    public boolean canRead() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	//Create the VOMS proxy if needed
    	GliteSecurityUtils.touchVomsProxy(gatContext);
    	try {
			return lfcConnector.canRead(location.getPath());
		} catch (IOException e) {
			throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
		}
    }

    /** {@inheritDoc} */
    public boolean canWrite() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	//Create the VOMS proxy if needed
    	GliteSecurityUtils.touchVomsProxy(gatContext);
    	try {
			return lfcConnector.canWrite(location.getPath());
		} catch (IOException e) {
			throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
		}
    }

    /** {@inheritDoc} */
    public boolean delete() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
        try {
            String lfn = location.getPath();
            GliteSecurityUtils.touchVomsProxy(gatContext);
            LOGGER.info("Deleting " + lfn);
            return lfcConnector.deletePath(lfn);
        } catch (IOException e) {
            LOGGER.info(e.toString());
            return false;
        }
    }

    /** {@inheritDoc} */
    public boolean exists() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	//Create the VOMS proxy if needed
    	GliteSecurityUtils.touchVomsProxy(gatContext);
    	try {
    		return lfcConnector.exist(location.getPath());
		} catch (IOException e) {
			throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
		}
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
	public <V extends FileAttributeView> V getFileAttributeView(Class<V> type, boolean followSymbolicLinks)  throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	if(PosixFileAttributeView.class.equals(type)){
    		return (V) new PosixLfnFileAttributeView(location, followSymbolicLinks, lfcConnector, gatContext);
    	}
    	return null;
    }
    
    /** {@inheritDoc} */
    public boolean isDirectory() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
        if (location.getPath().endsWith(File.separator)) {
            return true;
        }
        //Create the VOMS proxy if needed
    	GliteSecurityUtils.touchVomsProxy(gatContext);
    	try {
			return lfcConnector.stat(location.getPath(), false).isDirectory();
		} catch (IOException e) {
			throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
		}
    }

    /** {@inheritDoc} */
    public boolean isFile() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	if (location.getPath().endsWith(File.separator)) {
            return false;
        }
        //Create the VOMS proxy if needed
    	GliteSecurityUtils.touchVomsProxy(gatContext);
    	try {
			return lfcConnector.stat(location.getPath(), false).isRegularFile();
		} catch (IOException e) {
			throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
		}
    }
    
    /** {@inheritDoc} */
    public long lastModified() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	//Create the VOMS proxy if needed
    	GliteSecurityUtils.touchVomsProxy(gatContext);
    	try {
			return lfcConnector.stat(location.getPath(), false).lastModifiedTime();
		} catch (IOException e) {
			throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
		}
    }

    /** {@inheritDoc} */
    public long length() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	//Create the VOMS proxy if needed
    	GliteSecurityUtils.touchVomsProxy(gatContext);
    	try {
			return lfcConnector.stat(location.getPath(), false).size();
		} catch (IOException e) {
			throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
		}
    }

    /** {@inheritDoc} */
    public String[] list() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	//Create the VOMS proxy if needed
    	GliteSecurityUtils.touchVomsProxy(gatContext);
    	
        LOGGER.debug("listing the content of " + location.getPath());
        Collection<String> lfnUris = null;;
		try {
			Collection<LFCFile> files = lfcConnector.list(location.getPath(),false);
			if(files != null){
				lfnUris = new ArrayList<String>(files.size());
				for (Iterator<LFCFile> iterator = files.iterator(); iterator.hasNext();) {
					lfnUris.add("lfn://"+location.getHost()+":"+location.getPort()+"/"+location.getPath() + (location.getPath().endsWith("/") ? "" : "/") + iterator.next().getFileName());
				}
				LOGGER.info("LFN URIs: " + lfnUris);
		        String[] content = new String[lfnUris.size()];
		        return lfnUris.toArray(content);
			}else{
				return null;
			}
		} catch (IOException e) {
			LOGGER.info("Unable to list the directory "+location+": "+e.getMessage());
			return null;
		}
    }

    /** {@inheritDoc} */
    public boolean mkdir() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	//Create the VOMS proxy if needed
    	GliteSecurityUtils.touchVomsProxy(gatContext);
    	try {
			lfcConnector.mkdir(location.getPath());
			return true;
		} catch (IOException e) {
			LOGGER.info(GLITE_LFC_FILE_ADAPTOR + ": Unable to create the "+location+" directory:" + e.getMessage());
			return false;
		}
    }

    /** {@inheritDoc} */
    public boolean renameTo(File newFile) throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	if (!newFile.toGATURI().isCompatible(LFN)) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR
                    + ": " + CANNOT_HANDLE_THIS_URI + newFile.toGATURI());
        }
    	
    	//Create the VOMS proxy if needed
    	GliteSecurityUtils.touchVomsProxy(gatContext);
    	try {
			lfcConnector.rename(location.getPath(), newFile.toGATURI().getPath());
			return true;
		} catch (IOException e) {
			LOGGER.info(GLITE_LFC_FILE_ADAPTOR + ": Unable to create the "+location+" directory:" + e.getMessage());
			return false;
		}
    }
}
