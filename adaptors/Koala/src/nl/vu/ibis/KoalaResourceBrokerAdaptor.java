package nl.vu.ibis;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.CoScheduleJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.koala.DM.IFileTransfer;
import org.koala.JDL.JobDescriptionLanguage;
import org.koala.JDL.globusRSL;
import org.koala.common.RFModuleFactory;

public class KoalaResourceBrokerAdaptor extends ResourceBrokerCpi {

    public static String getDescription() {
        return "The Koala ResourceBroker Adaptor implements the ResourceBroker object on the Koala distributed scheduler.";
    }

    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                            .getSupportedCapabilities();
        capabilities.put("submitJob", true);
        return capabilities;
    }
    
    public static String[] getSupportedSchemes() {
        return new String[] { "koala"};
    }	
    
	protected static Logger logger = LoggerFactory.getLogger(KoalaResourceBrokerAdaptor.class);
	
	static { 
		
		// TODO: It seems that Koala is very 'single job per process' oriented, 
		// which may result in BIG issues in GAT.... 
		JobDescriptionLanguage jdl = new globusRSL(); 
		IFileTransfer dm = new KoalaFileTransfer();
		RFModuleFactory.registerDM(dm);
		RFModuleFactory.registerJDL(jdl);		
	}
	
	public KoalaResourceBrokerAdaptor(GATContext context, 
    		URI brokerURI) throws GATObjectCreationException {

    	super(context, getBrokerURI(brokerURI));

        // Prevent recursively using this resourcebroker by checking for a 
        // magic preference.
        if (context.getPreferences().containsKey("postKoala")) { 
            throw new GATObjectCreationException("Preventing recursive call " +
                    "into the KoalaResourceBroker");
        }
    }
    
	private static URI getBrokerURI(URI brokerURI) throws GATObjectCreationException { 
		if (brokerURI != null) { 
			return brokerURI;
		}
		
		// return fake uri!
		try {
			return new URI("koala://server.koala.org");
		} catch (URISyntaxException e) {
			throw new GATObjectCreationException("Failed to create fake Koala URI", e);
		}
	}
    
    public Job submitJob(AbstractJobDescription description, MetricListener listener,
            String metricName) throws GATInvocationException {
        
    	logger.info("Entering Koala.submitJob()");        
        //System.err.println("@@@ creating prestaged");

        //PreStagedFileSet pre =
        //    new PreStagedFileSet(gatContext, description,
        //            null, null, false);

//        System.err.println("@@@ creating poststaged");

  //      PostStagedFileSet post =
    //        new PostStagedFileSet(gatContext, description,
      //              null, null, false, false);

    	CoScheduleJobDescription tmp = null;
    	
        if (description instanceof CoScheduleJobDescription) { 
        	tmp = (CoScheduleJobDescription) description;
        } else if (description instanceof JobDescription) {              
        	// Wrap the single jobsdescription in a CoScheduled job description 
        	// to save us some trouble
        	tmp = new CoScheduleJobDescription((JobDescription) description);
        } else { 
        	throw new GATInvocationException("Unknown type of JobDescription: "
        			+ description.getClass().getName());
        }
        
        KoalaJob job = new KoalaJob(gatContext, tmp);
        
        logger.info("Submitting KoalaJob");        
        
        try {
            job.submitToScheduler();
        } catch (IOException e) {
            throw new GATInvocationException("Koala Adaptor failed!!");
        }
        
        return job;
    }  
}
