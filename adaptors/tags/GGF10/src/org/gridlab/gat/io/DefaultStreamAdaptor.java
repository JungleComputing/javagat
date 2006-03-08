package org.gridlab.gat.io;

import java.util.List;
import java.util.Vector;
import java.io.IOException;
import java.io.PipedInputStream;
import java.rmi.RemoteException;
import java.io.PipedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.Preferences;
import org.gridlab.gat.util.Buffer;
import java.io.InvalidObjectException;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Metric;
import org.gridlab.gat.monitoring.Monitorable;
import org.gridlab.gat.monitoring.MetricListener;

/**
 * A Stream represents a connection to another process.  It implements
 * Streamable and the communication methods are derived from that interface.
 * <p>
 * A Stream represents a streaming connection to another process, and has
 * semantics similar to the standard BSD socket interface.  Once a Stream
 * is created it can either be placed in a listening state, or be
 * connected to a Stream on a remote process.
 * <p>
 * To send data down a Stream it is necessary to construct a Buffer, and
 * pack it with data.  Similarly to receive data a buffer must be created
 * in which the data will be stored.  Sends and receives may either be
 * blocking, or asynchronous.  Asynchronous sends or receives must be
 * completed by an appropriate call.  These methods are found in the
 * Streamable class.
 */
public class DefaultStreamAdaptor extends StreamCpi
{  
    protected Buffer buffer = null;
    protected ObjectInputStream objectInputStream = null;
    protected ObjectOutputStream objectOutputStream = null;
    
   /**
    * Constructs a unconnected instance of this class.
    *
    * @param gatCantext A GATContext used to broker resources
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public DefaultStreamAdaptor(GATContext gatContext) throws Exception
    {
        super(gatContext);        
    }
    
   /**
    * This method connects this Stream. When a Stream is found, e.g. 
    * from a Collection, it is not initially in a connected state.  
    * The connect method changes the state.
    * <p>
    * This method takes no parameters.
    *
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public synchronized void connect() throws IOException
    {
            if(true == isConnected() )
              return;

            PipedInputStream pipedInputStream = new PipedInputStream();
            PipedOutputStream pipedOutputStream = new PipedOutputStream();

            pipedInputStream.connect(pipedOutputStream);

           /*
            * For why the flush is required see the documentation for the
            * single argument constructors for ObjectOutputStream and for
            * ObjectInputStream.
            */ 
            objectOutputStream = new ObjectOutputStream(pipedOutputStream);
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(pipedInputStream);

            notifyAll();
    }
    
   /**
    * This method blocks until a call to connect is made on this Stream.
    * <p>
    * For example, a peer creates a Stream advertises it in a Collection,
    * then calls this method. When a second peer finds the Stream in the
    * Collection removes it, and calls connect on the stream this method
    * will stop blocking and calls may be made to the variosu read/write
    * methods on the instance of this class.
    *
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    * @throws java.lang.InterruptedException Thrown if another thread has
    * interrupted the current thread.
    */
    public synchronized void waitForConnect() throws IOException, InterruptedException
    {
        if( false == isConnected() )
          wait();
    }
    
   /**
    * This method returns a boolean indicating if this instance is connected
    *
    * @return  A boolean indicating if this instance is connected
    */
    public synchronized boolean isConnected()
    {
        if( (null != objectInputStream) && (null != objectOutputStream) )
          return true;
          
        return false;
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
        if(false == isConnected())
          throw new IllegalStateException("Stream is not connected");
        
        while(buffer.canPack())
        {
            try
            {
                Object object = objectInputStream.readObject();
                buffer.pack(object);
            }
            catch(ClassNotFoundException classNotFoundException)
            {
                throw new InvalidObjectException(classNotFoundException.getMessage());
            }
        }
    }
    
   /**
    * Writes data from the given Buffer through the Streamable.  The buffer is not
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
        if(false == isConnected())
          throw new IllegalStateException("Stream is not connected");
          
        while(buffer.canUnPack())
        {
            Class clazz = buffer.getNextUnPackableType();
            Object object = buffer.unPack(clazz);
            objectOutputStream.writeObject(object);
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
    * Writes data from the given Buffer through the Streamable, but returning
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
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public long writeTest() throws IOException
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
        try
        {
            objectInputStream.close();
        }
        finally
        {
            objectOutputStream.close();
        }        
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
    * @throws java.rmi.RemoteException Thrown upon problems 
    * accessing the remote instance
    */
    public List getMetrics() throws RemoteException
    {
        return new Vector();
    }    
}