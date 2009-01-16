package org.gridlab.gat.io.cpi.glite.lfc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.cpi.glite.srm.SrmConnector;

/**
 * Provides a High-Level view of an LFC server.
 * 
 * @author Max Berger
 */
public class LfcConnector {
    protected static Logger logger = Logger.getLogger(LfcConnector.class);

    private final String vo;
    private final String server;
    private final int port;

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
    public LfcConnector(String server, int port, String vo) {
        this.server = server;
        this.port = port;
        this.vo = vo;
    }

    /**
     * Get a List of File Replicas
     * 
     * @param guid
     *            GUID of the file
     * @return A list of SRM URIs
     * @throws IOException
     *             if anything goes wrong
     */
    public Collection<String> listReplicas(String guid) throws IOException {
        LfcConnection connection = new LfcConnection(server, port);
        return connection.listReplica(null, guid);
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
        Collection<String> replicas = new LfcConnection(server, port)
                .listReplica(null, guid);
        final SrmConnector connector = new SrmConnector();
        for (String replicaURI : replicas) {
            logger.info("Deleting Replica: " + replicaURI);
            new LfcConnection(server, port).delReplica(guid, replicaURI);
            try {
                connector.delete(new URI(replicaURI));
            } catch (URISyntaxException e) {
                // ignore.
            } catch (IOException e) {
                logger.warn(e.toString());
                logger.warn("Failed to delete Replica " + replicaURI);
            }
        }
        logger.info("Deleting GUID: " + guid);
        return new LfcConnection(server, port).delFiles(guid, false);
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
            new LfcConnection(server, port).mkdir(parent, UUID.randomUUID()
                    .toString());
        } catch (IOException e) {
            logger.debug("Creating parent", e);
        }
        String path = parent + "/file-" + guid;
        logger.info("Creating " + guid + " with path " + path);
        new LfcConnection(server, port).creat(path, guid);
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
        new LfcConnection(server, port).addReplica(guid, target.toJavaURI());
    }

}
