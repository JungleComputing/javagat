package org.gridlab.gat.io;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
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
public class DefaultFileAdaptor extends FileCpi
{    
   /**
    * Constructs a DefaultFileAdaptor instance which corresponds to the physical file
    * identified by the passed Location and whose access rights are
    * determined by the passed GATContext. 
    *
    * @param location A Location which represents the URI corresponding 
    * to the physical file.
    * @param gatContext A GATContext which is used to determine the access 
    * rights for this DefaultFileAdaptor.
    */
    public DefaultFileAdaptor(Location location, GATContext gatContext)
    {            
        super(location,gatContext);
    }
        
        
   /**
    * Tests this FileCpi for equality with the passed Object.
    * <p>
    * If the given object is not a File, then this method immediately
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
    public boolean equals(Object object)
    {
        if( false == (object instanceof org.gridlab.gat.io.File) )
          return false;
          
        org.gridlab.gat.io.File file = (org.gridlab.gat.io.File) object;
        
        return location.equals(file.getLocation());
    }
            
   /**
    * This method copies the physical file represented by this 
    * File instance to a physical file identified by the passed 
    * Location.
    *
    * @param loc The new location
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void copy(Location loc) throws IOException
    {
        // Step 1: Create destination file
        File destinationFile = new File(loc.toString());
        destinationFile.createNewFile();
        
        // Step 2: Create source file
        File sourceFile = new File(location.toString());
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
    * This method moves the physical file represented by this 
    * File instance to a physical file identified by the passed 
    * Location.
    *
    * @param loc The Location to which to move the physical 
    * file corresponding to this File instance
    * @throws java.io.IOException Upon non-remote IO problem
    */
    public void move(Location loc) throws IOException
    {
        // Step 1: Copy the original file
        copy(loc);
        
        // Step 2: Delete the original file
        DefaultFileAdaptor defaultFileAdaptor = new DefaultFileAdaptor(loc,gatcontext);
        defaultFileAdaptor.delete();
          
        // Step 3: Update location
        location = loc;
    }
        
   /**
    * This method deletes the physical file represented by this 
    * File instance.
    */
    public void delete()
    {
       File file = new File(location.toString());
       file.delete();
    }
    
   /**
    * Returns a boolean indicating if the physical file corresponding
    * to this instance is readable.
    *
    * @return A boolean indicating readability
    */
    public boolean isReadable()
    {
       File file = new File(location.toString());
       
       return file.canRead();
    }
    
   /**
    * Returns a boolean indicating if the physical file corresponding 
    * to this instance is writable.
    *
    * @return A boolean indicating writability
    */
    public boolean isWritable()
    {
       File file = new File(location.toString());
       
       return file.canWrite();
    }
    
   /**
    * Returns a long indicating the length in bytes of the physical 
    * file corresponding to this instance.
    *
    * @return A long indicating the length in bytes of the physical 
    * file corresponding to this instance.
    */
    public long getLength()
    {
       File file = new File(location.toString());
       
       return file.length();
    }
    
   /**
    * Returns the number of milliseconds after January 1, 1970, 
    * 00:00:00 GMT at which this file was last modified.
    *
    * @return A long indicating the number of milliseconds after 
    * January 1, 1970, 00:00:00 GMT at which this file was last 
    * modified.
    */
    public long lastModified()
    {
       File file = new File(location.toString());
       
       return file.lastModified();
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
        return new Vector();
    }
}