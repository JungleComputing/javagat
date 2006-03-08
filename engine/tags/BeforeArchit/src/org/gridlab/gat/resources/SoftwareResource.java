/*
 * Created on Apr 22, 2004
 */
package org.gridlab.gat.resources;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.resources.cpi.SoftwareResourceCpi;

/**
 * @author rob
 */
public abstract class SoftwareResource implements Resource {

	GATContext gatContext;

	Preferences preferences;

	protected SoftwareResource(GATContext gatContext, Preferences preferences) {
		this.gatContext = gatContext;
		this.preferences = preferences;
	}

	public static SoftwareResource create(GATContext gatContext) {
		return create(gatContext, null);
	}

	public static SoftwareResource create(GATContext gatContext,
			Preferences preferences) {
		GATEngine gatEngine = GATEngine.getGATEngine();

		SoftwareResourceCpi s = (SoftwareResourceCpi) gatEngine.getAdaptor(
				SoftwareResourceCpi.class, gatContext, preferences, null);
		return s;
	}
}