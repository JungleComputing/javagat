package org.gridlab.gat.resources.cpi.unicore6;

import org.gridlab.gat.GATContext;
import org.gridlab.gat.resources.HardwareResourceDescription;
import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.ResourceDescription;
import org.gridlab.gat.resources.cpi.HardwareResourceCpi;

/**
 * Describes a Site Resource in Unicore.
 * 
 * @author Andreas Bender
 * 
 */
public class Unicore6SiteResource extends HardwareResourceCpi {

	private HardwareResourceDescription resourceDescription = new HardwareResourceDescription();

	/**
	 * generated serial version UID
	 */
	private static final long serialVersionUID = 2863105745387082397L;

	/**
	 * Constructor.
	 * 
	 * @param gatContext
	 */
	public Unicore6SiteResource(GATContext gatContext) {
		super(gatContext);
	}

	/**
	 * @see HardwareResourceCpi#getResourceDescription()
	 */
	@Override
	public ResourceDescription getResourceDescription() {
		return resourceDescription;
	}

	/**
	 * @see HardwareResourceCpi#getReservation()
	 */
	@Override
	public Reservation getReservation() {
		return null;
	}

	/**
	 * @see HardwareResourceCpi#marshal()
	 */
	@Override
	public String marshal() {
		return null;
	}

}
