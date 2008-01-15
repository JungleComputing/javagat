/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources;

import java.util.Map;

/**
 * @author rob
 * 
 * An instance of this class is a description of a piece of software (component)
 * which is to be submitted as a job. It currently takes a table describing this
 * piece of software's attributes to any underlying job submission system.
 * 
 * The following attributes are defined in the specification and should be
 * recognized by ResourceBroker adaptors.
 * 
 * <ul>
 * <li> directory (String): working directory.
 * <li> count (Integer/String): number of executables to run.
 * <li> hostCount (Integer/String): number of hosts to distribute on.
 * <li> maxTime (Long/String): The maximum walltime or cputime for a single
 * execution of the executable. The units is in minutes.
 * <li> maxWallTime (Long/String): maximal WALL time in minutes.
 * <li> maxCPUTime (Long/String): maximal CPU time in minutes.
 * <li> jobType (String): single|multiple|mpi|condor|...
 * <li> queue (String): target queue name.
 * <li> project (String): project to use, for accounting purposes.
 * <li> dryRun (Boolean/String): if set, dont submit but return success.
 * <li> minMemory (Integer/String): minimal required memory in MB.
 * <li> maxMemory (Integer/String): maximal required memory in MB.
 * <li> saveState (Boolean/String): keep job data persistent for restart.
 * <li> restart=ID (String): restart job with given ID.
 * </ul>
 * 
 * Several JavaGAT adaptors also have support for running Java programs. This
 * works as follows: The "location" (the executable) can be set to
 * "java:my.ClassName", where my.Classname is the fully qualified name of your
 * class that contains main. You can put the classpath in the attributes, with
 * as key "java.classpath", and as value the literal string you would use as
 * argument to the -classpath option to the JVM. If you do not set a classpath,
 * JavaGAT will set it to all files in your prestaged file set that have a .jar
 * extension. Furthermore, all variables in the environment will be passed to
 * the JVM as -D options. JavaGAT also has to know which JVM to use, you can set
 * this by providing a "java.home" key in the attributes. The value of java.home
 * must be a URI. Finally, you can specify a "java.flags" attribute to pass
 * options to the JVM itself, like "-server" or "-Xmx800M".
 * 
 */
@SuppressWarnings("serial")
public class JavaSoftwareDescription extends SoftwareDescription {
    
    private String[] options;
    private String[] systemProperties;
    private String main;
    private String[] javaArguments;
    
    /**
     * Create a software description, which describes the application you want
     * to run.
     * 
     */
    public JavaSoftwareDescription() {
        super();
    }

    /**
     * Create a software description, which describes the application you want
     * to run.
     * 
     * @param attributes
     *                See the comment above for a list of known attributes.
     */
    @SuppressWarnings("unchecked")
    public JavaSoftwareDescription(Map<String, Object> attributes) {
        super(attributes);
        main = (String) attributes.get("java.main");
        options = (String[]) attributes.get("java.options");
        systemProperties = (String[]) attributes.get("java.system.properties");
        javaArguments = (String[]) attributes.get("java.arguments");
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String[] getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(String[] systemProperties) {
        this.systemProperties = systemProperties;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String[] getJavaArguments() {
        return javaArguments;
    }

    public void setJavaArguments(String[] javaArguments) {
        this.javaArguments = javaArguments;
    }
    
    public void setArguments(String[] arguments) {
    }
    
    public String[] getArguments() {
        String result[] = new String[options.length + systemProperties.length + 1 + javaArguments.length];
        int pos= 0;
        for (String option : options) {
            result[pos++] = option; 
        }
        for (String systemProperty : systemProperties) {
            result[pos++] = "-D" + systemProperty;
        }
        result[pos++] = main;
        for (String javaArgument: javaArguments) {
            result[pos++] = javaArgument;
        }
        return result;
    }
    
    public String getExecutable() {
        if (super.getExecutable() == null) {
            return "java";
        } else {
            return super.getExecutable();
        }
    }

}
