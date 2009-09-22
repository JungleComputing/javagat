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
package org.globus.wsrf.security.authorization;

/*
 * I just changed the value of the field AUTHZ_CONFIG_FILE to avoid to keep
 * the etc/globus_wsrf_core/authz-algorithm-config file inside the main folder of
 * javaGAT. Now it is possible to keep it inside the external folder.
 * */

import java.security.Provider;
import java.security.Security;

import java.util.Set;
import java.util.Iterator;
import java.util.Properties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.globus.wsrf.config.ContainerConfig;
import org.globus.wsrf.config.ConfigException;
import org.globus.wsrf.security.authorization.client.Authorization;

import org.globus.util.I18n;

import org.globus.util.ClassLoaderUtils;  

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provider class that initializes authorization provider from a
 * configuration file. The configuration is read from 
 * GLOBUS_LOCATION/etc/globus_wsrf_core/authz-algorithm-config
 */ 
public class AuthorizationProvider extends Provider {

    private static I18n i18n =
        I18n.getI18n(Authorization.RESOURCE,
                     AuthorizationProvider.class.getClassLoader());

    private static Log logger =
        LogFactory.getLog(AuthorizationProvider.class.getName());

    public static final String AUTHZ_CONFIG_PROPERTY = 
        "authzAlgorithmConfig";
    // I changed the value of this string
    public static final String AUTHZ_CONFIG_FILE = System.getProperty("gat.adaptor.path")
    + java.io.File.separator + "GT42Adaptor"
    + java.io.File.separator+"etc/globus_wsrf_core/authz-algorithm-config";
    //public static final String AUTHZ_CONFIG_FILE ="etc/globus_wsrf_core/authz-algorithm-config";
    public static final String PROVIDER_NAME = "Globus Authz";
    public static final String PROVIDER_INFO = 
        "Globus Authz Combining Algorithms";
    public static final double PROVIDER_VERSION = 4.2;

    private static AuthorizationProvider authzProvider = null;

    public synchronized static void initialize() throws ConfigException {
        if (authzProvider == null) {
            authzProvider = new AuthorizationProvider();
            Security.addProvider(authzProvider);
        }
    }

    private AuthorizationProvider() throws ConfigException {

        super(PROVIDER_NAME, PROVIDER_VERSION, PROVIDER_INFO);
        String authzFileName = System.getProperty(AUTHZ_CONFIG_PROPERTY);
        if (authzFileName == null) {
            authzFileName = AUTHZ_CONFIG_FILE;
        }
        
        InputStream input = null;
        File file = new File(authzFileName);
        if (file.isAbsolute()) {
            try {
            	  input = new FileInputStream(file);
            } catch (FileNotFoundException exp) {
                String err = i18n.getMessage("authzConfigFile", authzFileName);
                throw new ConfigException(err, exp);
            }
        } else {
            if (!file.exists()) {
                input = ClassLoaderUtils.getResourceAsStream(authzFileName);
         
                if (input == null) {
                    String configPath = ContainerConfig.getBaseDirectory();
                    System.out.println("base directory  "+configPath);
                    if (configPath == null) {
                        configPath = ".";
                    }
                    file = new File(configPath, authzFileName);
                    try {
                        input = new FileInputStream(file);
                    } catch (FileNotFoundException exp) {
                        String err = i18n.getMessage("authzConfigFile", 
                                                     file.getAbsolutePath());
                        throw new ConfigException(err, exp);
                    }
                }
            } else {
                try {
                    input = new FileInputStream(file);
                } catch (FileNotFoundException exp) {
                    String err = i18n.getMessage("authzConfigFile", 
                                                 authzFileName);
                    throw new ConfigException(err, exp);
                }
            }
        }

        if (input == null) {
            logger.warn("No authorization provider configured");
            return;
        }
        Properties properties = new Properties();
        try {
            properties.load(input);
        } catch (IOException ioe) {
            String err = i18n.getMessage("authzConfigLoadError", 
                                         new Object[] { authzFileName });
            logger.error(err, ioe);
            throw new ConfigException(err, ioe);
        }
        
        Set set = properties.keySet();
        if (set != null) {
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                String name = (String)iterator.next();
                String className = properties.getProperty(name);
                logger.debug("Added: " + name + " " + className);
                setProperty(name, className);
            }
        }
        logger.debug("Added authz providers");
    }
}
