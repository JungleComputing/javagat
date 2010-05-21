/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.jndi;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;

import org.apache.naming.ContextBindings;
import org.apache.naming.SynchronizedContext;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.axis.Constants;

import org.globus.tools.DeployConstants;
import org.globus.wsrf.config.ContainerConfig;
import org.globus.wsrf.tools.jndi.JNDIConfigRuleSet;
import org.globus.wsrf.utils.Resources;
import org.globus.util.I18n;

import org.xml.sax.SAXException;

/**
 * A utility class containing methods for setting up the JNDI environment and
 * performing JNDI lookups
 */
public class JNDIUtils
{
    //TODO: most of these methods should be internal only
    private static Log logger =
        LogFactory.getLog(JNDIUtils.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public static final String JNDI_CONFIG = "jndi-config.xml";

    private static Context initialContext = null;

    /**
     * Configure JNDI with the Apache Tomcat naming service classes and create
     * the comp and env contexts
     *
     * @return The initial context
     * @throws Exception
     */
    public static Context initJNDI()
        throws Exception
    {
        Context result = null;
        Context compContext = null;

        // set up naming

        String value = "org.apache.naming";
        String oldValue =
            System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);

        if(oldValue != null)
        {
            if(oldValue.startsWith(value + ":"))
            {
                value = oldValue;
            }
            else
            {
                value = value + ":" + oldValue;
            }
        }

        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, value);

        // Commented out the code below. JavaGAT does not set the system property, but instead
        // now uses a jndi.properties file. This allows us to have a GT42 adaptor as well. --Ceriel
        /*
        value = System.getProperty(
            javax.naming.Context.INITIAL_CONTEXT_FACTORY);

        if(value == null)
        {
            System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                               "org.apache.naming.java.javaURLContextFactory");
        }
        else
        {
            logger.debug(i18n.getMessage("initialContextFactorySet", value));
        }
        */

        Hashtable env = new Hashtable();
        env.put(SynchronizedContext.SYNCHRONIZED, "true");

        result = new InitialContext(env);
        if(!ContextBindings.isClassLoaderBound())
        {
            ContextBindings.bindContext("wsrfContext", result);
            ContextBindings.bindClassLoader("wsrfContext");
        }

        try
        {
            result.lookup("java:comp/env");
        }
        catch(NameNotFoundException e)
        {
            compContext = result.createSubcontext("comp");
            compContext.createSubcontext("env");
        }
        return result;
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
    public static String getJNDIConfigFileName(MessageContext messageContext)
    {
        String file = null;
        if(messageContext != null)
        {
            AxisEngine engine = messageContext.getAxisEngine();
            ContainerConfig config = ContainerConfig.getConfig(engine);
            file = config.getOption(i18n.getMessage("jndiConfigFileOption"));
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
        throws Exception
    {
        parseJNDIConfig(new InitialContext(), configInput, null);
    }

    /**
     * Parse the given JNDI configuration and populate the JNDI registry using
     * the parsed configuration
     *
     * @param configInput The configuration stream to parse
     * @throws NamingException
     * @throws IOException
     * @throws SAXException
     */
    public static void parseJNDIConfig(Context initContext, 
                                       InputStream configInput,
                                       AxisEngine engine)
        throws NamingException, IOException, SAXException 
    {

        if (configInput == null)
        {
            throw new IllegalArgumentException(
                i18n.getMessage("nullJNDIConfigInput"));
        }

        if (initContext == null)
        {
            throw new IllegalArgumentException();
        }

        Context envContext = (Context) initContext.lookup("java:comp/env");
        Digester digester = new Digester();

        // Don't do any validation for now
        // TODO: Need to write a real schema for this stuff

        digester.setNamespaceAware(true);
        digester.setValidating(false);
        digester.addRuleSet(new JNDIConfigRuleSet("jndiConfig/"));
        digester.push(new NamingContext(envContext, engine));
        digester.parse(configInput);
        digester.clear();
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
        throws NamingException
    {
        if(context == null)
        {
            throw new IllegalArgumentException(i18n.getMessage(
                "nullArgument", "context"));
        }
        if(type == null)
        {
            throw new IllegalArgumentException(i18n.getMessage(
                "nullArgument", "type"));
        }
        Object tmp = context.lookup(name);
        if(type.isAssignableFrom(tmp.getClass()))
        {
            return tmp;
        }
        else
        {
            Object[] args = 
                new Object[] {type.getName(),
                              (tmp == null) ? null : tmp.getClass().getName()};
            throw new NamingException(i18n.getMessage("expectedType", args));
        }
    }

    private static class DirFilter implements FileFilter {
        public boolean accept(File path) {
            return path.isDirectory();
        }
    }

    // multiple file configuration
    public static synchronized Context initializeDir(MessageContext msgCtx) 
        throws Exception 
    {
        if (initialContext == null)
        {
            Context context = initJNDI();

            String configProfile = 
                (String)msgCtx.getProperty(ContainerConfig.CONFIG_PROFILE);

            String configFile = (configProfile == null) ? 
                JNDI_CONFIG : configProfile + "-" + JNDI_CONFIG;

            String dir =
                (String)msgCtx.getProperty(Constants.MC_CONFIGPATH);

            String configDir = (dir == null) ?
                DeployConstants.CONFIG_BASE_DIR :
                dir + File.separator + DeployConstants.CONFIG_BASE_DIR;
            
            File fDir = new File(configDir);
            File [] dirs = fDir.listFiles(new DirFilter());
            for (int i = 0; i < dirs.length; i++) 
            {
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
        throws Exception 
    {
        File file = new File(dir, configFile);
        if (!file.exists()) 
        {
            return;
        }

        logger.debug("Loading jndi configuration from file: " + file);

        InputStream in = null;
        try
        {
            in = new FileInputStream(file);
            parseJNDIConfig(context, in, engine);
        } 
        catch (NamingException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            NamingException ex = 
                new NamingException(i18n.getMessage("jndiConfigParseError",
                                                    file));
            ex.setRootCause(e);
            throw ex;
        }
        finally 
        {
            if (in != null) 
            {
                try 
                {
                    in.close();
                } 
                catch (IOException e) 
                {
                }
            }
        }
    }

    // single file configuration
    public static synchronized Context initializeFile(MessageContext msgCtx) 
        throws Exception 
    {
        if (initialContext == null)
        {
            Context context = initJNDI();

            InputStream configInput;
            String configFileName =
                JNDIUtils.getJNDIConfigFileName(msgCtx);
            try
            {
                String cfgDir = null;
                if (msgCtx == null) 
                {
                    cfgDir = ContainerConfig.getGlobusLocation();
                } 
                else 
                {
                    cfgDir = (String)msgCtx.getProperty(Constants.MC_CONFIGPATH);
                    if (cfgDir == null) 
                    {
                        cfgDir = ".";
                    }
                }
                String file = cfgDir + File.separator + configFileName;
                logger.debug(
                        "Trying to load jndi configuration from file: " +
                        file);

                configInput = new FileInputStream(file);
            }
            catch (FileNotFoundException e)
            {
                logger.debug(
                             "Trying to load jndi configuration from resource stream: " + configFileName);

                configInput =
                    JNDIUtils.class.getClassLoader().getResourceAsStream(
                                                      configFileName
                                                      );

                if (configInput == null)
                {
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
        throws NamingException
    {
        StringBuffer buf = new StringBuffer();
        toString(buf, ctx, name, "");
        return buf.toString();
    }
        
    private static void toString(StringBuffer buf, Context ctx, 
                                 String name, String tab)
        throws NamingException
    {
        buf.append(tab).append("context: ").append(name).append("\n");
        NamingEnumeration list = ctx.list(name);
        while (list.hasMore()) 
        {
            NameClassPair nc = (NameClassPair)list.next();
            if (nc.getClassName().equals("org.apache.naming.NamingContext") ||
                nc.getClassName().equals("org.apache.naming.SynchronizedContext"))
            {
                toString(buf, ctx, name + "/" + nc.getName(), tab + "  ");
            } 
            else 
            {
                buf.append(tab).append(" ").append(nc).append("\n");
            }
        }
    }
    
    /**
     * Create all intermediate subcontexts.
     */
    public static Context createSubcontexts(Context currentContext,
                                            String name)
        throws NamingException 
    {
        StringTokenizer tokenizer = new StringTokenizer(name, "/");

        while(tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            if((!token.equals("")) && (tokenizer.hasMoreTokens()))
            {
                try
                {
                    currentContext = currentContext.createSubcontext(token);
                }
                catch(NamingException e)
                {
                    // Silent catch. Probably an object is already bound in
                    // the context.
                    currentContext = (Context) currentContext.lookup(token);
                }
            }
        }

        return currentContext;
    }

}
