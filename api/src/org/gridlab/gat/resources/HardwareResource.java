package org.gridlab.gat.resources;

import org.gridlab.gat.monitoring.Monitorable;

/**
 * An instance of this interface is an abstract representation of a physical
 * hardware resource which is {@link Monitorable}.
 * <p>
 * An instance of this interface allows on to examine the various properties of
 * the physical hardware resource to which this instance corresponds. In
 * addition, it allows one to monitor the physical hardware resource to which
 * this instance corresponds.
 */
public interface HardwareResource extends Resource {

}
