package org.gridlab.gat.io.cpi.glite.lfc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.AccessType;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCFile;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.LFCReplica;
import org.gridlab.gat.io.cpi.glite.lfc.LfcConnection.ReceiveException;
import org.gridlab.gat.io.cpi.glite.srm.SrmConnector;

/**
 * Provides a High-Level view of an LFC server.
 * 
 * @author Max Berger
 * @author Jerome Revillard
 */
public class LfcConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(LfcConnector.class);

    private final String vo;
    private final String server;
    private final int port;
    private final String proxyPath;

    /**
     * Create a new LfcConector
     * 
     * @param server
     *            Server to connect to
     * @param port
     *            Port to connect to
     * @param vo
     *            VO of this server
     */
    public LfcConnector(String server, int port, String vo, String proxyPath) {
        this.server = server;
        this.port = port;
        this.vo = vo;
        this.proxyPath = proxyPath;
    }

    /**
     * Get the list of file replicas according to the file path 
     * or to the file guid
     * 
     * @param path
     * 			Path of the file
     * @param guid
     * 			GUID of the file
     * @return A list of SRM URIs
     * @throws IOException
     *             if anything goes wrong
     */
    public Collection<LFCReplica> listReplicas(String path, String guid) throws IOException {
        LfcConnection connection = new LfcConnection(server, port,proxyPath);
        return connection.listReplica(path, guid);
    }
    
    /**
     * Test if the given path is a directory
     * 
     * @param path
     *            path of the directory
     * @return true if the given path is a directory
     * @throws IOException
     *             if anything goes wrong
     */
    public boolean isDirectory(String path) throws IOException {
        LFCFile file = new LfcConnection(server, port, proxyPath).lstat(path);
        return file.isDirectory();
    }
    
    /**
     * Test if the given path is a file
     * 
     * @param path
     *            path of the file
     * @return true if the given path is a regular file
     * @throws IOException
     *             if anything goes wrong
     */
    public boolean isFile(String path) throws IOException {
        LFCFile file = new LfcConnection(server, port, proxyPath).lstat(path);
        return file.isFile();
    }
    
    /**
     * Test if the file or the directory can be read.
     * @param path
     * @return <code>true</code> if it can be read
     * @throws IOException if a problem occurs
     */
    public boolean canRead(String path) throws IOException {
    	try {
			new LfcConnection(server, port, proxyPath).access(path, AccessType.READ_OK);
		} catch (IOException e) {
			if(e instanceof ReceiveException){
				if(((ReceiveException)e).getError() == 13){ //Permission denied
					return false;
				}
			}
			throw e;
		}
		return true;
    }
    
    /**
     * Test if the file or the directory can be written.
     * @param path
     * @return <code>true</code> if it can be written
     * @throws IOException if a problem occurs
     */
    public boolean canWrite(String path) throws IOException {
    	try {
			new LfcConnection(server, port, proxyPath).access(path, AccessType.WRITE_OK);
		} catch (IOException e) {
			if(e instanceof ReceiveException){
				if(((ReceiveException)e).getError() == 13){ //Permission denied
					return false;
				}
			}
			throw e;
		}
		return true;
    }
    
    /**
     * Test the existence of a path
     * @param path	path to test
     * @return <code>true</code> if the path exists
     * @throws IOException if anything goes wrong
     */
    public boolean exist(String path) throws IOException {
    	try {
			new LfcConnection(server, port, proxyPath).access(path, AccessType.EXIST_OK);
		} catch (IOException e) {
			if(e instanceof ReceiveException){
				if(((ReceiveException)e).getError() == 2){ //No such file or directory
					return false;
				}
			}
			throw e;
		}
		return true;
	}
    
    /**
     * Get the last modified date in millisecond
     * @param path	Path of the file/directory
     * @return Returns the last modified date in millisecond
     * @throws IOException	If anything goes wrong
     */
    public long lastModified(String path) throws IOException {
    	LFCFile file = new LfcConnection(server, port, proxyPath).lstat(path);
    	return file.getMDate().getTime();
	}
    
    /**
     * Get the length of a file in bytes
     * @param path	Path of the file
     * @return Returns the size of the file in bytes.
     * @throws IOException	If anything goes wrong
     */
    public long length(String path) throws IOException {
    	LFCFile file = new LfcConnection(server, port, proxyPath).lstat(path);
    	return file.getFileSize();
	}
    
    /**
     * Get the content of a directory. If the given path is not a directory
     * it will return <code>null</code>
     * 
     * @param path
     *            path of the directory
     * @return A collection of files or directories inside the given path or null if the given path is not a directory
     * @throws IOException
     *             if anything goes wrong
     */
    public Collection<LFCFile> list(String path) throws IOException {
        LFCFile file = new LfcConnection(server, port, proxyPath).lstat(path);
        if(!file.isDirectory()){
        	return null;
        }
        
        LfcConnection lfcConnection = new LfcConnection(server, port, proxyPath);
        long fileID = lfcConnection.opendir(path,null);
        Collection<LFCFile> files = lfcConnection.readdir(fileID);
        lfcConnection.closedir();
        
        return files;
    }
    
    /**
     * Create a directory in the LFC
     * @param path	path of the directory
     * @throws IOException	if anything goes wrong
     */
    public void mkdir(String path) throws IOException {
        new LfcConnection(server, port, proxyPath).mkdir(path, UUID.randomUUID().toString());
    }
    
    /**
     * Delete a file or a directory from the LFC.
     * If it's a file, delete also all the replicas from the DPMs
     * @param path	path of the file/directory
     * @return	<code>true</code> is everything was ok.
     * @throws IOException	if anything goes wrong
     */
    public boolean deletepath(String path) throws IOException {
    	if(new LfcConnection(server,port, proxyPath).lstat(path).isDirectory()){
    		if(new LfcConnection(server, port, proxyPath).delete(path) == 0){
        		return true;
        	}
    	}else{
	    	Collection<LFCReplica> replicas = new LfcConnection(server, port, proxyPath).listReplica(path, null);
	        final SrmConnector connector = new SrmConnector(proxyPath);
	        for (LFCReplica replica : replicas) {
	            LOGGER.info("Deleting Replica: " + replica.getSfn());
	            new LfcConnection(server, port, proxyPath).delReplica(replica.getGuid(), replica.getSfn());
	            try {
	                connector.delete(new URI(replica.getSfn()));
	            } catch (URISyntaxException e) {
	                // ignore.
	            } catch (IOException e) {
	                LOGGER.warn("Failed to delete Replica " + replica.getSfn(),e);
	            }
	            LOGGER.info("Deleting path: " +path);
		        if(!new LfcConnection(server, port, proxyPath).delFiles(replica.getGuid(), false)){
		        	return false;
		        }
	        }
	        return true;
    	}
    	return false;
    }

    /**
     * Try to delete a file.
     * 
     * @param guid
     *            GUID of the file.
     * @return true if the file was deleted.
     * @throws IOException
     *             if anything goes wrong
     */
    public boolean delete(String guid) throws IOException {
        Collection<LFCReplica> replicas = new LfcConnection(server, port, proxyPath).listReplica(null, guid);
        final SrmConnector connector = new SrmConnector(proxyPath);
        for (LFCReplica replica : replicas) {
            LOGGER.info("Deleting Replica: " + replica.getSfn());
            new LfcConnection(server, port, proxyPath).delReplica(guid, replica.getSfn());
            try {
                connector.delete(new URI(replica.getSfn()));
            } catch (URISyntaxException e) {
                // ignore.
            } catch (IOException e) {
                LOGGER.warn("Failed to delete Replica " + replica.getSfn(),e);
            }
        }
        LOGGER.info("Deleting GUID: " + guid);
        return new LfcConnection(server, port, proxyPath).delFiles(guid, false);
    }

    /**
     * Create a new File
     * 
     * @return a GUID to the new file
     * @throws IOException
     *             if anything goes wrong
     */
    public String create() throws IOException {
        String guid = UUID.randomUUID().toString();
        String parent = "/grid/" + vo + "/generated/" + dateAsPath();
        try {
            new LfcConnection(server, port, proxyPath).mkdir(parent, UUID.randomUUID()
                    .toString());
        } catch (IOException e) {
            LOGGER.debug("Creating parent", e);
        }
        String path = parent + "/file-" + guid;
        URI uri = null;
        try {
            uri = new URI("lfn:///" + path);
        } catch (URISyntaxException e) {
        }
        return create(uri);
    }

    /**
     * Create a new File at a specific location
     * 
     * @param location
     *            The lfn of the file to create (lfn:////grid/vo/...). Please
     *            note the 4 (!) slashes. These are required to specify an empty
     *            hostname and an absolute directory.
     * @return a GUID to the new file
     * @throws IOException
     *             if anything goes wrong
     */
    public String create(URI location) throws IOException {
        String guid = UUID.randomUUID().toString();
        String path = location.getPath();
        LOGGER.info("Creating " + guid + " with path " + path);
        new LfcConnection(server, port, proxyPath).creat(path, guid);
        return guid;
    }

    private String dateAsPath() {
        Calendar c = GregorianCalendar.getInstance();
        StringBuilder b = new StringBuilder();
        b.append(c.get(Calendar.YEAR));
        b.append('-');
        // Java counts month starting with 0 !
        int month = c.get(Calendar.MONTH) + 1;
        if (month < 10)
            b.append('0');
        b.append(month);
        b.append('-');
        int day = c.get(Calendar.DAY_OF_MONTH);
        if (day < 10)
            b.append('0');
        b.append(day);
        return b.toString();
    }

    public void addReplica(String guid, URI target) throws IOException {
        new LfcConnection(server, port, proxyPath).addReplica(guid, target.toJavaURI());
    }

}
