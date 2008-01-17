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
 * <li> java.system.properties (String[]): the java system properties {"a=b", "c=d"}
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

    public String getExecutable() {
        if (super.getExecutable() == null) {
            return "java";
        } else {
            return super.getExecutable();
        }
    }

}
