package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * Some utilities for filling in the pbs script file.
 *
 * @author: Alexander Beck-Ratzka, AEI, July 2010
 *
 */



public class scriptWriter extends PrintWriter {
//    private static final DateFormat sFormatter = new SimpleDateFormat("yyyyMMddHHmm.ss");
    private static final String pbsSuffix = "#PBS";
    private static final String sgeSuffix = "#$";

    public scriptWriter (Writer out) {
    	super(out);
    }
    
    public void addOption(String opt, Object param) {
	addPbsOption(opt, param);
	addSgeOption(opt, param);
    }
    
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
    
    public void addSgeOption(String opt, Object param) {
	addOption(sgeSuffix, opt, param);
    }
    
    private void addString(String suffix, String s) {
	print(suffix);
	print (" ");
	println(s);
    }
    
    public void addPbsString(String s) {
	addString(pbsSuffix, s);
    }
    
    
    public void addSgeString(String s) {
	addString(sgeSuffix, s);
    }
    
    public void addString(String s) {
	addPbsString(s);
	addSgeString(s);
    }
}