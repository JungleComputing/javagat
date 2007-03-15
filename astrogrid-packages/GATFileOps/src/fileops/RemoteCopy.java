package fileops;

import java.lang.*;
import java.util.*;
import java.io.*;
import org.apache.commons.cli.*;

import org.gridlab.gat.*;
import org.gridlab.gat.io.File;

/** @name RemoteCopy
 *
 * @brief Class for remote copy of file with GAT.
 * 
 * This java class enables the remote copy of datasets.
 * The way of how a remote machine can be reached is 
 * estimated by the GAT engine, which takes the first
 * possible adaptor to do the remote copy. 
 *
 * However, it is possible to say via the option
 * File.adaptor, which adaptor is all be exculsively
 * used.
 *
 * The class RemoteCopy is part of the package fileops.
 *
 * @param The argument list of the call:
 *
 *        -File.adaptor <adaptor> ... which adaptor shall be used exclusively
 *        <source file> ... name of the source file as URI.
 *        <dest  file>  ... name of the destination file as 
 *
 * The -File.adaptor argument isn't necessary, the source and the destination file
 * must be given.
 * 
 * @author A. Beck-Ratzka, AEI, Potsdam, Germany.
 * @version 1.0
 * @date 21-11-2006, created.
 */


public class RemoteCopy 
{
    public RemoteCopy (String[] args) throws Exception 
    {

        /*
          declarations
        */

        CommandLine cmd=null;
        HashMap CopyArgs=null;
        Options option = new Options();
        String KeyValue=null;

        /*
          create the options (only one in this case)
        */

        option.addOption("Adaptor", true, "name of adaptor to be used");

        CommandLineParser parser = new GnuParser();

        try 
            {
                cmd  = parser.parse(option,args);
            }
        catch(ParseException exp)
            {
                System.err.println("Parsing failed. Reason: " + exp.getMessage() );
            }

        try 
            {
        	CopyArgs = RemoteCopy.CopyArgs(cmd);
            } 
        catch(IllegalArgumentException ee) 
            {
        	//System.out.println("wrong number of arguments.");
        	ee.printStackTrace();
        	return;
            }
        
        GATContext context = new GATContext();
        Preferences prefs  = new Preferences();

        if (CopyArgs.containsKey("File.adaptor") )
            {
                KeyValue = (String) CopyArgs.get("File.adaptor");
                prefs.put("File.adaptor.name",KeyValue);
            }
        context.addPreferences(prefs);

        URI src = new URI(CopyArgs.get("SrcFile").toString());
        URI dest = new URI(CopyArgs.get("DestFile").toString());
        File file = GAT.createFile(context, src);             // create file object
        file.copy(dest);                                      // and copy it
        GAT.end();
    }

    /***************************************************************************************/
    
    /** @brief method CopyArgs create HashMap with arguments for GAT File Copy
     *
     * @fn  public static HashMap CopyArgs(CommandLine CmdLine) throws IllegalArgumentException
     *
     * @return HashMap with argument list.
     *
     * @version 1.0
     * @author A. Beck-Ratzka, AEI.
     * @date 21.11.2006; created.
     */


    public static HashMap CopyArgs(CommandLine CmdLine) throws IllegalArgumentException
    {
        
        /**
           declarations
        */

        HashMap Args = new HashMap();
        String ArgValue = null;
        String[] FileNames = null;

        FileNames = CmdLine.getArgs();
        
        
        if (FileNames.length!=2)
            {
                throw new IllegalArgumentException("wrong number of arguments");
            }
        else
            {
                Args.put("SrcFile",FileNames[0]);
                Args.put("DestFile",FileNames[1]);
                ArgValue = CmdLine.getOptionValue("Adaptor");
                if (ArgValue!=null)
                    {
                        Args.put("File.adaptor",ArgValue);
                    }
                return(Args);
            }
    }


}
