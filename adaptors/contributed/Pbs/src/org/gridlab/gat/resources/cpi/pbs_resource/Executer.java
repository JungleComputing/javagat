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

/**
 * @author  doerl
 */
public class Executer implements IParameter {
    private static final Map<String, String> ARCH_TYPES = new HashMap<String, String>();
//    private static final String sPbsBin;
    private static String[] sExport;

    static {
        String appPath = System.getenv(PATH);
        if (appPath == null) {
            appPath = "/bin:/usr/bin:/usr/X11R6/bin";
        }
        String pbsExec = System.getenv(PBS_EXEC);
        if (pbsExec == null) {
            pbsExec = "/usr/local/pbs";
        }
        String pbsHome = System.getenv(PBS_HOME);
        if (pbsHome == null) {
            pbsHome = "/var/spool/pbs";
        }
        
        Vector<String> exp = new Vector<String>();
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

    public static Vector<String> allResults(String command) throws IOException {
        Vector<String> result = new Vector<String>();
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

    public static Object getArch(Map<String, String> res) {
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

    public static Map<String, String> getProperties(Vector<String> lines, int ch) {
        HashMap<String, String> result = new HashMap<String, String>();
        for (Enumeration<String> elem = lines.elements(); elem.hasMoreElements();) {
            String line = (String) elem.nextElement();
            int pos = line.indexOf(ch);
            if (pos >= 0) {
                result.put(line.substring(0, pos).trim(), line.substring(pos + 1).trim());
            }
        }
        return result;
    }

    public static Map<String, Object> getPropertiesForm(Vector<String> lines, int ch) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        String lastKey = null;
        for (Enumeration<String> elem = lines.elements(); elem.hasMoreElements();) {
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

    public static void setExport(Vector<String> export) {
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
