package de.mpg.aei.gat.resources.cpi.sshpbs;

import java.io.PrintWriter;
import java.io.Writer;
/*import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;*/

/**
 * sciptWriter - some utilities for filling in the 
 * pbs scritp file
 *
 *
 * @author: Alexander Beck-Ratzka, AEI, July 2010
 *
 */



public class scriptWriter extends PrintWriter {
//    private static final DateFormat sFormatter = new SimpleDateFormat("yyyyMMddHHmm.ss");
    private String mSuffix;

    public scriptWriter (Writer out, String suffix) {
    	super(out);
    	mSuffix = suffix;
    }

    public void addString(String opt, Object param) {
        if (param != null) {
            addOption(opt, param);
        }
    }

    private void addOption(String opt, Object param) {
        print(mSuffix);
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
    
    public void addString(String s) {
	print(mSuffix);
	print (" ");
	println(s);
    }
}