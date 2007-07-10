package gatjobstatus;

import java.lang.*;
import java.util.*;
import java.io.*;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.AdvertService;
import org.gridlab.gat.resources.Job;

import gatjobstatus.GATJobOperations;


/** @name JobStateMain
 *
 * @brief Main class for performing job status requests on running jobs, via AdvertService.
 * 
 * This java class is the entry point for operational requests on running jobs, as
 * getting the status, put job on hold, kill a job, etc... It imports the class
 * GatJobOperations, which is doing the requests finally.
 *
 *
 * In its first version, it must be called with the name of the 
 * adaptor which has been invoked to start the job, and the JobID returned
 * by the job submit command of a previous Broker.Submit of Java-GAT.
 *
 * @param The argument list of the call:
 *        -RB.Adaptor <adaptor> ... name of the ResourceBrokerAdaptor used prevously.
 *        -JobID <job ID> ... ID of the jobs previously submitted (as a string).
 *        -operation <operation> ... Operation for the job. This can be "status", 
 *                                   and "kill" right now in version 0.1.
 *
 * @author A. Beck-Ratzka, AEI, Potsdam, Germany.
 * @version 0.1
 * @date 18-06-2007, created
 */

public class JobStateMain
{
    
    private String[] cmdLine;
    private int cmdLineIndex;
    private Hashtable parameterList;


    /**
     * @method JobStateMain.buildEmptyParameterList
     *
     * @brief Build an empty parameter list and put it to the  Hashtable parameterList.
     *
     * @author A. Beck-Ratzka, AEI, Potsdam. 
     * @version 0.1
     * @date 18-06-2007, created
     */

    private void buildEmptyParameterList()
    {
        parameterList.put("-RB.Adaptor","");
        parameterList.put("-JobID","");
        parameterList.put("-operation","");
    }

    /**
     * @method JobStateMain.printUsage
     *
     *
     * @brief print the usage of gat-job-operation
     *
     * @author A. Beck-Ratzka, AEI, Potsdam. 
     * @version 0.1
     * @date 18-06-2007, created
     */

    private void printUsage()
    {
        System.out.println("\nUsage: gat-job-operation [OPTIONS]");
        System.out.println("\nOPTIONS:\n");
        System.out.println("  -RB.Adaptor    [STRING]   adaptor invoked for the job submit\n");
        System.out.println("  -JobID         [STRING]   Job ID of the job submitted\n");
        System.out.println("  -operation     [STRING]   which operation? Available operations: status, kill\n");
    }

    /**
     * @class JobStateMain
     *
     * @brief Fill in the parameterList, or exit in case of an error in the command line call.
     *
     * @author A. Beck-Ratzka, AEI, Potsdam. 
     * @version 0.1
     * @date 18-06-2007, created
     */

    public JobStateMain(String[] args)
    {
        cmdLine = args;
        parameterList = new Hashtable();

        buildEmptyParameterList();

        /**
           now fill in the parameterList, combined with a
           check for the right call...
        */

        if (cmdLine.length == 0) {printUsage(); System.exit(1);}
        for (cmdLineIndex=0; cmdLineIndex<cmdLine.length; cmdLineIndex++)
            {
                if ( cmdLine[cmdLineIndex].startsWith("-") )
                    {
                        if ( parameterList.containsKey(cmdLine[cmdLineIndex]) )
                            {
                                if (cmdLineIndex == cmdLine.length-1 )
                                    {
                                        System.err.println("\nMissing argument for parameter: "+cmdLine[cmdLineIndex]);
                                        printUsage();
                                        System.exit(1);
                                    }
                                else
                                    {
                                        if ( cmdLine[cmdLineIndex+1].startsWith("-") )
                                            {
                                                System.err.println("\nMissing argument for parameter: "+cmdLine[cmdLineIndex]);
                                                printUsage();
                                                System.exit(1);
                                            }
                                        else
                                            {
                                                parameterList.put(cmdLine[cmdLineIndex],cmdLine[cmdLineIndex+1]);
                                                cmdLineIndex++;
                                            }
                                    }
                                
                            }
                        else
                            {
                                System.err.println("\nUnknown parameter: "+cmdLine[cmdLineIndex]);
                                printUsage();
                                System.exit(1);
                            }
                    }
                else
                    {
                        break;
                    }
            }

        /**
           check whether all the arguments are filled in
        */

        String ArgValue = null;
        ArgValue = (String) parameterList.get("-RB.Adaptor");
        if (ArgValue==null) {System.err.println("\n missing value for -RB.Adaptor\n"); printUsage();  System.exit(1);}

        ArgValue  = null;
        ArgValue = (String) parameterList.get("-JobID");
        if (ArgValue==null) {System.err.println("\n missing value for -JobID\n"); printUsage();  System.exit(1);}

        ArgValue  = null;
        ArgValue = (String) parameterList.get("-operation");
        if (ArgValue==null) {System.err.println("\n missing value for -operation\n"); printUsage();  System.exit(1);}
        
    }


    /**
     * @method JobStateMain.invoke
     *
     * @brief Called for inoking one of the desired operations an a job
     *
     * @author A. Beck-Ratzka, AEI, Potsdam. 
     * @version 0.1
     * @date 18-06-2007, created
     *
     */

    public void invoke()
    {
        int GAT_RC;

        /*        String Adaptor   = (String) parameterList.get("-RB.Adaptor");
                  String JobID     = (String) parameterList.get("-JobID");
                  String Operation = (String) parameterList.get("-operation");*/

       GATJobOperations gat_job_stat = new GATJobOperations();

        /**
           fill in the arguments of the CLI call. Because
           the check of the arguments was already done in the
           method Main, it is not necessary anymore, to do it
           here.
        */
        
       gat_job_stat.SetAdaptor((String)   parameterList.get("-RB.Adaptor"));
       gat_job_stat.SetJobID((String)     parameterList.get("-JobID"));
       gat_job_stat.SetOperation((String) parameterList.get("-operation"));
       
       GAT_RC  = gat_job_stat.JobOp();
       System.out.println("JobOperation() exited with the rc " + GAT_RC);
    }

    /**
     * @brief main method of class GatJobStatus.
     *
     * This method creates a new instance of Main, and
     * it invokes it afterwards.
     *
     * @author A. Beck-Ratzka, AEI, Potsdam. 
     * @version 0.1
     * @date 18-06-2007, created
     *
     */
    
    public static void main(String[] args) throws Exception
    {
        JobStateMain main = new JobStateMain(args);
        main.invoke();
    }
}
