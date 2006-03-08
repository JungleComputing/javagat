package org.gridlab.gat.io;

import java.util.List;
import java.util.Vector;
import java.io.IOException;
import java.io.EOFException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.util.Buffer;
import org.gridlab.gat.net.Location;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.monitoring.MetricListener;

/*
 * Notes:
 * 1. This class should have getLength() method !!!
 * 2. The method writeFinish() should return an int !!!
 * 3. The method writeTest() should return an int !!!
 * 4. Semantics of writeTest()/readTest() is not consistant !!!
 * 5. Semantics of writeFinish()/readFinish() is not consistant !!!
 */

/**
 * A DefaultFileStreamAdaptor represents a connection to open file, the file may be
 * either remote or local.
 * <p>
 * A DefaultFileStreamAdaptor represents a seekable connection to a file and has
 * semantics similar to a standard Unix filedescriptor.  It provides
 * methods to query the current position in the file and to seek to new positions.
 * <p>
 * To Write data to a DefaultFileStreamAdaptor it is necessary to construct a Buffer
 * and pack it with data.  Similarly, to read data a buffer must be
 * created to store the read data.  Writes and reads may either be blocking,
 * or asynchronous. Asynchronous writes or reads must be completed by 
 * appropriate call.
 */
public class DefaultFileStreamAdaptor extends FileStreamCpi
{
   protected Buffer buffer = null;
   protected RandomAccessFile randomAccessFile = null;
   
  /**
   * This creates a DefaultFileStreamAdaptor attached to the physical file at the
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
   * @throws java.io.IOException Thrown upon an I/O error of some sort 
   */
   public DefaultFileStreamAdaptor(GATContext gatContext, Location location, Integer mode) throws IOException
   {
       super(gatContext,location,mode);
       
       if(FileStream.READ == this.mode)
       {
         randomAccessFile = new RandomAccessFile(location.toString(),"r");
       }
       if(FileStream.WRITE == this.mode)
       {
         randomAccessFile = new RandomAccessFile(location.toString(),"rws");
       }
       if(FileStream.READWRITE == this.mode)
       {
         randomAccessFile = new RandomAccessFile(location.toString(),"rws");
       }
       if(FileStream.APPEND == this.mode)
       {
         randomAccessFile = new RandomAccessFile(location.toString(),"rws");
         randomAccessFile.seek(randomAccessFile.length());
       }
   }
         
  /**
   * This returns the current position in the file.
   *
   * @return The current position in the file, a long
   * @throws java.io.IOException Thrown upon an I/O error of some sort
   */
   public long position() throws IOException
   {
       return randomAccessFile.getFilePointer();
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
       if(FileStream.BEGINNING == whence)
       {
         randomAccessFile.seek(offset);
       }
       if(FileStream.CURRENT == whence)
       {
         randomAccessFile.seek(position() + offset);
       }
       if(FileStream.END == whence)
       {
         randomAccessFile.seek(randomAccessFile.length() + offset);
       }
   }
   
  /**
   * Make sure that any underlying file-writing mechanisms have flushed
   * data to disk. 
   */
   public void flush()
   {
     // RandomAccessFile is opened in "rws" mode so this is un-needed
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
        try
        {
            while(buffer.canPack())
            {
                Class nextPackableType = buffer.getNextPackableType();
                
                if(nextPackableType.isAssignableFrom(Boolean.class))
                {
                    buffer.pack( new Boolean( randomAccessFile.readBoolean() ) );
                }
                if(nextPackableType.isAssignableFrom(Byte.class))
                {
                    buffer.pack( new Byte( randomAccessFile.readByte() ) );
                }
                if(nextPackableType.isAssignableFrom(Character.class))
                {
                    buffer.pack( new Character( randomAccessFile.readChar() ) );
                }
                if(nextPackableType.isAssignableFrom(Double.class))
                {
                    buffer.pack( new Double( randomAccessFile.readDouble() ) );
                }
                if(nextPackableType.isAssignableFrom(Float.class))
                {
                    buffer.pack( new Float( randomAccessFile.readFloat() ) );
                }
                if(nextPackableType.isAssignableFrom(Integer.class))
                {
                    buffer.pack( new Integer( randomAccessFile.readInt() ) );
                }
                if(nextPackableType.isAssignableFrom(Long.class))
                {
                    buffer.pack( new Long( randomAccessFile.readLong() ) );
                }
                if(nextPackableType.isAssignableFrom(Short.class))
                {
                    buffer.pack( new Short( randomAccessFile.readShort() ) );
                }
            }
        }
        catch(EOFException exception)
        {
            // No more reads should be attempted
        }
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
        while(buffer.canUnPack())
        {
            Class nextUnPackableType = buffer.getNextUnPackableType();
            
            if(nextUnPackableType.isAssignableFrom(Boolean.class))
            {
                randomAccessFile.writeBoolean( ( (Boolean) buffer.unPack( Boolean.class ) ).booleanValue() );
            }
            if(nextUnPackableType.isAssignableFrom(Byte.class))
            {
                randomAccessFile.writeByte( ( (Byte) buffer.unPack( Byte.class ) ).byteValue() );
            }
            if(nextUnPackableType.isAssignableFrom(Character.class))
            {
                randomAccessFile.writeChar( ( (Character) buffer.unPack( Character.class ) ).charValue() );
            }
            if(nextUnPackableType.isAssignableFrom(Double.class))
            {
                randomAccessFile.writeDouble( ( (Double) buffer.unPack( Double.class ) ).doubleValue() );
            }
            if(nextUnPackableType.isAssignableFrom(Float.class))
            {
                randomAccessFile.writeFloat( ( (Float) buffer.unPack( Float.class ) ).floatValue() );
            }
            if(nextUnPackableType.isAssignableFrom(Integer.class))
            {
                randomAccessFile.writeInt( ( (Integer) buffer.unPack( Integer.class ) ).intValue() );
            }
            if(nextUnPackableType.isAssignableFrom(Long.class))
            {
                randomAccessFile.writeLong( ( (Long) buffer.unPack( Long.class ) ).longValue() );
            }
            if(nextUnPackableType.isAssignableFrom(Short.class))
            {
                randomAccessFile.writeShort( ( (Short) buffer.unPack( Short.class ) ).shortValue() );
            }
        }
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
    */
    public void iRead(Buffer buffer)
    {
        this.buffer = buffer;
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
        int initialPosition = buffer.getPosition();
        
        read(buffer);
        
        int finalPosition = buffer.getPosition();
        
        return (initialPosition != finalPosition);
    }
    
    
   /**
    * This tests if data has been received in the current iRead.
    *
    * @return A boolean indicating if data was received
    */
    public boolean readTest()
    {
        return false;
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
    */
    public void iWrite(Buffer buffer)
    {
        this.buffer = buffer;
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
        write(buffer);
                
        return (long) buffer.getPosition();
    }
    
   /**
    * Tests if data has been sent from  the current iWrite call on this Streamable.
    *
    * @return The position in the buffer right after the last data sent, a long
    */
    public long writeTest()
    {
        return (long) buffer.getPosition();
    }
    
   /**
    * Closes this Streamable instance.
    *
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public void close() throws IOException
    {
        randomAccessFile.close();
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