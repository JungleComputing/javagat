/*
 * Main.java
 *
 * Created on April 6, 2006, 6:24 PM
 *
 */

package gatjobrun;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import gatjobrun.GATJobRunner;

/**
 *
 * @author kba79
 */
public class Main {
    
    private String[] cmdLine;
    private int cmdLineIndex;
    private String hostname;
    private String executable;
    //private Hashtable<String,String> parameterList; // only Java 1.5
    private Hashtable parameterList; 
    
    //private ArrayList<String> execArgs;       // only Java 1.5
    //private ArrayList<String> prestageFiles;  // only Java 1.5
    //private ArrayList<String> poststageFiles; // only Java 1.5
    private ArrayList execArgs;
    private ArrayList prestageFiles;
    private ArrayList poststageFiles;


    
    private void buildEmptyParameterList() {
        parameterList.put("-username","");
        parameterList.put("-password","");
        parameterList.put("-SubmitOnly","false");
        parameterList.put("-JobName","");
        parameterList.put("-MailOptions","");
        parameterList.put("-MailUser","");
         
        parameterList.put("-stdout","");
        parameterList.put("-stdin","");
        parameterList.put("-stderr","");
        parameterList.put("-join","");
        parameterList.put("-prestage","");
        parameterList.put("-poststage","");
        
        parameterList.put("-Walltime","");
        parameterList.put("-CPUTime","");
        parameterList.put("-Memsize","");
        parameterList.put("-Nodes","");
     //   RB.jobmanager
        parameterList.put("-RB.adaptor","");
        parameterList.put("-RB.jobmanager","");
    }    
    
    private void printUsage() {
        System.out.println("\nUsage: gat-job-run [OPTIONS] hostname executable [ARGUMENTS]");
        System.out.println("\nOPTIONS:");
        System.out.println("  -username      [STRING]     username for security context");
        System.out.println("  -password      [STRING]     password for security context\n");
        System.out.println("  -SubmitOnly    [true/false] submit only (true) or poll still exit\n");
        System.out.println("  -JobName       [STRING]     name of the job");
        System.out.println("  -MailOptions   [STRING]     options for sending Emails (SGE)");
        System.out.println("  -MailUser      [STRING]     recipient of the mails");
        System.out.println("  -stdin         [FILE]       path to input file");
        System.out.println("  -stdout        [FILE]       path to output file");
        System.out.println("  -stderr        [FILE]       path to error output file\n");
        System.out.println("  -join          [STRING]    how to join stdout and stderr");
        System.out.println("  -prestage      [FILE],...   path to prestage file(s) - comma separated");
        System.out.println("  -poststage     [FILE],...   path to poststage file(s) - comma spearated\n");
        System.out.println("  -Walltime      [STRING]     Walltime limit for thids job in seconds\n");
        System.out.println("  -CPUTime       [STRING]     CPU time limit for this job in seconds\n");
        System.out.println("  -Memsize       [STRING]     Memory needed for this job\n");
        System.out.println("  -RB.adaptor    [STRING]     force the use of a specific Resource Broker adaptor");        
        System.out.println("  -RB.jobmanager [STRING]     force the use of a specific Resource Broker jobmanager\n");      
    }
    
    /** Creates a new instance of Main */
    public Main(String[] args) {
        
        cmdLine = args;
        //parameterList = new Hashtable<String,String>();  // only Java 1.5
        parameterList = new Hashtable();
        
        hostname = null;
        executable = null;
        
        //execArgs = new ArrayList<String>(); // only Java 1.5
        execArgs = new ArrayList();
        
        buildEmptyParameterList();
        
        int hostnameIndex = 0;
        
        for( cmdLineIndex=0; cmdLineIndex<cmdLine.length; cmdLineIndex++ )
            {
                if( cmdLine[cmdLineIndex].startsWith("-") ) {
                    /* Parsing parameters and arguments */
                    if( parameterList.containsKey(cmdLine[cmdLineIndex]) ) {
                    
                        if( cmdLineIndex == cmdLine.length-1 ) {
                            System.err.println("\nMissing argument for parameter: "+cmdLine[cmdLineIndex]);
                            printUsage();
                            System.exit(1);
                        }
                        else {
                            if( cmdLine[cmdLineIndex+1].startsWith("-") ) {
                                System.err.println("\nMissing argument for parameter: "+cmdLine[cmdLineIndex]);
                                printUsage();
                                System.exit(1);
                            }
                            else {
                                parameterList.put(cmdLine[cmdLineIndex],cmdLine[cmdLineIndex+1]);
                                cmdLineIndex++;
                            }
                        }
                    }
                    else {
                        System.err.println("\nUnknown parameter: "+cmdLine[cmdLineIndex]);
                        printUsage();
                        System.exit(1);
                    }
                }
                else {
                    break;
                }
            }
        
        if( cmdLineIndex+1>=cmdLine.length ) {
            System.err.println("\nMisssing host and/or executable string");
            printUsage();
            System.exit(1);
        }
        else {
            hostname = cmdLine[cmdLineIndex];
            executable = cmdLine[cmdLineIndex+1];
            
            if( cmdLineIndex+2<cmdLine.length ) {
                int argIndex = 0;
                for( int i=cmdLineIndex+2; i<cmdLine.length; i++ )
                    {
                        execArgs.add(cmdLine[i]);
                        argIndex++;
                    }
            }
        }
    }
            
    public void invoke()
    {
        int GAT_RC;
        GATJobRunner gat_job_run = new GATJobRunner();
        
        //System.err.println(parameterList.toString());
        
        if( ( ((java.lang.String)parameterList.get("-username")).length() > 0) && 
            (((java.lang.String)parameterList.get("-password")).length() > 0) )
            gat_job_run.setSecurityContext((java.lang.String)parameterList.get("-username"),
                                           (java.lang.String)parameterList.get("-password"));
        
        if( ((java.lang.String)parameterList.get("-SubmitOnly")).length() > 0 )
        {
            String aa=(java.lang.String)parameterList.get("-SubmitOnly");
	    if (aa.matches("true"))
        	{
        		gat_job_run.Submit = true;
        	}
        	else
        	{
        		gat_job_run.Submit = false;
        	}
        }
        
        
        if( ((java.lang.String)parameterList.get("-JobName")).length() > 0 )
            gat_job_run.SetJobName((java.lang.String)parameterList.get("-JobName"));
        if( ((java.lang.String)parameterList.get("-MailOptions")).length() > 0 )
            gat_job_run.SetMailOptions((java.lang.String)parameterList.get("-MailOptions"));
        if( ((java.lang.String)parameterList.get("-MailUser")).length() > 0 )
            gat_job_run.SetMailUser((java.lang.String)parameterList.get("-MailUser"));
            
        if( ((java.lang.String)parameterList.get("-stdin")).length() > 0 )
            gat_job_run.setStdin((java.lang.String)parameterList.get("-stdin"));
        if( ((java.lang.String)parameterList.get("-stdout")).length() > 0 )
            gat_job_run.setStdout((java.lang.String)parameterList.get("-stdout"));
        if( ((java.lang.String)parameterList.get("-stderr")).length() > 0 )
            gat_job_run.setStderr((java.lang.String)parameterList.get("-stderr"));
        
        if( ((java.lang.String)parameterList.get("-RB.adaptor")).length() > 0 )
            gat_job_run.setRBAdaptor((java.lang.String)parameterList.get("-RB.adaptor"));
        if( ((java.lang.String)parameterList.get("-RB.jobmanager")).length() > 0 )
            gat_job_run.setRBJobmanager((java.lang.String)parameterList.get("-RB.jobmanager"));
        
        
        if( executable.length() > 0 )
            gat_job_run.setExecutable(executable);
        if( hostname.length() > 0 )
            gat_job_run.setExecutionHost(hostname);  
        if( execArgs.size() > 0 )
            gat_job_run.setExecutableArguments(execArgs);
        
        if( ((java.lang.String)parameterList.get("-prestage")).length() > 0 ) {
            
            String[] strings = ((java.lang.String)parameterList.get("-prestage")).split(",");
            
            for( int i=0; i<strings.length; i++ ) {
                gat_job_run.addPreStageFile(strings[i]);
            }
        }
        
        if( ((java.lang.String)parameterList.get("-poststage")).length() > 0 ) {
            
            String[] strings = ((java.lang.String)parameterList.get("-poststage")).split(",");
            
            for( int i=0; i<strings.length; i++ ) {
                gat_job_run.addPostStageFile(strings[i]);
            }
        }        
        
        GAT_RC=gat_job_run.runJob();
        System.out.println("runJob() exited with rc " + GAT_RC);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
     
        Main main = new Main(args);
        main.invoke();
  }
   
}
