/*
 * UnicoreResourceBrokerAdaptor.java
 *
 * Created on July 8, 2008
 *
 */

package org.gridlab.gat.resources.cpi.unicore;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.TimePeriod;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
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
//import org.gridlab.gat.resources.cpi.sge.SgeJob;

/**
 * The Unicore / HiLA staff
 */

import de.fzj.hila.*;
import de.fzj.hila.common.jsdl.JSDL;
import de.fzj.hila.exceptions.HiLAException;
import de.fzj.hila.exceptions.HiLALocationSyntaxException;


/**
 * 
 * @author Alexander Beck-Ratzka, AEI.
 * 
 */

public class UnicoreResourceBrokerAdaptor extends ResourceBrokerCpi {
	
	protected static Logger logger = Logger.getLogger(UnicoreResourceBrokerAdaptor.class);
	
    public UnicoreResourceBrokerAdaptor(GATContext gatContext, URI brokerURI) throws GATObjectCreationException {
    	super(gatContext, brokerURI);
    	
    }
    
    public Reservation reserveResource(ResourceDescription resourceDescription,
            TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Reservation reserveResource(Resource resource, TimePeriod timePeriod) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List findResources(
            ResourceDescription resourceDescription) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public JSDL createJsdl (SoftwareDescription sd) {
    	
    	Map preStaged=null;
    	Map postStaged=null;
    	JSDL JsdlStruc=null;
    	
    	JsdlStruc.getJsdl();
    	
    	/**
    	 * fill in with elements of software description. Right now this concerns pre and poststaging dataset.
    	 */
    	
    	preStaged=sd.getPreStaged();
    	if (preStaged!=null) {
    		Set keys = preStaged.keySet();
    		Iterator i = keys.iterator();
            while (i.hasNext()) {
                File srcFile = (File) i.next();
                File destFile = (File) preStaged.get(srcFile);
                
                if (destFile == null) {
                    logger.debug("ignoring prestaged file, no destination set!");
                    continue;
                    } 
                
                JsdlStruc.addStageIn(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
                }            
        	
        	postStaged=sd.getPostStaged();
        	if (postStaged!=null) {
        		Set keysout = postStaged.keySet();
        		Iterator iout = keysout.iterator();
                while (iout.hasNext()) {
                    File srcFile = (File) iout.next();
                    File destFile = (File) preStaged.get(srcFile);
                    
                    if (destFile == null) {
                        logger.debug("ignoring prestaged file, no destination set!");
                        continue;
                        } 
                    
                    JsdlStruc.addStageOut(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
                    }            
        	}
    	}
    	return JsdlStruc;
    	
    }
    public Job submitJob(JobDescription description, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {

        SoftwareDescription sd = description.getSoftwareDescription();
        String host = getHostname();

        Sandbox sandbox = null;

        /* Handle pre-/poststaging */
        if (host != null) {
            sandbox = new Sandbox(gatContext, description, host,
                    null, false, true, true, true);
        }
        
        UnicoreJob job = new UnicoreJob(gatContext, description, sandbox);
		try {
			job.site  = (Site) HiLAFactory.getInstance().locate(new Location(host));
			job.jsdl  = this.createJsdl(sd);
			Task task = job.site.submit(job.jsdl);
			
            task.startSync(); // FIXME: startsync richtig machen...
            job.setState(Job.SCHEDULED);
            job.setTask(task);

			
		} catch (HiLAFactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GATInvocationException("UnicoreResourceBrokerAdaptor", e);
			} catch (HiLALocationSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GATInvocationException("UnicoreResourceBrokerAdaptor", e);
		} catch (HiLAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GATInvocationException("UnicoreResourceBrokerAdaptor", e);
		}

        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }
        job.setHostname(getHostname());
        job.setState(Job.PRE_STAGING);
        
//        job.setSession(SGEsession);
        sandbox.prestage();

        if (sd == null) {
            throw new GATInvocationException(
                    "The job description does not contain a software description");
        }

        try {
            if (sd.getArguments() != null) {
                jt.setArgs(sd.getArguments());
            }

            if (sd.getStdout() != null) {
                jt.setOutputPath(host + ":" + sd.getStdout().getName());
            }

            job.setJobID(SGEsession.runJob(jt));
            job.setState(Job.SCHEDULED);
            job.startListener();

            SGEsession.deleteJobTemplate(jt);
            return job;
        } catch (DrmaaException e) {
            throw new GATInvocationException("SGEResourceBrokerAdaptor", e);
        }
    }

}