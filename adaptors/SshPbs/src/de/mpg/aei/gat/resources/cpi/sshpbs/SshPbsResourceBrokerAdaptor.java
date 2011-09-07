/*
 * SshPbsBrokerAdaptor.java
 *
 * Created on July 8, 2008
 *
 */

package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.StringBuffer;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.URI;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.io.File;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.MetricListener;
import org.gridlab.gat.resources.AbstractJobDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.mpg.aei.gat.resources.cpi.sshpbs.SshPbsSecurityUtils;


/**
 * 
 * @author Alexander Beck-Ratzka, AEI.
 * 
 */

public class SshPbsResourceBrokerAdaptor extends ResourceBrokerCpi {
	
    protected static Logger logger = LoggerFactory.getLogger(SshPbsResourceBrokerAdaptor.class);
	
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("submitJob", true);

        return capabilities;
    }
     
    private static final String PREFIX = "#PBS";
    
    private static final String SSH_PORT_STRING = "sshpbs.ssh.port";    

    public static final int SSH_PORT = 22;

    public static String[] getSupportedSchemes() {
        return new String[] { "sshpbs", "ssh"};
    }

    private final int ssh_port;
    
    private Map<String, String> securityInfo;
    
    public SshPbsResourceBrokerAdaptor(GATContext gatContext, URI brokerURI) throws GATObjectCreationException, AdaptorNotApplicableException {
    	
    	super(gatContext, brokerURI);

    	if (brokerURI.getScheme() == null) {
    		throw new AdaptorNotApplicableException("cannot handle this URI: " + brokerURI);
    		}
 /*   	if (!brokerURI.refersToLocalHost()) {
    		throw new AdaptorNotApplicableException("cannot handle this URI: " + brokerURI);
    		}*/
    	
        /* allow ssh port override */
    	
        if (brokerURI.getPort() != -1) {
            ssh_port = brokerURI.getPort();
        } else {
            String port = (String) gatContext.getPreferences().get(SSH_PORT_STRING);
            if (port != null) {
                ssh_port = Integer.parseInt(port);
            } else {
                ssh_port = SSH_PORT;
            }
        }
        
        try {
            securityInfo = SshPbsSecurityUtils.getSshCredential(
                    gatContext, "sshpbs", brokerURI, ssh_port);
        } catch (Throwable e) {
            logger
                    .info("SshPbsResourceBrokerAdaptor: failed to retrieve credentials"
                            + e);
            securityInfo = null;
        }

        if (securityInfo == null) {
            throw new GATObjectCreationException(
                    "Unable to retrieve user info for authentication");
        }

        if (securityInfo.containsKey("privatekeyfile")) {
            if (logger.isDebugEnabled()) {
                logger.debug("key file argument not supported yet");
            }
        }
    	
    }
    
    public URI getBrokerURI() {
    	return brokerURI;
    }
    
    public  String pbsHost() {
    	return getHostname();
    }
    public static void init() {
        GATEngine.registerUnmarshaller(SshPbsJob.class);
    }
    

    public Job submitJob(AbstractJobDescription abstractDescription, MetricListener listener,
            String metricDefinitionName) throws GATInvocationException {

        if (!(abstractDescription instanceof JobDescription)) {
            throw new GATInvocationException(
                    "can only handle JobDescriptions: "
                            + abstractDescription.getClass());
        }

        JobDescription description = (JobDescription) abstractDescription;

        SoftwareDescription sd = description.getSoftwareDescription();
        ResourceDescription rd=description.getResourceDescription();

        /**
           it still requires a check, whether  the protocol type any iy appropioate here.
        */

//        String host = "any:" + brokerURI.getPath();
        
        /**
         * the code (host and authority) below is an extract of the CommandLinesshAdaptor, still
         * requires testing!
         */
        
        String path = getExecutable(description);
        String authority = getAuthority();
        if (authority == null) {
            authority = "localhost";
        }

        String host = getHostname();
        if (host == null) {
            host = "localhost";
        }

        System.out.println("found as exec host in resource description: " + host);

        logger.debug("SshPbs adaptor will use '" + host +"' as execution host");
//        String host = getHostname();
        
        

//        Sandbox sandbox = null;
        Sandbox sandbox = new Sandbox(gatContext, description, authority, null,
                true, false, false, false);

        /* Handle pre-/poststaging */
 /*       if (host != null) {
            sandbox = new Sandbox(gatContext, description, host,
                    null, false, true, true, true);
        }*/
        
        SshPbsJob job = new SshPbsJob(gatContext, this, description, sandbox);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                .createMetric(null);
            job.addMetricListener(listener, metric);
        }

	/**
	 * prestage done by sandbox
	 */

 //       sandbox.prestage();
 

        
//        prestageFiles(sd);

        String jobid=PBS_Submit(job, description, sd, rd, sandbox);
        
        if (jobid!=null) {
            job.setState(Job.JobState.SCHEDULED);
            job.setSoft(sd);
            job.setJobID(jobid);
            job.startListener();
        } else {
        	logger.error("no qsub script created); could not submit sshPbs job");
        }
			
        return job;
    }
    
    private void prestageFiles(SoftwareDescription sd) throws GATInvocationException {
    	
    	java.io.File locFile;
//    	de.fzj.hila.File remFile;
    	Map<org.gridlab.gat.io.File, org.gridlab.gat.io.File> preStaged=null;
    	/**
    	 * 	    	 * fill in with elements of software description. Right now this concerns pre and poststaging dataset.
    	*/
    	preStaged=sd.getPreStaged();
    	if (preStaged!=null) {
    	    for (java.io.File srcFile : preStaged.keySet()) {
                File destFile = preStaged.get(srcFile);
    			
                String srcName = srcFile.getPath();
	                
                if (destFile == null) {
                    logger.debug("ignoring prestaged file, no destination set!");
                    continue;
                }
                locFile = new java.io.File(srcName);
                logger.debug("Prestage: Name of destfile: '" + destFile.getName() +"'");
	                
                /*
                  here an appropiate scp command must be entered...
                */
            }
        }
    }
    
    /**
     * the method submit is for the submission of sshPbs jobs.
     * It submit qsub via ssh on the remote host, and retrieves the job ID.
     * 
     * @param  Map <String, String> containing the security info
     * @param SoftwareDescription containing the software description
     * @param ResoureceDescription containg the resource description
     * @author Alexander Beck-Ratzka, AEI, July 2010.
     */
    
    private String PBS_Submit(SshPbsJob PbsJob, JobDescription description, SoftwareDescription sd,  ResourceDescription rd, Sandbox sandbox) throws GATInvocationException {
    
    	
    	/**
    	 * first of all: create the qsub file
    	 */
    	
    	String jobid=null;
    	
        
        /**
         * Create the qsub script...
         */
       
        try {
        	String qsubFileName = createQsubScript(sd, rd, sandbox);
        	if (qsubFileName!=null) {
        		jobid = sshPbsSubmission(PbsJob, description, qsubFileName, sandbox);
        	} else {
        		return(null);
        	}
        	
        } catch (GATInvocationException e) {
        	logger.error("creation of qsub script file failed");
        	e.printStackTrace();
        }
        
        return(jobid);
    }
    
    @SuppressWarnings({ "finally", "finally" })
	String createQsubScript(SoftwareDescription sd,  ResourceDescription rd, Sandbox sandbox)
                                  throws GATInvocationException {
    	
    	String Queue = null;
    	String Time  = null;
    	String Filesize = null;
    	String Memsize = null;
    	String Nodes = null;
    	String LString = null;
    	String HwArg = null;
    	java.io.File temp;
    	scriptWriter job = null;
    	HashMap<String, Object> rd_HashMap = null;
    	
    	rd_HashMap = (HashMap<String, Object>) rd.getDescription();
    	
		try {
			temp = java.io.File.createTempFile("pbs", null);
			try {
				job = new scriptWriter(new BufferedWriter(new FileWriter(temp)), PREFIX);
				job.println("#!/bin/sh");
				job.println("# qsub script automatically generated by GAT SshPbs adaptor");			
				
				/**
				 * 			 * First of all the hardware ressources...
				*/
				Queue = (String) rd_HashMap.get("machine.queue");
				if (Queue == null) {
					Queue = new String("");
					} else {
					job.addString("q", Queue);
					}
				
				Time = (String) rd_HashMap.get("cpu.walltime");
				if (Time == null) {
					Time = new String("00:01:00");
					}
				
				Filesize = (String) rd_HashMap.get("file.size");
				if (Filesize == null) {
					Filesize = new String("");
					}
				
				Memsize = (String) rd_HashMap.get("memory.size");
				if (Memsize == null) {
					Memsize = new String("");
					}
				
				Nodes = (String) rd_HashMap.get("cpu.count");
				if (Nodes == null) {
					Nodes = new String("1");
					}
				
				if (Queue.length() == 0) {
					LString = new String("walltime=" + Time + ",file="
							+ Filesize + ",mem=" + Memsize + ",nodes="
							+ Nodes);
					} else {
						LString = new String("walltime=" + Time + ",file="
								+ Filesize + ",mem=" + Memsize + ",nodes="
								+ Nodes + ":" + Queue);
						}
				if (LString == null) {
					throw new GATInvocationException(
							"Cannot construct -l option; job submit to PBS failed.");
					} else {
						job.addString("l", LString);
						}
				
				HwArg = (String) rd_HashMap.get("Jobname");
				if (HwArg == null) {
					HwArg = System.getProperty("user.name");
					}
				
				if (HwArg != null)
					job.addString("N", HwArg);
				
				/** System.getProperty can deliver a null... */
				
				HwArg = null;
				
				HwArg = (String) rd_HashMap.get("YankEO");
				if (HwArg != null) {
					job.addString("j", HwArg);
					HwArg = null;
					}
				
				/**
				 * the values out of the software description...
				 */
				
				if (sd.getStdout() != null) {
					job.addString("o", sd.getStdout().getName().toString());
				}
				
				if (sd.getStderr() != null) {
					job.addString("e", sd.getStderr().getName().toString());
				}
				
				/**
				 * the arguments for the executable...
				 */
				
				StringBuffer cmd = new StringBuffer();
				
				/**
				 * mapping the rc of the executable...
				 */
				
				cmd.append("$HOME/" + sandbox.getSandboxPath() + "/" + sd.getExecutable().toString());
				if (sd.getArguments() != null) {
					String[] args = sd.getArguments();
					for (int i=0; i<args.length; ++i) {
						cmd.append(" ");
						cmd.append(args[i]);
					}
				}
				job.println(cmd.toString());
				
				/**
				 * RETVALUE and PBS_JOBID not know for Torque, use PBS_O_JOBID and $? instead...
				 
				job.println("  echo \"retvalue = $RETVALUE\" > $HOME.rc.$PBS_JOBID");
				*/

				job.println("  echo \"retvalue = $?\" > $HOME/.rc.$PBS_JOBID");
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new GATInvocationException("Cannot create temporary qsub file" + temp.getAbsolutePath());
				} 
			finally {
				if (job != null) job.close();
				return(temp.getAbsolutePath().toString());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return(null);
				}
    }
    /**
     * String sshPbsSubmission
     * 
     * Submit a PBS job via ssh on a PBS host, and transfers the
     * qsub file to the PBS host in front of the job submission.
     * 
     * @param qsubFileName
     * @return String jobID; the job ID of the PBS job on the remote host
     */
    
    String sshPbsSubmission(SshPbsJob PbsJob, JobDescription description, String qsubFileName, Sandbox sandbox) throws GATInvocationException {
    	
    	String JobID = null;
    	
        SoftwareDescription sd = description.getSoftwareDescription();
        ResourceDescription rd=description.getResourceDescription();
        
    	String username = securityInfo.get("username");
//        String password = securityInfo.get("password");
        
        int privateKeySlot = -1;
        try {
            String v = securityInfo.get("privatekeyslot");
            if (v != null) {
                privateKeySlot = Integer.parseInt(v);
            }
        } catch (NumberFormatException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("unable to parse private key slot: " + e);
            }
        }
 
        String authority = getAuthority();
        if (authority == null) {
            authority = "localhost";
        }
        
        String host = getHostname();
        if (host == null) {
            host = "localhost";
        }
        
        ArrayList<String> command = new ArrayList<String>();
        ArrayList<String> scpCommand = new ArrayList<String>();

        /**
         * add the qsubfile to the prestage datasets...
         */
        
        try {
        	File qsubFile = GAT.createFile(gatContext,qsubFileName);
//			sd.addPreStagedFile(qsubFile);
	        
	        /**
	         * prestaging by sandbox, the qsubFile will be prestaged separately by scp.
	         */
	        
	        sandbox.prestage();
	        
	        scpCommand.add("/usr/bin/scp");
	        if (ssh_port != SSH_PORT) {
                scpCommand.add("-p");
                scpCommand.add("" + ssh_port);
	        }
//	        scpCommand.add("-o");
//	        scpCommand.add("BatchMode=yes");
//	        scpCommand.add("-t");
//	        scpCommand.add("-t");
	        scpCommand.add(qsubFileName);
	        if (sandbox.getSandboxPath() != null) {  
	        	scpCommand.add(username + "@" + host + ":~/" + sandbox.getSandboxPath().toString() + "/" + qsubFile.getName().toString());
	        } else {
	        	scpCommand.add(username + "@" + host + ":~/" + qsubFile.getName().toString());
            }
	        
	        String ScpRes[] = PbsJob.singleResult(scpCommand);
//	        logger.debug("result string of scp: ' " + ScpRes[0] + "'");
	        
	        /**
	         * and create the ssh command for qsub...
	         */
	        
	        command.add("/usr/bin/ssh");
	        if (ssh_port != SSH_PORT) {
                command.add("-p");
                command.add("" + ssh_port);
	        }
            command.add("-o");
            command.add("BatchMode=yes");
            command.add("-t");
            command.add("-t");
            command.add(username + "@" + host);
            if (sandbox.getSandboxPath() != null) {
                command.add("cd");
                command.add(sandbox.getSandboxPath());
                command.add("&&");
            }
            command.add("qsub");
            command.add(qsubFile.getName());
            
            String[] PbsJobID = PbsJob.singleResult(command);
            
            return(PbsJobID[0]);
	        
		} catch (GATObjectCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return(null);
		} catch (IOException e) {
			e.printStackTrace();
			return(null);
		}   
    }
}
