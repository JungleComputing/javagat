package org.gridlab.gat.io;

import java.io.IOException;
import org.gridlab.gat.GATContext;
import org.gridlab.gat.engine.GATEngine;
import org.gridlab.gat.monitoring.Monitorable;

/**
 * Capability provider interface to the Stream class.
 * <p>
 * Capability provider wishing to provide the functionality 
 * of the Stream class must extend this class and implement 
 * all of the abstract methods in this class. Each abstract 
 * method in this class mirrors the corresponding method in
 * this Stream class and will be used to implement the 
 * corresponding method in the Stream class at runtime. 
 */
public abstract class StreamCpi implements Monitorable, Streamable
{    
    protected GATContext gatCantext = null;
    
   /**
    * Constructs a unconnected instance of this class.
    *
    * @param gatCantext A GATContext used to broker resources
    * @throws  java.lang.Exception Thrown upon creation problems
    */
    public StreamCpi(GATContext gatContext) throws Exception
    {
        this.gatCantext = gatCantext;
    }
        
   /**
    * This method connects this StreamCpi. When a StreamCpi is found, 
    * e.g. from a Collection, it is not initially in a connected state.  
    * The connect method changes the state.
    * <p>
    * This method takes no parameters.
    *
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public abstract void connect() throws IOException;
    
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
    public abstract void waitForConnect() throws IOException, InterruptedException;    
    
   /**
    * This method returns a boolean indicating if this instance is connected
    *
    * @return  A boolean indicating if this instance is connected
    * @throws java.io.IOException Thrown upon an I/O error of some sort
    */
    public abstract boolean isConnected() throws IOException;    
}