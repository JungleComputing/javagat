/*
 * UnicoreResourceBrokerAdaptor.java
 *
 * Created on July 8, 2008
 *
 */

package org.gridlab.gat.resources.cpi.unicore;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.XMLWriter;
import org.gridlab.gat.AdaptorNotApplicableException;
import org.gridlab.gat.GATContext;
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
import org.gridlab.gat.resources.cpi.ResourceBrokerCpi;
import org.gridlab.gat.resources.cpi.Sandbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fzj.hila.HiLAFactory;
import de.fzj.hila.HiLAFactoryException;
import de.fzj.hila.Site;
import de.fzj.hila.Storage;
import de.fzj.hila.Task;
import de.fzj.hila.common.jsdl.JSDL;
import de.fzj.hila.exceptions.HiLAException;
import de.fzj.hila.exceptions.HiLALocationSyntaxException;



/**
 * 
 * @author Alexander Beck-Ratzka, AEI.
 * 
 */

public class UnicoreResourceBrokerAdaptor extends ResourceBrokerCpi {
	
    protected static Logger logger = LoggerFactory.getLogger(UnicoreResourceBrokerAdaptor.class);
	
    public static Map<String, Boolean> getSupportedCapabilities() {
        Map<String, Boolean> capabilities = ResourceBrokerCpi
                .getSupportedCapabilities();
        capabilities.put("submitJob", true);

        return capabilities;
    }

    public UnicoreResourceBrokerAdaptor(GATContext gatContext, URI brokerURI) throws GATObjectCreationException, AdaptorNotApplicableException {
    	
    	super(gatContext, brokerURI);

    	if (!brokerURI.isCompatible("unicore6")) {
    		throw new AdaptorNotApplicableException("cannot handle this URI: " + brokerURI);
    		}
    	if (!brokerURI.refersToLocalHost()) {
    		throw new AdaptorNotApplicableException("cannot handle this URI: " + brokerURI);
    		}
    	
    	
    }
    
    public static void init() {
        GATEngine.registerUnmarshaller(UnicoreJob.class);
    }
    

//    public synchronized JSDL createUnicoreJsdl (SoftwareDescription sd) throws GATInvocationException  {
    public synchronized JSDL createUnicoreJsdl (SoftwareDescription sd) throws GATInvocationException  {
   	
    	/**
    	 * necessary name space definitions
    	 */
    	
    	// String nameSpaceUri  = "http://schemas.ogf.org/jsdl/2005/11/jsdl";
     	String nameSpaceJsdl="jsdl";
    	String nameSpaceJsdlPosix="jsdl-posix";
    	String nameSpaceJsdlHead = nameSpaceJsdl +":";
    	String nameSpaceJsdlPosixHead = nameSpaceJsdlPosix +":";
    	
		String rootElement   = "JobDefinition";
   	
    	DocumentFactory docJsdl = new DocumentFactory();
		Namespace jsdlNs = new Namespace(nameSpaceJsdl, "http://schemas.ggf.org/jsdl/2005/11/jsdl");
		Namespace jsdlNsPosix = new Namespace(nameSpaceJsdlPosix, "http://schemas.ggf.org/jsdl/2005/11/jsdl-posix");
		
		Element Root = docJsdl.createElement(nameSpaceJsdlHead + rootElement);
		Root.add(jsdlNs);
		Root.add(jsdlNsPosix);
		Document jsdlDoc = DocumentHelper.createDocument(Root);
		
		/**
		 * starting with the job description...
		 */

		Element jDescr = Root.addElement(nameSpaceJsdlHead + "JobDescription");
		
		Element jIdent = jDescr.addElement(nameSpaceJsdlHead + "JobIdentification");
		jIdent.addElement(nameSpaceJsdlHead + "JobName").addText("Unicore-GAT" + sd.getExecutable()); // FIXME: Change, whenever SofwareDescription offers JobName.
		
		/**
		 * The executable and its arguments...
		 */
		
		Element jAppl = jDescr.addElement(nameSpaceJsdlHead + "Application");
		Element posixAppl = jAppl.addElement(nameSpaceJsdlPosixHead + "POSIXApplication");
		
/*		posixAppl.addElement(nameSpaceJsdlPosixHead + "Executable").addText("/bin/ls");
		posixAppl.addElement(nameSpaceJsdlPosixHead + "Argument").addText("-la");*/
		
		/**
		 * only put ./ before the executable, if the name its name doesn't contain any path delimiter, 
		 * so that is only to be found in the local directory..
		 */
		
		String execFile = new String (sd.getExecutable());
		if (execFile.indexOf("/") == -1 ) {
			posixAppl.addElement(nameSpaceJsdlPosixHead + "Executable").addText("./" + execFile);
		} else {
			posixAppl.addElement(nameSpaceJsdlPosixHead + "Executable").addText(sd.getExecutable());			
		}
		
		String [] jobArgs = sd.getArguments();
		
		if (jobArgs!=null) {
			for (int ii=0; ii < jobArgs.length; ii++) {
				posixAppl.addElement(nameSpaceJsdlPosixHead + "Argument").addText(jobArgs[ii]);
			}
		}
		
		/** 
		 * futher stuff as USPACE...
		 */
		
		Element jResources = jDescr.addElement(nameSpaceJsdlHead + "Resources");
		jResources.addElement(nameSpaceJsdlHead + "FileSystem").addText("name=\"USPACE\"").addElement(nameSpaceJsdlHead + "Description").addText("Unicore working dir");
		
		
		/**
		 * try it via stream...
		 */
		
//       OutputFormat fout = OutputFormat.createPrettyPrint();

		XMLWriter writer;
		
		ByteArrayOutputStream XMLout = new ByteArrayOutputStream ();
		
		
		try {
			writer = new XMLWriter(XMLout);
			writer.write(jsdlDoc);
			ByteArrayInputStream XMLReader = new ByteArrayInputStream(XMLout.toByteArray());
			JSDL jsdl = new JSDL(XMLReader);
			logger.debug("JSDL-List" + jsdl);
			return jsdl;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new GATInvocationException("Unicore JSDL handling " + e1);
		} catch (IOException e) {
			e.printStackTrace();
			throw new GATInvocationException("Unicore JSDL handling " + e);

		} catch (XmlException ee) {
			ee.printStackTrace();
			throw new GATInvocationException("Unicore JSDL handling " + ee);
		}

		
/**		String strJSDL = docJsdl.toString();
		System.out.println("JSDL-String: " + docJsdl);
		InputStream is = new ByteArrayInputStream(docJsdl.toString().getBytes());
	     try
	     {
	    	 JobDefinitionDocument finalJsdlDoc = JobDefinitionDocument.Factory.parse(is);
	    	 return(finalJsdlDoc);
	     } catch (XmlException Xe) {
	    	 Xe.printStackTrace();
	    	 throw new GATInvocationException("Unicore JSDL handling " + Xe);
	     } catch (IOException e) {
	    	 e.printStackTrace();
	    	 throw new GATInvocationException("Unicore JSDL handling " + e);
	    }*/
		/**
		 * That's it. Save the whole stuff to file and open it again with HiLA JSDL...
		 */
		
		
/**		OutputFormat fout = OutputFormat.createPrettyPrint();
		XMLWriter writer;
		String homeDir = System.getProperty("user.home");
		try {
			writer = new XMLWriter(new FileWriter(homeDir + "/.hila/job.xml"), fout);
			writer.write(jsdlDoc);
			writer.close();
			
			JSDL jsdl = new JSDL(new java.io.File(homeDir + "/.hila/job.xml"));
			java.io.File fJsdl = new java.io.File(homeDir + "/.hila/job.xml");
			fJsdl.delete();
			return jsdl;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GATInvocationException("Unicore JSDL handling " + e);
		} catch (XmlException Xe) {
			Xe.printStackTrace();
			throw new GATInvocationException("Unicore JSDL handling " + Xe);
		}**/
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
        // ResourceDescription testDescr=description.getResourceDescription();
        String host = "unicore6:" + brokerURI.getPath();
//        String host = testDescr.getDescription().get("execHost").toString();
        System.out.println("found as exec host in resource description: " + host);

        logger.debug("Unicore adaptor will use '" + host +"' as execution host");
//        String host = getHostname();
        
        

        Sandbox sandbox = null;

        /* Handle pre-/poststaging */
 /*       if (host != null) {
            sandbox = new Sandbox(gatContext, description, host,
                    null, false, true, true, true);
        }*/
        
        UnicoreJob job = new UnicoreJob(gatContext, description, sandbox);
        if (listener != null && metricDefinitionName != null) {
            Metric metric = job.getMetricDefinitionByName(metricDefinitionName)
                    .createMetric(null);
            job.addMetricListener(listener, metric);
        }

		try {
/*			de.fzj.hila.Location loc = new de.fzj.hila.Location(host);
			
			Site testSite = (Site) HiLAFactory.getInstance().locate(loc);
			job.site = (Site) HiLAFactory.getInstance().locate(loc);*/
			job.site  = (Site) HiLAFactory.getInstance().locate(new de.fzj.hila.Location(host));
			if (job.site == null) {
				throw new GATInvocationException("UnicoreResourceBrokerAdaptor job.site = null");
			}
			job.jsdl  =  this.createUnicoreJsdl(sd);
			Task task = job.site.submit(job.jsdl);
			
			/**
			 * add prestage datasets and prestage them...
			 */
			
			Storage st = (Storage) task.getLocatableChild("wd");
			prestageFiles(sd,task,st);
			
			
            task.startASync(); // FIXME: startsync richtig machen...
            job.setState(Job.JobState.SCHEDULED);
            job.setTask(task);
            job.setSoft(sd);
            job.setJobID(task.getID());
            job.startListener();

			
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
		return job;
    }
    
    private void prestageFiles(SoftwareDescription sd,Task task, Storage st) throws GATInvocationException {
    	
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
	                
	            try {
	            	de.fzj.hila.File remFile = st.asFile(destFile.getName());
	            	remFile.importLocalFile(locFile, true).block();
	            	if (locFile.canExecute()) {
	            		try {
	            			remFile.chmod(true, false, true);
	            			} catch (HiLAException e) {
	            				e.printStackTrace();
	            				logger.warn("chmod failure of" + remFile.getName() + "might cause that the program can't be executed");
	            				}
	            			}
	            } catch (HiLAException e) {
	            	e.printStackTrace();
	    			throw new GATInvocationException("UNCICORE Adaptor: creating remfile for prestaging failed");
	    			}
	            }
    		}
    	}
}
    
