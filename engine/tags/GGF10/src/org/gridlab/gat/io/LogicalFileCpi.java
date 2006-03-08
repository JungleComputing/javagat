package org.gridlab.gat.io;

import java.util.List;
import java.io.IOException;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.net.Location;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * Capability provider interface to the LogicalFile class.
 * <p>
 * Capability provider wishing to provide the functionality
 * of the LogicalFile class must extend this class and
 * implement all of the abstract methods in this class. Each
 * abstract method in this class mirrors the corresponding 
 * method in this LogicalFile class and will be used to 
 * implement the corresponding method in the LogicalFile 
 * class at runtime.
 */
public abstract class LogicalFileCpi implements Monitorable
{
   protected Location location = null;
   protected GATContext gatContext = null;
   
   /**
    * This constructor creates a LogicalFileCpi corresponding to the passed
    * Location instance and uses the passed GATContext to broker
    * resources. 
    *
    * @param location The Location of one physical file in this LogicalFileCpi
    * @param gatContext The GATContext used to broker resources
    */
    public LogicalFileCpi(Location location, GATContext gatContext)
    {
        this.location = location;
        this.gatContext = gatContext;      
    }
        
   /**
    * Adds the passed File instance to the set of physical files represented
    * by this LogicalFileCpi instance.
    *
    * @param file A File instance to add to the set of physical files represented
    * by this LogicalFileCpi instance.
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void addFile(File file) throws RemoteException;
    
   /**
    * Adds the physical file at the passed Location to the set of physical
    * files represented by this LogicalFileCpi instance.
    *
    * @param location The Location of a physical file to add to the set of 
    * physical files represented by this LogicalFileCpi instance.
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void addLocation(Location location) throws RemoteException; 
    
   /**
    * Removes the passed File instance from the set of physical files represented
    * by this LogicalFileCpi instance.
    *
    * @param file A File instance to remove from the set of physical files represented
    * by this LogicalFileCpi instance.
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void removeFile(File file) throws RemoteException;
    
   /**
    * Removes the physical file at the passed Location from the set of physical
    * files represented by this LogicalFileCpi instance.
    *
    * @param location The Location of a physical file to remove from the set of 
    * physical files represented by this LogicalFileCpi instance.
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void removeLocation(Location location) throws RemoteException;
    
   /**
    * Replicates the logical file represented by this instance to the
    * physical file specified by the passed Location.
    *
    * @param location The Location of the new physical file
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract void replicate(Location location) throws RemoteException, IOException;
    
   /**
    * Returns a java.util.List of Location instances each of which is the
    * Location of a physical file represented by this instance.
    *
    * @return The java.util.List of Locations
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract List getLocations() throws RemoteException;
    
   /**
    * Returns a java.util.List of File instances each of which is a
    * File corresponding to a physical file represented by this instance.
    *
    * @return The java.util.List of Locations
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract List getFiles() throws RemoteException;   
}