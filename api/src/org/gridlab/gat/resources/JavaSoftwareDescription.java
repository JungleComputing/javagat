/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An instance of this class is a description of a piece of java software
 * (component) which is to be submitted as a job.
 * <p>
 * The {@link JavaSoftwareDescription} is tailored to Java jobs. Although, it is
 * possible to submit a java job with the regular {@link SoftwareDescription},
 * the {@link JavaSoftwareDescription} is a convenience object that makes it
 * easier and more straight forward to describe a java job. Besides the
 * {@link JavaSoftwareDescription} being a convenience object, some JavaGAT
 * adaptors may only accept {@link SoftwareDescription}s of the type
 * {@link JavaSoftwareDescription}.
 * <p>
 * The following example shows the relation between the
 * {@link SoftwareDescription} and the {@link JavaSoftwareDescription}.
 * Consider the command line:
 * <p>
 * <code>/path/to/java -classpath a:b:c/d -Xoption1 -Dmykey=myvalue my.package.Main arg1 arg2</code>
 * <p>
 * The {@link SoftwareDescription} needs this invocations to construct this
 * command line:
 * <p>
 * <code>
 * setExecutable("/path/to/java"); <br>
 * setArguments(new String[]{"-classpath", "a:b:c/d", "-Xoption1", "-Dmykey=myvalue", "my.package.Main",
 * "arg1", "arg2"});</code>
 * <p>
 * The {@link JavaSoftwareDescription} needs the following invocations for the
 * same command line:
 * <p>
 * <code>
 * setExecutable("/path/to/java"); <br>
 * setJavaClassPath("a:b:c/d"); <br> 
 * setJavaOptions(new String[]{"-Xoption1}); <br>
 * Map<String, String> systemProperties = new HashMap<String, String>(); <br>
 * systemProperties.put("mykey", "myvalue"); <br>
 * setJavaSystemProperties(systemProperties); <br>
 * setJavaMain("my.package.Main"); <br>
 * setJavaArguments(new String[]{"arg1", "arg2"}); </code>
 * 
 * 
 * @author roelof
 */
@SuppressWarnings("serial")
public class JavaSoftwareDescription extends SoftwareDescription {
    /**
     * Attribute name: indicates the main class that should be executed.
     * See {@link #JavaSoftwareDescription(Map)}.
     */
    public static final String JAVA_MAIN = "java.main";
    
    /**
     * Attribute name: indicates the jvm options for this java application.
     * See {@link #JavaSoftwareDescription(Map)}.
     */
    public static final String JAVA_OPTIONS = "java.options";
    
    /**
     * Attribute name: indicates the java system properties.
     * See {@link #JavaSoftwareDescription(Map)}.
     */    
    public static final String JAVA_SYSTEM_PROPERTIES = "java.system.properties";
    
    /**
     * Attribute name: indicates the java arguments for the main class of
     * the java application.
     * See {@link #JavaSoftwareDescription(Map)}.
     */   
    public static final String JAVA_ARGUMENTS = "java.arguments";    

    private String[] javaOptions;

    private Map<String, String> javaSystemProperties;

    private String javaMain;

    private String[] javaArguments;

    private String javaClassPath;

    /**
     * Create a {@link JavaSoftwareDescription}, which describes the java
     * application.
     */
    public JavaSoftwareDescription() {
        super();
    }
    
    JavaSoftwareDescription(JavaSoftwareDescription jd) {
	super(jd);
	javaOptions = jd.javaOptions.clone();
	javaSystemProperties = new HashMap<String, String>(jd.javaSystemProperties);
	javaMain = jd.javaMain;
	javaArguments = jd.javaArguments.clone();
	javaClassPath = jd.javaClassPath;
    }
    
    public Object clone() {
	return new JavaSoftwareDescription(this);
    }

    /**
     * Create a {@link JavaSoftwareDescription} with the provided
     * <code>attributes</code>, which describes the java application. See
     * {@link SoftwareDescription} for a list of well known attributes. Besides
     * this list the {@link JavaSoftwareDescription} knows these attributes:
     * <p>
     * <TABLE border="2" frame="box" rules="groups" summary="Minimum set of
     * supported attributes"> <CAPTION>supported name/value pairs of
     * JavaSoftwareDescription</CAPTION> <COLGROUP align="left"> <COLGROUP
     * align="center"> <COLGROUP align="left" > <THEAD valign="top">
     * <TR>
     * <TH>Name
     * <TH>Type
     * <TH>Description <TBODY>
     * <TR>
     * <TD>java.main
     * <TD>{@link String}
     * <TD>the main class that should be executed
     * <TR>
     * <TD>java.options
     * <TD>{@link String}[]
     * <TD>the jvm options for this java application
     * <TR>
     * <TD>java.system.properties
     * <TD>{@link Map}<{@link String}, {@link String}>
     * <TD>the java system properties
     * <TR>
     * <TD>java.arguments
     * <TD>{@link String}[]
     * <TD>the java arguments for the main class of the java application
     * <TR></TBODY> </TABLE>
     * 
     * @param attributes
     *                the attributes belonging to this
     *                {@link JavaSoftwareDescription}.
     */
    @SuppressWarnings("unchecked")
    public JavaSoftwareDescription(Map<String, Object> attributes) {
        super(attributes);
        javaMain = (String) attributes.get(JAVA_MAIN);
        setJavaOptions((String[]) attributes.get(JAVA_OPTIONS));
        javaSystemProperties = (Map<String, String>) attributes
                .get(JAVA_SYSTEM_PROPERTIES);
        javaArguments = (String[]) attributes.get(JAVA_ARGUMENTS);
        checkArguments();
    }
    
    private void checkArguments() {
        if (javaArguments != null) {
            // Check the individual arguments.
            for (int i = 0; i < javaArguments.length; i++) {
                if (javaArguments[i] == null) {
                    throw new NullPointerException("Argument " + i + " is null");
                }
            }
        }
    }

    /**
     * Returns the jvm options.
     * 
     * @return the jvm options.
     */
    public String[] getJavaOptions() {
        return javaOptions;
    }

    /**
     * Sets the jvm options.
     * 
     * @param options
     *                the jvm options.
     */
    public void setJavaOptions(String... options) {
        javaOptions = options;
    }

    /**
     * Returns the java system properties.
     * 
     * @return the java system properties.
     */
    public Map<String, String> getJavaSystemProperties() {
        return javaSystemProperties;
    }

    /**
     * Sets the system properties. A system property should be passed as a key
     * value pair <"a", "b">, not as <"-Da", "b">, JavaGAT will add the -D to
     * the property.
     * 
     * @param systemProperties
     *                the system properties.
     */
    public void setJavaSystemProperties(Map<String, String> systemProperties) {
        this.javaSystemProperties = systemProperties;
    }

    /**
     * Adds a system property to the current set of system properties. The key
     * of the system property should not start with "-D".
     * 
     * @param key
     *                the key of the system property to be added
     * @param value
     *                the value belonging to the key of the system property to
     *                be added
     */
    public void addJavaSystemProperty(String key, String value) {
        if (javaSystemProperties == null) {
            javaSystemProperties = new HashMap<String, String>();
        }
        javaSystemProperties.put(key, value);
    }

    /**
     * Returns the main class of the java application.
     * 
     * @return the main class.
     */
    public String getJavaMain() {
        return javaMain;
    }

    /**
     * Sets the main class.
     * 
     * @param main
     *                the main class.
     */
    public void setJavaMain(String main) {
        this.javaMain = main;
    }

    /**
     * Returns the arguments for the main class.
     * 
     * @return the arguments for the main class
     */
    public String[] getJavaArguments() {
        return javaArguments;
    }

    /**
     * Sets the arguments of the java main class.
     * 
     * @param javaArguments
     *                the arguments of the java main class.
     */
    public void setJavaArguments(String... javaArguments) {
        this.javaArguments = javaArguments;
        checkArguments();
    }

    /**
     * <b>This method should not be used</b>. This method will ignore all
     * arguments. The methods {@link #setJavaClassPath(String)},
     * {@link #setJavaOptions(String[])}, {@link #setJavaSystemProperties(Map)},
     * {@link #setJavaMain(String)} and {@link #setJavaArguments(String[])}
     * should be used to construct the command line arguments.
     * 
     * @param arguments
     */
    public void setArguments(String... arguments) {
    }

    /**
     * Constructs the command line arguments from the class path, the jvm
     * options, the system properties, the main and the java arguments of this
     * {@link SoftwareDescription}.
     * 
     * @return the command line arguments
     */
    public String[] getArguments() {
        if (toWrapper) {
            return super.getArguments();
        }
        ArrayList<String> result = new ArrayList<String>();
        if (getJavaOptions() != null) {
            for (String option : getJavaOptions()) {
                result.add(option);
            }
        }
        if (getJavaClassPath() != null) {
            result.add("-classpath");
            result.add(getJavaClassPath());
        }

        if (getJavaSystemProperties() != null) {
            Map<String, String> properties = getJavaSystemProperties();
            for (String key : properties.keySet()) {
                // null values ignored
                if (properties.get(key) != null) {
                    result.add("-D" + key + "=" + properties.get(key));
                }
            }
        }
        if (getJavaMain() != null) {
            result.add(getJavaMain());
        } else {
            return null;
        }
        if (getJavaArguments() != null) {
            for (String javaArgument : getJavaArguments()) {
                result.add(javaArgument);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the executable. If no executable is set the default executable
     * will be "java".
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

    /**
     * Returns the java class path.
     * 
     * @return the java class path.
     */
    public String getJavaClassPath() {
        return javaClassPath;
    }

    /**
     * Sets the java class path.
     * 
     * @param javaClassPath
     *                the class path to be set.
     */
    public void setJavaClassPath(String javaClassPath) {
        this.javaClassPath = javaClassPath;
    }

}
