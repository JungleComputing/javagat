/*
 * SGEBrokerAdaptor.java
 *
 * Created on May 16, 2006, 11:25 AM
 *
 */

package org.gridlab.gat.resources.cpi.sge;

// org.ggf.drmaa imports
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.gridlab.gat.FilePrestageException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;

/**
 *
 * @author ole.weidner
 */
public class SGEBrokerAdaptor extends ResourceBrokerCpi {
    
    private Session SGEsession;

    private SGEJob sgejob;
    
    public SGEBrokerAdaptor(GATContext gatContext, Preferences preferences)
            throws GATObjectCreationException {
        super(gatContext, preferences);    
        
        SessionFactory factory = SessionFactory.getFactory();
        SGEsession = factory.getSession();             
        
        //Properties props = System.getProperties();
        //props.setProperty("org.ggf.drmaa.SessionFactory","NONE");
        
    }
    
    public Reservation reserveResource(ResourceDescription resourceDescription,
            TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }    
    
    public Reservation reserveResource(Resource resource, TimePeriod timePeriod)
            throws RemoteException, IOException {
        throw new UnsupportedOperationException("Not implemented");
    }    
    
    public List findResources(ResourceDescription resourceDescription) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    public Job submitJob(JobDescription description)
            throws GATInvocationException {
        
       SoftwareDescription sd = description.getSoftwareDescription();  
       String host = getHostname(description);
       
       URI hostURI;
       
        try {
            hostURI = new URI(host);
        } catch (Exception e) {
            throw new GATInvocationException("SGEBrokerAdaptor", e);
        }
       
       /* Handle pre-/poststaging */
        if (host != null) {
            try {
                removePostStagedFiles(description, host, null);
            } catch (GATInvocationException e) {
                // ignore, maybe the files did not exist anyway
            }

            try {
                preStageFiles(description, host, null);
            } catch (Exception e) {
                throw new FilePrestageException("SGEBrokerAdaptor", e);
            }
        }
       
        
        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }        
       
        try {          
            
            SGEsession.init(null);
            JobTemplate jt = SGEsession.createJobTemplate();
        
            if( sd.getLocation() != null)
                jt.setRemoteCommand(sd.getLocation().getPath());
            System.out.println(jt.getRemoteCommand());
        
            if( sd.getArguments() != null)
                jt.setArgs(sd.getArguments());
            
            if( sd.getStdout() != null)
               jt.setOutputPath(host+":"+sd.getStdout().getName());
        
            String id = SGEsession.runJob (jt);
        
            SGEsession.deleteJobTemplate(jt);
        
            sgejob = new SGEJob(this, description,SGEsession, id);
        
        
        } catch(DrmaaException e) {
            System.err.println("Execption in SGEBRokerAdaptor");
            System.err.println(e);
        }
        
        return sgejob;
    }
}
