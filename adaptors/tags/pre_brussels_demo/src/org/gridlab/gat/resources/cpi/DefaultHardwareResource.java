package org.gridlab.gat.resources.cpi;

import java.util.Hashtable;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.advert.Advertisable;
import org.gridlab.gat.resources.HardwareResource;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.ResourceDescription;

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
public class DefaultHardwareResource extends HardwareResource {

	/**
	 * Constructs a DefaultHardwareResource
	 */
	public DefaultHardwareResource(GATContext gatContext,
			Preferences preferences) {
		super(gatContext, preferences);
	}

	/**
	 * Gets the HardwareResourceDescription which describes this
	 * HardwareResource instance.
	 * 
	 * @return A HardwareResourceDescription which describes this
	 *         HardwareResource instance.
	 */
	public ResourceDescription getResourceDescription() {
		Hashtable hashtable = new Hashtable();

		return new HardwareResourceDescription(hashtable);
	}

	public Reservation getReservation() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.advert.Advertisable#marshal()
	 */
	public String marshal() {
		throw new Error("Not implemented");
	}
	/* (non-Javadoc)
	 * @see org.gridlab.gat.advert.Advertisable#unmarshal(java.lang.String)
	 */
	public Advertisable unmarshal(String input) {
		throw new Error("Not implemented");
	}
}