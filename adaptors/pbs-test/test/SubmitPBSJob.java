package test;


import java.net.URISyntaxException;
import java.util.*;

import org.apache.commons.cli.*;

import org.gridlab.gat.GAT;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.GATObjectCreationException;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Job;
import org.gridlab.gat.resources.JobDescription;
import org.gridlab.gat.resources.ResourceBroker;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.security.*;

import org.gridlab.gat.resources.cpi.pbs_resource.IArgument;

/** @name SubmitPBSJob
 * 
 * @brief Example API entry for submiting a Job via GAT, PBS preferred.
 *
 * @author  doerl (created)
 *
 * @version 1.0
 * @author A. Beck-Ratzka, AEI; extended for usage with commmand line arguments.
 * @date 28-04-06, extended for usage with commmand line arguments.
 *
 */
public class SubmitPBSJob {

    public static void main(String[] args) throws Exception {

        /**
           Retrieve the arguments; added 28.04.06 by A. Beck-Ratzka, AEI.
        */

    	int sleeper=0;
        String Queue = null;
        String Time = null;
        String Filesize = null;
        String Memsize = null;
        String Nodes = null;
        String LString = null;
        String JobID = null;


        CommandLine cmd=null;
    	HashMap JobArgs=null;
        ResourceDescription rd=null;
        SoftwareDescription sd=null;
        
        ResourceDescription rdJob=null;
        HashMap rdJob_attr=null;

        Options option =  new Options();

        /**
           prepare the option hashmap.
        */

        option.addOption("q", true, "name of PBS queue; mandatory!");
        option.addOption("e", true, "PBS error file");
        option.addOption("o", true, "PBS output file");
        option.addOption("j", true, "How to yank error and output file.");
        option.addOption("n", true, "Nodes; default  is 1");
        option.addOption("m", true, "The memory size");
        option.addOption("f", true, "The file size");
        option.addOption("t", true, "Walltime; . Default is 00:01:00");
        option.addOption("N", true, "jobname ;default is the LOGNAME.");
       
        /**
           create the command line parser, and parse
        */

        CommandLineParser parser = new PosixParser();

        try 
            {
                cmd  = parser.parse(option,args);
            }
        catch(ParseException exp)
            {
                System.err.println("Parsing failed. Reason: " + exp.getMessage() );
            }

        /**
           retrieve the arguemnts. Case PBS here, must be modified for other scheduler!
        */

    
        try 
            {
                JobArgs=SubmitPBSJob.PBS_Arguments(cmd);
            }
        
        catch (IllegalArgumentException e)
            {
                System.out.print("\nError in argument list. Right call:\n"
                                 + "\njava test.SubmitPBSJob"
                                 + "\t-q <queue>"
                                 + "\n\t\t\t-j <yank mode stderr stdout>"
                                 + "\n\t\t\t-n <nodes> (Default is 1)"
                                 + "\n\t\t\t-m <memory size>"
                                 + "\n\t\t\t-f <file size>" 
                                 + "\n\t\t\t-t <Walltime> (Default is 00:01:00"
                                 + "\n\t\t\t-N <jobname> (Default is LOGNAME)"
                                 + "\n\t\t\t<executable [arg1] [arg2] ...>  \n\n");
                System.out.println("Error in argument list: " + e.getMessage());
                return;
            }

        /**
           prepare the GATJob submit
        */
        
        String ll = System.getenv("GAT_LOCATION");
        if (ll == null)
            {
        	throw new Exception("environment varibale GAT_LOCATION not set");
            }

        try {
            try {
                System.setProperty("gat.adaptor.path", ll + "/adaptors/lib");
            }
            catch (SecurityException SecErr)
                {
                    System.out.println("System.setProperty of gat.adaptor.path=" + ll + "/adaptors/lib failed of security reasons");
                    return;
                }
            catch (NullPointerException NullErr)
                {
                    System.out.println("System.setProperty of gat.adaptor.path=" + ll + "/adaptors/lib failed of security reasons");
                    return;
                }
            catch (IllegalArgumentException IllErr)
                {
                    System.out.println("System.setProperty of gat.adaptor.path=" + ll + "/adaptors/lib failed of security reasons");
                    return;
                }


            GATContext context = new GATContext();
        
            Preferences prefs = new Preferences();
            prefs.put("ResourceBroker.adaptor.name", "PbsBrokerAdaptor");
            ResourceBroker broker = GAT.createResourceBroker(context, prefs);

            /**
             * 
             * test for security
             */
            
            CertificateSecurityContext c = new CertificateSecurityContext();
            c.addNote("host", "peyote.aei.mpg.de");
            c.setUsername("alibeck");
            c.setPassphrase("regina");
            
            rd = CreateHardwareResourceDescriptionn (JobArgs);
            sd = CreateSoftwareResourceDescription (context,JobArgs);
            //            SoftwareDescription sd = new SoftwareDescription();
            
            String ExecFile = (String) JobArgs.get( "Executable" );
            StringTokenizer ExToken = new StringTokenizer(ExecFile," ");
            sd.setLocation(ExToken.nextToken());
            
            /*			sd.setStdout(GAT.createFile(context, STDOUT));
              sd.setStderr(GAT.createFile(context, STDERR));*/
            //        System.out.println("hallo Ali, noch bin noch da..1");
            //          sd.addPreStagedFile(GAT.createFile(context, EXE_SRC), GAT.createFile(context, EXE_FILE));
            //			sd.addPostStagedFile(GAT.createFile(context, STDOUT), GAT.createFile(context, STDOUT_DST));
            //			sd.addPostStagedFile(GAT.createFile(context, STDERR), GAT.createFile(context, STDERR_DST));
			
            Map attr = new HashMap();
            attr.put(IArgument.MAIL, "alexander.beck-ratzka@aei.mpg.de");
            attr.put(IArgument.CWD, "/home/ali/tmp/xxxx");
            //			sd.setAttributes(attr);
			
            //            System.out.println("hallo Ali, noch bin noch da..2");
			
            JobDescription jd = new JobDescription(sd, rd);
            
            /**
             *  test for getting ResourceDescription from JobDescription.
             */
            
            rdJob = new HardwareResourceDescription();
            
            rdJob = jd.getResourceDescription();
            
            rdJob_attr = new HashMap(); 
            rdJob_attr = (HashMap) rdJob.getDescription();
            String str = (String) rdJob_attr.get("machine.queue");  


            Queue = (String) rdJob_attr.get("machine.queue");
 
            Time = (String) rdJob_attr.get("cpu.walltime");
            if (Time==null)
                {
                    Time = new String("00:01:00");
                }
            Filesize = (String) rdJob_attr.get("file.size");
            if (Filesize==null)
                {
                    Filesize = new String("");
                }

            Memsize = (String) rdJob_attr.get("memory.size");
            if (Memsize==null)
                {
                    Memsize = new String("");
                }

            Nodes = (String) rdJob_attr.get("cpu.nodes");
            if (Nodes==null)
                {
                    Nodes = new String("1");
                }
                
            if (Queue!=null)
                {
                    LString = new String("walltime=" + Time + ",file=" + Filesize + ",mem=" + Memsize + ",nodes=" + Nodes + ":" + Queue);
                }
            else
                {
                    LString = new String("walltime=" + Time + ",file=" + Filesize + ",mem=" + Memsize + ",nodes=" + Nodes);
                }
 

         
            Job job = broker.submitJob(jd);
            JobID = job.getJobID();
            System.out.println("JobID found:" + JobID);
            while ((job.getState() != Job.STOPPED) && (job.getState() != Job.SUBMISSION_ERROR)) {
                if (sleeper < 5)
                    {
                        System.err.println("job state = " + job.getInfo());
                        sleeper++;
                        Thread.sleep(1000);
                    }
                else
                    {
                        try {
                            job.stop();
                            break;
                        }
                        catch (Exception Ex) {
                            Ex.printStackTrace();
                        }
                    }
            }
            GAT.end();
        }
        catch (Exception ex) 
            {
                ex.printStackTrace();
            }
    }

    /***************************************************************************************/
    
    /** @brief method PBS_Arguments create HashMap with arguments
     *
     * @fn  public static HashMap PBS_Arguments(CommandLine CmdLine) throws IllegalArgumentException
     *
     * @return HashMap with argument list.
     *
     * @version 1.0
     * @author A. Beck-Ratzka, AEI.
     * @date 28.04.2006; created.
     */


    public static HashMap PBS_Arguments(CommandLine CmdLine) throws IllegalArgumentException
    {
        
        /**
           declarations
        */

    	int ii=0;
    	boolean first=true;
    	
        HashMap Args = new HashMap();
        String ResArgs[]=null;

        String ArgValue = null;
        String ExArg = null;

        /**
           put arguments into the hashmap Args, but throw an IllegalArgumentException 
           if the argument of queue or executable is not set. In all other cases set the
           defaults or set nothing...
        */

        ArgValue = CmdLine.getOptionValue("q");
        if (ArgValue!=null)
            {
                Args.put("Queue",ArgValue);
                ArgValue = null;
            }

        ArgValue = CmdLine.getOptionValue("o");
        if (ArgValue!=null)
            {
                Args.put("Stdout",ArgValue);
                ArgValue = null;
            }
        
        ArgValue = CmdLine.getOptionValue("e");
        if (ArgValue!=null)
            {
                Args.put("Stderr",ArgValue);
                ArgValue = null;
            }
        
        ArgValue = CmdLine.getOptionValue("m");
        if (ArgValue!=null)
            {
                Args.put("Memsize",ArgValue);
                ArgValue = null;
            }
        
        ArgValue = CmdLine.getOptionValue("f");
        if (ArgValue!=null)
            {
                Args.put("Filesize",ArgValue);
                ArgValue = null;
            }

        ArgValue = CmdLine.getOptionValue("j");
        if (ArgValue==null)
            {
                Args.put("YankEO","eo");
                ArgValue = null;
            }
        else
            {
                Args.put("YankEO",ArgValue);
                ArgValue = null;
            }

        ArgValue = CmdLine.getOptionValue("t");
        if (ArgValue==null)
            {
                Args.put("Walltime","00:01:00");
                ArgValue = null;
            }
        else
            {
                Args.put("Walltime",ArgValue);
                ArgValue = null;
            }

        ArgValue = CmdLine.getOptionValue("n");
        if (ArgValue==null)
            {
                Args.put("Nodes","1");
                ArgValue = null;
            }
        else
            {
                Args.put("Nodes",ArgValue);
                ArgValue = null;
            }

        ArgValue = CmdLine.getOptionValue("N");
        if (ArgValue==null)
            {
                Args.put("Jobname",System.getProperty("user.name"));
                ArgValue = null;
            }
        else
            {
                Args.put("Jobname",ArgValue);
                ArgValue = null;
            }
        
        /**
         * the remaining arguments (executable and its arguments...)
         */
        
        List ListArgs = CmdLine.getArgList();
        if (ListArgs.isEmpty())
            {
        	throw new IllegalArgumentException("missing executable");
            }
        Iterator ResIt = ListArgs.iterator();
        while (ResIt.hasNext())
            {
        	if (first)
                    {
        		ArgValue = (String) ResIt.next();
        		ExArg = ArgValue;
                    }
        	else
                    {
        		ArgValue = ExArg.concat(" " + (String) ResIt.next());
        		ExArg = ArgValue;
                    }
        	if (first) first=false;
            }
        /*        ii=1;
                  while (ResArgs[ii] != null)
                  {
                  ArgValue = ExArg.concat(" " + ResArgs[ii]);
                  ExArg = ArgValue;
                  ii++;
                  }*/
        Args.put("Executable",ArgValue);
        ArgValue = null;

        return(Args);
    }

    /***************************************************************************************/
    
    /** @brief method CreateHardwareResourceDescription create ResourceDescription
     *
     * @fn  public static ResourceDescription CreateHardwareResourceDescriptionn (HashMap Args) 
     *
     * @return ResourceDescription with argument list.
     *
     * @version 1.0
     * @author A. Beck-Ratzka, AEI.
     * @date 28.04.2006; created.
     */


    public static ResourceDescription CreateHardwareResourceDescriptionn (HashMap Args) 
    {

        /**
           declarations
        */
        
        String KeyValue=null;
        boolean KeyFinder = false;
        ResourceDescription rd = new HardwareResourceDescription();
        
        /**
           go through the argument hashmap, and add the available values to the resource description.
        */

        KeyFinder = Args.containsKey( "Queue");
        if (KeyFinder) 
            {
                KeyValue = (String) Args.get( "Queue" );
                rd.addResourceAttribute("machine.queue",KeyValue);
                KeyValue=null;
                KeyFinder = false;
            }
        

        KeyFinder = Args.containsKey( "Memsize");
        if (KeyFinder) 
            {
                KeyValue = (String) Args.get( "Memsize" );
                rd.addResourceAttribute("memory.size",KeyValue);
                KeyFinder = false;
                KeyValue=null;
            }

        KeyFinder = Args.containsKey( "Filesize");
        if (KeyFinder) 
            {
                KeyValue = (String) Args.get( "Filesize" );
                rd.addResourceAttribute("file.size",KeyValue);
                KeyFinder = false;
                KeyValue=null;
            }

        KeyFinder = Args.containsKey( "YankEO");
        if (KeyFinder) 
            {
                KeyValue = (String) Args.get( "YankEO" );
                rd.addResourceAttribute("file.yeo",KeyValue);
                KeyValue=null;
                KeyFinder = false;
            }

        KeyFinder = Args.containsKey( "Walltime");
        if (KeyFinder) 
            {
                KeyValue = (String) Args.get( "Walltime" );
                rd.addResourceAttribute("cpu.walltime",KeyValue);
                KeyValue=null;
                KeyFinder = false;
            }

        KeyFinder = Args.containsKey( "Jobname");
        if (KeyFinder) 
            {
                KeyValue = (String) Args.get( "Jobname" );
                rd.addResourceAttribute("file.Jobname",KeyValue);
                KeyValue=null;
                KeyFinder = false;
            }

        KeyFinder = Args.containsKey( "Nodes");
        if (KeyFinder) 
            {
                KeyValue = (String) Args.get( "Nodes" );
                rd.addResourceAttribute("cpu.nodes",KeyValue);
                KeyValue=null;
                KeyFinder = false;
            }

        return rd;
    }


    /***************************************************************************************/
    
    /** @brief method CreateSoftwareResourceDescription (GATContext context, HashMap JobArgs) 
     *  create SoftwareDescription
     *
     * @fn  public static SoftwareDescription CreateSoftwareResourceDescription (HashMap Args) 
     *
     * @return ResourceDescription with argument list.
     *
     * @version 1.0
     * @author A. Beck-Ratzka, AEI.
     * @throws GATObjectCreationException 
     * @date 03.05.2006; created.
     */


    public static SoftwareDescription CreateSoftwareResourceDescription (GATContext context, HashMap Args) throws GATObjectCreationException 
    {

        /**
           declarations
        */
        
        String KeyValue=null;
        Vector args=new Vector();
        String SingleArg=null;
        int index=0;
        boolean KeyFinder = false;
        SoftwareDescription sd = new SoftwareDescription();
        
        /**
           go through the argument hashmap, and add the available values to the resource description.
        */

        KeyFinder = Args.containsKey( "Executable");
        if (KeyFinder) 
            {
                KeyValue = (String) Args.get( "Executable" );
                
                /**
                 * 	Here the single argument have to be extracted.
                 *      First create a StringTokenizer.
                 */

                StringTokenizer sToken = new StringTokenizer(KeyValue," ");

                /**
                 * Is there more than the call...
                 */
                
                if (sToken.countTokens() > 1)
                    {
                        SingleArg = sToken.nextToken();
                        try {
                            sd.setLocation(SingleArg);
                        } catch (URISyntaxException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        while (sToken.hasMoreTokens())
                            {
                                SingleArg = sToken.nextToken();
                                args.add(SingleArg);
                            }

                        sd.setArguments((String[]) args.toArray(new String[args.size()]));
                    }
                else
                    {
                        try {
                            sd.setLocation(KeyValue);
                        } catch (URISyntaxException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                KeyValue=null;
                KeyFinder = false;
            }
        
        KeyFinder = Args.containsKey( "Stdout");
        if (KeyFinder) 
            {
                KeyValue = (String) Args.get( "Stdout" );
                try {
                    sd.setStdout(GAT.createFile(context, (String) KeyValue));
                } catch (GATObjectCreationException e) {
                    System.out.println("Cannot add Stdout to SoftwareDescription " + e.getMessage());
                    e.printStackTrace();
                }
                KeyValue=null;
                KeyFinder = false;
            }

        KeyFinder = Args.containsKey( "Stderr");
        if (KeyFinder) 
            {
                KeyValue = (String) Args.get( "Stderr" );
                try {
                    sd.setStderr(GAT.createFile(context,KeyValue));
                } catch (GATObjectCreationException e) {
                    System.out.println("Cannot add Stderr to SoftwareDescription " + e.getMessage());
                    e.printStackTrace();
                }
               
                KeyValue=null;
                KeyFinder = false;
            }
        
        


        return sd;
    }

}
