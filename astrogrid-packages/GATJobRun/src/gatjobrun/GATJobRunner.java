/*
 * GATJobRunner.java
 *
 * Created on April 6, 2006, 6:25 PM
 *
 */
package gatjobrun;

//~--- non-JDK imports --------------------------------------------------------

//import org.apache.log4j.BasicConfigurator;

/* Import log4j classes */
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/* Exceptions */
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATInvocationException;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.Job.JobState;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.security.PasswordSecurityContext;
import org.gridlab.gat.security.SecurityContext;

//~--- JDK imports ------------------------------------------------------------

//import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;

import javax.security.auth.callback.PasswordCallback;

//~--- classes ----------------------------------------------------------------

/**
 *
 * @author kba79
 */

public class GATJobRunner {
	private URI                 Executable;
    private String              ExecutionHost;
    private File                FileStderr;
    private org.gridlab.gat.io.File                FileStdin;
    private File                FileStdout;
    private String              Password;
    private BufferedReader      StdoutReader;
    private String              Username;
    private String              Jobname;
    private String              MailOptions;
    private String              MailUser;
    private ResourceBroker      broker;
    public boolean              Submit;
    
    //private ArrayList<String> execArguments;	// only Java 1.5
    private ArrayList           execArguments;

    private GATContext          globalContext;
    private Preferences         globalPrefs;
    private Properties          globalProps;
    private JobDescription      jd;

    //private ArrayList<File>   poststageFiles; // only Java 1.5
    private ArrayList           poststageFiles;
    private HashMap             diffPoststageList;

    //private ArrayList<File>   prestageFiles;  // only Java 1.5
    private ArrayList           prestageFiles;
    private HashMap             diffPrestageList;
    
    private SoftwareDescription sd;
    private Job                 theJob;

    // CONSTANTS
    private final String	DEFAULT_STDOUT = "GATJobRun_STDOUT.tmp";
    private final String	DEFAULT_STDERR = "GATJobRun_STDERR.tmp";

    //~--- constructors -------------------------------------------------------

    /** Creates a new instance of GATJobRunner */
    public GATJobRunner() {
    	
    	/* Configure log4j to avoid nasty console outputs */
    	
        BasicConfigurator.configure();  

        globalProps       = System.getProperties();
        globalContext     = new GATContext();
        globalPrefs       = new Preferences();
        FileStdout        = null;
        FileStdin         = null;
        FileStderr        = null;
        StdoutReader      = null;
        Username          = null;
        Jobname           = null;
        MailOptions       = null;
        MailUser          = null;
        prestageFiles     = new ArrayList(); // WARNING:
        poststageFiles    = new ArrayList(); // ArrayList has an initial capacity of 10
        execArguments     = new ArrayList(); // this may not fit in some cases...
        diffPrestageList  = new HashMap();
        diffPoststageList = new HashMap();
        ExecutionHost     = null;
        Executable        = null;
        sd                = null;
        jd                = null;
        broker            = null;
        Submit            = false;
        theJob            = null;
        
        // Turn on DEBUG & VERBOSE for GatEngine
        globalProps.setProperty("gat.verbose", "false");    
        }
    
    //~--- methods ------------------------------------------------------------

    public void setRBAdaptor(String adaptorname) {
        globalPrefs.put("ResourceBroker.adaptor.name", adaptorname);     	
        //    globalPrefs.put("File.adaptor.name",adaptorname );
		       		
        //    globalPrefs.put("File.adaptor.name","gridftp" );
                		
    }
    
    public void setRBJobmanager(String jobmanagername) {
        globalPrefs.put("ResourceBroker.jobmanager", jobmanagername);        
    }
    
    public void addExecutableArgument(String arg) {
        execArguments.add(arg);
    }
      
    /*
     *  This method adds a filename to the list of poststage files. Poststage files
     * are copied back from the execution host after the job has finished.
     */
    
    public void addPostStageFile(String src, String dest) {
    	
    	if (dest!=null) {
    		diffPoststageList.put(src, dest);
    	}
    	else {
            try {
                poststageFiles.add(GAT.createFile(globalContext, globalPrefs, new URI(src)));
            } catch (Exception e) {
                System.err.println("Error creating poststage file: " + src);
                System.exit(1);
            }   		
    	}
    }

    /*
     *  This method adds a filename to the list of prestage files with different
     *  names of the remote and local host. Prestage files
     * are copied to the execution host before the job starts
     */
    
    public void addPreStageFile(String src, String dest) {
    	
    	if (dest!=null) {
    		diffPrestageList.put(src, dest);
    		}
    	else {
            try {
                prestageFiles.add(GAT.createFile(globalContext, globalPrefs, new URI(src)));
            } catch (Exception e) {
                System.err.println("Error creating prestage file: " + src);
                e.printStackTrace();
                System.exit(1);
            }   		
    	}
    }

    /* This method builds the job description from resource and software desc. */
    private void createJobDescription() 
    {

        if (Submit)
            {
                globalPrefs.put("job.stop.on.exit","false");
               globalPrefs.put("AdvertService.adaptor.name","generic");
                globalContext.addPreferences(globalPrefs);
            }
        
        try {
            jd     = new JobDescription(sd);
            if (globalPrefs.get("adaptor.job.id") == "globus") {
                String JobManagerContact = new String("https://" + ExecutionHost);
                broker = GAT.createResourceBroker(globalContext, globalPrefs, new URI(JobManagerContact));
            }
            else
            {
//            	String JobManagerContact = new String("any://" + ExecutionHost);
            	String JobManagerContact = new String(ExecutionHost);
            	broker = GAT.createResourceBroker(globalContext, globalPrefs, new URI(JobManagerContact));
            }
            
          
        } catch (Exception e) {
            System.err.println("Error creating job description description.");
            System.exit(1);
        }
    }


    private void createSecurityContext() {
        try {
            SecurityContext pwd = new PasswordSecurityContext(Username,
                                                              Password);

            globalContext.addSecurityContext(pwd);
        } catch (Exception e) {}
    }

    /*
     *  This method creates a software description for the execution environment.
     * At least an executable must have been set - if not this method fails!
     */
    
    private void createSoftwareDescription() {
        if (Executable != null) {

            if (!Submit) {
            	if (FileStdout == null) {
            		setStdout(DEFAULT_STDOUT);
            		}
            	if (FileStderr == null) {
            		setStderr(DEFAULT_STDERR);
            		}
            	}
            
            sd = new SoftwareDescription();
//            sd.setLocation("any://" + ExecutionHost + "/" + Executable);
            sd.setExecutable(Executable.toString());
            sd.setStdin(FileStdin);
            sd.setStdout(FileStdout);
            sd.setStderr(FileStderr);

            /* Executable arguments */
            if (execArguments.size() > 0) {
                String[] args = new String[execArguments.size()];

                for (int i = 0; i < execArguments.size(); i++) {
                    args[i] = (java.lang.String)execArguments.get(i);
                    }
                sd.setArguments(args);
                }
            
            /* Prestage files */
            if (prestageFiles.size() > 0) {
            	File[] files = new File[prestageFiles.size()];
            	
            	for (int i = 0; i < prestageFiles.size(); i++) {
            		files[i] = (org.gridlab.gat.io.File)prestageFiles.get(i);
            		}
            	sd.setPreStaged(files);
            	}
            if ( diffPrestageList.size() >0 ) {
            	Iterator it = diffPrestageList.keySet().iterator();
            	while (it.hasNext()) {
            		String srcFile = (String) it.next();
            		String destFile = (String) diffPrestageList.get(srcFile);
            		try {
            			sd.addPreStagedFile(GAT.createFile(globalContext, new URI(srcFile)),
            					            GAT.createFile(globalContext, new URI(destFile)));
            			}  catch (Exception e) {
            				System.out.println("Could not add prestage file: " + srcFile + " -> " + destFile);
            				e.printStackTrace();
            				}
            			}  
            	}

            /**
             * Posstage files
             */
            
            if (poststageFiles.size() > 0) {
            	File[] files = new File[poststageFiles.size()];
            	for (int i = 0; i < poststageFiles.size(); i++) {
            		files[i] = (org.gridlab.gat.io.File)poststageFiles.get(i);
            		}
            	sd.setPostStaged(files);
            	}
            else if (diffPoststageList.size() > 0) {
            	Iterator it = diffPoststageList.keySet().iterator();
            	while (it.hasNext()) {
            		String srcFile = (String) it.next();
            		String destFile = (String) diffPoststageList.get(srcFile);
            		try {
            			sd.addPostStagedFile(GAT.createFile(globalContext, new URI(srcFile)),
            				                 GAT.createFile(globalContext, new URI(destFile)));
            		} catch (Exception e) {
            			System.out.println("Could not add poststage file: " + srcFile + " -> " + destFile);
            			e.printStackTrace();
            			}
            		}
            	} 
             }
        else  {
        	System.err.println("Error creating SoftwareDescription: no executable specified.");
        	GAT.end();
        	System.exit(1);        	
        }
    }

    /* This method tries to execute the job */
        
    public int runJob() 
    {
        createSecurityContext();
        createSoftwareDescription();
        createJobDescription();

        try {
            theJob = broker.submitJob(jd);
        } catch (Exception e) {
            System.err.println("Error submiting job: " + e);

            e.printStackTrace();
            GAT.end();
            return(-1);
        }
    
        String J_String = theJob.toString();
        //        System.out.println("string representation of Job: '" + J_String + "'");
        
        /**
         * If called with SubmitOnly=true, write the job description to the 
         * advert service. A later job can call this advertservice to get the 
         * connection to the job again. The name of the advert service is 
         * triggered via the job ID.
         */
        
        if (Submit)
            {

                try
                    {
                        while (1>0)
                            {
                                String JobID = theJob.getInfo().get("adaptor.job.id").toString();
                                if (JobID == null)
                                    {
                                        Thread.sleep(200);
                                    }
                                else
                                    {
                                        /**
                                           here's the part with the AdvertService
                                        */
                                        
                                        AdvertService AdvService = GAT.createAdvertService(globalContext);
                                        MetaData mData = new MetaData();
                                        
                                        mData.put("name",JobID);
                                        AdvService.add(theJob,mData,"/tmp/" + JobID);
                                        AdvService.exportDataBase(new URI("file:////tmp/gat-ssh"));

                                        System.out.println(JobID);
                                        break;
                                    }
                            }

                        
                        GAT.end();
                        return(0);
                    }
                catch (Exception e)
                    {
                        System.err.println("getJobID failed: " + e);
                        e.printStackTrace();
                        GAT.end();
                        return(-1);
                    }
            }
	else
            {
                //                System.out.println("Submit was set to false");
            }
        
         while (true) 
            {
                try 
                    {
                	   
            	        System.out.println("before getting the job info");
                        Map info = theJob.getInfo();
                        System.out.println("after getting the job info");
                        System.out.println("Job info" + info);
                    
                        /*                System.out.print("job info: ");
                                          System.out.println(info); */
                        
                        //                String state = (String) info.get("state");
//                        System.out.println("before getting the job state");
//                        int istate =  theJob.getState();
//                        System.out.println("before getting the job state");
//                        System.out.println("job status: " + istate);
                        
                        //                if (state.equals("STOPPED")) {
                        if ( (theJob.getState() == JobState.STOPPED) ) 
                            {
                                System.out.println();
                                System.out.println("JOB SUMMARY");
                                System.out.println("================================================");
                                System.out.println("Command:    "+sd.getExecutable());
                                System.out.println("Job ID:     "+info.get("adaptor.job.id"));
                                System.out.println("Exit value: "+theJob.getExitStatus());
                                System.out.println("================================================");
                                System.out.println();
                                
                                
                                if (FileStdout.toGATURI().toString().equals( DEFAULT_STDOUT )) 
                                    {	
                                        BufferedReader reader = this.getBufferedStdoutStream();
                                        String         line;
                                        
                                        System.out.println("STDOUT");
                                        System.out.println("================================================");
                                        
                                        try 
                                            {
                                                while ((line = reader.readLine()) != null) 
                                                    {
                                                        System.out.println(line);
                                                    }
                                                reader.close();
                                                FileStdout.deleteOnExit();
                                                FileStderr.deleteOnExit();
                                            } 
                                        catch (IOException e) 
                                            {
                                                System.out.println("error reading GATs default stdout");
                                                e.printStackTrace();
                                            }
                                        catch (NullPointerException e)
                                            {
                                                System.out.println("error reading GATs default stdout. Value of Reader: " + reader);
                                                e.printStackTrace();
                                            }
                                        System.out.println("================================================");
                                        System.out.println();
                                    }
                                
                                GAT.end();
                                return(0);
                                //                 } else if (state.equals("SUBMISSION_ERROR")) {
                                //                     System.err.println("SUBMISSION_ERROR");
                                //                     System.exit(1);
                                //                 }
                            } 
                        else if (theJob.getState() == JobState.SUBMISSION_ERROR) 
                            {
                                System.err.println("SUBMISSION_ERROR");
                                GAT.end();
                                return(1);
                            }
                    
                        Thread.sleep(1000);
                    }
                catch (Exception e) 
                    {
                        
                        System.err.println("getInfo failed: " + e);
                        e.printStackTrace();
                        break;
                    }
            }
        
        //GAT.end();
        return 0;
    }

    //~--- get methods --------------------------------------------------------

    public BufferedReader getBufferedStdoutStream() {
    	File bb;
        try {
            FileInputStream in = GAT.createFileInputStream(globalContext,
                                                           FileStdout.toGATURI());
            java.io.InputStreamReader reader =
                new java.io.InputStreamReader(in);

            StdoutReader = new BufferedReader(reader);
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return StdoutReader;
    }

    //~--- set methods --------------------------------------------------------

    public void SetJobName(String name)
    {
        Jobname = name;
    }

    public void SetMailOptions(String opts)
    {
        MailOptions = opts; 
    }

    public void SetMailUser(String name)
    {
        MailUser = name; 
    }

    public void setExecutable(String exe) {
        try {
            Executable = new URI(exe);
        } catch (Exception e) {
            System.err.println("Error setting executable file to: " + exe);
            System.exit(1);
        }
    }

    //  public void setExecutableArguments(ArrayList<String> args) { // only Java 1.5
    public void setExecutableArguments(ArrayList args) {
        execArguments = args;
    }

    /* This method sets the execution host - if not set, it defaults to "localhost" */
    public void setExecutionHost(String host) {
        ExecutionHost = host;
    }

    public void setSecurityContext(String username, String password) {
        Username = username;
        Password = password;
    }

    /* This method sets the job's stderr file - you can use GAT's URI syntax */
    public void setStderr(String stderr) {
        try {
            FileStderr = GAT.createFile(globalContext, globalPrefs,
                                        new URI(stderr));
        } catch (Exception e) {
            System.err.println("Error creating stderr file: " + stderr);
            System.exit(1);
        }
    }

    /* This method sets the job's stdin file - you can use GAT's URI syntax.*/
    public void setStdin(String stdin) {
        try {
            FileStdin = GAT.createFile(globalContext, globalPrefs,
                                       new URI(stdin));
        } catch (Exception e) {
            System.err.println("Error creating stdin file: " + stdin);
            System.exit(1);
        }
    }

    /* This method sets the job's stdout file - you can use GAT's URI syntax */
    public void setStdout(String stdout) {
        try {
            FileStdout = GAT.createFile(globalContext, globalPrefs,
                                        new URI(stdout));
        }
        catch (Exception e) {
            System.err.println("Error creating stdout file: " + stdout);
            System.exit(1);
        }
    }
}


;
