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
     * Get the list of file replicas according to the file path or to the file
     * guid
     * 
     * @param path
     *            Path of the file
     * @param guid
     *            GUID of the file
     * @return A list of SRM URIs
     * @throws IOException
     *             if anything goes wrong
     */
    public Collection<LFCReplica> listReplicas(String path, String guid)
            throws IOException {
        final LfcConnection connection = new LfcConnection(server, port,
                proxyPath);
        final Collection<LFCReplica> retVal;
        try {
            retVal = connection.listReplica(path, guid);
        } finally {
            connection.close();
        }
        return retVal;
    }
    
    private LFCFile lstat(String path) throws IOException {
        final LfcConnection connection = new LfcConnection(server, port,
                proxyPath);
        final LFCFile file;
        try {
            file = connection.lstat(path);
        } finally {
            connection.close();
        }
        return file;
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
        final LFCFile file = this.lstat(path);
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
        final LFCFile file = this.lstat(path);
        return file.isFile();
    }
    
    /**
     * Test if the file or the directory can be read.
     * @param path
     * @return <code>true</code> if it can be read
     * @throws IOException if a problem occurs
     */
    public boolean canRead(String path) throws IOException {
        final LfcConnection connection = new LfcConnection(server, port, proxyPath);
    	try {
			connection.access(path, AccessType.READ_OK);
		} catch (IOException e) {
			if(e instanceof ReceiveException){
				if(((ReceiveException)e).getError() == 13){ //Permission denied
					return false;
				}
			}
			throw e;
		} finally {
		    connection.close();
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
        final LfcConnection connection = new LfcConnection(server, port, proxyPath);
    	try {
			connection.access(path, AccessType.WRITE_OK);
		} catch (IOException e) {
			if(e instanceof ReceiveException){
				if(((ReceiveException)e).getError() == 13){ //Permission denied
					return false;
				}
			}
			throw e;
		} finally {
		    connection.close();
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
        final LfcConnection connection = new LfcConnection(server, port, proxyPath);
    	try {
			connection.access(path, AccessType.EXIST_OK);
		} catch (IOException e) {
			if(e instanceof ReceiveException){
				if(((ReceiveException)e).getError() == 2){ //No such file or directory
					return false;
				}
			}
			throw e;
		} finally {
		    connection.close();
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
        final LFCFile file = this.lstat(path);
    	return file.getMDate().getTime();
	}
    
    /**
     * Get the length of a file in bytes
     * @param path	Path of the file
     * @return Returns the size of the file in bytes.
     * @throws IOException	If anything goes wrong
     */
    public long length(String path) throws IOException {
        final LFCFile file = this.lstat(path);
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
        final LFCFile file = this.lstat(path);
        if (!file.isDirectory()) {
            return null;
        }

        final LfcConnection lfcConnection = new LfcConnection(server, port,
                proxyPath);
        final Collection<LFCFile> files;
        try {
            final long fileID = lfcConnection.opendir(path, null);
            files = lfcConnection.readdir(fileID);
            lfcConnection.closedir();
        } finally {
            lfcConnection.close();
        }
        return files;
    }
    
    /**
     * Create a directory in the LFC
     * @param path	path of the directory
     * @throws IOException	if anything goes wrong
     */
    public void mkdir(String path) throws IOException {
        final LfcConnection connection = new LfcConnection(server, port, proxyPath);
        try {
            connection.mkdir(path, UUID.randomUUID().toString());
        } finally {
            connection.close();
        }
    }
    
    /**
     * Delete a file or a directory from the LFC.
     * If it's a file, delete also all the replicas from the DPMs
     * @param path	path of the file/directory
     * @return	<code>true</code> is everything was ok.
     * @throws IOException	if anything goes wrong
     */
    public boolean deletePath(String path) throws IOException {
        if (this.lstat(path).isDirectory()) {
            final LfcConnection connection = new LfcConnection(server, port,
                    proxyPath);
            try {
                if (connection.delete(path) == 0) {
                    return true;
                }
            } finally {
                connection.close();
            }
        } else {
            final LfcConnection connection = new LfcConnection(server, port, proxyPath);
            final Collection<LFCReplica> replicas;
            try {
                 replicas = connection.listReplica(path, null);
            } finally {
                connection.close();
            }
	        final SrmConnector connector = new SrmConnector(proxyPath);
	        for (LFCReplica replica : replicas) {
	            LOGGER.info("Deleting Replica: " + replica.getSfn());
	            final LfcConnection connection2 = new LfcConnection(server, port, proxyPath);
	            try {
	                connection2.delReplica(replica.getGuid(), replica.getSfn());
	            } finally {
	                connection2.close();
	            }
	            try {
	                connector.delete(new URI(replica.getSfn()));
	            } catch (URISyntaxException e) {
	                // ignore.
	            } catch (IOException e) {
	                LOGGER.warn("Failed to delete Replica " + replica.getSfn(),e);
	            }
                LOGGER.info("Deleting path: " + path);
                final LfcConnection connection3 = new LfcConnection(server,
                        port, proxyPath);
                try {
                    if (!connection3.delFiles(replica.getGuid(), false)) {
                        return false;
                    }
                } finally {
                    connection3.close();
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
    public boolean deleteGuid(String guid) throws IOException {
        final LfcConnection connection = new LfcConnection(server, port,
                proxyPath);
        final Collection<LFCReplica> replicas;
        try {
            replicas = connection.listReplica(null, guid);
        } finally {
            connection.close();
        }
        final SrmConnector connector = new SrmConnector(proxyPath);
        for (LFCReplica replica : replicas) {
            LOGGER.info("Deleting Replica: " + replica.getSfn());
            final LfcConnection connection2 = new LfcConnection(server, port,
                    proxyPath);
            try {
                connection2.delReplica(guid, replica.getSfn());
            } finally {
                connection2.close();
            }
            try {
                connector.delete(new URI(replica.getSfn()));
            } catch (URISyntaxException e) {
                // ignore.
            } catch (IOException e) {
                LOGGER.warn("Failed to delete Replica " + replica.getSfn(), e);
            }
        }
        LOGGER.info("Deleting GUID: " + guid);
        final LfcConnection connection3 = new LfcConnection(server, port,
                proxyPath);
        try {
            return connection3.delFiles(guid, false);
        } finally {
            connection3.close();
        }
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
        final LfcConnection connection = new LfcConnection(server, port,
                proxyPath);
        try {
            connection.mkdir(parent, UUID.randomUUID().toString());
        } catch (IOException e) {
            LOGGER.debug("Creating parent", e);
        } finally {
            connection.close();
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
        final LfcConnection connection = new LfcConnection(server, port,
                proxyPath);
        try {
            connection.creat(path, guid);
        } finally {
            connection.close();
        }
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

    /**
     * Add a Replica entry for the given file
     * 
     * @param guid
     *            GUID of the file (without decoration)
     * @param target
     *            an SRM uri.
     * @throws IOException
     *             if anything goes wrong
     */
    public void addReplica(String guid, URI target) throws IOException {
        final LfcConnection connection = new LfcConnection(server, port,
                proxyPath);
        try {
            connection.addReplica(guid, target.toJavaURI());
        } finally {
            connection.close();
        }
    }

    /**
     * @return the server used by this connector.
     */
    public String getServer() {
        return server;
    }
    
    /**
     * @return the port used by this connector.
     */
    public int getPort() {
        return port;
    }
    
}
