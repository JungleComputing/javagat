package org.gridlab.gat.io;

import java.util.List;
import java.io.IOException;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.net.Location;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * An abstract representation of a physical file.
 * <p>
 * An instance of this class presents an abstract, system-independent
 * view of a physical file. User interfaces and operating systems use
 * system-dependent pathname strings to identify physical files. GAT,
 * however, uses an operating system independent pathname string to
 * identify a physical file. A physical file in GAT is identified by a
 * Location.
 * <p>
 * An instance of this File class allows for various high-level
 * operations to be preformed on a physical file. For example, one can,
 * with a single API call, copy a physical file from one location to a
 * second location, move a physical file from one location to a second
 * location, delete a physical file, and preform various other operations
 * on a physical file. The utility of this high-level view of a physical
 * file is multi-fold. The client of an instance of this class does not
 * have to concern themselves with the details of reading every single
 * byte of a physical file when all they wish to do is copy the physical
 * file to a new location. Similarly, a client does not have to deal with
 * all the various error states that can occur when moving a physical
 * file ( Have all the various bytes been read correctly? Have all the
 * various bytes been saved correctly? Did the deletion of the original
 * file proceed correctly? ); the client simply has to call a single API
 * call and the physical file is moved.
 */
public class File implements Monitorable
{
    private FileCpi fileCpi = null;
    
   /**
    * Constructs a File instance which corresponds to the physical file
    * identified by the passed Location and whose access rights are
    * determined by the passed GATContext. 
    *
    * @param location A Location which represents the URI corresponding 
    * to the physical file.
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this File.
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public File(Location location, GATContext gatContext) throws Exception
    {            
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[2];
        array[0] = location;
        array[1] = gatContext;
                
        fileCpi = (FileCpi) gatEngine.constructCpiClass(FileCpi.class, array);
    }
    
   /**
    * Constructs a File instance which corresponds to the physical file
    * identified by the passed Location and whose access rights are
    * determined by the passed GATContext. 
    *
    * @param location A Location which represents the URI corresponding 
    * to the physical file.
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this File.
    * @param preferences A Preferences which is used to determine the user's 
    * preferences for this File.
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public File(Location location, GATContext gatContext, Preferences preferences) throws Exception
    {            
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[2];
        array[0] = location;
        array[1] = gatContext;
                
        fileCpi = (FileCpi) gatEngine.constructCpiClass(FileCpi.class, preferences, array);
    }   
    
   /**
    * Tests this File for equality with the passed Object.
    * <p>
    * If the given object is not a File, then this method immediately
    * returns false.
    * <p>
    * If the given object is a File, then it is deemed equal to this
    * instance if a Location object constructed from this File's location
    * and a Location object constructed from the passed File's Location are
    * equal as determined by the Equals method of Location. 
    *
    * @param object The Object to test for equality
    * @return A boolean indicating equality
    */
    public boolean equals(Object object)
    {
        return fileCpi.equals(object);
    }
    
   /**
    * This method returns the Location of this File
    *
    * @return The Location of this File
    */
    public Location getLocation()
    {
        return fileCpi.getLocation();
    }
    
   /**
    * This method copies the physical file represented by this 
    * File instance to a physical file identified by the passed 
    * Location.
    *
    * @param loc The new location
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void copy(Location loc) throws IOException, RemoteException
    {
        fileCpi.copy(loc);
    }
    
   /**
    * This method moves the physical file represented by this 
    * File instance to a physical file identified by the passed 
    * Location.
    *
    * @param location The Location to which to move the physical 
    * file corresponding to this File instance
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance    
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void move(Location location) throws IOException, RemoteException
    {
        fileCpi.move(location);
    }
        
   /**
    * This method deletes the physical file represented by this 
    * File instance.
    *
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void delete() throws RemoteException
    {
       fileCpi.delete();
    }
    
   /**
    * Returns a boolean indicating if the physical file corresponding
    * to this instance is readable.
    *
    * @return A boolean indicating readability
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public boolean isReadable() throws RemoteException
    {
        return fileCpi.isReadable();
    }
    
   /**
    * Returns a boolean indicating if the physical file corresponding 
    * to this instance is writable.
    *
    * @return A boolean indicating writability
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public boolean isWritable() throws RemoteException
    {
        return fileCpi.isWritable();
    }
    
   /**
    * Returns a long indicating the length in bytes of the physical 
    * file corresponding to this instance.
    *
    * @return A long indicating the length in bytes of the physical 
    * file corresponding to this instance.
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public long getLength() throws RemoteException
    {
        return fileCpi.getLength();
    }
    
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
    public long lastModified() throws RemoteException
    {
        return fileCpi.lastModified();
    }
    
   /**
    * This method adds the passed instance of a MetricListener to the java.util.List
    * of MetricListeners which are notified of MetricEvents by an
    * instance of this class. The passed MetricListener is only notified of
    * MetricEvents which correspond to Metric instance passed to this
    * method.
    *
    * @param metricListener The MetricListener to notify of MetricEvents
    * @param metric The Metric corresponding to the MetricEvents for which the passed
    * MetricListener will be notified
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void addMetricListener(MetricListener metricListener, Metric metric) throws RemoteException
    {
        fileCpi.addMetricListener(metricListener, metric);
    }
    
   /**
    * Removes the passed MetricListener from the java.util.List of MetricListeners
    * which are notified of MetricEvents corresponding to the passed
    * Metric instance.
    *
    * @param metricListener The MetricListener to notify of MetricEvents
    * @param metric The Metric corresponding to the MetricEvents for which the passed
    * MetricListener will be notified
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void removeMetricListener(MetricListener metricListener, Metric metric) throws RemoteException
    {
        fileCpi.removeMetricListener(metricListener, metric);
    }
    
   /**
    * This method returns a java.util.List of Metric instances. Each Metric 
    * instance in this java.util.List is a Metric which can be monitored on 
    * this instance.
    *
    * @return An java.util.List of Metric instances. Each Metric instance
    * in this java.util.List is a Metric which can be monitored on this 
    * instance.
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public List getMetrics() throws RemoteException
    {
        return fileCpi.getMetrics();
    }
}