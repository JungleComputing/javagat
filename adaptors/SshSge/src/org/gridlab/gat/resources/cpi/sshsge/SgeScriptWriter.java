package org.gridlab.gat.resources.cpi.sshsge;

import java.io.PrintWriter;
import java.io.Writer;

public class SgeScriptWriter extends PrintWriter {
    
    private static final String sgeSuffix = "#$";

    public SgeScriptWriter (Writer out) {
    	super(out);
    }
    
    /**
     * Adds an option line for PBS as well as SGE.
     * @param opt the option itself.
     * @param param an optional additional parameter.
     */
    public void addOption(String opt, Object param) {
        print(sgeSuffix);
        print(" -");
        print(opt);
        if (param != null) {
            print(" ");
            print(param);
        }
        print('\n');
    }

    /**
     * Adds an option line for SGE as well as PBS.
     * @param s the full option string.
     */
    public void addString(String s) {
	print(sgeSuffix);
	print (" ");
	print(s);
	print('\n');
    }
}