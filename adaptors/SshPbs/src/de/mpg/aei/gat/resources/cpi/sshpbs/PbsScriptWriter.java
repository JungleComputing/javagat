package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Some utilities for filling in the pbs script file.
 *
 * @author: Alexander Beck-Ratzka, AEI, July 2010
 *
 * Modified by Ceriel Jacobs to generate a script that works for SGE as well
 * as PBS. The trick is that PBS recognizes lines starting with "#PBS",
 * and SGE recognizes lines starting with "#$". Both consider lines that
 * they don't recognize but are starting with "#" as comment. 
 */

public class PbsScriptWriter extends PrintWriter {
    
    private static final String pbsSuffix = "#PBS";
    private static final String sgeSuffix = "#$";

    public PbsScriptWriter (Writer out) {
    	super(out);
    }
    
    /**
     * Adds an option line for PBS as well as SGE.
     * @param opt the option itself.
     * @param param an optional additional parameter.
     */
    public void addOption(String opt, Object param) {
	addPbsOption(opt, param);
	addSgeOption(opt, param);
    }
    
    /**
     * Adds an option line for PBS.
     * @param opt the option itself.
     * @param param an optional additional parameter.
     */
    public void addPbsOption(String opt, Object param) {
	addOption(pbsSuffix, opt, param);
    }
    
    private void addOption(String suffix, String opt, Object param) {
        print(suffix);
        print(" -");
        print(opt);
        if (param != null) {
            print(" ");
            println(param);
        }
        else {
            println();
        }
    }
    
    /**
     * Adds an option line for SGE.
     * @param opt the option itself.
     * @param param an optional additional parameter.
     */
    public void addSgeOption(String opt, Object param) {
	addOption(sgeSuffix, opt, param);
    }
    
    private void addString(String suffix, String s) {
	print(suffix);
	print (" ");
	println(s);
    }
    
    /**
     * Adds an option line for PBS.
     * @param s the full option string.
     */
    public void addPbsString(String s) {
	addString(pbsSuffix, s);
    }
       
    /**
     * Adds an option line for SGE.
     * @param s the full option string.
     */
    public void addSgeString(String s) {
	addString(sgeSuffix, s);
    }
    
    /**
     * Adds an option line for SGE as well as PBS.
     * @param s the full option string.
     */
    public void addString(String s) {
	addPbsString(s);
	addSgeString(s);
    }
}