package org.gridlab.gat.resources.cpi.gt42;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.configuration.FileProvider;

/**
 * This subclass of {@link EngineConfigurationFactory} refers to the
 * client-config.wsdd that is shipped with the Globus adaptor of GAT.
 * 
 * @author Stefan Bozic
 */
public class GlobusEngineConfigurationFactory implements
        EngineConfigurationFactory {

    /**
     * Factory method that creates a new instance of
     * {@link EngineConfigurationFactory}.
     * 
     * @param param
     *            a factory parameter
     * @return a new instance of this class
     */
    public static EngineConfigurationFactory newFactory(Object param) {
        return new GlobusEngineConfigurationFactory();
    }

    /**
     * @see EngineConfigurationFactory#getClientEngineConfig()
     */
    public EngineConfiguration getClientEngineConfig() {
        String wsddPath = System.getProperty("gat.adaptor.path")
                + java.io.File.separator + "GT42Adaptor"
                + java.io.File.separator + "client-config.wsdd";

        FileProvider cfg = new FileProvider(wsddPath);

        return cfg;
    }

    /**
     * @see EngineConfigurationFactory#getServerEngineConfig()
     */
    public EngineConfiguration getServerEngineConfig() {
        return null;
    }
}
