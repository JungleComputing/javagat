package org.gridlab.gat.io.cpi.glite;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnector;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCFile;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCReplica;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.glite.GliteConstants;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder;
import org.gridlab.gat.resources.cpi.glite.LDAPResourceFinder.SEInfo;
import org.gridlab.gat.security.glite.GliteSecurityUtils;

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

    protected static final Logger LOGGER = Logger.getLogger(GliteLfnFileAdaptor.class);

    private LfcConnector lfcConnector;

    private boolean localFile;

    private final String vo;
    
    public GliteLfnFileAdaptor(GATContext gatCtx, URI location)
            throws GATObjectCreationException {
    	super(gatCtx, location);
    	
    	//Get all the needed information to fill location URI if needed
    	String server = fetchServer(gatContext);
        String portStr = (String) gatContext.getPreferences().get("LfcServerPort", "5010");
        int port = Integer.parseInt(portStr);
    	try{
            if(location.getHost() == null){
            	location = location.setHost(server);
        	}
            if (location.getPort() == -1) {
            	location = location.setPort(port);
			}
        } catch (URISyntaxException e) {
        	throw new GATObjectCreationException(GLITE_LFC_FILE_ADAPTOR, e);
		}
    	
        vo = (String) gatContext.getPreferences().get(GliteConstants.PREFERENCE_VIRTUAL_ORGANISATION);
        
        if (location.isCompatible("file") && location.refersToLocalHost()) {
            localFile = true;
        } else {
            localFile = false;
            if (!location.isCompatible(LFN)) {
                throw new AdaptorNotApplicableException(
                        "cannot handle this URI: " + location);
            }
            this.initLfcConnector(server, port);
            logger.info("Instantiated gliteLfnFileAdaptor for " + location);
        }
    }

    private void initLfcConnector(String server, int port) throws GATObjectCreationException {
        if (lfcConnector == null) {
            lfcConnector = new LfcConnector(server, port, vo);
        }
    }

    private String fetchServer(GATContext gatContext)
            throws GATObjectCreationException {
        String retVal;
        retVal = (String) gatContext.getPreferences().get("LfcServer");
        if (retVal == null) {
            try {
                List<String> lfcs = new LDAPResourceFinder(null).fetchLFCs(vo);
                if (lfcs == null) {
                    retVal = null;
                } else if (lfcs.size() < 1)
                    retVal = null;
                else
                    retVal = lfcs.get(0);
            } catch (NamingException e) {
                retVal = null;
            }
        }
        if (retVal == null) {
            throw new GATObjectCreationException(
                    "Failed to find LFC in preferences!");
        }
        return retVal;
    }

    /** {@inheritDoc} */
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
        Preferences preferences = ResourceBrokerCpi.getSupportedPreferences();
        GliteSecurityUtils.addGliteSecurityPreferences(preferences);
        return preferences;
    }

    /** {@inheritDoc} */
    public void copy(URI dest) throws GATInvocationException {
        try {
            if (localFile) {
                if (!dest.isCompatible(LFN)) {
                    throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR
                            + ": " + CANNOT_HANDLE_THIS_URI + dest);
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
                // TEMP SOLUTION
                Collections.shuffle(ses);
                // END TEMP SOLUTION
                
                SEInfo pickedSE = ses.get(0);
                
                String guid = UUID.randomUUID().toString();
                URI target = new URI("srm://" + pickedSE.getSeUniqueId()
                        + pickedSE.getPath() + "/file-" + guid);
                logger.info("Uploading " + guid + " to " + target);

                GATContext newContext = (GATContext) gatContext.clone();
                newContext.addPreference("File.adaptor.name", "GliteSrm");
                org.gridlab.gat.io.File transportFile = GAT.createFile(
                        newContext, location);
                transportFile.copy(target);
                logger.info("Registering file in the LFC...");
                try{
                	lfcConnector.create(dest);
                }catch (Exception e) {
                	//If an error occurs, remove the created file from the SE
                	org.gridlab.gat.io.File toDeleteFile = GAT.createFile(newContext, target);
                	toDeleteFile.delete();
                	throw e;
				}
            } else {
                String lfnPath = location.getPath();
                GliteSecurityUtils.touchVomsProxy(gatContext);
                logger.info("Copying " + lfnPath + " to " + dest);
                Collection<LFCReplica> replicas = lfcConnector.listReplicas(lfnPath, null);
                logger.info("Replicas: " + replicas);
                LFCReplica someReplica = replicas.iterator().next(); //Choose the first replica from the collection
                GliteSrmFileAdaptor srmFile = new GliteSrmFileAdaptor(gatContext, new URI(someReplica.getSfn()));
                srmFile.copy(dest);
            }
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
        logger.info("create new file: "+location);
        try {
            GliteSecurityUtils.touchVomsProxy(gatContext);
            lfcConnector.create(location);
        } catch (IOException e) {
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
            logger.info("Deleting " + lfn);
            return lfcConnector.deletepath(lfn);
        } catch (IOException e) {
            logger.error(e.toString());
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
			return lfcConnector.isDirectory(location.getPath());
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
			return lfcConnector.isFile(location.getPath());
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
			return lfcConnector.lastModified(location.getPath());
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
			return lfcConnector.length(location.getPath());
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
    	
        logger.debug("listing the content of " + location.getPath());
        Collection<String> lfnUris = null;;
		try {
			Collection<LFCFile> files = lfcConnector.list(location.getPath());
			if(files != null){
				lfnUris = new ArrayList<String>(files.size());
				for (Iterator<LFCFile> iterator = files.iterator(); iterator.hasNext();) {
					lfnUris.add("lfn://"+location.getHost()+":"+location.getPort()+"/"+location.getPath() + (location.getPath().endsWith("/") ? "" : "/") + iterator.next().getFileName());
				}
				logger.info("LFN URIs: " + lfnUris);
		        String[] content = new String[lfnUris.size()];
		        return lfnUris.toArray(content);
			}else{
				return null;
			}
		} catch (IOException e) {
			logger.error("",e);
			throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
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
			throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR, e);
		}
    }

    /** {@inheritDoc} */
    public boolean renameTo(File arg0) throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	//TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    public boolean setLastModified(long arg0) throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	//TODO
        throw new UnsupportedOperationException("Not implemented");
    }

    /** {@inheritDoc} */
    public boolean setReadOnly() throws GATInvocationException {
    	if (localFile) {
            throw new GATInvocationException(GLITE_LFC_FILE_ADAPTOR + ": "
                    + CANNOT_HANDLE_THIS_URI + location);
        }
    	//TODO
        throw new UnsupportedOperationException("Not implemented");
    }
    

}
