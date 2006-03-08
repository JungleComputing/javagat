package org.gridlab.gat.io;

import java.util.List;
import java.io.IOException;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.util.Buffer;
import org.gridlab.gat.net.Location;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.monitoring.MetricListener;
/**
 * A FileStream represents a connection to open file, the file may be
 * either remote or local.
 * <p>
 * A FileStream represents a seekable connection to a file and has
 * semantics similar to a standard Unix filedescriptor.  It provides
 * methods to query the current position in the file and to seek to new positions.
 * <p>
 * To Write data to a FileStream it is necessary to construct a Buffer
 * and pack it with data.  Similarly, to read data a buffer must be
 * created to store the read data.  Writes and reads may either be blocking,
 * or asynchronous. Asynchronous writes or reads must be completed by 
 * appropriate call.
 */
public class FileStream implements Monitorable, Streamable
{
   /** Read file indicator */
   public static final int READ = 1;
   
   /** Write file indicator */
   public static final int WRITE = 2;
   
   /** Read/Write file indicator */
   public static final int READWRITE = 3;
   
   /** Read/Write file indicator */
   public static final int APPEND = 4;
   
   /** Beginning file indicator */
   public static final int BEGINNING = 1;
   
   /** Current file indicator */
   public static final int CURRENT = 2;
   
   /** End file indicator */
   public static final int END = 4;
   
   private FileStreamCpi fileStreamCpi = null;
   
  /**
   * This creates a FileStream attached to the physical file at the
   * specified Location. The file may be opened in several modes:
   * <ul>
   *     <li>FileStream.READ --- Open file for reading.  The stream 
   *         is positioned at the beginning of the file.</li>
   *     <li>FileStream.WRITE --- Truncate file to zero length or 
   *         create file for writing.  The stream is positioned at 
   *         the beginning of the file.</li>
   *     <li>FileStream.READWRITE --- Open for reading and writing.
   *         The stream is positioned at the beginning of the file.</li>
   *     <li>FileStream.APPEND --- Open for appending (writing at end of
   *         file).  The file is created if it does not exist.  The 
   *         stream is positioned at the end of the file.</li>
   * </ul>
   *
   * @param gatContext The GATContext used to broker resources
   * @param location The Location of the file to open.
   * @param mode The mode to open it --- READ, WRITE, READWRITE, or APPEND,
   * member variables of this class
   * @throws java.lang.Exception Thrown upon creation error of some sort
   */
   public FileStream(GATContext gatContext, Location location, int mode) throws Exception
   {
       GATEngine gatEngine = GATEngine.getGATEngine();
       
       Object[] array = new Object[3];
       array[0] = gatContext;
       array[1] = location;
       array[2] = new Integer(mode);
                
       fileStreamCpi = (FileStreamCpi) gatEngine.constructCpiClass(FileStreamCpi.class, array);
   }
   
  /**
   * This creates a FileStream attached to the physical file at the
   * specified Location. The file may be opened in several modes:
   * <ul>
   *     <li>FileStream.READ --- Open file for reading.  The stream 
   *         is positioned at the beginning of the file.</li>
   *     <li>FileStream.WRITE --- Truncate file to zero length or 
   *         create file for writing.  The stream is positioned at 
   *         the beginning of the file.</li>
   *     <li>FileStream.READWRITE --- Open for reading and writing.
   *         The stream is positioned at the beginning of the file.</li>
   *     <li>FileStream.APPEND --- Open for appending (writing at end of
   *         file).  The file is created if it does not exist.  The 
   *         stream is positioned at the end of the file.</li>
   * </ul>
   *
   * @param gatContext The GATContext used to broker resources
   * @param preferences The Preferences for this instance
   * @param location The Location of the file to open.
   * @param mode The mode to open it --- READ, WRITE, READWRITE, or APPEND,
   * member variables of this class
   * @throws java.lang.Exception Thrown upon creation error of some sort
   */
   public FileStream(GATContext gatContext, Preferences preferences, Location location, int mode) throws Exception
   {
       GATEngine gatEngine = GATEngine.getGATEngine();
       
       Object[] array = new Object[3];
       array[0] = gatContext;
       array[1] = location;
       array[2] = new Integer(mode);
                
       fileStreamCpi = (FileStreamCpi) gatEngine.constructCpiClass(FileStreamCpi.class, preferences, array);
   }
      
  /**
   * This returns the current position in the file.
   *
   * @return The current position in the file, a long
   * @throws java.io.IOException Thrown upon an I/O error of some sort
   */
   public long position() throws IOException
   {
       return fileStreamCpi.position();
   }
   
  /**
   * This changes the current position in the file.  The position can
   * either be calculated from the current position in the file or from the
   * beginning or end. 
   *
   * @param offset A long,  from <em>whence</em>
   * @param whence One of the member variables used for this purpose, 
   * BEGINNING, CURRENT or END of file.
   * @throws java.io.IOException Thrown upon an I/O error of some sort
   */
   public void seek(long offset, int whence) throws IOException
   {
       fileStreamCpi.seek(offset, whence);
   }
   
  /**
   * Make sure that any underlying file-writing mechanisms have flushed
   * data to disk. 
   *
   * @throws java.io.IOException Thrown upon an I/O error of some sort
   */
   public void flush() throws IOException
   {
       fileStreamCpi.flush();
   }
   
   /**
    * Reads from this Streamable into the given buffer. 
    * <p>
    * This is a blocking call and only returns if the buffer is full
    * or there is no more data to read.
    *
    * @param buffer The Buffer into which data are to be transferred
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void read(Buffer buffer) throws IOException
    {
        fileStreamCpi.read(buffer);
    }
    
   /**
    * Writes data from the given Buffer to the Streamable.  The buffer is not
    * modified.
    * <p> 
    * This is a blocking call and only returns when all the data in the
    * buffer has been sent.
    *
    * @param buffer The Buffer from which data are to be transferred
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void write(Buffer buffer) throws IOException
    {
        fileStreamCpi.write(buffer);
    }
    
   /**
    * Reads from this Streamable into the given buffer, but returning
    * immediately. The buffer should not be used until a ReadWait has
    * been called on this Streamable.
    * <p>
    * Only one iRead may be active at once on a given Streamable.
    * <p>
    *This is a non-blocking call.
    *
    * @param buffer The Buffer into which data are to be transferred
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void iRead(Buffer buffer) throws IOException
    {
        fileStreamCpi.iRead(buffer);
    }
    
   /**
    * This finishes the current iRead call on this Streamable.  All data
    * received between posting the iRead and this call will be available
    * in the associated buffer on return.
    *
    * @return A boolean indicating if data was received
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public boolean readFinish() throws IOException
    {
        return fileStreamCpi.readFinish();
    }
    
    
   /**
    * This tests if data has been received in the current iRead.
    *
    * @return A boolean indicating if data was received
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public boolean readTest() throws IOException
    {
        return fileStreamCpi.readTest();
    }
    
   /**
    * Writes data from the given Buffer to the Streamable, but returning
    * immediately.  The buffer should not be used until a WriteWait has been
    * called on this Streamable.  The buffer is not modified, but the position of
    * the next item in the buffer which would be sent is returned, and this
    * can be used to remove old data from the buffer.
    * <p>
    * This is a non-blocking call.
    *
    * @param buffer The Buffer from which data are to be transferred
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void iWrite(Buffer buffer) throws IOException
    {
        fileStreamCpi.iWrite(buffer);
    }
    
   /**
    * This finishes the current iWrite call on this Streamable.  Not all data
    * in the buffer may have been sent.
    *
    * @return The position in the buffer right after the last data sent, a long
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public long writeFinish() throws IOException
    {
        return fileStreamCpi.writeFinish();
    }
    
   /**
    * Tests if data has been sent from  the current iWrite call on this Streamable.
    *
    * @return The position in the buffer right after the last data sent, a long
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public long writeTest() throws IOException
    {
        return fileStreamCpi.writeTest();
    }
    
   /**
    * Closes this Streamable instance.
    *
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void close() throws IOException
    {
        fileStreamCpi.close();
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
        fileStreamCpi.addMetricListener(metricListener, metric);
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
        fileStreamCpi.removeMetricListener(metricListener, metric);
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
        return fileStreamCpi.getMetrics();
    }    
}