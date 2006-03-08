package org.gridlab.gat.resources;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * An instance implementing this interface represents a reservation for resources.
 */
public interface Reservation
{
   /**
    * This method upon successfully completing cancels the reservation 
    * corresponding to the associated Reservation instance.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract void cancel() throws RemoteException, IOException;
}
