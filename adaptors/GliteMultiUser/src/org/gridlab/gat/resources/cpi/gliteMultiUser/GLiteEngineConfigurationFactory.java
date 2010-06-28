package org.gridlab.gat.resources.cpi.gliteMultiUser;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.EngineConfigurationFactory;
import org.apache.axis.configuration.FileProvider;

/**
 * This subclass of {@link EngineConfigurationFactory} refers to client-config.wsdd that was shipped with the globus
 * adaptor of GAT.
 * 
 * @author Stefan Bozic
 */
public class GLiteEngineConfigurationFactory implements EngineConfigurationFactory {

	/**
	 * Factory method that creates a new instance of {@link EngineConfigurationFactory}.
	 * 
	 * @param param
	 *            a factory parameter
	 * @return a new instance of this class
	 */
	public static EngineConfigurationFactory newFactory(Object param) {
		return new GLiteEngineConfigurationFactory();
	}

	/**
	 * @see EngineConfigurationFactory#getClientEngineConfig()
	 */
	public EngineConfiguration getClientEngineConfig() {
		String wsddPath = "client-config.wsdd";

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
