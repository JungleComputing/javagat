package org.gridlab.gat.resources.cpi;

import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;

/**
 * An instance of this class represents a reservation for resources.
 */
public class DefaultReservation implements Reservation {
	/**
	 * Constructs a DefaultReservation instance
	 */
	public DefaultReservation() {
		super();
	}

	/**
	 * This method upon successfully completing cancels the reservation
	 * corresponding to the associated Reservation instance.
	 *  
	 */
	public void cancel() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gridlab.gat.resources.Reservation#getResource()
	 */
	public Resource getResource() {
		// TODO Auto-generated method stub
		return null;
	}
}