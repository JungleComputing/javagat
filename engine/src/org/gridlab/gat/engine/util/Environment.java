/*
 * Created on Jul 5, 2005 by rob
 */
package org.gridlab.gat.engine.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * This class allows you to access environment variables. It is for internal gat
 * use.
 * 
 * @author rob
 */
public class Environment {
    private HashMap<String, String> env = new HashMap<String, String>();

    public Environment() {
        String command = "env";

        Process p = null;

        try {
            p = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            return;
        }

        try {
            p.getOutputStream().close();
        } catch (Throwable e) {
            // ignore
        }

        // we must always read the output and error streams to avoid deadlocks
        OutputForwarder output = new OutputForwarder(p.getInputStream(), true);
        new OutputForwarder(p.getErrorStream(), true);

        String envList = output.getResult().toString();
        StringTokenizer st = new StringTokenizer(envList, "\n");

        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            int pos = line.indexOf("=");

            if (pos < 0) {
                continue;
            }

            String key = line.substring(0, pos);
            String val = line.substring(pos + 1, line.length());
            env.put(key, val);
        }
    }

    /** Gets the value of an environment variable */
    public String getVar(String var) {
        return (String) env.get(var);
    }
}
