package org.gridlab.gat.resources.cpi.commandlineSshPrun;

import org.gridlab.gat.resources.Reservation;
import org.gridlab.gat.resources.Resource;

/**
 * An instance of this class represents a reservation for resources.
 */
public class CommandlineSshPrunReservation implements Reservation {
    /**
     * Constructs a CommandlineSshPrunReservation instance
     */
    public CommandlineSshPrunReservation() {
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
