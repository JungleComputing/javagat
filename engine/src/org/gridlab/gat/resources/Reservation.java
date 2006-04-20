package org.gridlab.gat.resources;

import java.io.IOException;
import java.rmi.RemoteException;

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
     * @throws java.rmi.RemoteException
     *             Thrown upon problems accessing the remote instance
     * @throws java.io.IOException
     *             Upon non-remote IO problem
     */
    public void cancel() throws RemoteException, IOException;

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
