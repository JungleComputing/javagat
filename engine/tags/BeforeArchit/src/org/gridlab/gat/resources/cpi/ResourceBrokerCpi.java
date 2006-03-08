package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.ResourceBroker;

/**
 * Capability provider interface to the ResourceBroker class.
 * <p>
 * Capability provider wishing to provide the functionality of the
 * ResourceBroker class must extend this class and implement all of the abstract
 * methods in this class. Each abstract method in this class mirrors the
 * corresponding method in this ResourceBroker class and will be used to
 * implement the corresponding method in the ResourceBroker class at runtime.
 */
public abstract class ResourceBrokerCpi implements ResourceBroker {

	GATContext gatContext;

	Preferences preferences;

	/**
	 * This method constructs a ResourceBrokerCpi instance corresponding to the
	 * passed GATContext.
	 * 
	 * @param gatContext
	 *            A GATContext which will be used to broker resources
	 */
	protected ResourceBrokerCpi(GATContext gatContext, Preferences preferences)
			throws Exception {
		this.gatContext = gatContext;
		this.preferences = preferences;
	}
}