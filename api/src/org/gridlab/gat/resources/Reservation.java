package org.gridlab.gat.resources;

import org.gridlab.gat.GATInvocationException;

/**
 * An instance implementing this interface represents a reservation for
 * {@link Resource}s.
 * 
 * @author rob
 */
public interface Reservation {
    /**
     * This method upon successfully completing cancels the reservation
     * corresponding to the associated {@link Reservation} instance.
     * 
     */
    public void cancel() throws GATInvocationException;

    /**
     * This operation returns the {@link Resource} corresponding to this
     * {@link Reservation} instance. That instance can in turn call the
     * operation <code>getReservation</code> to obtain this
     * {@link Reservation} instance.
     * 
     * @return The {@link Resource}, or <code>null</code> when the
     *         {@link Reservation} is expired or canceled.
     */
    public Resource getResource();
}
