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
 * An abstract representation of a set of identical physical files.
 * <p>
 * A LogicalFile is an abstract representation of a set of identical
 * physical files. This abstraction is useful for a number of
 * reasons. For example, if one wishes to replicate a physical file which
 * is at one Location to a second Location. Normally, one takes all the
 * data at the first Location and replicates it to the second Location
 * even though the "network distance," between the first and second
 * Location may be great. A better solution to this problem is to have a
 * set of identical physical files distributed at different locations in
 * "network space." If one then wishes to replicate a physical file
 * from one Location to a second Location, GAT can then first determine
 * which physical file is closest in "network space" to the second
 * Location, chose that physical file as the source file, and copy it to
 * the destination Location. Similarly, the construct of a LogicalFile
 * allows for migrating programs to, while at a given point in "network
 * space," use the closest physical file in "network space" to its
 * physical location.
 */
public class LogicalFile implements Monitorable
{
    private LogicalFileCpi logicalFileCpi = null;
    
   /**
    * This constructor creates a LogicalFile corresponding to the passed
    * Location instance and uses the passed GATContext to broker
    * resources. 
    *
    * @param location The Location of one physical file in this LogicalFile
    * @param gatContext The GATContext used to broker resources
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public LogicalFile(Location location, GATContext gatContext) throws Exception
    {            
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[2];
        array[0] = location;
        array[1] = gatContext;
                
        logicalFileCpi = (LogicalFileCpi) gatEngine.constructCpiClass(LogicalFileCpi.class, array);
    }
    
   /**
    * This constructor creates a LogicalFile corresponding to the passed
    * Location instance and uses the passed GATContext to broker
    * resources. 
    *
    * @param location The Location of one physical file in this LogicalFile
    * @param gatContext The GATContext used to broker resources
    * @param preferences The Preferences for this instance
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public LogicalFile(Location location, GATContext gatContext, Preferences preferences) throws Exception
    {            
        GATEngine gatEngine = GATEngine.getGATEngine();
        
        Object[] array = new Object[2];
        array[0] = location;
        array[1] = gatContext;
                
        logicalFileCpi = (LogicalFileCpi) gatEngine.constructCpiClass(LogicalFileCpi.class, preferences, array);
    }
    
   /**
    * Adds the passed File instance to the set of physical files represented
    * by this LogicalFile instance.
    *
    * @param file A File instance to add to the set of physical files represented
    * by this LogicalFile instance.
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void addFile(File file) throws RemoteException
    {
        logicalFileCpi.addFile(file);
    }
    
   /**
    * Adds the physical file at the passed Location to the set of physical
    * files represented by this LogicalFile instance.
    *
    * @param location The Location of a physical file to add to the set of 
    * physical files represented by this LogicalFile instance.
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void addLocation(Location location) throws RemoteException
    {
        logicalFileCpi.addLocation(location);
    } 
    
   /**
    * Removes the passed File instance from the set of physical files represented
    * by this LogicalFile instance.
    *
    * @param file A File instance to remove from the set of physical files represented
    * by this LogicalFile instance.
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void removeFile(File file) throws RemoteException
    {
        logicalFileCpi.removeFile(file);
    }
    
   /**
    * Removes the physical file at the passed Location from the set of physical
    * files represented by this LogicalFile instance.
    *
    * @param location The Location of a physical file to remove from the set of 
    * physical files represented by this LogicalFile instance.
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public void removeLocation(Location location) throws RemoteException
    {
        logicalFileCpi.removeLocation(location);
    }
    
   /**
    * Replicates the logical file represented by this instance to the
    * physical file specified by the passed Location.
    *
    * @param location The Location of the new physical file
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void replicate(Location location) throws RemoteException, IOException
    {
        logicalFileCpi.replicate(location);
    }
    
   /**
    * Returns a java.util.List of Location instances each of which is the
    * Location of a physical file represented by this instance.
    *
    * @return The java.util.List of Locations
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public List getLocations() throws RemoteException
    {
        return logicalFileCpi.getLocations();
    }
    
   /**
    * Returns a java.util.List of File instances each of which is a
    * File corresponding to a physical file represented by this instance.
    *
    * @return The java.util.List of Locations
    * @throws  java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public List getFiles() throws RemoteException
    {
        return logicalFileCpi.getFiles();
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
        logicalFileCpi.addMetricListener(metricListener, metric);
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
        logicalFileCpi.removeMetricListener(metricListener, metric);
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
        return logicalFileCpi.getMetrics();
    }       
}