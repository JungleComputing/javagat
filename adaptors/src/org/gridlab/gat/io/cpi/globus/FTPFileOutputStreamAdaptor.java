package org.gridlab.gat.io.cpi.globus;

import java.io.OutputStream;
import java.util.List;

import org.globus.io.streams.FTPOutputStream;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

public class FTPFileOutputStreamAdaptor extends GlobusFileOutputStreamAdaptor {
    public FTPFileOutputStreamAdaptor(GATContext gatContext,
            Preferences preferences, URI location, Boolean append)
            throws GATObjectCreationException {
        super(gatContext, preferences, location, append);

        if (!location.isCompatible("ftp")) {
            throw new GATObjectCreationException("cannot handle this URI");
        }

        // now try to create a stream.
        try {
            out = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("ftp outputstream", e);
        }
    }

    protected OutputStream createStream() throws GATInvocationException {
        List l = SecurityContextUtils.getValidSecurityContextsByType(
            gatContext, preferences,
            "org.gridlab.gat.security.PasswordSecurityContext", "ftp", location
                .resolveHost(), location
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

        String host = location.resolveHost();
        String path = location.getPath();

        int port = location.getPort(GlobusFileAdaptor.DEFAULT_FTP_PORT);

        try {
            FTPOutputStream output = new FTPOutputStream(host, port, user,
                password, path, append);

            return output;
        } catch (Exception e) {
            throw new GATInvocationException("ftp", e);
        }
    }
}
