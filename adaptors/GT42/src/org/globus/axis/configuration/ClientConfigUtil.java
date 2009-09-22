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
package org.globus.axis.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.axis.ConfigurationException;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.DirProvider;
import org.apache.axis.configuration.EngineConfigurationFactoryFinder;
import org.apache.axis.utils.Admin;
import org.apache.axis.utils.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.tools.DeployConstants;
import org.globus.wsrf.config.ContainerConfig;
import org.w3c.dom.Document;

public class ClientConfigUtil 
{
    protected static Log log =
        LogFactory.getLog(ClientConfigUtil.class.getName());
    
    private static String dir = ContainerConfig.getGlobusLocation() +
            File.separator + DeployConstants.CONFIG_BASE_DIR;    
    
    //I changed the value of the fileName variable look up to the right file
    private static final String fileName = "client-configGT42.wsdd";
    //private static final String fileName = "client-config.wsdd";
    private static final String filePath = 
        ContainerConfig.getGlobusLocation() + File.separator + fileName;

    private static EngineConfiguration engineConfig = null;     
    
    public static EngineConfiguration getClientEngineConfig() 
    {        
        return EngineConfigurationFactoryFinder.
                newFactory().getClientEngineConfig();
    }         
    
    public static EngineConfiguration getDefaultClientEngineConfig() 
    {
        if (engineConfig == null) {
            engineConfig = getClientEngineConfig();
        }
        return engineConfig;
    } 
    
    public static EngineConfiguration getClientEngineConfig(String baseDir,
                                                            String configFile) 
    {        
        DirProvider config = null;
        try {
            config = new DirProvider(baseDir, configFile);
        } catch (ConfigurationException e) {
            log.error("Exception while processing client-configGT42.wsdd files", e);
        }        
        return config;
    }
    
    public static void writeConfiguration(String configFile)
        throws ConfigurationException
    {
        if (configFile == null) {
            configFile = filePath;
        }
        
        AxisClient client = 
            new AxisClient(getClientEngineConfig(dir, fileName));
        client.init();
        
        try {
            Document doc = Admin.listConfig(client);
            Writer osWriter = new OutputStreamWriter(
                    new FileOutputStream(configFile),XMLUtils.getEncoding());
            PrintWriter writer = new PrintWriter(new BufferedWriter(osWriter));
            XMLUtils.DocumentToWriter(doc, writer);
            writer.println();
            writer.close();
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }             
    }
    
    public static void main(String[] args) 
    {
        String file = ((args != null) && (args.length>0)) ? args[0] : null;
        try {
            writeConfiguration(file);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
