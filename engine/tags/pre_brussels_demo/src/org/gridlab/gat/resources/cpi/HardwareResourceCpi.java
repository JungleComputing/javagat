/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.resources.HardwareResource;

/**
 * @author rob
 */
public abstract class HardwareResourceCpi extends HardwareResource {
	protected HardwareResourceCpi(GATContext gatContext, Preferences preferences) {
		super(gatContext, preferences);
	}
}