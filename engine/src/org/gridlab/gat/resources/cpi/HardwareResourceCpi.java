/*
 * Created on Apr 22, 2004
 *
 */
package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.monitoring.cpi.MonitorableCpi;
import org.gridlab.gat.resources.HardwareResource;

/**
 * @author rob
 */
public abstract class HardwareResourceCpi extends MonitorableCpi implements HardwareResource {
    protected GATContext gatContext;
    protected HardwareResourceCpi(GATContext gatContext) {
        this.gatContext = gatContext;
    }
}
