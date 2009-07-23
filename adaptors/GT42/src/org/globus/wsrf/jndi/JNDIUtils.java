/*
 * Copyright 1999-2006 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.globus.wsrf.jndi;

import org.apache.axis.AxisEngine;
import org.apache.axis.Constants;
import org.apache.axis.MessageContext;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Rule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.naming.ContextBindings;
import org.apache.naming.SynchronizedContext;
import org.globus.tools.DeployConstants;
import org.globus.util.I18n;
import org.globus.wsrf.config.ContainerConfig;
import org.globus.wsrf.tools.jndi.*;
import org.globus.wsrf.utils.Resources;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Attributes;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.GT42InitialContext;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * A utility class containing methods for setting up the JNDI environment and
 * performing JNDI lookups.
 */
public class JNDIUtils {
    //TODO: most of these methods should be internal only
    private static Log logger =
            LogFactory.getLog(JNDIUtils.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public static final String JNDI_CONFIG = "jndi-config.xml";

    private static final String DEFAULT_CONTEXT_FACTORY =
            "org.globus.wsrf.jndi.javaURLContextFactory";

    private static final String CONTEXT_NAME = "wsrfContext";

    private static final String HOME =
            org.globus.wsrf.Constants.HOME_NAME.substring(1);

    private static final Hashtable ENV;

    static {
        ENV = new Hashtable();
        /*ENV.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                DEFAULT_CONTEXT_FACTORY);*/
        ENV.put("java.naming.factory.initialGT42",
                DEFAULT_CONTEXT_FACTORY);
    }

    private static Context initialContext = null;

    /**
     * Configure JNDI with the Apache Tomcat naming service classes and create
     * the <code>java:comp/env</code> context. The JNDI context will be
     * associated with the current thread context class loader.
     * <p/>
     * <i>This is an internal method and should not be called directly.</i>
     *
     * @return The initial context
     * @throws Exception
     */
    public static Context initJNDI()
            throws Exception {
        Context result = null;
        Context compContext = null;

        // set up naming

        String value = "org.apache.naming";
        String oldValue =
                System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);

        if (oldValue != null) {
            if (oldValue.startsWith(value + ":")) {
                value = oldValue;
            } else {
                value = value + ":" + oldValue;
            }
        }

        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, value);

    /*    if (value == null) {
            System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                    DEFAULT_CONTEXT_FACTORY);
        } else {
            logger.debug(i18n.getMessage("initialContextFactorySet", value));
        }
	*/
       
        
      /* System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                    DEFAULT_CONTEXT_FACTORY);*/
       
        System.setProperty("java.naming.factory.initialGT42",
                DEFAULT_CONTEXT_FACTORY);
       value = System.getProperty(
               "java.naming.factory.initialGT42");
     
       System.out.println("\n\n INITIAL_CONTEXT_FACTORY GT42: "+value+" \n\n"); 
          
       
           logger.debug(i18n.getMessage("initialContextFactorySet", value));
               
        
        Hashtable env = new Hashtable();
        env.put(SynchronizedContext.SYNCHRONIZED, "true");
     
        /*env.put("javax.naming.Context.INITIAL_CONTEXT_FACTORY",
                DEFAULT_CONTEXT_FACTORY);*/
        env.put("java.naming.factory.initialGT42",
                DEFAULT_CONTEXT_FACTORY);
        result = new GT42InitialContext(env);
        if (!ContextBindings.isClassLoaderBound()) {
            ContextBindings.bindContext(CONTEXT_NAME, result);
            ContextBindings.bindClassLoader(CONTEXT_NAME);
        }

        try {
            result.lookup("java:comp/env");
        }
        catch (NameNotFoundException e) {
            compContext = result.createSubcontext("comp");
            compContext.createSubcontext("env");
        }

        return result;
    }

    /**
     * Looks for a <code>home</code> object associated with the specified
     * service and calls <code>destroy</code> operation on it if it is
     * initialized and it implements the <code>Destroyable</code> interface.
     */
    public static void destroyHome(String serviceName)
            throws NamingException {
        Context initialContext = getInitialContext();
        Context servicesContext = (Context) initialContext.lookup(
                org.globus.wsrf.Constants.JNDI_SERVICES_BASE_NAME);
        NamingEnumeration enumeration = null;
        try {
            enumeration = servicesContext.listBindings(serviceName);
        } catch (NameNotFoundException e) {
            // that's ok, some services don't need JNDI entries
            return;
        }
        while (enumeration.hasMore()) {
            Binding binding = (Binding) enumeration.next();
            if (HOME.equals(binding.getName())) {
                Object object = binding.getObject();
                if (object instanceof Destroyable) {
                    try {
                        ((Destroyable) object).destroy();
                    } catch (RuntimeException e) {
                        logger.debug(i18n.getMessage("jndiDestroyFailed"), e);
                    }
                }
                break;
            }
        }
    }

    /**
     * Goes through the entire JNDI tree and calls <code>destroy</code>
     * operation on any object that is initialized and implements
     * the <code>Destroyable</code> interface. It skips any object
     * with the name <code>home</code>.
     * <p/>
     * <i>This is an internal method and should not be called directly.</i>
     */
    public static synchronized void destroyAll() {
        try {
            Context initialContext = JNDIUtils.getInitialContext();
            destroyAll(initialContext,
                    org.globus.wsrf.Constants.JNDI_BASE_NAME);
        } catch (NamingException e) {
            logger.debug(i18n.getMessage("jndiDestroyAllFailed"), e);
        }
    }

    private static void destroyAll(Context context, String name)
            throws NamingException {
        NamingEnumeration enumeration = context.listBindings(name);
        while (enumeration.hasMore()) {
            Binding binding = (Binding) enumeration.next();
            Object object = binding.getObject();
            if (object instanceof Context) {
                try {
                    destroyAll(context, name + "/" + binding.getName());
                } catch (NamingException e) {
                    logger.debug(i18n.getMessage("jndiDestroyAllFailed"), e);
                }
            } else if (!HOME.equals(binding.getName()) &&
                    object instanceof Destroyable) {
                try {
                    ((Destroyable) object).destroy();
                } catch (RuntimeException e) {
                    logger.debug(i18n.getMessage("jndiDestroyFailed"), e);
                }
            }
        }
    }

    /**
     * Destroys the JNDI context associated with the current ClassLoader
     * (must be called with the thread thas has the right context class
     * loader).
     * <p/>
     * <i>This is an internal method and should not be called directly.</i>
     */
    public static synchronized void destroy() {
        // call destroy() on Destroyable objects
        destroyAll();
        // unbind the context from class loader
        ContextBindings.unbindClassLoader(CONTEXT_NAME);
        ContextBindings.unbindContext(CONTEXT_NAME);
        // reset the context
        initialContext = null;
    }

    /**
     * Get the location of the JNDI configuration file from the deployment
     * descriptor
     *
     * @param messageContext The message context to use for discovering
     *                       deployment information
     * @return Location of JNDI configuration file relative to the root of the
     *         installation
     */
    public static String getJNDIConfigFileName(MessageContext messageContext) {
        String file = null;
        if (messageContext != null) {
            AxisEngine engine = messageContext.getAxisEngine();
            ContainerConfig config = ContainerConfig.getConfig(engine);
            file = config.getOption("jndiConfigFile");
        }
        return (file == null) ? "etc/" + JNDI_CONFIG : file;
    }

    /**
     * Parse the given JNDI configuration and populate the JNDI registry using
     * the parsed configuration
     *
     * @param configInput The configuration stream to parse
     * @throws Exception
     */
    public static void parseJNDIConfig(InputStream configInput)
            throws Exception {
        parseJNDIConfig(new InitialContext(), configInput, null);
    }

    /**
     * Parse the given JNDI configuration and populate the JNDI registry using
     * the parsed configuration
     *
     * @throws NamingException
     * @throws IOException
     * @throws SAXException
     */
    public static void parseJNDIConfig(Context initContext,
                                       InputStream configInput,
                                       AxisEngine engine)
            throws NamingException, IOException, SAXException {

        if (configInput == null) {
            throw new IllegalArgumentException(
                    i18n.getMessage("nullJNDIConfigInput"));
        }

        if (initContext == null) {
            throw new IllegalArgumentException();
        }

        Context envContext = (Context) initContext.lookup("java:comp/env");
        Digester digester = new Digester();

        // Don't do any validation for now
        // TODO: Need to write a real schema for this stuff

        digester.setNamespaceAware(true);
//        digester.setValidating(true);
        String ruleClassName = (org.apache.commons.digester.Rule.class).getName();
        DigesterDefinitionDiscoverer ddd = new DigesterDefinitionDiscoverer(digester);
        ddd.loadDefinitions();

        digester.push(new NamingContext(envContext, engine));
        digester.parse(configInput);
        digester.clear();
    }

    /*
     * Returns properly-initialized initial context.
     *
     * @return initial context
     */
    public static InitialContext getInitialContext()
            throws NamingException {
        return new InitialContext(ENV);
    }

    /**
     * Retrieves the named object on the specified context. The object returned
     * must be of assignable from the type specified.
     *
     * @param context the context to perform lookup on
     * @param name    the name of the object to lookup
     * @param type    the expected type of the object returned
     */
    public static Object lookup(
            Context context,
            String name,
            Class type)
            throws NamingException {
        if (context == null) {
            throw new IllegalArgumentException(i18n.getMessage(
                    "nullArgument", "context"));
        }
        if (type == null) {
            throw new IllegalArgumentException(i18n.getMessage(
                    "nullArgument", "type"));
        }
        Object tmp = context.lookup(name);
        if (type.isAssignableFrom(tmp.getClass())) {
            return tmp;
        } else {
            Object[] args =
                    new Object[]{type.getName(),
                            (tmp == null) ? null : tmp.getClass().getName()};
            throw new NamingException(i18n.getMessage("expectedType", args));
        }
    }

    private static class DirFilter implements FileFilter {
        public boolean accept(File path) {
            return path.isDirectory();
        }
    }

    /*
     * Returns true if JNDI registry is initialized, false otherwise.
     */
    public static synchronized boolean isInitialized() {
        return (initialContext != null);
    }

    /**
     * Initializes JNDI registry using multiple configuration files.
     * <p/>
     * <i>This is an internal method and should not be called directly.</i>
     */
    public static synchronized Context initializeDir(MessageContext msgCtx)
            throws Exception {
        if (initialContext == null) {
            Context context = initJNDI();

            String configProfile =
                    (String) msgCtx.getProperty(ContainerConfig.CONFIG_PROFILE);

            String configFile = (configProfile == null) ?
                    JNDI_CONFIG : configProfile + "-" + JNDI_CONFIG;

            String dir =
                    (String) msgCtx.getProperty(Constants.MC_CONFIGPATH);

            String configDir = (dir == null) ?
                    DeployConstants.CONFIG_BASE_DIR :
                    dir + File.separator + DeployConstants.CONFIG_BASE_DIR;

            File fDir = new File(configDir);
            File[] dirs = fDir.listFiles(new DirFilter());
            for (int i = 0; i < dirs.length; i++) {
                processJNDIFile(context, dirs[i],
                        msgCtx.getAxisEngine(), configFile);
            }

            initialContext = context;
        }

        return initialContext;
    }

    private static void processJNDIFile(Context context,
                                        File dir,
                                        AxisEngine engine,
                                        String configFile)
            throws Exception {
        File file = new File(dir, configFile);
        if (!file.exists() || !file.canRead()) {
            return;
        }

        logger.debug("Loading jndi configuration from file: " + file);

        InputStream in = null;
        try {
            in = new FileInputStream(file);
            parseJNDIConfig(context, in, engine);
        }
        catch (NamingException e) {
            throw e;
        }
        catch (SAXParseException e) {
            Integer column = new Integer(e.getColumnNumber());
            Integer line = new Integer(e.getLineNumber());
            NamingException ex =
                    new NamingException(i18n.getMessage(
                            "jndiConfigParseError",
                            new Object[]{file,
                                    column,
                                    line,
                                    e.getMessage()}));
            ex.setRootCause(e);
            throw ex;
        }
        catch (Exception e) {
            NamingException ex =
                    new NamingException(i18n.getMessage("jndiConfigReadError",
                            file));
            ex.setRootCause(e);
            throw ex;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                }
            }
        }
    }

    /**
     * Initializes JNDI registry using a single configuration file.
     * <p/>
     * <i>This is an internal method and should not be called directly.</i>
     */
    public static synchronized Context initializeFile(MessageContext msgCtx)
            throws Exception {
        if (initialContext == null) {
            Context context = initJNDI();

            InputStream configInput;
            String configFileName =
                    JNDIUtils.getJNDIConfigFileName(msgCtx);
            try {
                String cfgDir = null;
                if (msgCtx == null) {
                    cfgDir = ContainerConfig.getGlobusLocation();
                } else {
                    cfgDir = (String) msgCtx.getProperty(Constants.MC_CONFIGPATH);
                    if (cfgDir == null) {
                        cfgDir = ".";
                    }
                }
                String file = cfgDir + File.separator + configFileName;
                logger.debug(
                        "Trying to load jndi configuration from file: " +
                                file);

                configInput = new FileInputStream(file);
            }
            catch (FileNotFoundException e) {
                logger.debug(
                        "Trying to load jndi configuration from resource stream: " + configFileName);

                configInput =
                        JNDIUtils.class.getClassLoader().getResourceAsStream(
                                configFileName
                        );

                if (configInput == null) {
                    throw new IOException(
                            i18n.getMessage("jndiConfigNotFound"));
                }
            }

            parseJNDIConfig(context, configInput, msgCtx.getAxisEngine());

            initialContext = context;
        }

        return initialContext;
    }

    public static String toString(Context ctx, String name)
            throws NamingException {
        StringBuffer buf = new StringBuffer();
        toString(buf, ctx, name, "");
        return buf.toString();
    }

    private static void toString(StringBuffer buf, Context ctx,
                                 String name, String tab)
            throws NamingException {
        buf.append(tab).append("context: ").append(name).append("\n");
        NamingEnumeration list = ctx.list(name);
        while (list.hasMore()) {
            NameClassPair nc = (NameClassPair) list.next();
            if (nc.getClassName().equals("org.apache.naming.NamingContext") ||
                    nc.getClassName().equals("org.apache.naming.SynchronizedContext")) {
                toString(buf, ctx, name + "/" + nc.getName(), tab + "  ");
            } else {
                buf.append(tab).append(" ").append(nc).append("\n");
            }
        }
    }

    /**
     * Create all intermediate subcontexts.
     */
    public static Context createSubcontexts(Context currentContext,
                                            String name)
            throws NamingException {
        StringTokenizer tokenizer = new StringTokenizer(name, "/");

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ((!token.equals("")) && (tokenizer.hasMoreTokens())) {
                try {
                    currentContext = currentContext.createSubcontext(token);
                }
                catch (NamingException e) {
                    // Silent catch. Probably an object is already bound in
                    // the context.
                    currentContext = (Context) currentContext.lookup(token);
                }
            }
        }

        return currentContext;
    }

    protected static class CreateResourceRuleFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            String factory = attributes.getValue("factory");
            String type = attributes.getValue("type");
            Rule resourceRule = null;
            if (factory != null) {
                resourceRule = new ResourceRule(factory, type);
            } else {
                resourceRule = new ResourceRule(type);
            }
            return resourceRule;
        }
    }

    protected static class AddResourceParameterRuleFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) {
            String name = attributes.getValue("name");
            return new org.globus.wsrf.tools.jndi.ResourceParameterRule(name);
        }
    }


}
