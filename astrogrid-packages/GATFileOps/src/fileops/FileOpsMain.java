package fileops;

import java.lang.*;
import java.util.*;
import java.io.*;
import org.apache.commons.cli.*;

import fileops.*;

public class FileOpsMain 
{
    private String[] cmdLine;
    private String FirstArg;
    private int cmdLineIndex;

    public FileOpsMain(String[] args) 
    {
        cmdLine = new String[args.length-1];
        for( cmdLineIndex=0; cmdLineIndex<args.length; cmdLineIndex++ )
            {
        	if (cmdLineIndex==0)
                    {
        		FirstArg = args[cmdLineIndex];
                    }
        	else
                    {
        		cmdLine[cmdLineIndex-1]=args[cmdLineIndex];
                     }
            }
//        cmdLine[(args.length)-1]=null;;
    }
	
    public void invoke()
    {
        /**
           call the separate methods dependent on the first argument...
        */

        if (FirstArg.equals("RemoteCopy"))
            {
        	    try
        	    {
        	    	RemoteCopy function = new RemoteCopy(cmdLine);
        	    }
        	    catch (Exception e) {}
            }
        else if (FirstArg.equals("RemoteMove"))
            {
                try
                    {
                        RemoteMove function = new RemoteMove(cmdLine);
                    }
                 catch (Exception e) { };
            }
        else if (FirstArg.equals("DeleteFile"))
            {
                try
                    {
                        DeleteFile function = new DeleteFile(cmdLine);
                    }
                 catch (Exception e) { };
            }
        else
            {
                System.out.println("no available method found");
                System.out.println("Possible methods: RemoteCopy, RemoteMove, DeleteFile");
                return;
            }
                
    }	



    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
     
    	FileOpsMain main = new FileOpsMain(args);
        main.invoke();
    }
    
}
