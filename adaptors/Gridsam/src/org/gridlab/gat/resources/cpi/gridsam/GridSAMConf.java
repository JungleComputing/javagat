package org.gridlab.gat.resources.cpi.gridsam;

public class GridSAMConf {
    private static final String JAVAGAT_STDIN = ".javaGATInput";

    private static final String JAVAGAT_STDOUT = ".javaGATOutput";

    private static final String JAVAGAT_STDERR = ".javaGATStderr";

    public String getJavaGATStdin() {
        return JAVAGAT_STDIN;
    }

    public String getJavaGATStdout() {
        return JAVAGAT_STDOUT;
    }

    public String getJavaGATStderr() {
        return JAVAGAT_STDERR;
    }
}
