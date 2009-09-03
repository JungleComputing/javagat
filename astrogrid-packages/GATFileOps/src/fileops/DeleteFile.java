package fileops;

import java.lang.*;
import java.util.*;
import java.io.*;
import org.apache.commons.cli.*;

import org.gridlab.gat.*;
import org.gridlab.gat.io.File;

/** @name DeleteFile
 *
 * @brief Class for delete of a file (either local or remote) with GAT.
 * 
 * This java class enables the deletion of datasets.
 * The way of how a remote machine can be reached is 
 * estimated by the GAT engine, which takes the first
 * possible adaptor to do the remote move. 
 *
 * However, it is possible to say via the option
 * File.adaptor, which adaptor is all be exculsively
 * used.
 *
 * The class DeleteFile is part of the package fileops.
 *
 * @param The argument list of the call:
 *
 *        -File.adaptor <adaptor> ... which adaptor shall be used exclusively
 *        <file> ... name of the file to be deleted as URI.
 *
 * The -File.adaptor argument isn't necessary.
 * 
 * @author A. Beck-Ratzka, AEI, Potsdam, Germany.
 * @version 1.0
 * @date 27-11-2006, created.
 */


public class DeleteFile 
{
    public DeleteFile(String[] args) throws Exception 
    {

        /*
          declarations
        */

        CommandLine cmd=null;
        HashMap DeleteArgs=null;
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
        	DeleteArgs = DeleteFile.DeleteArgs(cmd);
            } 
        catch(IllegalArgumentException ee) 
            {
        	//System.out.println("wrong number of arguments.");
        	ee.printStackTrace();
        	return;
            }
        
        GATContext context = new GATContext();
        Preferences prefs  = new Preferences();

        if (DeleteArgs.containsKey("File.adaptor") )
            {
                KeyValue = (String) DeleteArgs.get("File.adaptor");
                prefs.put("File.adaptor.name",KeyValue);
            }
        context.addPreferences(prefs);

        URI src = new URI(DeleteArgs.get("SrcFile").toString());
        File file = GAT.createFile(context, src);                // create file object
        file.delete();                                       // and delete it
        GAT.end();
    }

    /***************************************************************************************/
    
    /** @brief method DeleteArgs create HashMap with arguments for GAT File Move
     *
     * @fn  public static HashMap DeleteArgs(CommandLine CmdLine) throws IllegalArgumentException
     *
     * @return HashMap with argument list.
     *
     * @version 1.0
     * @author A. Beck-Ratzka, AEI.
     * @date 27.11.2006; created.
     */


    public static HashMap DeleteArgs(CommandLine CmdLine) throws IllegalArgumentException
    {
        
        /**
           declarations
        */

        HashMap Args = new HashMap();
        String ArgValue = null;
        String[] FileNames = null;

        FileNames = CmdLine.getArgs();
        
        
        if (FileNames.length!=1)
            {
                throw new IllegalArgumentException("wrong number of arguments");
            }
        else
            {
                Args.put("SrcFile",FileNames[0]);
                ArgValue = CmdLine.getOptionValue("Adaptor");
                if (ArgValue!=null)
                    {
                        Args.put("File.adaptor",ArgValue);
                    }
                return(Args);
            }
    }


}
