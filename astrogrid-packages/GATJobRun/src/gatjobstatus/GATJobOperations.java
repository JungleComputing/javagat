package gatjobstatus;

/* Import log4j classes */
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/* Exceptions */
import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.advert.MetaData;
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

/**
 *
 * @name GATJobOperations
 *
 * @brief public class GATJobOperations invokes different methods
 *        for operations on running jobs.
 *
 * 
 * @author A. Beck-Ratzka, AEI, Potsdam, Germany.
 * @version 0.1
 * @date 18-06-2007, created
 */

public class GATJobOperations /** creates new instance of GatJobOperations */
{

    /* Configure log4j to avoid nasty console outputs */

    private Properties          globalProps;
    private GATContext          globalContext;
    private Preferences         globalPrefs;

    private String Adaptor;
    private String JobID;
    private String JobOperation;

    private Job  theJob;

    

 /** creates new instance of GatJobOperations */   
    public GATJobOperations() 
    {
       
        /* Configure log4j to avoid nasty console outputs */
        BasicConfigurator.configure();  

        globalProps    = System.getProperties();

        // Turn on DEBUG & VERBOSE for GatEngine
        globalProps.setProperty("gat.verbose", "false");

        globalContext  = new GATContext();
        globalPrefs    = new Preferences();


        Adaptor=null;
        JobID=null;
        JobOperation=null;

        // Turn on DEBUG & VERBOSE for GatEngine
        
        globalProps.setProperty("gat.verbose", "false");  
    }
    
    /**
     * @method SetAdaptor
     *
     * @brief set the Adaptor to be invoked.
     *
     * @author A. Beck-Ratzka, AEI, Potsdam. 
     * @version 0.1
     * @date 19-06-2007, created
     *
     */

    public void SetAdaptor(String adaptorname)
    {
        Adaptor = adaptorname;
        System.out.println("selected adaptor for Adaptor: " + Adaptor);
        //        globalPrefs.put("ResourceBroker.adaptor.name", adaptorname);
    }

    /**
     * @method SetJobID
     *
     * @brief set the Job ID of the job to be asked for.
     *
     * @author A. Beck-Ratzka, AEI, Potsdam. 
     * @version 0.1
     * @date 19-06-2007, created
     *
     */

    public void SetJobID(String jobid)
    {
        JobID=jobid;
    }

    /**
     * @method SetOperation
     *
     * @brief set the operation for the job.
     *
     * @author A. Beck-Ratzka, AEI, Potsdam. 
     * @version 0.1
     * @date 19-06-2007, created
     *
     */

    public void SetOperation(String operation)
    {
        JobOperation=operation;
    }

    /**
     * @method JobOp
     *
     * @brief perform the deired job operation.
     *
     * right now this are the options
     *
     * kill
     * status
     *
     * @author A. Beck-Ratzka, AEI, Potsdam. 
     * @version 0.1
     * @date 19-06-2007, created
     *
     */

    public int JobOp()
    {
        int state=0;

        // set the advert adapter prefs...
        
        globalPrefs.put("AdvertService.adaptor.name","local");
        globalPrefs.put("killJobsOnExit","false");
        globalPrefs.put("ResourceBroker.adaptor.name",Adaptor);
        System.out.println("selected ResourceBrokerAdaptor: " + Adaptor);

        globalContext.addPreferences(globalPrefs);


        /**
           First get contact to the advert service.
           Here the job class info is stored.
        */

        try 
            {
                AdvertService aa = GAT.createAdvertService(globalContext);
                theJob = (Job) aa.getAdvertisable("/tmp/" + JobID);
            } 
        catch (GATObjectCreationException e) {System.err.println("GAT.createAdvertService failed."); e.printStackTrace(); return(-1);}
        catch (org.gridlab.gat.GATInvocationException e)  {System.err.println("getAdvertisable failed."); e.printStackTrace(); return(-1);}
            
        /**
           different operations dependent on JobOperation's value
        */

        if (JobOperation.equals("stop"))
            {
                try 
                    {
                        theJob.stop();
                        System.out.println("JOB SUMMARY");
                        System.out.println("================================================");
                        System.out.println("Job ID: " + theJob.getJobID());
                        System.out.println("================================================");
                        System.out.println("Job stopped by user");
                    } catch (org.gridlab.gat.GATInvocationException e)  {System.err.println("stop job failed."); e.printStackTrace(); return(-1);}
            }
        else if (JobOperation.equals("status"))
            {
                try
                    {
                        state=theJob.getState();
                        System.out.println();
                        System.out.println("JOB SUMMARY");
                        System.out.println("================================================");
                        System.out.println("Job ID: " + theJob.getJobID() + "; Job State: " + theJob.getStateString(state));
                    } catch (org.gridlab.gat.GATInvocationException e)  {System.err.println("stop job failed."); e.printStackTrace(); return(-1);}

                /**
                   Handling for different job stages...
                */

                if (state==theJob.INITIAL)
                    {
                        System.out.println("Job " + JobID + " in INITIAL state; int value of state: " + state);
                    }
                else if (state==theJob.ON_HOLD)
                    {
                        System.out.println("Job " + JobID + " in ON_HOLD state; int value of state: " + state);
                    }
                else if (state==theJob.POST_STAGING)
                    {
                        System.out.println("Job " + JobID + " in POST_STAGING state; int value of state: " + state);
                    }
                else if (state==theJob.PRE_STAGING)
                    {
                        System.out.println("Job " + JobID + " in PRE_STAGING state; int value of state: " + state);
                    }
                else if (state==theJob.RUNNING)
                    {
                        System.out.println("Job " + JobID + " in RUNNING state; int value of state: " + state);
                    }
                else if (state==theJob.SCHEDULED)
                    {
                        System.out.println("Job " + JobID + " in SCHEDULED state; int value of state: " + state);
                    }
                else if (state==theJob.STOPPED)
                    {
                        System.out.println("Job " + JobID + " in STOPPED state; int value of state: " + state);
                    }
                else if (state==theJob.SUBMISSION_ERROR)
                    {
                        System.out.println("Job " + JobID + " in SUBMISSION_ERROR state; int value of state: " + state);
                    }
                else if (state==theJob.UNKNOWN)
                    {
                        System.out.println("Job " + JobID + " in UNKNOWN state; int value of state: " + state);
                    }
                else
                    {
                        System.out.println("Job " + JobID + "has a state not considered in GATJob; int value of state: " + state);
                    }


                /**
                   some special handling for case finished...
                */

                if (state == theJob.STOPPED)
                    {
                        try
                            {
                                System.out.println("Exit value: " + theJob.getExitStatus());
                            } catch (org.gridlab.gat.GATInvocationException e)  {System.err.println("Job.getExitStatus() failed."); e.printStackTrace(); return(-1);}
                    }

                System.out.println("================================================");
            }
        return(state);
    }

}
