/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources;

import java.util.Map;

/**
 * @author rob
 * 
 * An instance of this class is a description of a piece of java software
 * (component) which is to be submitted as a job.
 * 
 * The JavaSoftwareDescription is tailored to Java jobs. It is possible to
 * submit a java job with the regular SoftwareDescription, the
 * JavaSoftwareDescription is convenience object that makes it easier and more
 * straight forward to describe a java job. Besides the JavaSoftwareDescription
 * being a convenience object, some JavaGAT adaptors may only accept
 * SoftwareDescriptions of the type JavaSoftwareDescription.
 * 
 * <ul>
 * <li> java.main (String): the fully quantified name of your class
 * <li> java.options (String[]): the jvm options
 * <li> java.system.properties (String[]): the java system properties {"a=b",
 * "c=d"}
 * <li> java.arguments(String[]): The arguments for your main class
 * </ul>
 */
@SuppressWarnings("serial")
public class JavaSoftwareDescription extends SoftwareDescription {

    private String[] options;
    private String[] systemProperties;
    private String main;
    private String[] javaArguments;

    /**
     * Create a java software description, which describes the application you
     * want to run.
     * 
     */
    public JavaSoftwareDescription() {
        super();
    }

    /**
     * Create a java software description, which describes the application you
     * want to run.
     * 
     * @param attributes
     *                See the comment above for a list of known attributes.
     */
    @SuppressWarnings("unchecked")
    public JavaSoftwareDescription(Map<String, Object> attributes) {
        super(attributes);
        main = (String) attributes.get("java.main");
        setOptions((String[]) attributes.get("java.options"));
        systemProperties = (String[]) attributes.get("java.system.properties");
        javaArguments = (String[]) attributes.get("java.arguments");
    }

    /**
     * @return Returns the jvm options.
     */
    public String[] getOptions() {
        return options;
    }

    /**
     * Set the jvm options. Note that any option starting with -D or "-D will be
     * ignored. These should be set using the setSystemProperties
     * 
     * @param options
     *                the jvm options.
     */
    public void setOptions(String[] options) {
        int systemProperties = 0;
        for (String option : options) {
            if (option.startsWith("-D") || option.startsWith("\"-D")) {
                systemProperties++;
            }
        }
        int pos = 0;
        this.options = new String[options.length - systemProperties];
        for (String option : options) {
            this.options[pos++] = option;
        }
    }

    /**
     * @return Returns the java system properties.
     */
    public String[] getSystemProperties() {
        return systemProperties;
    }

    /**
     * Set the system properties. A system property should be passed as a=b, not
     * as -Da=b, JavaGAT will add the -D to the property.
     * 
     * @param systemProperties
     *                the system properties.
     */
    public void setSystemProperties(String[] systemProperties) {
        this.systemProperties = systemProperties;
    }

    /**
     * @return Returns the main class.
     */
    public String getMain() {
        return main;
    }

    /**
     * Set the main class.
     * 
     * @param main
     *                the main class.
     */
    public void setMain(String main) {
        this.main = main;
    }

    /**
     * @return Returns the arguments for the main class
     */
    public String[] getJavaArguments() {
        return javaArguments;
    }

    /**
     * Set the arguments of the java main class
     * 
     * @param javaArguments
     *                the arguments of the java main class.
     */
    public void setJavaArguments(String[] javaArguments) {
        this.javaArguments = javaArguments;
    }

    /**
     * This method should not be used, and will ignore all arguments. The
     * methods setOptions, setSystemProperties, setMain and setJavaArguments
     * should be used to construct the command line arguments.
     * 
     * @param arguments
     *                the command line arguments
     */
    public void setArguments(String[] arguments) {
    }

    /**
     * This method constructs the command line arguments from the jvm options,
     * the system properties, the main and the java arguments of this
     * SoftwareDescription.
     * 
     * @return the command line arguments
     */
    public String[] getArguments() {
        String result[] = new String[options.length + systemProperties.length
                + 1 + javaArguments.length];
        int pos = 0;
        for (String option : options) {
            result[pos++] = option;
        }
        for (String systemProperty : systemProperties) {
            result[pos++] = "-D" + systemProperty;
        }
        result[pos++] = main;
        for (String javaArgument : javaArguments) {
            result[pos++] = javaArgument;
        }
        return result;
    }

    /**
     * Returns the executable. If no executable is set the default executable
     * will be "java"
     * 
     * @return Returns the executable.
     */
    public String getExecutable() {
        if (super.getExecutable() == null) {
            return "java";
        } else {
            return super.getExecutable();
        }
    }

}
