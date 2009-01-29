package nl.vu.ibis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
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

	// Added for JavaGAT 2.0
	public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
        capabilities.put("beginMultiJob", false);
        capabilities.put("endMultiJob", false);
        capabilities.put("findResources", false); // NOTE fuzzy semantics!
        capabilities.put("reserveResource", false);
        capabilities.put("submitJob", true);

        return capabilities;
    }
	
	protected static Logger logger = Logger.getLogger(KoalaResourceBrokerAdaptor.class);
	
	static { 
		
		// TODO: It seems that Koala is very 'single job per process' oriented, 
		// which may result in BIG issues in GAT.... 
		JobDescriptionLanguage jdl = new globusRSL(); // new KoalaJobDescriptionLanguage();
		IFileTransfer dm = new KoalaFileTransfer();
		RFModuleFactory.registerDM(dm);
		RFModuleFactory.registerJDL(jdl);		
	}
	
	public KoalaResourceBrokerAdaptor(GATContext context, 
    		URI brokerURI) throws GATObjectCreationException {

    	super(context, brokerURI);

        // Prevent recursively using this resourcebroker by checking for a 
        // magic preference.
        if (context.getPreferences().containsKey("postKoala")) { 
            throw new GATObjectCreationException("Preventing recursive call " +
                    "into the KoalaResourceBroker");
        }
    }
    
    
//    // NOTE: This is based on the globus resource broker. The main difference is
//    //       that koala needs some annotations in the RSL which indicate the 
//    //       file size. The sandbox support has also been removed, since we 
//    //       don't actually submit this RSL anyway.   
//    //
//    //       TODO: Support jobs consisting of multiple components
//    protected String createRSL(JobDescription description, String host,
//            Sandbox sandbox, PreStagedFileSet pre, PostStagedFileSet post)
//            throws GATInvocationException {
//        
//        SoftwareDescription sd = description.getSoftwareDescription();
//
//        if (sd == null) {
//            throw new GATInvocationException(
//                    "The job description does not contain a software description");
//        }
//
//        String rsl = "";
//        String args = "";
//
//        /* TODO: FIX so it uses the new JavaJobDescription!!!
//         
//        if (isJavaApplication(description)) {
//            URI javaHome = (URI) sd.getAttributes().get("java.home");
//            if (javaHome == null) {
//                throw new GATInvocationException("java.home not set");
//            }
//
//            rsl += "& (executable = " + javaHome.getPath() + "/bin/java)";
//
//            String javaFlags =
//                    getStringAttribute(description, "java.flags", "");
//            if (javaFlags.length() != 0) {
//                StringTokenizer t = new StringTokenizer(javaFlags);
//                while (t.hasMoreTokens()) {
//                    args += " \"" + t.nextToken() + "\"";
//                }
//            }
//
//            // classpath
//            String javaClassPath =
//                    getStringAttribute(description, "java.classpath", "");
//            if (javaClassPath.length() != 0) {
//                args += " \"-classpath\" \"" + javaClassPath + "\"";
//            } else {
//                // TODO if not set, use jar files in prestaged set
//            }
//
//            // set the environment
//            Map env = sd.getEnvironment();
//            if (env != null && !env.isEmpty()) {
//                Set s = env.keySet();
//                Object[] keys = (Object[]) s.toArray();
//
//                for (int i = 0; i < keys.length; i++) {
//                    String val = (String) env.get(keys[i]);
//                    args += " \"-D" + keys[i] + "=" + val + "\"";
//                }
//            }
//
//            // main class name
//            args += " \"" + getLocationURI(description).getSchemeSpecificPart()
//                            + "\"";
//        } else {
//            String exe = getLocationURI(description).getPath();
//            rsl += "& (executable = " + exe + ")";
//        }*/
//        
//        String exe = description.getSoftwareDescription().getExecutable();
//        rsl += "& (executable = " + exe + ")";
//    
//        // parse the arguments
//        String[] argsA = getArgumentsArray(description);
//
//        if (argsA != null) {
//            for (int i = 0; i < argsA.length; i++) {
//                args += (" \"" + argsA[i] + "\" ");
//            }
//        }
//        if (args.length() != 0) {
//            rsl += (" (arguments = " + args + ")");
//        }
//
//        rsl += " (count = " + getProcessCount(description) + ")";
//
//        rsl += " (hostCount = " + getHostCount(description) + ")";
//
//        String jobType = getStringAttribute(description, "jobType", null);
//        if (jobType != null) {
//            rsl += " (jobType = " + jobType + ")";
//        }
//
//        if (sandbox != null) {
//            rsl += " (directory = " + sandbox.getSandbox() + ")";
//        }
//
//        long maxTime = getLongAttribute(description, "maxTime", -1);
//        if (maxTime > 0) {
//            rsl += " (maxTime = " + maxTime + ")";
//        }
//
//        long maxWallTime = getLongAttribute(description, "maxWallTime", -1);
//        if (maxWallTime > 0) {
//            rsl += " (maxWallTime = " + maxWallTime + ")";
//        }
//
//        long maxCPUTime = getLongAttribute(description, "maxCPUTime", -1);
//        if (maxCPUTime > 0) {
//            rsl += " (maxCPUTime = " + maxCPUTime + ")";
//        }
//
//        // stage in files with gram
//        if (pre != null) {
//            for (int i = 0; i < pre.size(); i++) {
//                PreStagedFile f = pre.getFile(i);
//
//                if (!f.getResolvedSrc().toGATURI().refersToLocalHost()) {
//                    throw new GATInvocationException(
//                            "Currently, we cannot stage in remote files with gram");
//                }
//
//              /*  String s =
//                        "(file_stage_in = (file:///"
//                                + f.getResolvedSrc().getPath() + " "
//                                + f.getResolvedDest().getPath() + "))";
//                */
//                
//                // Add file size here ....
//                String s =
//                    "(file_stage_in = (file:///"
//                            + f.getResolvedSrc().getPath() + ":1))";
//                rsl += s;
//            }
//        }
//
//        /*
//        if (post != null) {
//            for (int i = 0; i < post.size(); i++) {
//                PostStagedFile f = post.getFile(i);
//
//                if (!f.getResolvedDest().toGATURI().refersToLocalHost()) {
//                    throw new GATInvocationException(
//                            "Currently, we cannot stage out remote files with gram");
//                }
//
//                String s =
//                        "(file_stage_out = (" + f.getResolvedSrc().getPath()
//                                + " gsiftp://" + GATEngine.getLocalHostName()
//                                + "/" + f.getResolvedDest().getPath() + "))";
//                rsl += s;
//            }
//        }*/
//        
//        org.gridlab.gat.io.File stdout = sd.getStdout();
//        if (stdout != null) {
//            if (sandbox != null) {
//                rsl +=
//                        (" (stdout = " + sandbox.getRelativeStdout().getPath() + ")");
//            }
//        }
//
//        org.gridlab.gat.io.File stderr = sd.getStderr();
//        if (stderr != null) {
//            if (sandbox != null) {
//                rsl +=
//                        (" (stderr = " + sandbox.getRelativeStderr().getPath() + ")");
//            }
//        }
//
//        org.gridlab.gat.io.File stdin = sd.getStdin();
//        if (stdin != null) {
//            if (sandbox != null) {
//                rsl +=
//                        (" (stdin = " + sandbox.getRelativeStdin().getPath() + ")");
//            }
//        }
//       
//        // TODO: FIX so it uses the new JavaJobDescription!!!
//    //    if (!isJavaApplication(description)) {
//            // set the environment
//            Map env = sd.getEnvironment();
//            if (env != null && !env.isEmpty()) {
//                Set s = env.keySet();
//                Object[] keys = (Object[]) s.toArray();
//                rsl += "(environment = ";
//
//                for (int i = 0; i < keys.length; i++) {
//                    String val = (String) env.get(keys[i]);
//                    rsl += "(" + keys[i] + " \"" + val + "\")";
//                }
//                rsl += ")";
//            }
//     //   }
//
//        String queue = getStringAttribute(description, "queue", null);
//        if (queue != null) {
//            rsl += " (queue = " + queue + ")";
//        }
//
//        if (GATEngine.VERBOSE) {
//            System.err.println("RSL: " + rsl);
//        }
//
//        return rsl;
//    }
    
    
   
    
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
