package org.gridlab.gat.io.cpi.gt4;

import org.globus.cog.abstraction.interfaces.FileResource;
import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.abstraction.interfaces.ServiceContact;
import org.globus.cog.abstraction.impl.common.task.ServiceContactImpl;
import org.globus.cog.abstraction.impl.file.FileNotFoundException;
import org.globus.cog.abstraction.impl.file.DirectoryNotFoundException;
import org.globus.cog.abstraction.impl.file.GeneralException;
import org.globus.cog.abstraction.interfaces.GridFile;
import org.globus.cog.abstraction.interfaces.Permissions;

import org.globus.cog.abstraction.interfaces.Task;
import org.globus.cog.abstraction.impl.common.task.TaskImpl;
import org.globus.cog.abstraction.interfaces.FileOperationSpecification;
import org.globus.cog.abstraction.impl.common.task.FileOperationSpecificationImpl;
import org.globus.cog.abstraction.impl.common.task.FileTransferTaskHandler;
import org.globus.cog.abstraction.impl.common.execution.TaskHandlerImpl;
import org.globus.cog.abstraction.interfaces.FileTransferSpecification;
import org.globus.cog.abstraction.impl.common.task.FileTransferSpecificationImpl;
import org.globus.cog.abstraction.interfaces.Service;
import org.globus.cog.abstraction.impl.common.task.ServiceImpl;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.MethodNotApplicableException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.cpi.FileCpi;
import org.gridlab.gat.security.globus.GlobusSecurityUtils;
import org.ietf.jgss.GSSCredential;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Collection;
import java.util.Iterator;

//This class only supports the URIs with gsiftp
public class GT4GridFTPFileAdaptor extends GT4FileAdaptor {
    /**
     * @param gatContext
     * @param preferences
     * @param location
     */
    public GT4GridFTPFileAdaptor(GATContext gatContext, Preferences preferences,
			  URI location) throws GATObjectCreationException {
        super(gatContext, preferences, location, "gsiftp");
    }

    protected SecurityContext getSecurityContext() 
	throws AdaptorNotApplicableException, GATInvocationException {
	SecurityContext securityContext = null;
	try {
	    securityContext = AbstractionFactory.newSecurityContext(srcProvider);
	} catch(Exception e) {
	    throw new AdaptorNotApplicableException("GT4GridFTPFileAdaptor: cannot create SecurityContext: "+e);
	}
	GSSCredential cred = null;
	try {
	    cred = GlobusSecurityUtils.getGlobusCredential(gatContext, preferences,
							   "gt4gridftp", location, 
							   DEFAULT_GRIDFTP_PORT);
	} catch(Exception e) {
	    throw new GATInvocationException("GT4GridFTPFileAdaptor: could not initialize credentials, " + e);
	}
	securityContext.setCredentials(cred);
	return securityContext;
    }
}
