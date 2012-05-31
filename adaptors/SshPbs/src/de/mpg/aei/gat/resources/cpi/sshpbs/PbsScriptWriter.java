package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Some utilities for filling in the pbs script file.
 *
 * @author: Alexander Beck-Ratzka, AEI, July 2010
 */

public class PbsScriptWriter extends PrintWriter {
    
    private static final String pbsSuffix = "#PBS";

    public PbsScriptWriter (Writer out) {
    	super(out);
    }
    
    /**
     * Adds an option line.
     * @param opt the option itself.
     * @param param an optional additional parameter.
     */
    public void addOption(String opt, Object param) {
        print(pbsSuffix);
        print(" -");
        print(opt);
        if (param != null) {
            print(" ");
            print(param);
        }
        print('\n');
    }
    
    public void addString(String s) {
	print(pbsSuffix);
	print (" ");
	print(s);
	print('\n');
    }
}