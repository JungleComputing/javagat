package org.gridlab.gat.io;

import java.util.List;
import java.util.Vector;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import org.gridlab.gat.io.File;
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
public class DefaultLogicalFileAdaptor extends LogicalFileCpi
{  
   /**
    * Files in the LogicalFile
    */
    protected Vector files = null; 
    
   /**
    * Locations in the LogicalFile
    */
    protected Vector locations = null;
             
   /**
    * This constructor creates a DefaultLogicalFileAdaptor corresponding to the passed
    * Location instance and uses the passed GATContext to broker
    * resources. 
    *
    * @param location The Location of one physical file in this DefaultLogicalFileAdaptor
    * @param gatContext The GATContext used to broker resources
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public DefaultLogicalFileAdaptor(Location location, GATContext gatContext) throws Exception
    {   
        super(location, gatContext);
        
        files = new Vector();
        locations = new Vector();
        
        locations.add(location);
        files.add( new File(location,gatContext) );
    }
        
   /**
    * Adds the passed File instance to the set of physical files represented
    * by this LogicalFile instance.
    *
    * @param file A File instance to add to the set of physical files represented
    * by this LogicalFile instance.
    */
    public void addFile(File file)
    {
        files.add(file);
        locations.add(file.getLocation());
    }
    
   /**
    * Adds the physical file at the passed Location to the set of physical
    * files represented by this LogicalFile instance.
    *
    * @param location The Location of a physical file to add to the set of 
    * physical files represented by this LogicalFile instance.
    */
    public void addLocation(Location location)
    {
        locations.add( location );
        
        try
        {
            files.add( new File(location, gatContext) );
        }
        catch(Exception exception)
        {
            throw new RuntimeException( exception );
        }        
    } 
    
   /**
    * Removes the passed File instance from the set of physical files represented
    * by this LogicalFile instance.
    *
    * @param file A File instance to remove from the set of physical files represented
    * by this LogicalFile instance.
    */
    public void removeFile(File file)
    {
        files.remove( file );
        locations.remove( file.getLocation() );
    }
    
   /**
    * Removes the physical file at the passed Location from the set of physical
    * files represented by this LogicalFile instance.
    *
    * @param location The Location of a physical file to remove from the set of 
    * physical files represented by this LogicalFile instance.
    */
    public void removeLocation(Location location)
    {
        locations.remove( location );
        
        try
        {
            files.remove( new File(location, gatContext) );
        }
        catch(Exception exception)
        {
            throw new RuntimeException( exception );
        }
    }
    
   /**
    * Replicates the logical file represented by this instance to the
    * physical file specified by the passed Location.
    *
    * @param loc The Location of the new physical file
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void replicate(Location loc) throws IOException
    {
        // Step 1: Create destination file
        java.io.File destinationFile = new java.io.File(loc.toString());
        destinationFile.createNewFile();
        
        // Step 2: Create source file
        Location firstLocation = (Location) locations.firstElement();
        java.io.File sourceFile = new java.io.File(firstLocation.toString());
        sourceFile.createNewFile();
        
        // Step 3: Copy source to destination
        String nextLine = null;
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        BufferedReader bufferedReader = null;
        
        try
        {
            fileReader = new FileReader(sourceFile);
            fileWriter = new FileWriter(destinationFile);
            bufferedReader = new BufferedReader(fileReader);
            
            while(null != (nextLine = bufferedReader.readLine()) )
            {
                fileWriter.write(nextLine + "\n");
            }
        }
        finally
        {
            try
            {
                if(null != bufferedReader)
                  bufferedReader.close();
            }
            catch(IOException ioException)
            {
                // Ignore ioException
            }
            
            try
            {
                if(null != fileReader)
                  fileReader.close();
            }
            catch(IOException ioException)
            {
                // Ignore ioException
            }
            
            try
            {
                if(null != fileWriter)
                  fileWriter.close();
            }
            catch(IOException ioException)
            {
                // Ignore ioException
            }            
        }
    }
    
   /**
    * Returns a java.util.List of Location instances each of which is the
    * Location of a physical file represented by this instance.
    *
    * @return The java.util.List of Locations
    */
    public List getLocations()
    {
        return locations;
    }
    
   /**
    * Returns a java.util.List of File instances each of which is a
    * File corresponding to a physical file represented by this instance.
    *
    * @return The java.util.List of Files
    */
    public List getFiles()
    {
        return files;
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
    */
    public void addMetricListener(MetricListener metricListener, Metric metric)
    {
    }
    
   /**
    * Removes the passed MetricListener from the java.util.List of MetricListeners
    * which are notified of MetricEvents corresponding to the passed
    * Metric instance.
    *
    * @param metricListener The MetricListener to notify of MetricEvents
    * @param metric The Metric corresponding to the MetricEvents for which the passed
    * MetricListener will be notified
    */
    public void removeMetricListener(MetricListener metricListener, Metric metric)
    {
    }
    
   /**
    * This method returns a java.util.List of Metric instances. Each Metric 
    * instance in this java.util.List is a Metric which can be monitored on 
    * this instance.
    *
    * @return An java.util.List of Metric instances. Each Metric instance
    * in this java.util.List is a Metric which can be monitored on this 
    * instance.
    */
    public List getMetrics()
    {
        return new Vector();
    }       
}