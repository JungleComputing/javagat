
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
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.URI;
import org.gridlab.gat.io.File;
import org.gridlab.gat.io.FileInputStream;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
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

    //private ArrayList<File>   prestageFiles;  // only Java 1.5
    private ArrayList           prestageFiles;
    
    private ResourceDescription rd;
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

        globalProps    = System.getProperties();
        globalContext  = new GATContext();
        globalPrefs    = new Preferences();
        FileStdout     = null;
        FileStdin      = null;
        FileStderr     = null;
        StdoutReader   = null;
        prestageFiles  = new ArrayList(); // WARNING:
        poststageFiles = new ArrayList(); // ArrayList has an initial capacity of 10
        execArguments  = new ArrayList(); // this may not fit in some cases...
        ExecutionHost  = null;
        Executable     = null;
        sd             = null;
        rd             = null;
        jd             = null;
        broker         = null;
        Submit         = false;
        theJob         = null;
        
        // Turn on DEBUG & VERBOSE for GatEngine
        globalProps.setProperty("gat.verbose", "false");        
    }

    //~--- methods ------------------------------------------------------------

    public void setRBAdaptor(String adaptorname) {
        globalPrefs.put("ResourceBroker.adaptor.name", adaptorname);
                		
    }
    
    public void setRBJobmanager(String jobmanagername) {
        globalPrefs.put("ResourceBroker.jobmanager", jobmanagername);        
    }
    
    public void addExecutableArgument(String arg) {
        execArguments.add(arg);
    }

    /*
     *  This method adds a filename to the list of poststage file. Poststage files
     * are copied back from the execution host after the job has finished.
     */
    public void addPostStageFile(String file) {
        try {
            poststageFiles.add(GAT.createFile(globalContext, globalPrefs,
                                              new URI(file)));
        } catch (Exception e) {
            System.err.println("Error creating poststage file: " + file);
            System.exit(1);
        }
    }

    /*
     *  This method adds a filename to the list of prestage files. Prestage files
     * are copied to the execution host before the job starts
     */
    public void addPreStageFile(String file) {
        try {
            prestageFiles.add(GAT.createFile(globalContext, globalPrefs,
                                             new URI(file)));
        } catch (Exception e) {
            System.err.println("Error creating prestage file: " + file);
            System.exit(1);
        }
    }

    /* This method builds the job description from resource and software desc. */
    private void createJobDescription() 
    {

        if (Submit)
            {
                globalPrefs.put("killJobsOnExit","false");
            }
        
        try {
            jd     = new JobDescription(sd, rd);
            broker = GAT.createResourceBroker(globalContext, globalPrefs);
        } catch (Exception e) {
            System.err.println("Error creating job description description.");
            System.exit(1);
        }
    }

    /*
     *  This method creates a resource description - it fails if an execution
     * host has not been set
     */
    private void createResourceDescription() {
        if (ExecutionHost != null) {
            //Hashtable<String, String> hardwareAttributes =    // only Java 1.5
            //new Hashtable<String, String>();                  // only Java 1.5
            Hashtable hardwareAttributes = new Hashtable();

            hardwareAttributes.put("machine.node", ExecutionHost);

            // hardwareAttributes.put("memory.size","64");
            rd = new HardwareResourceDescription(hardwareAttributes);
        } else {
            System.err.println(
                               "Error creating ResourceDescription: no execution host specified.");
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

            if (!Submit)
                {
                    if (FileStdout == null) {
                        setStdout(DEFAULT_STDOUT);
                    }
                    
                    if (FileStderr == null) {
                        setStderr(DEFAULT_STDERR);
                    }
                }

            sd = new SoftwareDescription();
            sd.setLocation(Executable);
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

            /* Poststage files */
            if (poststageFiles.size() > 0) {
                File[] files = new File[poststageFiles.size()];

                for (int i = 0; i < poststageFiles.size(); i++) {
                    files[i] = (org.gridlab.gat.io.File)poststageFiles.get(i);
                }

                sd.setPostStaged(files);
            }
        } else {
            System.err.println("Error creating SoftwareDescription: no executable specified.");
            GAT.end();
            System.exit(1);
        }
    }

    /* This method tries to execute the job */
    public int runJob() {
        createSecurityContext();
        createSoftwareDescription();
        createResourceDescription();
        createJobDescription();

        try {
            theJob = broker.submitJob(jd);
        } catch (Exception e) {
            System.err.println("Error submiting job: " + e);

            e.printStackTrace();
            GAT.end();
            return(-1);
        }

        System.out.println();
        
        /**
         * If called with the option OnlySubmit true,
         * immediately return after job submission with 
         * the job ID as rc. Otherwise return the exit status
         * of the executable.
         */
        
        if (Submit)
            {
                try
                    {
                        while (1>0)
                            {
                                String JobID = theJob.getJobID();
                                if (JobID == null)
                                    {
                                        Thread.sleep(200);
                                    }
                                else
                                    {
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
                System.out.println("Submit was set to false");
            }
        
        while (true) 
            {
                try 
                    {
                        Map info = theJob.getInfo();
                    
                        /*                System.out.print("job info: ");
                                          System.out.println(info); */
                        
                        //                String state = (String) info.get("state");
                        int istate =  theJob.getState();
                        
                        //                if (state.equals("STOPPED")) {
                        if (istate == theJob.STOPPED) 
                            {
                                System.out.println();
                                System.out.println("JOB SUMMARY");
                                System.out.println("================================================");
                                System.out.println("Command:    "+sd.getLocation().toASCIIString());
                                System.out.println("Job ID:     "+theJob.getJobID());
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
                        else if (istate == theJob.SUBMISSION_ERROR) 
                            {
                                System.err.println("SUBMISSION_ERROR");
                                GAT.end();
                                return(1);
                            }
                    
                        Thread.sleep(200);
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
