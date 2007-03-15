/*
 * MPA Source File: Executer.java Copyright (c) 2003-2005 by MPA Garching
 *
 * $Created:	08.11.2005 (15:18:45) by doerl $ Last Change: 08.11.2005 (15:18:45) by doerl
 */
package org.gridlab.gat.resources.cpi.pbs_resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.gridlab.gat.util.Environment;

/**
 * @author  doerl
 */
public class Executer implements IParameter {
    private static final Map ARCH_TYPES = new HashMap();
//    private static final String sPbsBin;
    private static String[] sExport;

    static {
        Environment env = new Environment();
        String appPath = (env.getVar(PATH) != null) ? env.getVar(PATH)
            : "/bin:/usr/bin:/usr/X11R6/bin";
        String pbsExec = (env.getVar(PBS_EXEC) != null) ? env.getVar(PBS_EXEC) : "/usr/local/pbs";
        String pbsHome = (env.getVar(PBS_HOME) != null) ? env.getVar(PBS_HOME) : "/var/spool/pbs";
        Vector exp = new Vector();
        exp.add(PBS_EXEC + "=" + pbsExec);
        exp.add(PBS_HOME + "=" + pbsHome);
        exp.add(PATH + "=" + appPath);
        exp.add("LD_LIBRARY_PATH=" + pbsExec + "/lib/");
        setExport(exp);
//        sPbsBin = pbsExec + "/bin/";
        ARCH_TYPES.put(IUname.OS_LINUX, IResources.PBS_LINUX);
        ARCH_TYPES.put(IUname.OS_SOLARIS + "." + IUname.TYPE_SPARC, IResources.PBS_SOLARIS5);
        ARCH_TYPES.put(IUname.OS_SOLARIS + "." + IUname.TYPE_AMD64, IResources.PBS_SOLARIS7);
        ARCH_TYPES.put(IUname.OS_AIX, IResources.PBS_AIX);
        ARCH_TYPES.put(IUname.OS_HP_UX, IResources.PBS_HP);
    }

    public static Vector allResults(String command) throws IOException {
        Vector result = new Vector();
        BufferedReader br = null;
        try {
//             Process proc = Runtime.getRuntime().exec(sPbsBin + command, sExport);
            Process proc = Runtime.getRuntime().exec(command, sExport);
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String text;
            while ((text = br.readLine()) != null) {
                result.addElement(text);
            }
        }
        finally {
            if (br != null) {
                br.close();
            }
        }
        return result;
    }

    public static Object getArch(Map res) {
        Object arch = null;
        Object osName = res.get("os.name");
        Object osType = res.get("os.type");
        if (osType != null) {
            arch = Executer.ARCH_TYPES.get(osName + "." + osType);
        }
        if (arch == null) {
            arch = Executer.ARCH_TYPES.get(osName);
        }
        return arch;
    }

    public static Map getProperties(Vector lines, int ch) {
        HashMap result = new HashMap();
        for (Enumeration elem = lines.elements(); elem.hasMoreElements();) {
            String line = (String) elem.nextElement();
            int pos = line.indexOf(ch);
            if (pos >= 0) {
                result.put(line.substring(0, pos).trim(), line.substring(pos + 1).trim());
            }
        }
        return result;
    }

    public static Map getPropertiesForm(Vector lines, int ch) {
        HashMap result = new HashMap();
        String lastKey = null;
        for (Enumeration elem = lines.elements(); elem.hasMoreElements();) {
            String line = (String) elem.nextElement();
            if (line.length() == 0) {
                continue;
            }
            if (!line.startsWith("\t")) {
                int pos = line.indexOf(ch);
                if (pos >= 0) {
                    lastKey = line.substring(0, pos).trim();
                    result.put(lastKey, line.substring(pos + 1).trim());
                }
            }
            else if (lastKey != null) {
                String lastVal = (String) result.get(lastKey);
                result.put(lastKey, lastVal + line.trim());
            }
        }
        return result;
    }

    public static void setExport(Vector export) {
        if (export.size() > 0) {
            sExport = (String[]) export.toArray(new String[export.size()]);
        }
        else {
            sExport = null;
        }
    }

    public static String singleResult(String command) throws IOException {
        String result = null;
        BufferedReader br = null;
        try {
            //            Process proc = Runtime.getRuntime().exec(sPbsBin + command, sExport);
            Process proc = Runtime.getRuntime().exec(command, sExport);
            proc.waitFor();
            if (proc.exitValue() == 0) {
                br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                result = br.readLine();
            }
            else {
                br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                result = br.readLine();
                throw new IOException("rejected: " + result);
            }
        }
        catch (InterruptedException ex) {
            throw new IOException("process was interupted");
        }
        finally {
            if (br != null) {
                br.close();
            }
        }
        return result;
    }
}
