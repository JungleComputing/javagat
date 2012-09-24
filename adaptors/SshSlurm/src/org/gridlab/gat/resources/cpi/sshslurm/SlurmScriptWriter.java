package org.gridlab.gat.resources.cpi.sshslurm;

import java.io.PrintWriter;
import java.io.Writer;

public class SlurmScriptWriter extends PrintWriter {
    
    private static final String slurmSuffix = "#SBATCH";

    public SlurmScriptWriter (Writer out) {
    	super(out);
    }
    
    /**
     * Adds an option line for Slurm.
     * @param opt the option itself.
     * @param param an optional additional parameter.
     */
    public void addOption(String opt, Object param) {
        print(slurmSuffix);
        print(" --");
        print(opt);
        if (param != null) {
            print("=");
            print(param);
        }
        print('\n');
    }

    /**
     * Adds an option line for Slurm.
     * @param s the full option string.
     */
    public void addString(String s) {
	print(slurmSuffix);
	print (" ");
	print(s);
	print('\n');
    }
}