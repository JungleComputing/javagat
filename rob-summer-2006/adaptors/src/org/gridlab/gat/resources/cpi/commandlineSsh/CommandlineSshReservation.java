package org.gridlab.gat.resources.cpi.commandlineSsh;

import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;

/**
 * An instance of this class represents a reservation for resources.
 */
public class CommandlineSshReservation implements Reservation {
    /**
     * Constructs a CommandlineSshReservation instance
     */
    public CommandlineSshReservation() {
        super();
    }

    /**
     * This method upon successfully completing cancels the reservation
     * corresponding to the associated Reservation instance.
     *
     */
    public void cancel() {
        throw new Error("not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gridlab.gat.resources.Reservation#getResource()
     */
    public Resource getResource() {
        throw new Error("not implemented");
    }
}
