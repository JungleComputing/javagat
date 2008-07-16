package org.gridlab.gat.io.cpi.globus;

import java.io.OutputStream;
import java.util.List;

import org.globus.ftp.exception.ServerException;
import org.globus.io.streams.FTPOutputStream;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.InvalidUsernameOrPasswordException;
import org.gridlab.gat.URI;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;
import org.gridlab.gat.security.cpi.SecurityContextUtils;

public class FTPFileOutputStreamAdaptor extends GlobusFileOutputStreamAdaptor {

    public FTPFileOutputStreamAdaptor(GATContext gatContext, URI location,
            Boolean append) throws GATObjectCreationException {
        super(gatContext, location, append);

        if (!location.isCompatible("ftp")) {
            throw new AdaptorNotApplicableException("cannot handle this URI: "
                    + location);
        }

        // now try to create a stream.
        try {
            out = createStream();
        } catch (GATInvocationException e) {
            throw new GATObjectCreationException("ftp outputstream", e);
        }
    }

    protected OutputStream createStream() throws GATInvocationException {
        List<SecurityContext> l = SecurityContextUtils
                .getValidSecurityContextsByType(gatContext,
                        "org.gridlab.gat.security.PasswordSecurityContext",
                        "ftp", location.resolveHost(), location
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
            if (e instanceof ServerException) {
                if (((ServerException) e).getCode() == ServerException.SERVER_REFUSED) {
                    if (e
                            .getMessage()
                            .startsWith(
                                    "Server refused performing the request. Custom message: Bad password.")) {
                        throw new GATInvocationException("ftp",
                                new InvalidUsernameOrPasswordException(e));
                    }
                }
            }
            throw new GATInvocationException("ftp", e);
        }
    }
}
