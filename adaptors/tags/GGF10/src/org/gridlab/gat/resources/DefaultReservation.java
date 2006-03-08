package org.gridlab.gat.resources;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * An instance of this class represents a reservation for resources.
 */
public class DefaultReservation implements Reservation
{
   /**
    * Constructs a DefaultReservation instance
    */
    public DefaultReservation()
    {
        super();
    }
    
   /**
    * This method upon successfully completing cancels the reservation 
    * corresponding to the associated Reservation instance.
    *
    */
    public void cancel()
    {
    }
}