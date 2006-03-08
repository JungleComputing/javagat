/*
 * Created on Apr 22, 2004
 *  
 */
package org.gridlab.gat.resources;

import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * @author rob
 * 
 * Resource is a base interface which is realized by any class which wishes to
 * indicate it represents a node or component; currently both a
 * GATHardwareResource and a SoftwareResource realize this interface. A
 * Reservation may be associated with this Resource, and can be obtained by the
 * operation getReservation.
 */

public interface Resource extends Monitorable, Advertisable,
		java.io.Serializable {

	/**
	 * @return The GATResourceDescription which describes this Resource
	 *         instance.
	 */
	public ResourceDescription getResourceDescription();

	/**
	 * @return a Reservation associated with this resource, or null of no
	 *         reservation was associated.
	 */
	public Reservation getReservation();
}