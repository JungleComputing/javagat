package org.gridlab.gat.resources;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.resources.cpi.HardwareResourceCpi;

/**
 * An instance of this interface is an abstract representation of a physical
 * hardware resource which is monitorable.
 * <p>
 * An instance of this interface presents an abstract, system-independent view
 * of a physical hardware resource which is monitorable. Various systems use
 * system-dependent means of representing a physical hardware resource. GAT,
 * however, uses an instance of this interface as an operating system
 * independent description of a physical hardware resource which is monitorable.
 * <p>
 * An instance of this interface allows on to examine the various properties of
 * the physical hardware resource to which this instance corresponds. In
 * addition is allows one to monitor the physical hardware resource to which
 * this instance corresponds.
 */
public abstract class HardwareResource implements Resource {
	GATContext gatContext;

	Preferences preferences;

	protected HardwareResource(GATContext gatContext, Preferences preferences) {
		this.gatContext = gatContext;
		this.preferences = preferences;
	}

	public static HardwareResource create(GATContext gatContext) {
		return create(gatContext, null);
	}

	public static HardwareResource create(GATContext gatContext,
			Preferences preferences) {
		GATEngine gatEngine = GATEngine.getGATEngine();
		HardwareResourceCpi h = (HardwareResourceCpi) gatEngine.getAdaptor(
				HardwareResourceCpi.class, gatContext, preferences, null);
		return h;
	}
}