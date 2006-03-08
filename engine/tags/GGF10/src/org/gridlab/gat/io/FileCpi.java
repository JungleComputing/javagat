package org.gridlab.gat.io;

import java.io.IOException;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.net.Location;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * Capability provider interface to the File class.
 * <p>
 * Capability provider wishing to provide the functionality 
 * of the File class must extend this class and implement 
 * all of the abstract methods in this class. Each abstract 
 * method in this class mirrors the corresponding method 
 * in this File class and will be used to implement the 
 * corresponding method in the File class at runtime. 
 */
public abstract class FileCpi implements Monitorable
{    
   protected Location location = null;
   protected GATContext gatcontext = null;
   
   /**
    * Constructs a FileCpi instance which corresponds to the physical file
    * identified by the passed Location and whose access rights are
    * determined by the passed GATContext. 
    *
    * @param location A Location which represents the URI corresponding 
    * to the physical file.
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this FileCpi.
    */
    public FileCpi(Location location, GATContext gatContext)
    {
        super();
        
        this.location = location;
        this.gatcontext = gatcontext;           
    }
        
   /**
    * Tests this FileCpi for equality with the passed Object.
    * <p>
    * If the given object is not a FileCpi, then this method immediately
    * returns false.
    * <p>
    * If the given object is a FileCpi, then it is deemed equal to this
    * instance if a Location object constructed from this FileCpi's location
    * and a Location object constructed from the passed FileCpi's Location are
    * equal as determined by the Equals method of Location. 
    *
    * @param object The Object to test for equality
    * @return A boolean indicating equality
    */
    public abstract boolean equals(Object object);
    
   /**
    * This method returns the Location of this FileCpi
    *
    * @return The Location of this FileCpi
    */
    public Location getLocation()
    {
        return location;
    }
        
   /**
    * This method copies the physical file represented by this 
    * FileCpi instance to a physical file identified by the passed 
    * Location.
    *
    * @param loc The new location
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract void copy(Location loc) throws IOException, RemoteException;
    
   /**
    * This method moves the physical file represented by this 
    * FileCpi instance to a physical file identified by the passed 
    * Location.
    *
    * @param location The Location to which to move the physical 
    * file corresponding to this FileCpi instance
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance    
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public abstract void move(Location location) throws IOException, RemoteException;
        
   /**
    * This method deletes the physical file represented by this 
    * FileCpi instance.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract void delete() throws RemoteException;
        
   /**
    * Returns a boolean indicating if the physical file corresponding
    * to this instance is readable.
    *
    * @return A boolean indicating readability
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract boolean isReadable() throws RemoteException;
    
   /**
    * Returns a boolean indicating if the physical file corresponding 
    * to this instance is writable.
    *
    * @return A boolean indicating writability
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract boolean isWritable() throws RemoteException;
    
   /**
    * Returns a long indicating the length in bytes of the physical 
    * file corresponding to this instance.
    *
    * @return A long indicating the length in bytes of the physical 
    * file corresponding to this instance.
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract long getLength() throws RemoteException;
    
   /**
    * Returns the number of milliseconds after January 1, 1970, 
    * 00:00:00 GMT at which this file was last modified.
    *
    * @return A long indicating the number of milliseconds after 
    * January 1, 1970, 00:00:00 GMT at which this file was last 
    * modified.
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public abstract long lastModified() throws RemoteException;
}