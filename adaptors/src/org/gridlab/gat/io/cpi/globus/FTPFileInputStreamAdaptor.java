package org.gridlab.gat.io.cpi.globus;

import java.io.InputStream;
import java.util.List;

import org.globus.io.streams.FTPInputStream;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

/**
 * A DefaultFileStreamAdaptor represents a connection to open file, the file may
 * be either remote or local.
 * <p>
 * A DefaultFileStreamAdaptor represents a seekable connection to a file and has
 * semantics similar to a standard Unix filedescriptor. It provides methods to
 * query the current position in the file and to seek to new positions.
 * <p>
 * To Write data to a DefaultFileStreamAdaptor it is necessary to construct a
 * Buffer and pack it with data. Similarly, to read data a buffer must be
 * created to store the read data. Writes and reads may either be blocking, or
 * asynchronous. Asynchronous writes or reads must be completed by appropriate
 * call.
 */
public class FTPFileInputStreamAdaptor extends GlobusFileInputStreamAdaptor {
    public FTPFileInputStreamAdaptor(GATContext gatContext,
            Preferences preferences, URI location)
            throws GATObjectCreationException {
        super(gatContext, preferences, location);

        if (!location.isCompatible("ftp")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        // now try to create a stream.
        try {
            in = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("ftp file inputstream", e);
        }
    }

    protected InputStream createStream() throws GATInvocationException {
        List l = SecurityContextUtils.getValidSecurityContextsByType(
            gatContext, preferences,
            "org.gridlab.gat.security.PasswordSecurityContext", "ftp", location
                .getHost(), location
                .getPort(GlobusFileAdaptor.DEFAULT_FTP_PORT));

        if ((l == null) || (l.size() == 0)) {
            throw new GATInvocationException(
                "Could not find a valid security context for this " + ""
                    + "adaptor to use for the specified host/port");
        }

        // for now, just take the first one from the list that matches
        PasswordSecurityContext c = (PasswordSecurityContext) l.get(0);
        String user = c.getUsername();
        String password = c.getPassword();

        String host = location.getHost();
        String path = location.getPath();

        int port = GlobusFileAdaptor.DEFAULT_FTP_PORT;

        // allow port override
        if (location.getPort() != -1) {
            port = location.getPort();
        }

        try {
            FTPInputStream input = new FTPInputStream(host, port, user,
                password, path);

            return input;
        } catch (Exception e) {
            // ouch, both failed.
            throw new GATInvocationException("ftp", e);
        }
    }
}
