/*
 * SGEBrokerAdaptor.java
 *
 * Created on May 16, 2006, 11:25 AM
 *
 */

package org.gridlab.gat.resources.cpi.sge;

// org.ggf.drmaa imports
import java.util.List;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;

/**
 *
 * @author ole.weidner
 * 
 * renamed to SGEResourceBrokerAdaptor by Roelof Kemp
 * in order to be consistent with JavaGAT naming
 */



public class SGEResourceBrokerAdaptor extends ResourceBrokerCpi {

    
    private Session SGEsession;

    private SGEJob sgejob;
    
    public SGEResourceBrokerAdaptor(GATContext gatContext, Preferences preferences)
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
            {
        throw new UnsupportedOperationException("Not implemented");
    }    
    
    public List<HardwareResource> findResources(ResourceDescription resourceDescription) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    public Job submitJob(JobDescription description, MetricListener listener, String metricDefinitionName)
            throws GATInvocationException {
        
       SoftwareDescription sd = description.getSoftwareDescription();  
       String host = getHostname(description);
       
       URI hostURI;
       Sandbox sandbox = null;
       
        try {
            hostURI = new URI(host);
        } catch (Exception e) {
            throw new GATInvocationException("SGEBrokerAdaptor", e);
        }
       
       /* Handle pre-/poststaging */
        if (host != null) {
            sandbox = new Sandbox(gatContext, preferences, description, host, null, false, true, true, true);
        }
       
        if (sd == null) {
            throw new GATInvocationException(
                "The job description does not contain a software description");
        }        
       
        try {          
        
            SGEsession.init( hostURI.toString() );
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
        
            sgejob = new SGEJob(gatContext, preferences, this, description,SGEsession, id, sandbox);
        

            
            
        } catch(DrmaaException e) {
            System.err.println("Execption in SGEBRokerAdaptor");
            System.err.println(e);
        }
        
       
       
       
        return sgejob;
    }
}
