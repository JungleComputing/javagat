package org.gridlab.gat.resources.hardware;

import org.gridlab.gat.monitoring.Monitorable;

/**
 * An instance of this interface is an abstract representation of a physical
 * hardware resource which is monitorable.
 * <p>
 * An instance of this interface presents an abstract, system-independent
 * view of a physical hardware resource which is monitorable. Various
 * systems use system-dependent means of representing a physical hardware
 * resource. GAT, however, uses an instance of this interface as an operating
 * system independent description of a physical hardware resource which
 * is monitorable.
 * <p>
 * An instance of this interface allows on to examine the various properties
 * of the physical hardware resource to which this instance
 * corresponds. In addition is allows one to monitor the physical
 * hardware resource to which this instance corresponds.
 */
public interface HardwareResource extends Monitorable
{
   /**
    * Gets the HardwareResourceDescription which describes this
    * HardwareResource instance.
    *
    * @return A HardwareResourceDescription which describes this 
    * HardwareResource instance.
    */
    public abstract HardwareResourceDescription  getHardwareResourceDescription();
}
