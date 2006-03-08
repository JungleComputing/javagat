/*
 * Created on May 19, 2004
 */
package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Resource;
import org.gridlab.gat.resources.SoftwareDescription;
import org.gridlab.gat.resources.SoftwareResourceDescription;

/**
 * @author rob
 */
public class DefaultJobDescriptionAdaptor extends JobDescriptionCpi {

	/**
	 * @param gatContext
	 * @param softwareDescription
	 * @param resourceDescription
	 */
	public DefaultJobDescriptionAdaptor(GATContext gatContext,
			Preferences preferences, SoftwareDescription softwareDescription,
			HardwareResourceDescription resourceDescription) {
		super(gatContext, preferences, softwareDescription, resourceDescription);
	}

	public DefaultJobDescriptionAdaptor(GATContext gatContext,
			Preferences preferences, SoftwareDescription softwareDescription,
			SoftwareResourceDescription resourceDescription) {
		super(gatContext, preferences, softwareDescription, resourceDescription);
	}

	/**
	 * @param gatContext
	 * @param softwareDescription
	 * @param resource
	 */
	public DefaultJobDescriptionAdaptor(GATContext gatContext,
			Preferences preferences, SoftwareDescription softwareDescription,
			Resource resource) {
		super(gatContext, preferences, softwareDescription, resource);
	}
}