package org.gridlab.gat.resources;

import org.gridlab.gat.GATInvocationException;

/**
 * @author rob
 */
/** An instance implementing this interface represents a reservation for resources.
 */
public interface Reservation {
    /**
     * This method upon successfully completing cancels the reservation
     * corresponding to the associated Reservation instance.
     *
     */
    public void cancel() throws GATInvocationException;

    /**
     * This operation returns the GATResource corresponding to this
     * GATReservation instance. That instance can in turn call the operation
     * GetReservation to obtain this GATReservation instance.
     *
     * @return The Resource, or null when the reservation is expired or
     *         cancelled.
     */
    public Resource getResource();
}
